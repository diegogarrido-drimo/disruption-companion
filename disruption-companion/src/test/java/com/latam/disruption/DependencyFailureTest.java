package com.latam.disruption;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latam.disruption.adapter.fixture.FixtureAdapter;
import com.latam.disruption.api.ApiExceptionHandler;
import com.latam.disruption.api.EvaluationController;
import com.latam.disruption.application.DependencyUnavailableException;
import com.latam.disruption.application.DisruptionEvaluationService;
import com.latam.disruption.application.OperationalEventsPort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** AC-7 (stretch) and HTTP shape: controlled errors never carry an evaluation body. */
class DependencyFailureTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-15T11:03:00Z"), ZoneOffset.UTC);

    private static MockMvc mvc(OperationalEventsPort events) {
        FixtureAdapter f = new FixtureAdapter();
        var service = new DisruptionEvaluationService(f, events, f, f, TestData.AIRPORTS);
        return MockMvcBuilders.standaloneSetup(new EvaluationController(service, CLOCK))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void eventsDownGivesControlled503WithoutEvaluation() throws Exception {
        OperationalEventsPort down = ids -> { throw new DependencyUnavailableException("get_operational_events"); };
        mvc(down).perform(get("/api/v1/bookings/ABC123/disruption-evaluation"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DEPENDENCY_UNAVAILABLE"))
                .andExpect(jsonPath("$.dependency").value("get_operational_events"))
                .andExpect(jsonPath("$.recommendationSupported").doesNotExist());
    }

    @Test
    void baseCaseOverHttpUsesClockWhenAtIsOmitted() throws Exception {
        mvc(new FixtureAdapter()).perform(get("/api/v1/bookings/ABC123/disruption-evaluation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationSupported").value(false))
                .andExpect(jsonPath("$.humanInterventionRequired").value(true))
                .andExpect(jsonPath("$.evaluatedAt").value("2026-09-15T11:03:00Z"))
                .andExpect(jsonPath("$.actionsExecuted").isEmpty());
    }

    @Test
    void unknownBookingIs404WithoutEvaluationOrTripData() throws Exception {
        mvc(new FixtureAdapter()).perform(get("/api/v1/bookings/XYZ999/disruption-evaluation"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOKING_NOT_FOUND"))
                .andExpect(jsonPath("$.bookingId").value("XYZ999"))
                .andExpect(jsonPath("$.recommendationSupported").doesNotExist())
                .andExpect(jsonPath("$.flight").doesNotExist());
    }

    @Test
    void malformedInstantIs400() throws Exception {
        mvc(new FixtureAdapter()).perform(get("/api/v1/bookings/ABC123/disruption-evaluation?at=ayer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }
}
