#!/bin/sh

if [ -z "$JAVA_HOME" ]
then
  JAVA=java
else
  JAVA=$JAVA_HOME/bin/java
fi

rlwrap $JAVA -classpath \
target/NetLogoHeadless.jar:\
$HOME/.sbt/boot/scala-2.11.0-M4/lib/scala-library.jar:\
lib_managed/jars/asm/asm-all/asm-all-3.3.1.jar \
org.nlogo.headless.Shell "$@"
