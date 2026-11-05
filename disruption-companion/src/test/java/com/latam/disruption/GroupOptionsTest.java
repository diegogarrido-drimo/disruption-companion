package com.latam.disruption;

import static org.assertj.core.api.Assertions.assertThat;

import com.latam.disruption.domain.DisruptionEvaluator;
import com.latam.disruption.domain.Evaluation.OptionStatus;
import com.latam.disruption.domain.EvaluationInput;
import com.latam.disruption.domain.TripContext.Passenger;
import com.latam.disruption.domain.TripContext.PassengerType;
import java.util.List;
import org.junit.jupiter.api.Test;

/** AC-4 */
class GroupOptionsTest {

    private static OptionStatus statusOf(EvaluationInput in, String optionId) {
        return DisruptionEvaluator.evaluate(in).options().stream()
                .filter(o -> o.optionId().equals(optionId)).findFirst().orElseThrow().status();
    }

    @Test
    void optionWithOneSeatIsNotSuitableForTwoPassengers() {
        assertThat(statusOf(TestData.baseCase(), "O2")).isEqualTo(OptionStatus.NOT_SUITABLE_GROUP);
    }

    @Test
    void sameOptionIsCandidateForASinglePassenger() {
        EvaluationInput in = TestData.withPassengers(
                TestData.baseCase(), List.of(new Passenger("P1", PassengerType.ADULT, null)));
        assertThat(statusOf(in, "O2")).isEqualTo(OptionStatus.CANDIDATE);
    }

    @Test
    void refundIsNotVerifiableEvenThoughFareIsNotRefundable() {
        assertThat(statusOf(TestData.baseCase(), "O3")).isEqualTo(OptionStatus.NOT_VERIFIABLE);
    }
}
