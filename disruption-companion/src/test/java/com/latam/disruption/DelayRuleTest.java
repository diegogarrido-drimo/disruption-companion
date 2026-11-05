package com.latam.disruption;

import static org.assertj.core.api.Assertions.assertThat;

import com.latam.disruption.domain.DisruptionEvaluator;
import com.latam.disruption.domain.Evaluation;
import com.latam.disruption.domain.Evaluation.EntitlementState;
import com.latam.disruption.domain.EvaluationInput;
import com.latam.disruption.domain.Tri;
import com.latam.disruption.domain.TripContext.Passenger;
import com.latam.disruption.domain.TripContext.PassengerType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** AC-2 */
class DelayRuleTest {

    private static final List<Passenger> ADULTS = List.of(
            new Passenger("P1", PassengerType.ADULT, null), new Passenger("P2", PassengerType.ADULT, null));

    private static Tri result(Evaluation e, String ruleId) {
        return e.rules().stream().filter(r -> r.ruleId().equals(ruleId)).findFirst().orElseThrow().result();
    }

    @ParameterizedTest
    @CsvSource({"179,FALSE", "180,TRUE", "200,TRUE"})
    void r1Boundary(int delay, Tri expected) {
        Evaluation e = DisruptionEvaluator.evaluate(TestData.withDelay(TestData.baseCase(), delay));
        assertThat(result(e, "R1")).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"239,FALSE", "240,TRUE"})
    void r2BoundaryUsesInternationalRoute(int delay, Tri expected) {
        Evaluation e = DisruptionEvaluator.evaluate(TestData.withDelay(TestData.baseCase(), delay));
        assertThat(result(e, "R2")).isEqualTo(expected);
    }

    @Test
    void r2IsUnknownWhenAirportIsNotInTheTable() {
        EvaluationInput base = TestData.withDelay(TestData.baseCase(), 240);
        EvaluationInput noTable = new EvaluationInput(base.trip(), base.events(), base.options(), base.policy(),
                base.at(), false, java.util.Map.of());
        assertThat(result(DisruptionEvaluator.evaluate(noTable), "R2")).isEqualTo(Tri.UNKNOWN);
    }

    @Test
    void delay200WithoutMinorIsSupportedWithoutHuman() {
        EvaluationInput in = TestData.withPassengers(TestData.withDelay(TestData.baseCase(), 200), ADULTS);
        Evaluation e = DisruptionEvaluator.evaluate(in);
        assertThat(e.rules().get(0).state()).isEqualTo(EntitlementState.CONFIRMED);
        assertThat(e.recommendationSupported()).isTrue();
        assertThat(e.humanInterventionRequired()).isFalse();
    }

    @Test
    void delay200WithMinorIsSupportedButNeedsApproval() {
        Evaluation e = DisruptionEvaluator.evaluate(TestData.withDelay(TestData.baseCase(), 200));
        assertThat(e.recommendationSupported()).isTrue();
        assertThat(e.humanInterventionRequired()).isTrue();
        assertThat(e.entitlements()).extracting(x -> x.entitlement() + ":" + x.state())
                .containsExactly("REBOOK_NO_FEE:CONFIRMED", "PRIORITY_HANDLING:PENDING_APPROVAL");
    }

    @Test
    void unknownR3DoesNotBlockWhenSameRightIsConfirmedByR1() {
        EvaluationInput in = TestData.withPassengers(TestData.withDelay(TestData.baseCase(), 200), ADULTS);
        Evaluation e = DisruptionEvaluator.evaluate(in);
        assertThat(result(e, "R3")).isEqualTo(Tri.UNKNOWN);
        assertThat(e.entitlements()).hasSize(1);
        assertThat(e.recommendationSupported()).isTrue();
    }

    @Test
    void compensationRequestedConfirmsR4PendingApproval() {
        EvaluationInput base = TestData.withDelay(TestData.baseCase(), 200);
        EvaluationInput in = new EvaluationInput(base.trip(), base.events(), base.options(), base.policy(),
                base.at(), true, base.airportCountries());
        Evaluation e = DisruptionEvaluator.evaluate(in);
        assertThat(result(e, "R4")).isEqualTo(Tri.TRUE);
        assertThat(e.humanInterventionRequired()).isTrue();
    }

    @Test
    void ruleWithUnexpectedConditionConfirmsNothing() {
        EvaluationInput base = TestData.withDelay(TestData.baseCase(), 200);
        var policy = base.policy();
        var altered = policy.rules().stream()
                .map(r -> r.ruleId().equals("R1")
                        ? new com.latam.disruption.domain.PolicyRules.Rule("R1", "delay_minutes >= 120", r.entitlement(), false)
                        : r)
                .toList();
        var changed = new com.latam.disruption.domain.PolicyRules(policy.scenario(), policy.version(),
                policy.effectiveFrom(), altered, policy.retrievedAt(), policy.supersededVersions());
        EvaluationInput in = new EvaluationInput(base.trip(), base.events(), base.options(), changed,
                base.at(), false, base.airportCountries());
        Evaluation e = DisruptionEvaluator.evaluate(in);
        assertThat(result(e, "R1")).isEqualTo(Tri.UNKNOWN);
        assertThat(e.recommendationSupported()).isFalse();
    }
}
