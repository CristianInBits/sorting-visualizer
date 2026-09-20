#!/bin/sh
#
# Starts the visualizer. Run it from a terminal, or from your file manager if
# it is set up to execute shell scripts.
#
# The first run downloads Maven and about 30 MB of dependencies, so give it a
# minute. Java 17 or newer is the only thing you need installed.

cd "$(dirname "$0")" || exit 1

# The wrapper falls back to JAVA_HOME when java is not on the PATH, so either
# one is enough.
if [ -z "$JAVA_HOME" ] && ! command -v java > /dev/null 2>&1; then
    echo "No Java found."
    echo
    echo "Install a JDK, version 17 or newer, from https://adoptium.net/"
    echo "and open a new terminal so the change to PATH takes effect."
    exit 1
fi

exec ./mvnw javafx:run
