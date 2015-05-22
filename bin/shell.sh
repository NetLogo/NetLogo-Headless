#!/bin/sh

if [ -n "$1" ]; then
   ARG=" \"$1\""
fi

rlwrap ./sbt \
  -Djline.terminal=jline.UnsupportedTerminal \
  --warn \
  "jvmBuild/runMain org.nlogo.headless.Shell $ARG"
