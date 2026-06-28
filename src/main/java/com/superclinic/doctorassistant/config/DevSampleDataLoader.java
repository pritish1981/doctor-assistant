package com.superclinic.doctorassistant.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Profile("dev")
@Component
@Order(10)
@RequiredArgsConstructor
public class DevSampleDataLoader implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        Integer doctorCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM doctors", Integer.class);
        if (doctorCount != null && doctorCount > 0) {
            log.debug("Dev sample data already present ({} doctors), skipping seed", doctorCount);
            return;
        }

        log.info("No doctors in database — loading db/sample/sample_data.sql");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/sample/sample_data.sql"));
        populator.setContinueOnError(false);
        try {
            DatabasePopulatorUtils.execute(populator, dataSource);
            log.info("Development sample data loaded");
        } catch (Exception ex) {
            log.warn("Full sample_data.sql failed, loading minimal dev_bootstrap.sql", ex);
            ResourceDatabasePopulator fallback = new ResourceDatabasePopulator();
            fallback.addScript(new ClassPathResource("db/sample/dev_bootstrap.sql"));
            DatabasePopulatorUtils.execute(fallback, dataSource);
            log.info("Development bootstrap patient loaded (doctors still missing — run sample_data.sql manually)");
        }
    }
}
