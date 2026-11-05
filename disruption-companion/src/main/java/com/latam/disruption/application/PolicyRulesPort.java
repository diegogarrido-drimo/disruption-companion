package com.latam.disruption.application;

import com.latam.disruption.domain.PolicyRules;

public interface PolicyRulesPort {
    PolicyRules getPolicyRules(String scenario);
}
