package com.latam.disruption;

import static org.assertj.core.api.Assertions.assertThat;

import com.latam.disruption.domain.DisruptionEvaluator;
import com.latam.disruption.domain.Evaluation;
import com.latam.disruption.domain.Evaluation.OptionStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** AC-5 */
class OptionExpiryTest {

    private static Evaluation.OptionResult o1(String at) {
        return DisruptionEvaluator.evaluate(TestData.at(TestData.baseCase(), at)).options().get(0);
    }

    @Test
    void validOneSecondBeforeExpiry() {
        assertThat(o1("2026-09-15T11:04:34Z").status()).isEqualTo(OptionStatus.CANDIDATE);
    }

    @Test
    void expiredFromTheExpiryInstant() {
        var o = o1("2026-09-15T11:04:35Z");
        assertThat(o.status()).isEqualTo(OptionStatus.REQUIRES_REVALIDATION);
        assertThat(o.validUntil()).isEqualTo(Instant.parse("2026-09-15T11:04:35Z"));
    }

    @Test
    void expiredOptionIsListedAndAddsAReason() {
        Evaluation e = DisruptionEvaluator.evaluate(TestData.at(TestData.baseCase(), "2026-09-15T11:10:00Z"));
        assertThat(e.options()).hasSize(3);
        assertThat(e.reasons()).extracting(r -> r.code() + ":" + r.optionId())
                .contains("OPTION_REQUIRES_REVALIDATION:O1");
    }
}
