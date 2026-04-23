#!/bin/sh
set -e

# Provide a default value for local docker-compose
export BACKEND_URL=${BACKEND_URL:-http://backend:8080}

# Replace environment variables in nginx.conf
envsubst '${BACKEND_URL}' < /etc/nginx/conf.d/nginx.conf.template > /etc/nginx/conf.d/default.conf

# Execute CMD
exec "$@"
