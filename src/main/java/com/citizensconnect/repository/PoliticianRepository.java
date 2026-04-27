package com.citizensconnect.repository;

import com.citizensconnect.models.Politician;
import com.citizensconnect.models.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoliticianRepository extends JpaRepository<Politician, Long> {
    List<Politician> findByStatus(UserStatus status);
}
