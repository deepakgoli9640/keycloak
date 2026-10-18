package com.electric_titans.keycloak.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpEventWebhookDispatcherTest {

    @Test
    void signsPayloadWithHmacSha256() {
        assertEquals(
                "sha256=5c9901c0df2b283128e041d108af7c742efc417456618c634cda79be506ded7e",
                HttpEventWebhookDispatcher.sign("{\"event\":\"REGISTER\"}", "shared-secret"));
    }
}
