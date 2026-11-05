package com.latam.disruption.domain;

import com.latam.disruption.domain.Evaluation.Connection;
import com.latam.disruption.domain.Evaluation.ConnectionStatus;
import com.latam.disruption.domain.Evaluation.Flight;
import com.latam.disruption.domain.Evaluation.SegmentFlight;
import com.latam.disruption.domain.PolicyRules.Rule;
import com.latam.disruption.domain.TripContext.Passenger;
import com.latam.disruption.domain.TripContext.PassengerType;
import com.latam.disruption.domain.TripContext.Segment;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Explicit code for R1..R5, one per rule_id. A rule whose id or condition text is not the known one
 * is UNKNOWN and confirms nothing; there is deliberately no generic condition parser.
 */
public final class RuleEvaluator {

    private static final Map<String, String> KNOWN_CONDITIONS = Map.of(
            "R1", "delay_minutes >= 180",
            "R2", "delay_minutes >= 240 AND international",
            "R3", "missed_connection == true",
            "R4", "compensation_requested == true",
            "R5", "group_includes_minor == true");

    private static final int R1_THRESHOLD = 180;
    private static final int R2_THRESHOLD = 240;

    private RuleEvaluator() {}

    /** A policy version listed as superseded can not confirm any right. */
    public static boolean policyVerified(PolicyRules policy) {
        return !policy.supersededVersions().contains(policy.version());
    }

    public static Tri evaluate(Rule rule, EvaluationInput in, Flight flight, List<Connection> connections) {
        if (!policyVerified(in.policy())) return Tri.UNKNOWN;
        if (!rule.condition().equals(KNOWN_CONDITIONS.get(rule.ruleId()))) return Tri.UNKNOWN;
        return switch (rule.ruleId()) {
            case "R1" -> anySegment(flight, s -> delayAtLeast(s, R1_THRESHOLD));
            case "R2" -> anySegment(flight, s -> delayAtLeast(s, R2_THRESHOLD)
                    .and(international(in, s.segmentId())));
            case "R3" -> missedConnection(connections);
            case "R4" -> Tri.of(in.compensationRequested());
            case "R5" -> groupIncludesMinor(in.trip().passengers());
            default -> Tri.UNKNOWN;
        };
    }

    private static Tri delayAtLeast(SegmentFlight s, int threshold) {
        return switch (s.state()) {
            case DELAYED -> Tri.of(s.delayMinutes() >= threshold);
            case NO_DELAY_REPORTED -> Tri.FALSE;
            case UNCERTAIN, NO_INFORMATION -> Tri.UNKNOWN;
        };
    }

    private static Tri anySegment(Flight flight, Function<SegmentFlight, Tri> test) {
        return flight.segments().stream().map(test).reduce(Tri.FALSE, Tri::or);
    }

    private static Tri international(EvaluationInput in, String segmentId) {
        return in.trip().segments().stream()
                .filter(s -> s.segmentId().equals(segmentId))
                .findFirst()
                .map(s -> international(s, in.airportCountries()))
                .orElse(Tri.UNKNOWN);
    }

    private static Tri international(Segment s, Map<String, String> countries) {
        String origin = countries.get(s.origin());
        String destination = countries.get(s.destination());
        if (origin == null || destination == null) return Tri.UNKNOWN;
        return Tri.of(!origin.equals(destination));
    }

    /** The data never states a connection as missed, so a risk is UNKNOWN and never TRUE. */
    private static Tri missedConnection(List<Connection> connections) {
        return connections.stream()
                .map(c -> c.status() == ConnectionStatus.NO_RISK ? Tri.FALSE : Tri.UNKNOWN)
                .reduce(Tri.FALSE, Tri::or);
    }

    private static Tri groupIncludesMinor(List<Passenger> passengers) {
        return passengers.stream().map(RuleEvaluator::isMinor).reduce(Tri.FALSE, Tri::or);
    }

    private static Tri isMinor(Passenger p) {
        Integer age = p.age();
        PassengerType type = p.type();
        if (type == PassengerType.UNKNOWN) {
            return age == null ? Tri.UNKNOWN : Tri.of(age < 18);
        }
        boolean minorType = type == PassengerType.CHILD || type == PassengerType.INFANT;
        if (age == null) return Tri.of(minorType);
        // Type and age disagree (S-6): do not guess.
        return (age < 18) == minorType ? Tri.of(minorType) : Tri.UNKNOWN;
    }
}
