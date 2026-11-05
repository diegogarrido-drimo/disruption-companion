package com.latam.disruption.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/** Minimal projection of get_trip_context: no name, email, phone, document or loyalty data. */
public record TripContext(
        String bookingId,
        List<Passenger> passengers,
        List<Segment> segments,
        Instant retrievedAt,
        Set<String> missingFields) {

    public static final String CHANGE_PENALTY_FIELD = "fare_conditions.change_penalty_amount";

    public TripContext {
        passengers = List.copyOf(passengers);
        segments = List.copyOf(segments);
        missingFields = Set.copyOf(missingFields);
    }

    public enum PassengerType { ADULT, CHILD, INFANT, UNKNOWN }

    /** {@code age} is null when the source does not provide it. */
    public record Passenger(String passengerId, PassengerType type, Integer age) {}

    public record Segment(
            String segmentId,
            String flightNumber,
            String origin,
            String destination,
            LocalDate date,
            LocalTime scheduledDeparture,
            LocalTime scheduledArrival) {}
}
