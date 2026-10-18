package com.electric_titans.keycloak.events;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

/**
 * Converts Keycloak events to a transport-neutral payload and dispatches them after Keycloak accepts the event.
 */
final class PlatformWebhookEventListenerProvider implements EventListenerProvider {

    private static final Logger LOG = Logger.getLogger(PlatformWebhookEventListenerProvider.class);

    private final WebhookSettings settings;
    private final EventWebhookDispatcher dispatcher;

    PlatformWebhookEventListenerProvider(WebhookSettings settings, EventWebhookDispatcher dispatcher) {
        this.settings = settings;
        this.dispatcher = dispatcher;
    }

    @Override
    public void onEvent(Event event) {
        dispatch(EventPayloadMapper.userEvent(event, settings.includeEventDetails()));
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Admin representations can contain credentials or other sensitive user data. Never forward them.
        dispatch(EventPayloadMapper.adminEvent(event));
    }

    private void dispatch(String payload) {
        try {
            dispatcher.dispatch(payload);
        } catch (RuntimeException exception) {
            // Event listeners must not make authentication or administration fail when the audit sink is unavailable.
            LOG.warn("Unable to dispatch Keycloak event webhook", exception);
        }
    }

    @Override
    public void close() {
        // The dispatcher is owned by the factory and can be shared between provider instances.
    }
}
