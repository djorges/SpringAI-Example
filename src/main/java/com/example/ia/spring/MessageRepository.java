@Repository
public interface MessageRepository
        extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findByConversationIdOrderByCreatedAtAsc(
            Long conversationId
    );
}
