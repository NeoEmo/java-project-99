#!/bin/sh
set -eu

KEY_DIR=/run/secrets
PRIVATE_KEY="$KEY_DIR/jwt_private_key"
PUBLIC_KEY="$KEY_DIR/jwt_public_key"

mkdir -p "$KEY_DIR"
if [ ! -s "$PRIVATE_KEY" ] || [ ! -s "$PUBLIC_KEY" ]; then
    if [ -s "$PRIVATE_KEY" ] || [ -s "$PUBLIC_KEY" ]; then
        echo "Only one RSA key exists under $KEY_DIR; refusing to replace a partial key pair." >&2
        exit 1
    fi
    umask 077
    openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$PRIVATE_KEY"
    openssl pkey -in "$PRIVATE_KEY" -pubout -out "$PUBLIC_KEY"
fi

chmod 600 "$PRIVATE_KEY"
chmod 644 "$PUBLIC_KEY"
exec "$@"
