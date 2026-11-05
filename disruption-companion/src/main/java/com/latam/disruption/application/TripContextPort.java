package com.latam.disruption.application;

import com.latam.disruption.domain.TripContext;

public interface TripContextPort {
    TripContext getTripContext(String bookingId);
}
