package com.example.ia.spring.repository;

import com.example.ia.spring.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository
        extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findByConversationIdOrderByCreatedAtAsc(
            Long conversationId
    );
}
