package com.example.ia.spring.repository;

import com.example.ia.spring.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository
        extends JpaRepository<ConversationEntity, Long> {
}
