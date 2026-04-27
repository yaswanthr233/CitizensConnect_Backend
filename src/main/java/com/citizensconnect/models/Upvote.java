package com.citizensconnect.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "upvotes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "issue_id"})
    })
public class Upvote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
    
    public Upvote(User user, Issue issue) {
        this.user = user;
        this.issue = issue;
    }
}
