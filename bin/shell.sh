#!/bin/sh

if [ -z "$JAVA_HOME" ]
then
  JAVA=java
else
  JAVA=$JAVA_HOME/bin/java
fi

if [ ! -f "target/NetLogoHeadless.jar" ]; then
echo "missing target/NetLogoHeadless.jar; run 'sbt package'"
exit 1
fi

rlwrap $JAVA -classpath \
target/NetLogoHeadless.jar:\
$HOME/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.11.0-M4.jar:\
lib_managed/jars/org.scala-lang/scala-parser-combinators/scala-parser-combinators-2.11.0-M4.jar:\
lib_managed/jars/asm/asm-all/asm-all-3.3.1.jar \
org.nlogo.headless.Shell "$@"
