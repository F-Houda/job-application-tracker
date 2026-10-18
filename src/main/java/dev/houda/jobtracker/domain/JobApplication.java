package dev.houda.jobtracker.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_application")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String position;

    @Column(name = "job_url")
    private String jobUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.WISHLIST;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("changedAt ASC")
    private List<StatusChange> history = new ArrayList<>();

    protected JobApplication() {
        // for JPA
    }

    public JobApplication(String company, String position, String jobUrl, String notes) {
        this.company = company;
        this.position = position;
        this.jobUrl = jobUrl;
        this.notes = notes;
        this.history.add(new StatusChange(this, null, status));
    }

    /** Moves the application to a new status if the transition is allowed. */
    public void moveTo(ApplicationStatus next) {
        if (!status.canMoveTo(next)) {
            throw new IllegalStatusTransitionException(status, next);
        }
        history.add(new StatusChange(this, status, next));
        this.status = next;
        touch();
    }

    public void update(String company, String position, String jobUrl, String notes) {
        this.company = company;
        this.position = position;
        this.jobUrl = jobUrl;
        this.notes = notes;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getCompany() { return company; }
    public String getPosition() { return position; }
    public String getJobUrl() { return jobUrl; }
    public ApplicationStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<StatusChange> getHistory() { return history; }
}
