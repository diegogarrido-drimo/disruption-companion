package com.latam.disruption.adapter.fixture;

import com.latam.disruption.domain.EligibleOptions;
import com.latam.disruption.domain.OperationalEvents;
import com.latam.disruption.domain.PolicyRules;
import com.latam.disruption.domain.TripContext;
import com.latam.disruption.domain.TripContext.Passenger;
import com.latam.disruption.domain.TripContext.PassengerType;
import com.latam.disruption.domain.TripContext.Segment;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Maps the PDF's JSON to domain models. It reads only the fields the domain needs, so name, email, phone,
 * document and loyalty tier can not reach the output.
 */
final class FixtureReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FixtureReader() {}

    static JsonNode load(String resource) {
        try (InputStream in = FixtureReader.class.getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("Missing fixture " + resource);
            return MAPPER.readTree(in);
        } catch (IOException e) {
            throw new IllegalStateException("Unreadable fixture " + resource, e);
        }
    }

    static TripContext trip(JsonNode n) {
        List<Passenger> passengers = new ArrayList<>();
        for (JsonNode p : n.get("passengers")) {
            passengers.add(new Passenger(
                    p.get("passenger_id").asString(),
                    passengerType(p.path("type").asString("")),
                    p.hasNonNull("age") ? p.get("age").asInt() : null));
        }
        List<Segment> segments = new ArrayList<>();
        for (JsonNode s : n.get("segments")) {
            segments.add(new Segment(
                    s.get("segment_id").asString(),
                    s.get("flight_number").asString(),
                    s.get("origin").asString(),
                    s.get("destination").asString(),
                    LocalDate.parse(s.get("date").asString()),
                    LocalTime.parse(s.get("scheduled_departure").asString()),
                    LocalTime.parse(s.get("scheduled_arrival").asString())));
        }
        JsonNode meta = n.get("_meta");
        return new TripContext(
                n.get("booking_id").asString(), passengers, segments,
                Instant.parse(meta.get("retrieved_at").asString()), strings(meta.path("missing_fields")));
    }

    static OperationalEvents events(JsonNode n) {
        List<OperationalEvents.Event> events = new ArrayList<>();
        for (JsonNode e : n.get("events")) {
            events.add(new OperationalEvents.Event(
                    e.get("event_id").asString(),
                    e.get("segment_id").asString(),
                    e.get("type").asString(),
                    e.hasNonNull("status") ? e.get("status").asString() : null,
                    e.hasNonNull("delay_minutes") ? e.get("delay_minutes").asInt() : null,
                    e.hasNonNull("new_departure") ? e.get("new_departure").asString() : null,
                    e.get("source").asString(),
                    Instant.parse(e.get("recorded_at").asString())));
        }
        JsonNode meta = n.get("_meta");
        return new OperationalEvents(
                events, Instant.parse(meta.get("retrieved_at").asString()), strings(meta.path("sources_degraded")));
    }

    static EligibleOptions options(JsonNode n) {
        List<EligibleOptions.Option> options = new ArrayList<>();
        for (JsonNode o : n.get("options")) {
            options.add(new EligibleOptions.Option(
                    o.get("option_id").asString(),
                    o.get("type").asString(),
                    o.get("policy_ref").asString(),
                    o.hasNonNull("seats_available") ? o.get("seats_available").asInt() : null));
        }
        JsonNode meta = n.get("_meta");
        return new EligibleOptions(
                options, Instant.parse(meta.get("retrieved_at").asString()), meta.get("ttl_seconds").asInt());
    }

    static PolicyRules policy(JsonNode n) {
        List<PolicyRules.Rule> rules = new ArrayList<>();
        for (JsonNode r : n.get("rules")) {
            rules.add(new PolicyRules.Rule(
                    r.get("rule_id").asString(),
                    r.get("condition").asString(),
                    r.get("entitlement").asString(),
                    r.get("requires_human_approval").asBoolean()));
        }
        JsonNode meta = n.get("_meta");
        return new PolicyRules(
                n.get("scenario").asString(),
                n.get("version").asString(),
                LocalDate.parse(n.get("effective_from").asString()),
                rules,
                Instant.parse(meta.get("retrieved_at").asString()),
                strings(meta.path("superseded_versions")));
    }

    private static PassengerType passengerType(String raw) {
        try {
            return PassengerType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return PassengerType.UNKNOWN;
        }
    }

    private static Set<String> strings(JsonNode array) {
        Set<String> out = new HashSet<>();
        array.forEach(v -> out.add(v.asString()));
        return out;
    }
}
