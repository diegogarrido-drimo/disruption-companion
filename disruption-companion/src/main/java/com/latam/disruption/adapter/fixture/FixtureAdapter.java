package com.latam.disruption.adapter.fixture;

import com.latam.disruption.application.BookingNotFoundException;
import com.latam.disruption.application.EligibleOptionsPort;
import com.latam.disruption.application.OperationalEventsPort;
import com.latam.disruption.application.PolicyRulesPort;
import com.latam.disruption.application.TripContextPort;
import com.latam.disruption.domain.EligibleOptions;
import com.latam.disruption.domain.OperationalEvents;
import com.latam.disruption.domain.PolicyRules;
import com.latam.disruption.domain.TripContext;
import java.util.List;

/** Serves the PDF's example responses for booking ABC123 only; any other booking is not found. */
public class FixtureAdapter implements TripContextPort, OperationalEventsPort, PolicyRulesPort, EligibleOptionsPort {

    private static final String BOOKING_ID = "ABC123";
    private static final String BASE = "/fixtures/abc123/";

    @Override
    public TripContext getTripContext(String bookingId) {
        if (!BOOKING_ID.equals(bookingId)) throw new BookingNotFoundException(bookingId);
        return FixtureReader.trip(FixtureReader.load(BASE + "trip-context.json"));
    }

    @Override
    public OperationalEvents getOperationalEvents(List<String> segmentIds) {
        return FixtureReader.events(FixtureReader.load(BASE + "events.json"));
    }

    @Override
    public PolicyRules getPolicyRules(String scenario) {
        return FixtureReader.policy(FixtureReader.load(BASE + "policy-delay.json"));
    }

    @Override
    public EligibleOptions getEligibleOptions(TripContext trip) {
        return FixtureReader.options(FixtureReader.load(BASE + "options.json"));
    }
}
