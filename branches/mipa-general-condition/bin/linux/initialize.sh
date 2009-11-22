#! /bin/sh

MIPA_HOME="."
cd ../..
java -verbose:gc -Xloggc:log/initialize -cp $MIPA_HOME/build/classes:$CLASSPATH net.sourceforge.mipa.Initialize
