package com.citizensconnect.repository;

import com.citizensconnect.models.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    Page<Issue> findAll(Pageable pageable);
    List<Issue> findByUserId(Long userId);
    Page<Issue> findByUserId(Long userId, Pageable pageable);
    Long countByStatus(com.citizensconnect.models.IssueStatus status);
}
