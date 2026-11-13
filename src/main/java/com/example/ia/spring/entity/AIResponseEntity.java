@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "ai_responses",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"message_id", "provider"}
    )
)
public class AIResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private MessageEntity message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AIProvider provider;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResponseStatus status;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
