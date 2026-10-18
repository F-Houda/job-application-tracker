package dev.houda.jobtracker.api;

import dev.houda.jobtracker.domain.ApplicationStatus;
import dev.houda.jobtracker.domain.JobApplication;
import dev.houda.jobtracker.domain.StatusChange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class ApplicationDtos {

    private ApplicationDtos() {
    }

    public record ApplicationRequest(
            @NotBlank @Size(max = 200) String company,
            @NotBlank @Size(max = 200) String position,
            @Size(max = 500) String jobUrl,
            @Size(max = 2000) String notes) {
    }

    public record StatusRequest(@NotNull ApplicationStatus status) {
    }

    public record StatusChangeResponse(ApplicationStatus from, ApplicationStatus to, Instant changedAt) {
        static StatusChangeResponse of(StatusChange change) {
            return new StatusChangeResponse(change.getFromStatus(), change.getToStatus(), change.getChangedAt());
        }
    }

    public record ApplicationResponse(
            Long id,
            String company,
            String position,
            String jobUrl,
            ApplicationStatus status,
            String notes,
            Instant createdAt,
            Instant updatedAt,
            List<StatusChangeResponse> history) {

        static ApplicationResponse of(JobApplication app) {
            return new ApplicationResponse(
                    app.getId(), app.getCompany(), app.getPosition(), app.getJobUrl(),
                    app.getStatus(), app.getNotes(), app.getCreatedAt(), app.getUpdatedAt(),
                    app.getHistory().stream().map(StatusChangeResponse::of).toList());
        }

        /** List view: no history, so listing never triggers lazy loading per row. */
        static ApplicationResponse summary(JobApplication app) {
            return new ApplicationResponse(
                    app.getId(), app.getCompany(), app.getPosition(), app.getJobUrl(),
                    app.getStatus(), app.getNotes(), app.getCreatedAt(), app.getUpdatedAt(),
                    List.of());
        }
    }
}
