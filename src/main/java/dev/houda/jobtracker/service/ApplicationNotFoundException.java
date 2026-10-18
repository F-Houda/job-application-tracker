package dev.houda.jobtracker.service;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(Long id) {
        super("Application " + id + " not found");
    }
}
