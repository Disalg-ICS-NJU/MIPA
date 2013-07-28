set MIPA_HOME=.
set MIPA_CLASSPATH=%MIPA_HOME%\lib\automaton.jar;%MIPA_HOME%\build\classes\;%MIPA_HOME%\lib\log4j-1.2.17.jar;%CLASSPATH%
cd ..\..
java -cp "%MIPA_CLASSPATH%" net.sourceforge.mipa.REApplication