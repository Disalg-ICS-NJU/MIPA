#! /bin/sh

MIPA_HOME="."
cd ../..
java -cp $MIPA_HOME/build/classes:$MIPA_HOME/ext/lucene-core-2.4.1.jar:$CLASSPATH net.sourceforge.mipa.Initialize
