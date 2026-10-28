package com.electric_titans.keycloak.events;


import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityRegisteredEvent {
    private String eventId;
    private String eventType;
    private String realmId;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String source;
    private int eventVersion;
    private Instant timestamp;
}
