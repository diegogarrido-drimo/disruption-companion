package com.latam.disruption.domain;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record OperationalEvents(List<Event> events, Instant retrievedAt, Set<String> degradedSources) {

    public OperationalEvents {
        events = List.copyOf(events);
        degradedSources = Set.copyOf(degradedSources);
    }

    /** {@code status}, {@code delayMinutes} and {@code newDeparture} are null when not applicable to the type. */
    public record Event(
            String eventId,
            String segmentId,
            String type,
            String status,
            Integer delayMinutes,
            String newDeparture,
            String source,
            Instant recordedAt) {}
}
