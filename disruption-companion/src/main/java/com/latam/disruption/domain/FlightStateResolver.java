package com.latam.disruption.domain;

import com.latam.disruption.domain.Evaluation.DiscardedEvent;
import com.latam.disruption.domain.Evaluation.EventBasis;
import com.latam.disruption.domain.Evaluation.Flight;
import com.latam.disruption.domain.Evaluation.InformationalEvent;
import com.latam.disruption.domain.Evaluation.SegmentFlight;
import com.latam.disruption.domain.Evaluation.SegmentState;
import com.latam.disruption.domain.OperationalEvents.Event;
import com.latam.disruption.domain.TripContext.Segment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Resolves the current state of each segment from possibly contradictory sources. */
public final class FlightStateResolver {

    public static final String SUPERSEDED = "SUPERSEDED";
    public static final String DEGRADED_AND_OLDER = "DEGRADED_AND_OLDER";
    public static final String CONTRADICTS_DELAY = "CONTRADICTS_DELAY";
    public static final String DEGRADED_CONTRADICTS_DELAY = "DEGRADED_CONTRADICTS_DELAY";
    public static final String DEGRADED_DELAY_ONLY = "DEGRADED_DELAY_ONLY";
    public static final String UNRECOGNIZED_STATUS = "UNRECOGNIZED_STATUS";

    private static final String DELAY = "DELAY";
    private static final String STATUS = "STATUS";
    private static final String ON_TIME = "ON_TIME";
    private static final String SCHEDULED = "SCHEDULED";

    private FlightStateResolver() {}

    public static Flight resolve(List<Segment> segments, OperationalEvents events) {
        List<SegmentFlight> resolved = new ArrayList<>();
        List<DiscardedEvent> discarded = new ArrayList<>();
        List<InformationalEvent> informational = new ArrayList<>();

        for (Event e : events.events()) {
            if (!DELAY.equals(e.type()) && !STATUS.equals(e.type())) {
                informational.add(new InformationalEvent(e.eventId(), e.type()));
            }
        }
        for (Segment segment : segments) {
            List<Event> own = events.events().stream()
                    .filter(e -> segment.segmentId().equals(e.segmentId()))
                    .toList();
            resolved.add(resolveSegment(segment.segmentId(), own, events, discarded));
        }
        return new Flight(resolved, discarded, informational);
    }

    private static SegmentFlight resolveSegment(
            String segmentId, List<Event> own, OperationalEvents all, List<DiscardedEvent> discarded) {
        List<Event> relevant = own.stream()
                .filter(e -> DELAY.equals(e.type()) || STATUS.equals(e.type()))
                .toList();
        if (relevant.isEmpty()) {
            return new SegmentFlight(segmentId, SegmentState.NO_INFORMATION, null, null, null);
        }

        Optional<Event> delay = relevant.stream()
                .filter(e -> DELAY.equals(e.type()) && !all.degradedSources().contains(e.source()))
                .max(Comparator.comparing(Event::recordedAt));
        boolean degradedDelayOnly = delay.isEmpty()
                && relevant.stream().anyMatch(e -> DELAY.equals(e.type()));

        SegmentState state = delay.isPresent() ? SegmentState.DELAYED : SegmentState.NO_DELAY_REPORTED;

        if (degradedDelayOnly) {
            // A delay known only from a degraded source cannot be stated as fact.
            relevant.stream()
                    .filter(e -> DELAY.equals(e.type()))
                    .forEach(e -> discarded.add(discard(e, DEGRADED_DELAY_ONLY)));
            state = SegmentState.UNCERTAIN;
        }

        for (Event e : relevant) {
            if (!STATUS.equals(e.type())) continue;
            boolean degraded = all.degradedSources().contains(e.source());
            if (!ON_TIME.equals(e.status()) && !SCHEDULED.equals(e.status())) {
                discarded.add(discard(e, UNRECOGNIZED_STATUS));
                state = SegmentState.UNCERTAIN;
            } else if (ON_TIME.equals(e.status()) && delay.isPresent()) {
                if (e.recordedAt().isBefore(delay.get().recordedAt())) {
                    discarded.add(discard(e, degraded ? DEGRADED_AND_OLDER : SUPERSEDED));
                } else {
                    discarded.add(discard(e, degraded ? DEGRADED_CONTRADICTS_DELAY : CONTRADICTS_DELAY));
                    state = SegmentState.UNCERTAIN;
                }
            }
        }

        if (state == SegmentState.DELAYED) {
            Event d = delay.get();
            return new SegmentFlight(
                    segmentId, state, d.delayMinutes(), d.newDeparture(),
                    new EventBasis(d.eventId(), d.source(), d.recordedAt()));
        }
        return new SegmentFlight(segmentId, state, null, null, null);
    }

    private static DiscardedEvent discard(Event e, String reason) {
        return new DiscardedEvent(e.eventId(), e.segmentId(), reason, e.source());
    }
}
