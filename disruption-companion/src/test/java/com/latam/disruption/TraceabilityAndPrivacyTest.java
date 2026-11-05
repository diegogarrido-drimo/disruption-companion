package com.latam.disruption;

import static org.assertj.core.api.Assertions.assertThat;

import com.latam.disruption.application.DisruptionEvaluationService;
import com.latam.disruption.adapter.fixture.FixtureAdapter;
import com.latam.disruption.domain.Evaluation;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** AC-6, and the service wiring over the fixture adapter. */
class TraceabilityAndPrivacyTest {

    private static final ObjectMapper MAPPER =
            JsonMapper.builder().build();

    private final Evaluation evaluation = service().evaluate("ABC123", TestData.BASE_AT, false);

    private static DisruptionEvaluationService service() {
        FixtureAdapter fixtures = new FixtureAdapter();
        return new DisruptionEvaluationService(fixtures, fixtures, fixtures, fixtures, TestData.AIRPORTS);
    }

    @Test
    void outputCarriesPolicyRuleEventAndTimestampEvidence() throws Exception {
        String json = MAPPER.writeValueAsString(evaluation);
        assertThat(json).contains("POL-DELAY-2026-03", "R1", "R5", "E-1001", "E-1012", "E-1007", "OPS_CORE",
                "2026-09-15T11:02:31Z", "2026-09-15T11:02:33Z", "2026-09-15T11:02:35Z", "2026-09-15T11:02:36Z",
                "2026-09-15T11:03:00Z");
    }

    @Test
    void outputHasNoPersonalData() throws Exception {
        String json = MAPPER.writeValueAsString(evaluation);
        assertThat(json).doesNotContain(
                "maria.silva@example.com", "+56 9 8765 4321", "12.345.678-9",
                "María", "Tomás", "Silva", "GOLD", "loyalty");
        assertThat(evaluation.toString()).doesNotContain("María", "Tomás", "maria.silva");
    }

    @Test
    void serviceUsesTheGivenInstant() {
        Evaluation later = service().evaluate("ABC123", Instant.parse("2026-09-15T11:10:00Z"), false);
        assertThat(later.evaluatedAt()).isEqualTo(Instant.parse("2026-09-15T11:10:00Z"));
    }
}
