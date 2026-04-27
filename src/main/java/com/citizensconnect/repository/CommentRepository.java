package com.citizensconnect.repository;

import com.citizensconnect.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByIssueId(Long issueId);
    void deleteByIssueId(Long issueId);
    void deleteByUserId(Long userId);
}
