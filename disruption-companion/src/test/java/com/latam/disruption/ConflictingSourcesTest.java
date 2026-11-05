package com.latam.disruption;

import static org.assertj.core.api.Assertions.assertThat;

import com.latam.disruption.domain.DisruptionEvaluator;
import com.latam.disruption.domain.Evaluation;
import com.latam.disruption.domain.Evaluation.SegmentState;
import com.latam.disruption.domain.EvaluationInput;
import com.latam.disruption.domain.OperationalEvents.Event;
import java.time.Instant;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/** AC-3 */
class ConflictingSourcesTest {

    @Test
    void baseCaseDiscardsOlderDegradedOnTimeWithReason() {
        Evaluation e = DisruptionEvaluator.evaluate(TestData.baseCase());
        assertThat(e.flight().discardedEvents()).singleElement().satisfies(d -> {
            assertThat(d.eventId()).isEqualTo("E-1012");
            assertThat(d.reason()).isEqualTo("DEGRADED_AND_OLDER");
            assertThat(d.source()).isEqualTo("LEGACY_MIRROR");
        });
    }

    @Test
    void newerDegradedOnTimeMakesSegmentUncertain() {
        EvaluationInput in = TestData.withEvents(TestData.withDelay(TestData.baseCase(), 200), events -> {
            var all = new ArrayList<>(events);
            all.removeIf(e -> e.eventId().equals("E-1012"));
            all.add(new Event("E-1099", "S1", "STATUS", "ON_TIME", null, null, "LEGACY_MIRROR",
                    Instant.parse("2026-09-15T10:58:00Z")));
            return all;
        });
        Evaluation e = DisruptionEvaluator.evaluate(in);
        assertThat(e.flight().segments().get(0).state()).isEqualTo(SegmentState.UNCERTAIN);
        assertThat(e.recommendationSupported()).isFalse();
        assertThat(e.humanInterventionRequired()).isTrue();
        assertThat(e.flight().discardedEvents()).extracting(d -> d.eventId() + ":" + d.reason())
                .contains("E-1099:DEGRADED_CONTRADICTS_DELAY");
    }

    @Test
    void newerHealthyOnTimeAlsoMakesSegmentUncertain() {
        EvaluationInput in = TestData.withEvents(TestData.baseCase(), events -> {
            var all = new ArrayList<>(events);
            all.add(new Event("E-1100", "S1", "STATUS", "ON_TIME", null, null, "OPS_CORE",
                    Instant.parse("2026-09-15T10:58:00Z")));
            return all;
        });
        Evaluation e = DisruptionEvaluator.evaluate(in);
        assertThat(e.flight().segments().get(0).state()).isEqualTo(SegmentState.UNCERTAIN);
        assertThat(e.flight().discardedEvents()).extracting(d -> d.reason()).contains("CONTRADICTS_DELAY");
    }

    @Test
    void segmentWithoutEventsIsNoInformationNotOnTime() {
        EvaluationInput in = TestData.withEvents(TestData.baseCase(),
                events -> events.stream().filter(e -> !e.segmentId().equals("S2")).toList());
        Evaluation e = DisruptionEvaluator.evaluate(in);
        assertThat(e.flight().segments().get(1).state()).isEqualTo(SegmentState.NO_INFORMATION);
        assertThat(e.recommendationSupported()).isFalse();
    }
}
