package com.electric_titans.keycloak.events;

import org.jboss.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.HexFormat;

/** Sends event payloads asynchronously so a slow receiver cannot delay Keycloak's request processing. */
final class HttpEventWebhookDispatcher implements EventWebhookDispatcher {

    private static final Logger LOG = Logger.getLogger(HttpEventWebhookDispatcher.class);
    private static final String SIGNATURE_HEADER = "X-Keycloak-Event-Signature";

    private final WebhookSettings settings;
    private final HttpClient client;

    HttpEventWebhookDispatcher(WebhookSettings settings) {
        this(settings, HttpClient.newBuilder().connectTimeout(settings.connectTimeout()).build());
    }

    HttpEventWebhookDispatcher(WebhookSettings settings, HttpClient client) {
        this.settings = settings;
        this.client = client;
    }

    @Override
    public void dispatch(String payload) {
        HttpRequest.Builder request = HttpRequest.newBuilder(settings.endpoint())
                .timeout(settings.requestTimeout())
                .header("Content-Type", "application/json")
                .header("X-Keycloak-Event-Listener", PlatformWebhookEventListenerProviderFactory.ID)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));

        if (settings.hasSigningSecret()) {
            request.header(SIGNATURE_HEADER, sign(payload, settings.signingSecret()));
        }

        client.sendAsync(request.build(), HttpResponse.BodyHandlers.discarding())
                .whenComplete((response, exception) -> {
                    if (exception != null) {
                        LOG.warnv("Keycloak event webhook delivery failed: {0}", exception.getMessage());
                    } else if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        LOG.warnv("Keycloak event webhook returned HTTP {0}", response.statusCode());
                    }
                });
    }

    static String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return "sha256=" + HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to calculate webhook signature", exception);
        }
    }
}
