#! /bin/sh

MIPA_HOME="."
cd ../..
java -cp "$MIPA_HOME/build/classes:$CLASSPATH" net.sourceforge.mipa.MIPAInitialize
