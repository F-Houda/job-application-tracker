package dev.houda.jobtracker.service;

import dev.houda.jobtracker.domain.ApplicationStatus;
import dev.houda.jobtracker.domain.JobApplication;
import dev.houda.jobtracker.domain.JobApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobApplicationService {

    private final JobApplicationRepository repository;

    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public JobApplication create(String company, String position, String jobUrl, String notes) {
        return repository.save(new JobApplication(company, position, jobUrl, notes));
    }

    @Transactional(readOnly = true)
    public JobApplication get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<JobApplication> search(ApplicationStatus status, String company, Pageable pageable) {
        boolean hasCompany = company != null && !company.isBlank();
        if (status != null && hasCompany) {
            return repository.findByStatusAndCompanyContainingIgnoreCase(status, company, pageable);
        }
        if (status != null) {
            return repository.findByStatus(status, pageable);
        }
        if (hasCompany) {
            return repository.findByCompanyContainingIgnoreCase(company, pageable);
        }
        return repository.findAll(pageable);
    }

    public JobApplication update(Long id, String company, String position, String jobUrl, String notes) {
        JobApplication app = get(id);
        app.update(company, position, jobUrl, notes);
        return app;
    }

    public JobApplication changeStatus(Long id, ApplicationStatus next) {
        JobApplication app = get(id);
        app.moveTo(next);
        return app;
    }

    public void delete(Long id) {
        repository.delete(get(id));
    }
}
