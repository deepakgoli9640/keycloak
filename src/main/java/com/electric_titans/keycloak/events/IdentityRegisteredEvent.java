package com.electric_titans.keycloak.events;


import java.time.Instant;

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder realmId(String realmId) {
            this.realmId = realmId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder eventVersion(int eventVersion) {
            this.eventVersion = eventVersion;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public IdentityRegisteredEvent build() {
            IdentityRegisteredEvent event = new IdentityRegisteredEvent();
            event.eventId = this.eventId;
            event.eventType = this.eventType;
            event.realmId = this.realmId;
            event.userId = this.userId;
            event.email = this.email;
            event.firstName = this.firstName;
            event.lastName = this.lastName;
            event.source = this.source;
            event.eventVersion = this.eventVersion;
            event.timestamp = this.timestamp;
            return event;
        }
    }

    public IdentityRegisteredEvent() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(int eventVersion) {
        this.eventVersion = eventVersion;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
