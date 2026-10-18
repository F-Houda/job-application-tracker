package dev.houda.jobtracker.api;

import dev.houda.jobtracker.api.ApplicationDtos.ApplicationRequest;
import dev.houda.jobtracker.api.ApplicationDtos.ApplicationResponse;
import dev.houda.jobtracker.api.ApplicationDtos.StatusRequest;
import dev.houda.jobtracker.domain.ApplicationStatus;
import dev.houda.jobtracker.domain.JobApplication;
import dev.houda.jobtracker.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications")
public class JobApplicationController {

    private static final int MAX_PAGE_SIZE = 100;

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create an application (starts in WISHLIST)")
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequest request) {
        JobApplication created = service.create(
                request.company(), request.position(), request.jobUrl(), request.notes());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(ApplicationResponse.of(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one application including its status history")
    public ApplicationResponse get(@PathVariable Long id) {
        return ApplicationResponse.of(service.get(id));
    }

    @GetMapping
    @Operation(summary = "List applications, optionally filtered by status and company name")
    public Page<ApplicationResponse> list(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String company,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return service.search(status, company, pageable).map(ApplicationResponse::summary);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update the descriptive fields (status is changed separately)")
    public ApplicationResponse update(@PathVariable Long id, @Valid @RequestBody ApplicationRequest request) {
        return ApplicationResponse.of(service.update(
                id, request.company(), request.position(), request.jobUrl(), request.notes()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Move to a new status; invalid transitions return 409")
    public ApplicationResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return ApplicationResponse.of(service.changeStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
