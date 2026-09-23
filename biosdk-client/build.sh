#!/usr/bin/env bash
# Maven package / test wrapper (library — no process). Windows cmd: use build.bat
#
#   ./build.sh init | test | all
set -euo pipefail

MODULE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE="biosdk-client"
UNAME_S="$(uname -s 2>/dev/null || echo unknown)"

MVN_SKIP=(
  "-DskipTests"
  "-Dgpg.skip=true"
  "-Dmaven.javadoc.skip=true"
)

usage() {
  cat <<'EOF'
biosdk-client Maven wrapper (library — unit tests on MockWebServer :9098)

  Linux / macOS / Git Bash:
    ./build.sh init | test | all

  Windows cmd:
    build.bat init | test | all

  init    package this module (skip tests)
  test    Maven unit tests (MockWebServer :9098)
  all     init + test
EOF
  exit "${1:-0}"
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "error: '$1' is required on PATH" >&2
    exit 1
  }
}

check_prereqs() {
  need_cmd java
  need_cmd mvn
  echo "os: ${UNAME_S}"
  local ver
  ver="$(java -version 2>&1 | head -n 1 || true)"
  echo "java: $ver"
  if ! echo "$ver" | grep -E '"21[\. "]' >/dev/null 2>&1; then
    echo "warn: JDK 21 is required. Continuing anyway." >&2
  fi
}

mvn_module() {
  (
    cd "$MODULE_DIR"
    unset mosip_biosdk_service MOSIP_BIOSDK_SERVICE || true
    mvn "$@"
  )
}

cmd_init() {
  check_prereqs
  echo "==> packaging ${MODULE} (skip tests)"
  mvn_module clean package "${MVN_SKIP[@]}"
  echo "init complete"
}

cmd_test() {
  check_prereqs
  echo "==> maven unit tests (MockWebServer :9098)"
  mvn_module test "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
}

cmd_all() {
  echo "==> all: init + test"
  cmd_init
  cmd_test
}

main() {
  local cmd="${1:-}"
  shift || true
  case "$cmd" in
    -h|--help|help) usage 0 ;;
    init) cmd_init ;;
    test) cmd_test ;;
    all) cmd_all ;;
    "") usage 1 ;;
    *) echo "error: unknown command '$cmd'" >&2; usage 1 ;;
  esac
}

main "$@"
