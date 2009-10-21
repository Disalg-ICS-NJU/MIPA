#! /bin/sh

MIPA_HOME="."
cd ../..
java -verbose:gc -cp $MIPA_HOME/build/classes/:$CLASSPATH net.sourceforge.mipa.Application
