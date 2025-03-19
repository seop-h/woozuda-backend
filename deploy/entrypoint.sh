#!/bin/sh

envsubst < /etc/promtail/config.yml.template > /etc/promtail/config.yml
exec /usr/bin/promtail -config.file=/etc/promtail/config.yml
