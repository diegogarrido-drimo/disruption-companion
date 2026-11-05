package com.latam.disruption;

import static org.assertj.core.api.Assertions.assertThat;

import com.latam.disruption.domain.DisruptionEvaluator;
import com.latam.disruption.domain.Evaluation;
import com.latam.disruption.domain.Evaluation.ConnectionStatus;
import com.latam.disruption.domain.Evaluation.EntitlementState;
import com.latam.disruption.domain.Evaluation.OptionStatus;
import com.latam.disruption.domain.Evaluation.SegmentState;
import com.latam.disruption.domain.Tri;
import org.junit.jupiter.api.Test;

/** AC-1 */
class BaseCaseTest {

    private final Evaluation result = DisruptionEvaluator.evaluate(TestData.baseCase());

    @Test
    void rulesFollowThePolicy() {
        assertThat(result.rules()).extracting(r -> r.ruleId() + ":" + r.result())
                .containsExactly("R1:FALSE", "R2:FALSE", "R3:UNKNOWN", "R4:FALSE", "R5:TRUE");
        var r5 = result.rules().get(4);
        assertThat(r5.entitlement()).isEqualTo("PRIORITY_HANDLING");
        assertThat(r5.state()).isEqualTo(EntitlementState.PENDING_APPROVAL);
    }

    @Test
    void flightDelayedOnS1AndNothingReportedOnS2() {
        var s1 = result.flight().segments().get(0);
        assertThat(s1.state()).isEqualTo(SegmentState.DELAYED);
        assertThat(s1.delayMinutes()).isEqualTo(160);
        assertThat(s1.newDeparture()).isEqualTo("17:10");
        assertThat(s1.basis().eventId()).isEqualTo("E-1001");
        assertThat(result.flight().segments().get(1).state()).isEqualTo(SegmentState.NO_DELAY_REPORTED);
        assertThat(result.flight().informationalEvents()).extracting(e -> e.eventId()).containsExactly("E-1007");
    }

    @Test
    void connectionIsAtRiskNeverConfirmed() {
        var c = result.connections().get(0);
        assertThat(c.status()).isEqualTo(ConnectionStatus.AT_RISK);
        assertThat(c.confirmed()).isFalse();
        assertThat(c.projectedArrival()).isEqualTo("20:35");
        assertThat(c.nextDeparture()).isEqualTo("19:05");
        assertThat(c.deficitMinutes()).isEqualTo(90);
    }

    @Test
    void optionsAreClassified() {
        assertThat(result.options()).extracting(o -> o.optionId() + ":" + o.status())
                .containsExactly("O1:CANDIDATE", "O2:NOT_SUITABLE_GROUP", "O3:NOT_VERIFIABLE");
        assertThat(result.options().get(0).cost()).isEqualTo(Evaluation.CostStatus.TO_CONFIRM);
    }

    @Test
    void noSupportedRecommendationAndHumanRequired() {
        assertThat(result.recommendationSupported()).isFalse();
        assertThat(result.humanInterventionRequired()).isTrue();
        assertThat(result.actionsExecuted()).isEmpty();
        assertThat(result.reasons()).extracting(r -> r.code() + ":" + (r.ruleId() != null ? r.ruleId() : r.optionId()))
                .contains("CRITICAL_RULE_UNKNOWN:R3", "APPROVAL_PENDING:R5", "OPTION_COST_TO_CONFIRM:O1");
    }

    @Test
    void sameInputGivesSameOutput() {
        assertThat(DisruptionEvaluator.evaluate(TestData.baseCase())).isEqualTo(result);
    }

    @Test
    void triLogicFollowsKleene() {
        assertThat(Tri.FALSE.and(Tri.UNKNOWN)).isEqualTo(Tri.FALSE);
        assertThat(Tri.TRUE.and(Tri.UNKNOWN)).isEqualTo(Tri.UNKNOWN);
        assertThat(Tri.TRUE.or(Tri.UNKNOWN)).isEqualTo(Tri.TRUE);
        assertThat(Tri.FALSE.or(Tri.UNKNOWN)).isEqualTo(Tri.UNKNOWN);
    }
}
