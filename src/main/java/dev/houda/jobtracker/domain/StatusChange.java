package dev.houda.jobtracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "status_change")
public class StatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private JobApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private ApplicationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private ApplicationStatus toStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt = Instant.now();

    protected StatusChange() {
        // for JPA
    }

    StatusChange(JobApplication application, ApplicationStatus from, ApplicationStatus to) {
        this.application = application;
        this.fromStatus = from;
        this.toStatus = to;
    }

    public ApplicationStatus getFromStatus() { return fromStatus; }
    public ApplicationStatus getToStatus() { return toStatus; }
    public Instant getChangedAt() { return changedAt; }
}
