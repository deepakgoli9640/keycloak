FROM quay.io/keycloak/keycloak:28.8.0

COPY target/keycloak-event-listener-1.0.0.jar /opt/keycloak/providers/

RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", "--optimized"]