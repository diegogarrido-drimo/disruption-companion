package com.latam.disruption.domain;

import com.latam.disruption.domain.Evaluation.Connection;
import com.latam.disruption.domain.Evaluation.CostStatus;
import com.latam.disruption.domain.Evaluation.Entitlement;
import com.latam.disruption.domain.Evaluation.EntitlementState;
import com.latam.disruption.domain.Evaluation.Flight;
import com.latam.disruption.domain.Evaluation.Meta;
import com.latam.disruption.domain.Evaluation.OptionResult;
import com.latam.disruption.domain.Evaluation.OptionStatus;
import com.latam.disruption.domain.Evaluation.PolicyInfo;
import com.latam.disruption.domain.Evaluation.Reason;
import com.latam.disruption.domain.Evaluation.RuleResult;
import com.latam.disruption.domain.Evaluation.SegmentState;
import com.latam.disruption.domain.PolicyRules.Rule;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Pure function: same input, same output. No clock, no IO, no framework. */
public final class DisruptionEvaluator {

    private DisruptionEvaluator() {}

    public static Evaluation evaluate(EvaluationInput in) {
        boolean policyVerified = RuleEvaluator.policyVerified(in.policy());
        Flight flight = FlightStateResolver.resolve(in.trip().segments(), in.events());
        List<Connection> connections = ConnectionAnalyzer.analyze(in.trip().segments(), flight);

        List<RuleResult> ruleResults = new ArrayList<>();
        Map<String, Rule> rulesById = new LinkedHashMap<>();
        for (Rule rule : in.policy().rules()) {
            rulesById.put(rule.ruleId(), rule);
            Tri result = RuleEvaluator.evaluate(rule, in, flight, connections);
            ruleResults.add(result == Tri.TRUE
                    ? new RuleResult(rule.ruleId(), result, rule.entitlement(), stateOf(rule))
                    : new RuleResult(rule.ruleId(), result, null, null));
        }
        List<Entitlement> entitlements = entitlements(ruleResults);
        List<OptionResult> options = OptionEvaluator.evaluate(in);

        Set<String> confirmed = entitlements.stream()
                .filter(e -> e.state() == EntitlementState.CONFIRMED)
                .map(Entitlement::entitlement)
                .collect(java.util.stream.Collectors.toSet());
        boolean flightResolved = flight.segments().stream()
                .allMatch(s -> s.state() == SegmentState.DELAYED || s.state() == SegmentState.NO_DELAY_REPORTED);

        List<Reason> reasons = new ArrayList<>();
        if (!policyVerified) reasons.add(Reason.of("POLICY_NOT_VERIFIED"));
        flight.segments().stream()
                .filter(s -> s.state() == SegmentState.UNCERTAIN || s.state() == SegmentState.NO_INFORMATION)
                .forEach(s -> reasons.add(Reason.segment("FLIGHT_STATE_UNCERTAIN", s.segmentId())));
        boolean blockingUnknown = false;
        for (RuleResult r : ruleResults) {
            if (r.result() == Tri.UNKNOWN && !confirmed.contains(rulesById.get(r.ruleId()).entitlement())) {
                blockingUnknown = true;
                reasons.add(Reason.rule("CRITICAL_RULE_UNKNOWN", r.ruleId()));
            }
        }
        boolean approvalPending = false;
        for (RuleResult r : ruleResults) {
            if (r.state() == EntitlementState.PENDING_APPROVAL) {
                approvalPending = true;
                reasons.add(Reason.rule("APPROVAL_PENDING", r.ruleId()));
            }
        }
        for (OptionResult o : options) {
            if (o.status() == OptionStatus.REQUIRES_REVALIDATION) {
                reasons.add(Reason.option("OPTION_REQUIRES_REVALIDATION", o.optionId()));
            } else if (o.status() == OptionStatus.CANDIDATE && o.cost() == CostStatus.TO_CONFIRM) {
                reasons.add(Reason.option("OPTION_COST_TO_CONFIRM", o.optionId()));
            }
        }

        boolean supported = policyVerified && flightResolved && !confirmed.isEmpty() && !blockingUnknown;
        boolean humanRequired = !supported || approvalPending;
        if (!supported && reasons.isEmpty()) reasons.add(Reason.of("NO_CONFIRMED_ENTITLEMENT"));

        return new Evaluation(
                in.trip().bookingId(),
                in.at(),
                supported,
                humanRequired,
                reasons,
                flight,
                connections,
                ruleResults,
                entitlements,
                options,
                new PolicyInfo(in.policy().version(), in.policy().effectiveFrom(), policyVerified),
                new Meta(
                        in.trip().retrievedAt(),
                        in.events().retrievedAt(),
                        in.options().retrievedAt(),
                        in.policy().retrievedAt()),
                List.of());
    }

    private static EntitlementState stateOf(Rule rule) {
        return rule.requiresHumanApproval() ? EntitlementState.PENDING_APPROVAL : EntitlementState.CONFIRMED;
    }

    /** One entitlement per name: R1 and R3 both grant REBOOK_NO_FEE. CONFIRMED wins over PENDING_APPROVAL. */
    private static List<Entitlement> entitlements(List<RuleResult> results) {
        Map<String, List<RuleResult>> byName = new LinkedHashMap<>();
        for (RuleResult r : results) {
            if (r.result() == Tri.TRUE) byName.computeIfAbsent(r.entitlement(), k -> new ArrayList<>()).add(r);
        }
        List<Entitlement> out = new ArrayList<>();
        byName.forEach((name, rs) -> {
            boolean anyConfirmed = rs.stream().anyMatch(r -> r.state() == EntitlementState.CONFIRMED);
            out.add(new Entitlement(
                    name,
                    rs.stream().map(RuleResult::ruleId).toList(),
                    anyConfirmed ? EntitlementState.CONFIRMED : EntitlementState.PENDING_APPROVAL));
        });
        return out;
    }
}
