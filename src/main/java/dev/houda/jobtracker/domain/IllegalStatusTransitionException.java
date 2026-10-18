package dev.houda.jobtracker.domain;

public class IllegalStatusTransitionException extends RuntimeException {

    public IllegalStatusTransitionException(ApplicationStatus from, ApplicationStatus to) {
        super("Cannot move application from " + from + " to " + to + ". Allowed: " + from.allowedNext());
    }
}
