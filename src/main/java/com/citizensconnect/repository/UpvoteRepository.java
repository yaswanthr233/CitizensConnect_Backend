package com.citizensconnect.repository;

import com.citizensconnect.models.Upvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UpvoteRepository extends JpaRepository<Upvote, Long> {
    Optional<Upvote> findByUserIdAndIssueId(Long userId, Long issueId);
    Long countByIssueId(Long issueId);
    void deleteByIssueId(Long issueId);
    void deleteByUserId(Long userId);
}
