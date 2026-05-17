#!/usr/bin/env sh

DIR="$(cd "$(dirname "$0")"; pwd)"
CLASSPATH=$DIR/gradle/wrapper/gradle-wrapper.jar

java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
