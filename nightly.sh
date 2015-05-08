#!/bin/bash -ev

# -e makes the whole thing die with an error if any command does
# -v lets you see the commands as they happen

if [ "$1" == --clean ] ; then
  git clean -fdX
  git submodule update --init
  git submodule foreach git clean -fdX
fi

rm -rf tmp/nightly
mkdir -p tmp/nightly
mkdir -p jvm/tmp

# here we're using pipes so "-e" isn't enough to stop when something fails.
# maybe there's an easier way, than I've done it below, I don't know.
# I suck at shell scripting - ST 2/15/11

./sbt jvmBuild/test:compile "ensime generate" 2>&1 | tee tmp/nightly/compile.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: test:compile"; exit 1; fi
echo "*** done: test:compile"

./sbt jvmBuild/fast:test 2>&1 | tee tmp/nightly/0-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: fast:test"; exit 1; fi
echo "*** done: fast:test"

./sbt parserJVM/test 2>&1 | tee tmp/nightly/0-parser-jvm-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: parserJVM/test"; exit 1; fi
echo "*** done: parserJVM/test"

./sbt parserJS/test 2>&1 | tee tmp/nightly/0-parser-js-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: parserJS/test"; exit 1; fi
echo "*** done: parserJS/test"

./sbt jvmBuild/nogen jvmBuild/fast:test 2>&1 | tee tmp/nightly/1-nogen-fast-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen fast:test"; exit 1; fi
echo "*** done: nogen fast:test"

./sbt jvmBuild/all 2>&1 | tee tmp/nightly/2-sbt-all.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: sbt jvmBuild/all"; exit 1; fi
echo "*** done: sbt jvmBuild/all"

# turning off logBuffered for the slow tests keeps us from going 10
# minutes without producing any output, which would make Travis time
# out.  the tests from different suites get scrambled together in the
# output, but we can't turn off parallelExecution or the whole thing
# will take more than 50 minutes to run (the Travis limit)
# - ST 8/29/13, 4/1/14

./sbt 'set logBuffered in test := false' jvmBuild/slow:test 2>&1 | tee tmp/nightly/3-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: slow:test"; exit 1; fi
echo "*** done: slow:test"

./sbt 'set logBuffered in test := false' jvmBuild/nogen jvmBuild/slow:test 2>&1 | tee tmp/nightly/4-nogen-slow-test.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: nogen slow:test"; exit 1; fi
echo "*** done: nogen slow:test"

./sbt jvmBuild/depend 2>&1 | tee tmp/nightly/5-depend.txt
if [ ${PIPESTATUS[0]} -ne 0 ] ; then echo "*** FAILED: depend"; exit 1; fi
echo "*** done: depend"

./sbt jvmBuild/scalastyle parserCore/scalastyle parserJvm/scalastyle macros/scalastyle parserJs/scalastyle 2>&1 | tee tmp/nightly/6-scalastyle.txt
if [ `grep error target/scalastyle-result.xml | wc -l` -ne 0 ] ; then echo "*** FAILED: scalastyle"; exit 1; fi
echo "*** done: scalastyle"

echo "****** all done!"
