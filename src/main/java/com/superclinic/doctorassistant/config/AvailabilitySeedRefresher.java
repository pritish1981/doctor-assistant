package com.superclinic.doctorassistant.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Keeps demo availability fresh by regenerating a full week of open slots for every
 * active doctor on each application boot.
 *
 * <p>Seed data uses dates relative to when it was first inserted, so when the database
 * volume persists across days the slots drift into the past and the agent can no longer
 * find anything bookable. Rather than relying on a one-off manual SQL reset, this runner:
 *
 * <ol>
 *   <li>removes stale {@code OPEN} slots whose start is before today (booked slots are
 *       never touched, so existing appointments are preserved); and</li>
 *   <li>tops up a grid of half-hour {@code OPEN} slots across the next
 *       {@code days-ahead} days for each active doctor, skipping Sundays.</li>
 * </ol>
 *
 * <p>The top-up is idempotent: a slot is only inserted when no slot already exists for
 * that doctor at that exact start time, so repeated boots never create duplicates and the
 * table size stays bounded to roughly one week of grid slots per doctor.
 */
@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "doctor-assistant.availability",
        name = "reanchor-stale-slots-on-startup",
        havingValue = "true",
        matchIfMissing = true)
public class AvailabilitySeedRefresher implements ApplicationRunner {

    /** Half-hour slot start times generated for each working day. */
    private static final String SLOT_TIMES = """
            (TIME '09:00'),(TIME '09:30'),(TIME '10:00'),(TIME '10:30'),
            (TIME '11:00'),(TIME '11:30'),(TIME '14:00'),(TIME '14:30'),
            (TIME '15:00'),(TIME '15:30'),(TIME '16:00'),(TIME '16:30')
            """;

    private final JdbcTemplate jdbcTemplate;

    @Value("${doctor-assistant.availability.days-ahead:7}")
    private int daysAhead;

    @Override
    public void run(ApplicationArguments args) {
        Integer doctorCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM doctors WHERE active = true", Integer.class);
        if (doctorCount == null || doctorCount == 0) {
            log.debug("No active doctors found, skipping availability refresh");
            return;
        }

        int removed = jdbcTemplate.update(
                "DELETE FROM availability WHERE status = 'OPEN' AND slot_start < CURRENT_DATE");

        int horizon = Math.max(1, daysAhead);
        int created = jdbcTemplate.update(
                """
                INSERT INTO availability (id, doctor_id, slot_start, slot_end, status, created_by, updated_by)
                SELECT
                    gen_random_uuid(),
                    d.id,
                    s.slot_ts AT TIME ZONE 'UTC',
                    (s.slot_ts + INTERVAL '30 minutes') AT TIME ZONE 'UTC',
                    'OPEN',
                    'slot-refresher',
                    'slot-refresher'
                FROM doctors d
                CROSS JOIN generate_series(0, ? - 1) AS day_offset
                CROSS JOIN (VALUES %s) AS t(slot_time)
                CROSS JOIN LATERAL (
                    SELECT ((CURRENT_DATE + day_offset) + t.slot_time) AS slot_ts
                ) AS s
                WHERE d.active = true
                  AND EXTRACT(DOW FROM (CURRENT_DATE + day_offset)) <> 0
                  AND NOT EXISTS (
                        SELECT 1 FROM availability a
                        WHERE a.doctor_id = d.id
                          AND a.slot_start = s.slot_ts AT TIME ZONE 'UTC'
                  )
                """.formatted(SLOT_TIMES),
                horizon);

        log.info("Availability refresh complete: removed {} stale open slot(s), added {} new open slot(s) "
                + "across the next {} day(s) for {} active doctor(s)", removed, created, horizon, doctorCount);
    }
}
