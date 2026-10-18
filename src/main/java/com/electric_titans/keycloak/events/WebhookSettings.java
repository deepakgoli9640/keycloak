package com.electric_titans.keycloak.events;

import org.jboss.logging.Logger;
import org.keycloak.Config;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Locale;

/** Immutable configuration read once when Keycloak initializes this provider. */
record WebhookSettings(
        URI endpoint,
        String signingSecret,
        Duration connectTimeout,
        Duration requestTimeout,
        boolean includeEventDetails) {

    private static final Logger LOG = Logger.getLogger(WebhookSettings.class);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(5);

    static WebhookSettings from(Config.Scope config) {
        URI endpoint = parseEndpoint(config.get("webhook-url"));
        return new WebhookSettings(
                endpoint,
                config.get("webhook-secret", ""),
                durationFromMilliseconds(config.getLong("connect-timeout-ms", DEFAULT_CONNECT_TIMEOUT.toMillis()), DEFAULT_CONNECT_TIMEOUT),
                durationFromMilliseconds(config.getLong("request-timeout-ms", DEFAULT_REQUEST_TIMEOUT.toMillis()), DEFAULT_REQUEST_TIMEOUT),
                config.getBoolean("include-event-details", false));
    }

    static WebhookSettings disabled() {
        return new WebhookSettings(null, "", DEFAULT_CONNECT_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, false);
    }

    boolean enabled() {
        return endpoint != null;
    }

    boolean hasSigningSecret() {
        return signingSecret != null && !signingSecret.isBlank();
    }

    private static URI parseEndpoint(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            URI endpoint = new URI(value.trim());
            String scheme = endpoint.getScheme() == null ? "" : endpoint.getScheme().toLowerCase(Locale.ROOT);
            if (("http".equals(scheme) || "https".equals(scheme)) && endpoint.getHost() != null) {
                return endpoint;
            }
        } catch (URISyntaxException ignored) {
            // A clear warning below avoids preventing Keycloak from starting because of an optional integration.
        }

        LOG.warnv("Ignoring invalid Keycloak event webhook URL: {0}", value);
        return null;
    }

    private static Duration durationFromMilliseconds(Long value, Duration fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return Duration.ofMillis(value);
    }
}
