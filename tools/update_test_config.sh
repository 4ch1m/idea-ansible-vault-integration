#!/bin/bash

SCRIPT_PATH=$(dirname $(realpath "${0}"))

docker run --rm --pull always \
  ghcr.io/ansible/community-ansible-dev-tools:latest \
  /bin/bash -c "ansible-config init --disabled" | \
  tee "${SCRIPT_PATH}/../src/test/resources/ansible-stock.cfg"

sha256sum -t "${SCRIPT_PATH}/../src/test/resources/ansible-stock.cfg" | \
  cut -d ' ' -f 1 | tr -d '\n' | \
  tee "${SCRIPT_PATH}/../src/test/resources/ansible-stock.sha256"
