#!/usr/bin/env bash
set -euo pipefail

report_root="build/reports/dependency-verification"
if [[ -d "$report_root" ]]; then
  find "$report_root" -type f -name 'dependency-verification-report.html' -print -exec cat {} \;
fi
