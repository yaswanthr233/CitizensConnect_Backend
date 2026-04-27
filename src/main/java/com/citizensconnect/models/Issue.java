package com.citizensconnect.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    private String priority;

    @NotBlank
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private IssueStatus status = IssueStatus.PENDING;

    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private Integer feedbackRating;

    private LocalDateTime feedbackAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
