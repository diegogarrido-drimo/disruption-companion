package com.latam.disruption.domain;

/** Three-valued logic: missing information must never collapse into FALSE. */
public enum Tri {
    TRUE, FALSE, UNKNOWN;

    public static Tri of(boolean value) {
        return value ? TRUE : FALSE;
    }

    /** Kleene AND: FALSE dominates, then UNKNOWN. */
    public Tri and(Tri other) {
        if (this == FALSE || other == FALSE) return FALSE;
        if (this == UNKNOWN || other == UNKNOWN) return UNKNOWN;
        return TRUE;
    }

    /** Kleene OR: TRUE dominates, then UNKNOWN. */
    public Tri or(Tri other) {
        if (this == TRUE || other == TRUE) return TRUE;
        if (this == UNKNOWN || other == UNKNOWN) return UNKNOWN;
        return FALSE;
    }
}
