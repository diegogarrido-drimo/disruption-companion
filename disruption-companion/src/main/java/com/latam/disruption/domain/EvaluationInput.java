package com.latam.disruption.domain;

import java.time.Instant;
import java.util.Map;

/**
 * Everything the evaluator needs. The reference instant is a parameter: the domain never reads a clock.
 *
 * @param airportCountries airport code to country, used only to derive "international" (R2)
 */
public record EvaluationInput(
        TripContext trip,
        OperationalEvents events,
        EligibleOptions options,
        PolicyRules policy,
        Instant at,
        boolean compensationRequested,
        Map<String, String> airportCountries) {

    public EvaluationInput {
        airportCountries = Map.copyOf(airportCountries);
    }
}
