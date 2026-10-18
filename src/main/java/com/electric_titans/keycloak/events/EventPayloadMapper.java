package com.electric_titans.keycloak.events;

import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Produces a deliberately small payload and avoids Keycloak admin-event representations. */
final class EventPayloadMapper {

    private EventPayloadMapper() {
    }

    static String userEvent(Event event, boolean includeDetails) {
        Map<String, Object> payload = basePayload("USER_EVENT", event.getId(), event.getTime(), event.getRealmId(), event.getRealmName());
        payload.put("eventType", stringValue(event.getType()));
        putIfPresent(payload, "clientId", event.getClientId());
        putIfPresent(payload, "userId", event.getUserId());
        putIfPresent(payload, "sessionId", event.getSessionId());
        putIfPresent(payload, "ipAddress", event.getIpAddress());
        putIfPresent(payload, "error", event.getError());
        if (includeDetails && event.getDetails() != null && !event.getDetails().isEmpty()) {
            payload.put("details", new LinkedHashMap<>(event.getDetails()));
        }
        return JsonWriter.write(payload);
    }

    static String adminEvent(AdminEvent event) {
        Map<String, Object> payload = basePayload("ADMIN_EVENT", event.getId(), event.getTime(), event.getRealmId(), event.getRealmName());
        payload.put("operationType", stringValue(event.getOperationType()));
        payload.put("resourceType", stringValue(event.getResourceType()));
        putIfPresent(payload, "resourcePath", event.getResourcePath());
        putIfPresent(payload, "resourceId", event.getResourceId());
        putIfPresent(payload, "error", event.getError());

        AuthDetails auth = event.getAuthDetails();
        if (auth != null) {
            putIfPresent(payload, "authClientId", auth.getClientId());
            putIfPresent(payload, "authUserId", auth.getUserId());
            putIfPresent(payload, "authIpAddress", auth.getIpAddress());
        }
        return JsonWriter.write(payload);
    }

    private static Map<String, Object> basePayload(String kind, String id, long timestamp, String realmId, String realmName) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("kind", kind);
        putIfPresent(payload, "id", id);
        payload.put("timestamp", Instant.ofEpochMilli(timestamp).toString());
        putIfPresent(payload, "realmId", realmId);
        putIfPresent(payload, "realmName", realmName);
        return payload;
    }

    private static void putIfPresent(Map<String, Object> payload, String key, String value) {
        if (value != null && !value.isBlank()) {
            payload.put(key, value);
        }
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
