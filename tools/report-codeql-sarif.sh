#!/usr/bin/env bash
# Lists every CodeQL finding from the SARIF files in $1 and fails on an error-level finding.
# A result without its own level takes its rule's default level, and SARIF's default is warning.
set -euo pipefail

sarif_dir="${1:?SARIF directory required}"
shopt -s nullglob
sarif_files=("$sarif_dir"/*.sarif)
if [[ ${#sarif_files[@]} -eq 0 ]]; then
  echo "No SARIF files in $sarif_dir" >&2
  exit 1
fi

findings="$(jq -r '
  .runs[] as $run
  | $run.results[]?
  | . as $result
  | ($result.ruleIndex // $result.rule.index) as $ruleIndex
  | ($result.rule.toolComponent.index) as $componentIndex
  | (if $ruleIndex == null then null
     elif $componentIndex == null then $run.tool.driver.rules[$ruleIndex].defaultConfiguration.level
     else $run.tool.extensions[$componentIndex].rules[$ruleIndex].defaultConfiguration.level
     end) as $ruleLevel
  | [
      ($result.level // $ruleLevel // "warning"),
      ($result.ruleId // $result.rule.id // "unknown-rule"),
      (($result.locations[0].physicalLocation.artifactLocation.uri // "unknown-file")
        + ":" + (($result.locations[0].physicalLocation.region.startLine // 0) | tostring)),
      ($result.message.text | gsub("[\t\n]+"; " "))
    ]
  | @tsv
' "${sarif_files[@]}")"

total=0
errors=0
if [[ -n "$findings" ]]; then
  total="$(printf '%s\n' "$findings" | wc -l)"
  errors="$(printf '%s\n' "$findings" | awk -F'\t' '$1 == "error"' | wc -l)"
fi

{
  echo "### CodeQL findings"
  echo
  echo "Total: $total"
  echo
  echo "Error level: $errors"
  if [[ -n "$findings" ]]; then
    echo
    echo '```'
    printf '%s\n' "$findings" | awk -F'\t' '{ printf "%s  %s  %s\n  %s\n", toupper($1), $2, $3, $4 }'
    echo '```'
  fi
} | tee -a "${GITHUB_STEP_SUMMARY:-/dev/null}"

if [[ "$errors" -gt 0 ]]; then
  echo "CodeQL reported $errors error-level finding(s)." >&2
  exit 1
fi
