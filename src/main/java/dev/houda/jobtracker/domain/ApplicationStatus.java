package dev.houda.jobtracker.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle of a job application. Allowed transitions are enforced in the domain,
 * not in the controller, so every entry point shares the same rules.
 */
public enum ApplicationStatus {
    WISHLIST,
    APPLIED,
    INTERVIEW,
    OFFER,
    REJECTED,
    WITHDRAWN;

    public Set<ApplicationStatus> allowedNext() {
        return switch (this) {
            case WISHLIST -> EnumSet.of(APPLIED, WITHDRAWN);
            case APPLIED -> EnumSet.of(INTERVIEW, REJECTED, WITHDRAWN);
            case INTERVIEW -> EnumSet.of(OFFER, REJECTED, WITHDRAWN);
            case OFFER -> EnumSet.of(WITHDRAWN);
            case REJECTED, WITHDRAWN -> EnumSet.noneOf(ApplicationStatus.class);
        };
    }

    public boolean canMoveTo(ApplicationStatus next) {
        return allowedNext().contains(next);
    }
}
