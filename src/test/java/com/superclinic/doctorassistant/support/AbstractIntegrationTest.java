package com.superclinic.doctorassistant.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Import(IntegrationTestConfiguration.class)
@Sql(
        scripts = "/db/test-data.sql",
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
public abstract class AbstractIntegrationTest {

    protected static final UUID PATIENT_ALICE =
            UUID.fromString("b2000000-0000-4000-8000-000000000001");
    protected static final UUID DOCTOR_SHARMA =
            UUID.fromString("c3000000-0000-4000-8000-000000000001");
    protected static final UUID DOCTOR_NAIR =
            UUID.fromString("c3000000-0000-4000-8000-000000000002");
    protected static final UUID SLOT_SHARMA_TOMORROW =
            UUID.fromString("d4000000-0000-4000-8000-000000000010");
    protected static final UUID SLOT_NAIR_DAY2 =
            UUID.fromString("d4000000-0000-4000-8000-000000000011");

    protected LocalDate tomorrow() {
        return LocalDate.now(ZoneOffset.UTC).plusDays(1);
    }

    protected LocalDate inDays(int days) {
        return LocalDate.now(ZoneOffset.UTC).plusDays(days);
    }

    protected String isoDate(LocalDate date) {
        return date.toString();
    }
}
