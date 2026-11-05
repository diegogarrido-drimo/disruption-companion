package com.latam.disruption.application;

import com.latam.disruption.domain.EligibleOptions;
import com.latam.disruption.domain.TripContext;

public interface EligibleOptionsPort {
    EligibleOptions getEligibleOptions(TripContext trip);
}
