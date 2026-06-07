package com.superclinic.doctorassistant.persistence.entity;

import com.superclinic.doctorassistant.persistence.entity.enums.AvailabilityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "availability",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_availability_doctor_slot",
                columnNames = {"doctor_id", "slot_start"}
        )
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"doctor", "appointment"})
public class Availability extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @Column(name = "slot_start", nullable = false)
    private Instant slotStart;

    @NotNull
    @Column(name = "slot_end", nullable = false)
    private Instant slotEnd;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AvailabilityStatus status = AvailabilityStatus.OPEN;

    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;

    @OneToOne(mappedBy = "availability", fetch = FetchType.LAZY)
    private Appointment appointment;
}
