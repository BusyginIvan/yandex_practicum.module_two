#!/bin/sh
set -eu

KEYCLOAK_URL="${KEYCLOAK_URL:-http://127.0.0.1:8080}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-master}"
KEYCLOAK_ADMIN_REALM="${KEYCLOAK_ADMIN_REALM:-master}"
KEYCLOAK_CLIENT_ID="${KEYCLOAK_MARKET_CLIENT_ID:?KEYCLOAK_MARKET_CLIENT_ID is required}"
KEYCLOAK_CLIENT_SECRET="${KEYCLOAK_MARKET_CLIENT_SECRET:?KEYCLOAK_MARKET_CLIENT_SECRET is required}"
KEYCLOAK_ADMIN_USERNAME="${KC_BOOTSTRAP_ADMIN_USERNAME:?KC_BOOTSTRAP_ADMIN_USERNAME is required}"
KEYCLOAK_ADMIN_PASSWORD="${KC_BOOTSTRAP_ADMIN_PASSWORD:?KC_BOOTSTRAP_ADMIN_PASSWORD is required}"

/opt/keycloak/bin/kc.sh start-dev & KEYCLOAK_PID=$!

cleanup() { kill "$KEYCLOAK_PID" 2>/dev/null || true; }
trap cleanup INT TERM

echo "Waiting for Keycloak to become available..."
until /opt/keycloak/bin/kcadm.sh config credentials \
    --server "$KEYCLOAK_URL" \
    --realm "$KEYCLOAK_ADMIN_REALM" \
    --user "$KEYCLOAK_ADMIN_USERNAME" \
    --password "$KEYCLOAK_ADMIN_PASSWORD" >/dev/null 2>&1
do
    sleep 2
done

echo "Ensuring OAuth2 client '$KEYCLOAK_CLIENT_ID' exists..."
EXISTING_CLIENT_ID=$(
    /opt/keycloak/bin/kcadm.sh get clients \
        -r "$KEYCLOAK_REALM" \
        -q clientId="$KEYCLOAK_CLIENT_ID" \
        --fields id \
        --format csv \
        --noquotes | tr -d '\r\n'
)

if [ -z "$EXISTING_CLIENT_ID" ]; then
    /opt/keycloak/bin/kcadm.sh create clients -r "$KEYCLOAK_REALM" \
        -s enabled=true \
        -s protocol=openid-connect \
        -s clientId="$KEYCLOAK_CLIENT_ID" \
        -s publicClient=false \
        -s serviceAccountsEnabled=true \
        -s standardFlowEnabled=false \
        -s directAccessGrantsEnabled=false \
        -s implicitFlowEnabled=false \
        -s secret="$KEYCLOAK_CLIENT_SECRET" >/dev/null
    echo "Created OAuth2 client '$KEYCLOAK_CLIENT_ID'."
else
    echo "OAuth2 client '$KEYCLOAK_CLIENT_ID' already exists. Skipping creation."
fi

wait "$KEYCLOAK_PID"
