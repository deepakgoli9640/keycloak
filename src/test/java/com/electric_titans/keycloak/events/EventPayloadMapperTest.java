package com.electric_titans.keycloak.events;

import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventPayloadMapperTest {

    @Test
    void excludesEventDetailsUnlessExplicitlyEnabled() {
        Event event = registrationEvent();
        event.setDetails(Map.of("email", "ada@example.test", "username", "ada"));

        String payload = EventPayloadMapper.userEvent(event, false);

        assertTrue(payload.contains("\"eventType\":\"REGISTER\""));
        assertTrue(payload.contains("\"userId\":\"user-1\""));
        assertFalse(payload.contains("ada@example.test"));
        assertFalse(payload.contains("\"details\""));
    }

    @Test
    void includesEventDetailsOnlyWhenConfigured() {
        Event event = registrationEvent();
        event.setDetails(Map.of("username", "ada"));

        String payload = EventPayloadMapper.userEvent(event, true);

        assertTrue(payload.contains("\"details\":{\"username\":\"ada\"}"));
    }

    private Event registrationEvent() {
        Event event = new Event();
        event.setId("event-1");
        event.setTime(0L);
        event.setType(EventType.REGISTER);
        event.setRealmId("financial-health");
        event.setRealmName("financial-health");
        event.setUserId("user-1");
        return event;
    }
}
