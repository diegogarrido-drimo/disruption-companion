package com.latam.disruption.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record PolicyRules(
        String scenario,
        String version,
        LocalDate effectiveFrom,
        List<Rule> rules,
        Instant retrievedAt,
        Set<String> supersededVersions) {

    public PolicyRules {
        rules = List.copyOf(rules);
        supersededVersions = Set.copyOf(supersededVersions);
    }

    public record Rule(String ruleId, String condition, String entitlement, boolean requiresHumanApproval) {}
}
