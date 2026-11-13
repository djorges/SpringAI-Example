@Repository
public interface AIResponseRepository
        extends JpaRepository<AIResponseEntity, Long> {

    List<AIResponseEntity> findByMessageId(Long messageId);

    List<AIResponseEntity> findByProvider(AIProvider provider);

    List<AIResponseEntity> findByStatus(ResponseStatus status);
}
