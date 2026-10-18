package com.electric_titans.keycloak.events;

import org.junit.jupiter.api.Test;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistrationEventListenerTest {

    @Test
    void testRegisterEventPublishesWithoutThrowing() throws Exception {
        KeycloakSession session = mock(KeycloakSession.class);
        KeycloakTransactionManager txManager = mock(KeycloakTransactionManager.class);
        when(session.getTransactionManager()).thenReturn(txManager);

        Event event = mock(Event.class);
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(event.getUserId()).thenReturn("keycloak-user-123");
        when(event.getRealmId()).thenReturn("financial-health");
        when(event.getDetails()).thenReturn(Map.of(
                "email", "jane@example.com",
                "firstName", "Jane",
                "lastName", "Doe"
        ));
        when(event.getId()).thenReturn("event-123");

        RegistrationEventListener listener = new RegistrationEventListener(session, "localhost:9092", "IDENTITY-REGISTERED");
        assertDoesNotThrow(() -> listener.onEvent(event));
        listener.close();
    }

    @Test
    void testLoginEventIsIgnored() {
        KeycloakSession session = mock(KeycloakSession.class);
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(EventType.LOGIN);

        RegistrationEventListener listener = new RegistrationEventListener(session, "localhost:9092", "IDENTITY-REGISTERED");
        assertDoesNotThrow(() -> listener.onEvent(event));
        listener.close();
    }
}
