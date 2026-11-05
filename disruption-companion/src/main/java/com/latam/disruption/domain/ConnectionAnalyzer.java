package com.latam.disruption.domain;

import com.latam.disruption.domain.Evaluation.Connection;
import com.latam.disruption.domain.Evaluation.ConnectionStatus;
import com.latam.disruption.domain.Evaluation.Flight;
import com.latam.disruption.domain.Evaluation.SegmentFlight;
import com.latam.disruption.domain.TripContext.Segment;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Projects connections between consecutive segments. A projected deficit is a risk, never a confirmed
 * missed connection. All times share one time frame (assumption S-1) and no minimum connection time applies.
 */
public final class ConnectionAnalyzer {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private ConnectionAnalyzer() {}

    public static List<Connection> analyze(List<Segment> segments, Flight flight) {
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i + 1 < segments.size(); i++) {
            Segment from = segments.get(i);
            Segment to = segments.get(i + 1);
            Optional<Integer> fromDelay = delayOf(flight, from.segmentId());
            Optional<Integer> toDelay = delayOf(flight, to.segmentId());
            if (fromDelay.isEmpty() || toDelay.isEmpty()) {
                connections.add(new Connection(
                        from.segmentId(), to.segmentId(), ConnectionStatus.UNDETERMINED, false, null, null, null));
                continue;
            }
            LocalDateTime arrival = LocalDateTime.of(from.date(), from.scheduledArrival()).plusMinutes(fromDelay.get());
            LocalDateTime departure = LocalDateTime.of(to.date(), to.scheduledDeparture()).plusMinutes(toDelay.get());
            long deficit = Duration.between(departure, arrival).toMinutes();
            connections.add(new Connection(
                    from.segmentId(), to.segmentId(),
                    deficit > 0 ? ConnectionStatus.AT_RISK : ConnectionStatus.NO_RISK,
                    false,
                    arrival.format(HH_MM), departure.format(HH_MM), (int) Math.max(deficit, 0)));
        }
        return connections;
    }

    /** Minutes of delay, or empty when the segment state is not resolved. */
    static Optional<Integer> delayOf(Flight flight, String segmentId) {
        return flight.segments().stream()
                .filter(s -> s.segmentId().equals(segmentId))
                .findFirst()
                .flatMap(ConnectionAnalyzer::delayOf);
    }

    private static Optional<Integer> delayOf(SegmentFlight s) {
        return switch (s.state()) {
            case DELAYED -> Optional.of(s.delayMinutes());
            case NO_DELAY_REPORTED -> Optional.of(0);
            case UNCERTAIN, NO_INFORMATION -> Optional.empty();
        };
    }
}
