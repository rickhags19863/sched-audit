package com.schedaudit.alert;

import com.schedaudit.model.JobExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlertServiceTest {

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService();
    }

    @Test
    void alertMissedRun_withPreviousExecution_storesAlert() {
        JobExecution lastRun = mock(JobExecution.class);
        when(lastRun.getStartTime()).thenReturn(LocalDateTime.of(2024, 6, 1, 10, 0));

        alertService.alertMissedRun("backup-job", lastRun, "0 10 * * *");

        List<Alert> alerts = alertService.getIssuedAlerts();
        assertThat(alerts).hasSize(1);
        Alert alert = alerts.get(0);
        assertThat(alert.getJobName()).isEqualTo("backup-job");
        assertThat(alert.getCronExpression()).isEqualTo("0 10 * * *");
        assertThat(alert.getMessage()).contains("backup-job");
        assertThat(alert.getMessage()).contains("last run at");
        assertThat(alert.getLastKnownExecution()).isSameAs(lastRun);
        assertThat(alert.getIssuedAt()).isNotNull();
    }

    @Test
    void alertMissedRun_withNoExecution_indicatesNoPreviousRun() {
        alertService.alertMissedRun("report-job", null, "0 8 * * 1");

        List<Alert> alerts = alertService.getIssuedAlerts();
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getMessage()).contains("no previous run recorded");
        assertThat(alerts.get(0).getLastKnownExecution()).isNull();
    }

    @Test
    void multipleAlerts_areAllStored() {
        alertService.alertMissedRun("job-a", null, "* * * * *");
        alertService.alertMissedRun("job-b", null, "0 0 * * *");

        assertThat(alertService.getIssuedAlerts()).hasSize(2);
    }

    @Test
    void clearAlerts_removesAllStoredAlerts() {
        alertService.alertMissedRun("job-a", null, "* * * * *");
        alertService.clearAlerts();

        assertThat(alertService.getIssuedAlerts()).isEmpty();
    }

    @Test
    void getIssuedAlerts_returnsUnmodifiableList() {
        List<Alert> alerts = alertService.getIssuedAlerts();
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> alerts.add(mock(Alert.class))
        );
    }
}
