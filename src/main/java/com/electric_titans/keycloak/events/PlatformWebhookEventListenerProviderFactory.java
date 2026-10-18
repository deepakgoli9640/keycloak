package com.electric_titans.keycloak.events;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Registers the {@code platform-webhook} event-listener provider with Keycloak.
 */
public final class PlatformWebhookEventListenerProviderFactory implements EventListenerProviderFactory {

    public static final String ID = "platform-webhook";
    private static final Logger LOG = Logger.getLogger(PlatformWebhookEventListenerProviderFactory.class);

    private volatile WebhookSettings settings = WebhookSettings.disabled();
    private volatile EventWebhookDispatcher dispatcher = EventWebhookDispatcher.disabled();

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new PlatformWebhookEventListenerProvider(settings, dispatcher);
    }

    @Override
    public void init(Config.Scope config) {
        settings = WebhookSettings.from(config);
        dispatcher = settings.enabled()
                ? new HttpEventWebhookDispatcher(settings)
                : EventWebhookDispatcher.disabled();

        if (settings.enabled()) {
            LOG.infov("Keycloak event webhook is enabled for {0}", settings.endpoint());
        } else {
            LOG.warn("Keycloak event webhook is disabled because webhook-url is not configured or is invalid");
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // The listener is stateless and does not require a Keycloak session at startup.
    }

    @Override
    public void close() {
        // java.net.http.HttpClient does not need explicit shutdown.
    }

    @Override
    public String getId() {
        return ID;
    }
}
