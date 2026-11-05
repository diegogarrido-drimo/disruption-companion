package com.latam.disruption.application;

import com.latam.disruption.domain.DisruptionEvaluator;
import com.latam.disruption.domain.EligibleOptions;
import com.latam.disruption.domain.Evaluation;
import com.latam.disruption.domain.EvaluationInput;
import com.latam.disruption.domain.OperationalEvents;
import com.latam.disruption.domain.PolicyRules;
import com.latam.disruption.domain.TripContext;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Orchestrates the four read ports and the pure evaluator. Read-only: it performs no writes. */
public class DisruptionEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(DisruptionEvaluationService.class);
    private static final String SCENARIO = "DELAY";

    private final TripContextPort trips;
    private final OperationalEventsPort events;
    private final PolicyRulesPort policies;
    private final EligibleOptionsPort options;
    private final Map<String, String> airportCountries;

    public DisruptionEvaluationService(
            TripContextPort trips,
            OperationalEventsPort events,
            PolicyRulesPort policies,
            EligibleOptionsPort options,
            Map<String, String> airportCountries) {
        this.trips = trips;
        this.events = events;
        this.policies = policies;
        this.options = options;
        this.airportCountries = Map.copyOf(airportCountries);
    }

    public Evaluation evaluate(String bookingId, Instant at, boolean compensationRequested) {
        TripContext trip = trips.getTripContext(bookingId);
        List<String> segmentIds = trip.segments().stream().map(TripContext.Segment::segmentId).toList();
        OperationalEvents operationalEvents = events.getOperationalEvents(segmentIds);
        PolicyRules policy = policies.getPolicyRules(SCENARIO);
        EligibleOptions eligibleOptions = options.getEligibleOptions(trip);

        Evaluation evaluation = DisruptionEvaluator.evaluate(new EvaluationInput(
                trip, operationalEvents, eligibleOptions, policy, at, compensationRequested, airportCountries));
        log.info("evaluation bookingId={} at={} supported={} humanRequired={}",
                bookingId, at, evaluation.recommendationSupported(), evaluation.humanInterventionRequired());
        return evaluation;
    }
}
