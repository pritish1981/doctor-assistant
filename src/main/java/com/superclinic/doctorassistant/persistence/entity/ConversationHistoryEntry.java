package com.superclinic.doctorassistant.persistence.entity;

import com.superclinic.doctorassistant.persistence.entity.enums.ConversationMessageRole;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "conversation_history")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = "session")
public class ConversationHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationMessageRole role;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tool_calls", columnDefinition = "jsonb")
    private Object toolCalls;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rag_sources", columnDefinition = "jsonb")
    private Object ragSources;

    @Min(0)
    @Column(name = "token_count")
    private Integer tokenCount;

    @NotNull
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.Instant createdAt = java.time.Instant.now();

    @NotBlank
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy = "system";

    public static ConversationHistoryEntry create(
            ConversationSession session,
            ConversationMessageRole role,
            String content,
            int sequenceNumber) {
        ConversationHistoryEntry entry = new ConversationHistoryEntry();
        entry.setSession(session);
        entry.setRole(role);
        entry.setContent(content);
        entry.setSequenceNumber(sequenceNumber);
        entry.setCreatedAt(java.time.Instant.now());
        return entry;
    }
}
