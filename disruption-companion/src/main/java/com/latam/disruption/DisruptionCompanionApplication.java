package com.latam.disruption;

import com.latam.disruption.adapter.fixture.FixtureAdapter;
import com.latam.disruption.application.DisruptionEvaluationService;
import java.time.Clock;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DisruptionCompanionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DisruptionCompanionApplication.class, args);
    }

    @Bean
    FixtureAdapter fixtureAdapter() {
        return new FixtureAdapter();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    DisruptionEvaluationService evaluationService(
            FixtureAdapter fixtures, @Value("#{${disruption.airport-countries}}") Map<String, String> airports) {
        return new DisruptionEvaluationService(fixtures, fixtures, fixtures, fixtures, airports);
    }
}
