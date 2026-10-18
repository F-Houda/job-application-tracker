package dev.houda.jobtracker.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    /** Loads the status history together with the application (open-in-view is disabled). */
    @Override
    @EntityGraph(attributePaths = "history")
    Optional<JobApplication> findById(Long id);

    Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    Page<JobApplication> findByCompanyContainingIgnoreCase(String company, Pageable pageable);

    Page<JobApplication> findByStatusAndCompanyContainingIgnoreCase(
            ApplicationStatus status, String company, Pageable pageable);
}
