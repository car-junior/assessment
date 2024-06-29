package com.senior.assessment.domain.config.integration;

import com.senior.assessment.config.DatabaseCleanupService;
import com.senior.assessment.domain.config.AssessmentConfigTest;
import com.senior.assessment.domain.config.PostgreSQLContainerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class IntegrationTestBase extends PostgreSQLContainerConfig {
    private static final Logger log = LogManager.getLogger(IntegrationTestBase.class);
    @Autowired
    private DatabaseCleanupService databaseCleanupService;

    @BeforeAll
    public void cleanUp() {
        log.info("Cleaning up database before tests.");
        databaseCleanupService.truncate();
        log.info("Completed database clean up.");
    }
}
