#! /bin/sh

MIPA_HOME="."
cd ../..
echo $1
java -verbose:gc -cp $MIPA_HOME/build/classes/:$CLASSPATH net.sourceforge.mipa.Application predicate_oga.xml $1
