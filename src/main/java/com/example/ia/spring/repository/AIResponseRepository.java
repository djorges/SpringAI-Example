package com.example.ia.spring.repository;

import com.example.ia.spring.entity.AIResponseEntity;
import com.example.ia.spring.entity.enums.AIProvider;
import com.example.ia.spring.entity.enums.ResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIResponseRepository
        extends JpaRepository<AIResponseEntity, Long> {

    List<AIResponseEntity> findByMessageId(Long messageId);

    List<AIResponseEntity> findByProvider(AIProvider provider);

    List<AIResponseEntity> findByStatus(ResponseStatus status);
}
