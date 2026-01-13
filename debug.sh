#!/usr/bin/env bash

export CURRENT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Current directory is: $CURRENT_DIR"
cd  "$CURRENT_DIR/mondrian/build/image/bin" || exit 1
./java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -m org.mbari.mondrian/org.mbari.mondrian.App "$@"
#./Mondrian -J-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
