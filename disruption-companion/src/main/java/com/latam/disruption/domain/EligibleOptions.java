package com.latam.disruption.domain;

import java.time.Instant;
import java.util.List;

public record EligibleOptions(List<Option> options, Instant retrievedAt, int ttlSeconds) {

    public EligibleOptions {
        options = List.copyOf(options);
    }

    /** An option is expired from this instant on, inclusive. */
    public Instant validUntil() {
        return retrievedAt.plusSeconds(ttlSeconds);
    }

    /** {@code seatsAvailable} is null when the source omits it (e.g. refunds). */
    public record Option(String optionId, String type, String policyRef, Integer seatsAvailable) {}
}
