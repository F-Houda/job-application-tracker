package dev.houda.jobtracker;

import dev.houda.jobtracker.domain.ApplicationStatus;
import dev.houda.jobtracker.domain.IllegalStatusTransitionException;
import dev.houda.jobtracker.domain.JobApplication;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobApplicationTest {

    private JobApplication newApplication() {
        return new JobApplication("Acme", "Backend Developer", null, null);
    }

    @Test
    void startsInWishlistWithOneHistoryEntry() {
        JobApplication app = newApplication();

        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.WISHLIST);
        assertThat(app.getHistory()).hasSize(1);
        assertThat(app.getHistory().get(0).getFromStatus()).isNull();
    }

    @Test
    void followsTheHappyPathAndRecordsEveryStep() {
        JobApplication app = newApplication();

        app.moveTo(ApplicationStatus.APPLIED);
        app.moveTo(ApplicationStatus.INTERVIEW);
        app.moveTo(ApplicationStatus.OFFER);

        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.OFFER);
        assertThat(app.getHistory()).hasSize(4);
    }

    @Test
    void rejectsSkippingAStage() {
        JobApplication app = newApplication();

        assertThatThrownBy(() -> app.moveTo(ApplicationStatus.OFFER))
                .isInstanceOf(IllegalStatusTransitionException.class)
                .hasMessageContaining("WISHLIST")
                .hasMessageContaining("OFFER");
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.WISHLIST);
    }

    @Test
    void terminalStatusesCannotBeLeft() {
        JobApplication app = newApplication();
        app.moveTo(ApplicationStatus.WITHDRAWN);

        assertThatThrownBy(() -> app.moveTo(ApplicationStatus.APPLIED))
                .isInstanceOf(IllegalStatusTransitionException.class);
    }
}
