#!/usr/bin/sh

vegeta attack \
  -targets load.target \
  -duration 1s \
  -rate "$1" \
  | vegeta report
