package com.latam.disruption.domain;

import com.latam.disruption.domain.EligibleOptions.Option;
import com.latam.disruption.domain.Evaluation.CostStatus;
import com.latam.disruption.domain.Evaluation.OptionResult;
import com.latam.disruption.domain.Evaluation.OptionStatus;
import java.time.Instant;
import java.util.List;

/** Assigns one status per option, checked in a fixed order (first match wins). */
public final class OptionEvaluator {

    private static final String REFUND = "REFUND";

    private OptionEvaluator() {}

    public static List<OptionResult> evaluate(EvaluationInput in) {
        return in.options().options().stream().map(o -> evaluate(o, in)).toList();
    }

    private static OptionResult evaluate(Option o, EvaluationInput in) {
        Instant validUntil = in.options().validUntil();
        if (REFUND.equals(o.type())
                || !in.policy().version().equals(o.policyRef())
                || o.seatsAvailable() == null) {
            // Refund policy was never consulted; missing seats or a foreign policy_ref can not be verified.
            return new OptionResult(o.optionId(), OptionStatus.NOT_VERIFIABLE, null, null);
        }
        if (o.seatsAvailable() < in.trip().passengers().size()) {
            // The group must not be split.
            return new OptionResult(o.optionId(), OptionStatus.NOT_SUITABLE_GROUP, null, null);
        }
        CostStatus cost = in.trip().missingFields().contains(TripContext.CHANGE_PENALTY_FIELD)
                ? CostStatus.TO_CONFIRM
                : CostStatus.KNOWN;
        OptionStatus status = in.at().isBefore(validUntil)
                ? OptionStatus.CANDIDATE
                : OptionStatus.REQUIRES_REVALIDATION;
        return new OptionResult(o.optionId(), status, cost, validUntil);
    }
}
