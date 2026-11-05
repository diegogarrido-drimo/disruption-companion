package com.latam.disruption.application;

/** Thrown when no information exists for a booking. Carries only the id, never trip data. */
public class BookingNotFoundException extends RuntimeException {

    private final String bookingId;

    public BookingNotFoundException(String bookingId) {
        super("No information for booking " + bookingId);
        this.bookingId = bookingId;
    }

    public String bookingId() {
        return bookingId;
    }
}
