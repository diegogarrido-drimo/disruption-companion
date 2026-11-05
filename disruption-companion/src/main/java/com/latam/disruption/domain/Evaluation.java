package com.latam.disruption.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record Evaluation(
        String bookingId,
        Instant evaluatedAt,
        boolean recommendationSupported,
        boolean humanInterventionRequired,
        List<Reason> reasons,
        Flight flight,
        List<Connection> connections,
        List<RuleResult> rules,
        List<Entitlement> entitlements,
        List<OptionResult> options,
        PolicyInfo policy,
        Meta meta,
        List<String> actionsExecuted) {

    /** Only the fields relevant to the code are non-null. */
    public record Reason(String code, String ruleId, String optionId, String segmentId, String eventId) {
        public static Reason rule(String code, String ruleId) {
            return new Reason(code, ruleId, null, null, null);
        }

        public static Reason option(String code, String optionId) {
            return new Reason(code, null, optionId, null, null);
        }

        public static Reason segment(String code, String segmentId) {
            return new Reason(code, null, null, segmentId, null);
        }

        public static Reason of(String code) {
            return new Reason(code, null, null, null, null);
        }
    }

    public enum SegmentState { DELAYED, NO_DELAY_REPORTED, UNCERTAIN, NO_INFORMATION }

    public record EventBasis(String eventId, String source, Instant recordedAt) {}

    public record SegmentFlight(
            String segmentId, SegmentState state, Integer delayMinutes, String newDeparture, EventBasis basis) {}

    public record DiscardedEvent(String eventId, String segmentId, String reason, String source) {}

    public record InformationalEvent(String eventId, String type) {}

    public record Flight(
            List<SegmentFlight> segments,
            List<DiscardedEvent> discardedEvents,
            List<InformationalEvent> informationalEvents) {}

    public enum ConnectionStatus { AT_RISK, NO_RISK, UNDETERMINED }

    /** {@code confirmed} is always false: the data never says a connection was actually missed. */
    public record Connection(
            String from,
            String to,
            ConnectionStatus status,
            boolean confirmed,
            String projectedArrival,
            String nextDeparture,
            Integer deficitMinutes) {}

    public enum EntitlementState { CONFIRMED, PENDING_APPROVAL }

    /** {@code entitlement} and {@code state} are set only when the rule is TRUE. */
    public record RuleResult(String ruleId, Tri result, String entitlement, EntitlementState state) {}

    public record Entitlement(String entitlement, List<String> ruleIds, EntitlementState state) {}

    public enum OptionStatus { CANDIDATE, REQUIRES_REVALIDATION, NOT_SUITABLE_GROUP, NOT_VERIFIABLE }

    public enum CostStatus { TO_CONFIRM, KNOWN }

    /** {@code cost} and {@code validUntil} are set only for options that could be offered. */
    public record OptionResult(String optionId, OptionStatus status, CostStatus cost, Instant validUntil) {}

    public record PolicyInfo(String version, LocalDate effectiveFrom, boolean verified) {}

    public record Meta(
            Instant tripRetrievedAt,
            Instant eventsRetrievedAt,
            Instant optionsRetrievedAt,
            Instant policyRetrievedAt) {}
}
