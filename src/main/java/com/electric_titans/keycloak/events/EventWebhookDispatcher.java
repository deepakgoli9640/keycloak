package com.electric_titans.keycloak.events;

@FunctionalInterface
interface EventWebhookDispatcher {

    void dispatch(String payload);

    static EventWebhookDispatcher disabled() {
        return payload -> {
            // Intentionally empty: an endpoint is optional so local Keycloak can start without one.
        };
    }
}
