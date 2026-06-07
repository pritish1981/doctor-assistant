package com.superclinic.doctorassistant.persistence.entity;

import com.superclinic.doctorassistant.persistence.entity.enums.ConversationSessionStatus;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "conversation_sessions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"patient", "messages"})
public class ConversationSession extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationSessionStatus status = ConversationSessionStatus.ACTIVE;

    @Column(length = 255)
    private String title;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> context = new HashMap<>();

    @NotNull
    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @NotNull
    @Column(name = "last_active_at", nullable = false)
    private Instant lastActiveAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;

    @OneToMany(mappedBy = "session")
    private List<ConversationHistoryEntry> messages = new ArrayList<>();

    public static ConversationSession create(Patient patient, String title) {
        ConversationSession session = new ConversationSession();
        session.setPatient(patient);
        session.setTitle(title);
        session.setStatus(ConversationSessionStatus.ACTIVE);
        session.setStartedAt(Instant.now());
        session.setLastActiveAt(Instant.now());
        return session;
    }

    public void touch() {
        this.lastActiveAt = Instant.now();
    }

    public void close() {
        this.status = ConversationSessionStatus.CLOSED;
        this.endedAt = Instant.now();
        this.lastActiveAt = Instant.now();
    }
}
