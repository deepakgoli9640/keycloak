# Keycloak registration listener

This module adds the `identity-registration-kafka` Keycloak Event Listener SPI used by the Health Care Financial Services platform. It publishes only successful `REGISTER` events to Kafka after the transaction commits, so the payment gateway can provision a Stripe customer and continue the existing notification flow.

The listener intentionally ignores login and admin events and never forwards sensitive credentials or token data.

## Build and install

```powershell
cd "C:\Users\deepa\Documents\Health Care Financial Services platform\keycloak-registration-listener"
mvn clean package
```

Build the provider JAR and install it into the custom Keycloak image:

```powershell
docker build -t keycloak-registration-listener .
```

In the runtime, configure the provider using either environment variables or Keycloak SPI flags. The default Kafka bootstrap target is `localhost:9092` and the default topic is `IDENTITY-REGISTERED`.

```powershell
docker compose -f "C:\Users\deepa\Documents\Health Care Financial Services platform\payment-gateway-system-springboot\payment-gateway-services\docker-compose.yml" up -d
```

Then enable the `identity-registration-kafka` listener in the Keycloak Admin Console under **Realm settings → Events → Config**.

## Configuration options

| Provider option | Default | Purpose |
| --- | --- | --- |
| `bootstrap-servers` | `localhost:9092` | Kafka bootstrap server list. |
| `topic` | `IDENTITY-REGISTERED` | Kafka topic for registration notifications. |

Example startup flags:

```powershell
--spi-events-listener-identity-registration-kafka-bootstrap-servers=kafka:9092
--spi-events-listener-identity-registration-kafka-topic=IDENTITY-REGISTERED
```

## Event payload

```json
{
  "eventId": "event-id",
  "eventType": "IDENTITY-REGISTERED",
  "realmId": "financial-health",
  "userId": "keycloak-user-id",
  "email": "user@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "source": "keycloak",
  "eventVersion": 1,
  "timestamp": "2026-10-17T00:00:00Z"
}
```

The listener uses a producer with ACK=all, idempotence, and bounded retries, and it does not block the Keycloak transaction thread after commit.
