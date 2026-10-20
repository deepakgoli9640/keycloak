package com.electric_titans.keycloak.events;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class RegistrationEventListenerFactory implements EventListenerProviderFactory {

    public static final String ID = "identity-registration-kafka";
    private static final Logger LOG = Logger.getLogger(RegistrationEventListenerFactory.class);

    private String bootstrapServers;
    private String topic;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new RegistrationEventListener(session, bootstrapServers, topic);
    }

    @Override
    public void init(Config.Scope config) {
        bootstrapServers = config.get("bootstrap-servers", System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "8.231.71.198:19092"));
        topic = config.get("topic", System.getenv().getOrDefault("IDENTITY_REGISTERED_TOPIC", "IDENTITY-REGISTERED"));
        LOG.infov("Keycloak registration listener enabled. bootstrap-servers={0}, topic={1}", bootstrapServers, topic);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return ID;
    }
}
