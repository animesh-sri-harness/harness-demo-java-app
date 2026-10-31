#!/usr/bin/env sh
set -eu

BASE_URL="${1:-http://localhost:8080}"
EXPECTED_VERSION="${2:-}"
EXPECTED_COLOR="${3:-}"

curl --fail --silent --show-error "${BASE_URL}/actuator/health/readiness" |
  grep --quiet '"status":"UP"'

release="$(curl --fail --silent --show-error "${BASE_URL}/api/v1/info")"

if [ -n "${EXPECTED_VERSION}" ]; then
  printf '%s' "${release}" | grep --quiet "\"version\":\"${EXPECTED_VERSION}\""
fi

if [ -n "${EXPECTED_COLOR}" ]; then
  printf '%s' "${release}" | grep --quiet "\"color\":\"${EXPECTED_COLOR}\""
fi

printf 'Smoke test passed for %s\n' "${BASE_URL}"
printf '%s\n' "${release}"
