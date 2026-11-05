package com.latam.disruption.application;

public class DependencyUnavailableException extends RuntimeException {

    private final String dependency;

    public DependencyUnavailableException(String dependency) {
        super("Dependency unavailable: " + dependency);
        this.dependency = dependency;
    }

    public String dependency() {
        return dependency;
    }
}
