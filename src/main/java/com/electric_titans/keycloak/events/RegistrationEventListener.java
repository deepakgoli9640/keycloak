package com.electric_titans.keycloak.events;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;

import org.jboss.logging.Logger;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransaction;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class RegistrationEventListener implements EventListenerProvider {

    private static final Logger LOG =
            Logger.getLogger(RegistrationEventListener.class);

    private static final String DEFAULT_TOPIC = "IDENTITY-REGISTERED";
    private static final String TYPE_ID_HEADER = "__TypeId__";
    private static final String DEFAULT_BOOTSTRAP_SERVERS = "10.160.0.2:9092";

    private final KeycloakSession session;
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;
    private final String topic;

    public RegistrationEventListener(
            KeycloakSession session,
            String bootstrapServers,
            String topic) {

        this.session = session;

        this.topic = topic == null || topic.isBlank()
                ? DEFAULT_TOPIC
                : topic;

        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Properties producerProperties = new Properties();

        producerProperties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers == null || bootstrapServers.isBlank()
                        ? DEFAULT_BOOTSTRAP_SERVERS
                        : bootstrapServers
        );

        producerProperties.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProperties.put(ProducerConfig.RETRIES_CONFIG, 3);
        producerProperties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        producerProperties.put(
                ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                5
        );

        producerProperties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName()
        );

        producerProperties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName()
        );

        this.producer = new KafkaProducer<>(producerProperties);

        LOG.infov(
                "Keycloak registration listener initialized. Topic={0}, BootstrapServers={1}",
                this.topic,
                bootstrapServers == null || bootstrapServers.isBlank()
                        ? DEFAULT_BOOTSTRAP_SERVERS
                        : bootstrapServers
        );
    }

    @Override
    public void onEvent(Event event) {

        if (event == null || event.getType() != EventType.REGISTER) {
            return;
        }

        String userId = event.getUserId();

        if (userId == null || userId.isBlank()) {
            LOG.warnv(
                    "Skipping registration event without userId. EventId={0}",
                    event.getId()
            );
            return;
        }

        Map<String, String> details = event.getDetails() == null
                ? Collections.emptyMap()
                : event.getDetails();

        IdentityRegisteredEvent payload = IdentityRegisteredEvent.builder()
                .eventId(event.getId())
                .eventType("IDENTITY-REGISTERED")
                .realmId(event.getRealmId())
                .userId(userId)
                .email(safe(details.get("email")))
                .firstName(safe(details.get("firstName")))
                .lastName(safe(details.get("lastName")))
                .source("keycloak")
                .eventVersion(1)
                .timestamp(Instant.now())
                .build();

        try {
            String message = objectMapper.writeValueAsString(payload);
            publishAfterTransactionCommit(userId, message);

        } catch (Exception ex) {
            LOG.errorv(
                    ex,
                    "Failed to serialize registration event. UserId={0}",
                    userId
            );
        }
    }

    private void publishAfterTransactionCommit(
            String userId,
            String payload) {

        if (session == null || session.getTransactionManager() == null) {
            publishMessage(userId, payload);
            return;
        }

        session.getTransactionManager().enlistAfterCompletion(
                new KeycloakTransaction() {

                    @Override
                    public void begin() {
                    }

                    @Override
                    public void commit() {
                        publishMessage(userId, payload);
                    }

                    @Override
                    public void rollback() {
                        LOG.warnv(
                                "Keycloak transaction rolled back. Event not published. UserId={0}",
                                userId
                        );
                    }

                    @Override
                    public void setRollbackOnly() {
                    }

                    @Override
                    public boolean getRollbackOnly() {
                        return false;
                    }

                    @Override
                    public boolean isActive() {
                        return false;
                    }
                }
        );
    }

    private void publishMessage(String userId, String payload) {

        try {
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, userId, payload);

            // Add the Spring Kafka type header explicitly.
            String typeId = IdentityRegisteredEvent.class.getName();

            record.headers().remove(TYPE_ID_HEADER);
            record.headers().add(
                    new RecordHeader(
                            TYPE_ID_HEADER,
                            typeId.getBytes(StandardCharsets.UTF_8)
                    )
            );

            // Verify the header exists before sending.
            Header header = record.headers().lastHeader(TYPE_ID_HEADER);

            if (header == null) {
                LOG.error("Type header is missing before Kafka send.");
                return;
            }

            String headerValue =
                    new String(header.value(), StandardCharsets.UTF_8);

            LOG.infov(
                    "Sending registration event. Topic={0}, UserId={1}, Header={2}, TypeId={3}",
                    topic,
                    userId,
                    header.key(),
                    headerValue
            );

            producer.send(record, (metadata, exception) -> {

                if (exception != null) {
                    LOG.errorv(
                            exception,
                            "Kafka publish failed. Topic={0}, UserId={1}",
                            topic,
                            userId
                    );
                    return;
                }

                LOG.infov(
                        "Registration event published. Topic={0}, Partition={1}, Offset={2}, TypeId={3}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        typeId
                );
            });

        } catch (Exception ex) {
            LOG.errorv(
                    ex,
                    "Failed to enqueue registration event. Topic={0}, UserId={1}",
                    topic,
                    userId
            );
        }
    }

    private String safe(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .orElse(null);
    }

    @Override
    public void onEvent(
            org.keycloak.events.admin.AdminEvent event,
            boolean includeRepresentation) {
        // Intentionally ignored.
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.close();
            LOG.info("Kafka producer closed.");
        }
    }
}
