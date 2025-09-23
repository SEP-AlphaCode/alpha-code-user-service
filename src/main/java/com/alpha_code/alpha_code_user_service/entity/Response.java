package com.alpha_code.alpha_code_user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "responses")
public class Response {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "responder_id", nullable = false, columnDefinition = "uuid")
    private UUID responderId;

    @Column(name = "request_id", nullable = false, columnDefinition = "uuid")
    private UUID requestId;

    @Size(max = 255)
    @NotNull
    @Column(name = "response_content", nullable = false)
    private String responseContent;

    @NotNull
    @Column(name = "status", nullable = false)
    private Integer status;

    @NotNull
    @Column(name = "created_dated", nullable = false)
    private LocalDateTime createdDated;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;


    //Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id", nullable = false, insertable = false, updatable = false)
    private Account responder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false, insertable = false, updatable = false)
    private UserRequest request;
}