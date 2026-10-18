package dev.houda.jobtracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests against a real PostgreSQL (Testcontainers), so Flyway migrations,
 * JPA mappings and the HTTP layer are all exercised together.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class JobApplicationApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mvc;

    private long createApplication(String company) throws Exception {
        MvcResult result = mvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company":"%s","position":"Backend Developer"}
                                """.formatted(company)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(result.getResponse().getContentAsString());
        if (!m.find()) {
            throw new IllegalStateException("No id in response");
        }
        return Long.parseLong(m.group(1));
    }

    @Test
    void createsAndReadsAnApplication() throws Exception {
        long id = createApplication("Acme GmbH");

        mvc.perform(get("/api/applications/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Acme GmbH"))
                .andExpect(jsonPath("$.status").value("WISHLIST"))
                .andExpect(jsonPath("$.history", hasSize(1)));
    }

    @Test
    void walksThroughStatusesAndKeepsHistory() throws Exception {
        long id = createApplication("Flow AG");

        for (String next : new String[]{"APPLIED", "INTERVIEW"}) {
            mvc.perform(patch("/api/applications/{id}/status", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + next + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(next));
        }

        mvc.perform(get("/api/applications/{id}", id))
                .andExpect(jsonPath("$.history", hasSize(3)));
    }

    @Test
    void invalidTransitionReturns409() throws Exception {
        long id = createApplication("Skip KG");

        mvc.perform(patch("/api/applications/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFFER\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void validationErrorsReturn400WithFieldDetails() throws Exception {
        mvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"company\":\"\",\"position\":\"Dev\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.company").exists());
    }

    @Test
    void filtersByStatusAndCompany() throws Exception {
        long id = createApplication("Filterable Corp");
        mvc.perform(patch("/api/applications/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPLIED\"}"));

        mvc.perform(get("/api/applications")
                        .param("status", "APPLIED")
                        .param("company", "filterable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].company").value("Filterable Corp"));
    }

    @Test
    void deleteRemovesTheApplication() throws Exception {
        long id = createApplication("Gone Inc");

        mvc.perform(delete("/api/applications/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/applications/{id}", id)).andExpect(status().isNotFound());
    }
}
