package com.latam.disruption;

import com.latam.disruption.adapter.fixture.FixtureAdapter;
import com.latam.disruption.domain.EligibleOptions;
import com.latam.disruption.domain.EvaluationInput;
import com.latam.disruption.domain.OperationalEvents;
import com.latam.disruption.domain.OperationalEvents.Event;
import com.latam.disruption.domain.TripContext;
import com.latam.disruption.domain.TripContext.Passenger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/** Base case from the PDF plus helpers to derive synthetic scenarios. */
final class TestData {

    static final Instant BASE_AT = Instant.parse("2026-09-15T11:03:00Z");
    static final Map<String, String> AIRPORTS = Map.of("SCL", "CL", "LIM", "PE", "MEX", "MX");

    private static final FixtureAdapter FIXTURES = new FixtureAdapter();

    private TestData() {}

    static EvaluationInput baseCase() {
        TripContext trip = FIXTURES.getTripContext("ABC123");
        return new EvaluationInput(
                trip,
                FIXTURES.getOperationalEvents(List.of("S1", "S2")),
                FIXTURES.getEligibleOptions(trip),
                FIXTURES.getPolicyRules("DELAY"),
                BASE_AT,
                false,
                AIRPORTS);
    }

    static EvaluationInput at(EvaluationInput in, String instant) {
        return new EvaluationInput(in.trip(), in.events(), in.options(), in.policy(),
                Instant.parse(instant), in.compensationRequested(), in.airportCountries());
    }

    static EvaluationInput withEvents(EvaluationInput in, UnaryOperator<List<Event>> change) {
        OperationalEvents e = in.events();
        return new EvaluationInput(in.trip(),
                new OperationalEvents(change.apply(e.events()), e.retrievedAt(), e.degradedSources()),
                in.options(), in.policy(), in.at(), in.compensationRequested(), in.airportCountries());
    }

    static EvaluationInput withPassengers(EvaluationInput in, List<Passenger> passengers) {
        TripContext t = in.trip();
        return new EvaluationInput(
                new TripContext(t.bookingId(), passengers, t.segments(), t.retrievedAt(), t.missingFields()),
                in.events(), in.options(), in.policy(), in.at(), in.compensationRequested(), in.airportCountries());
    }

    /** Replaces E-1001 with a delay of the given minutes. */
    static EvaluationInput withDelay(EvaluationInput in, int minutes) {
        return withEvents(in, events -> events.stream()
                .map(e -> e.eventId().equals("E-1001")
                        ? new Event(e.eventId(), e.segmentId(), e.type(), null, minutes, null, e.source(), e.recordedAt())
                        : e)
                .toList());
    }
}
