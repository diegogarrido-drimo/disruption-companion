package com.latam.disruption.api;

import com.latam.disruption.application.DisruptionEvaluationService;
import com.latam.disruption.domain.Evaluation;
import java.time.Clock;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
public class EvaluationController {

    private final DisruptionEvaluationService service;
    private final Clock clock;

    public EvaluationController(DisruptionEvaluationService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    /** The clock is only used to fill {@code at} when the client omits it; the domain never reads it. */
    @GetMapping("/{bookingId}/disruption-evaluation")
    public Evaluation evaluate(
            @PathVariable String bookingId,
            @RequestParam(required = false) Instant at,
            @RequestParam(defaultValue = "false") boolean compensationRequested) {
        return service.evaluate(bookingId, at != null ? at : clock.instant(), compensationRequested);
    }
}
