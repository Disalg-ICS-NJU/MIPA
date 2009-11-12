#! /bin/sh

MIPA_HOME="."
cd ../..
java -verbose:gc -Xloggc:log/ECAInitialize -cp $MIPA_HOME/build/classes/:$CLASSPATH net.sourceforge.mipa.eca.ECAInitialize
