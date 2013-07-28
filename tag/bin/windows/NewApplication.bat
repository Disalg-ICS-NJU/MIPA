set MIPA_HOME=.
set MIPA_CLASSPATH=%MIPA_HOME%\build\classes\;%MIPA_HOME%\lib;%CLASSPATH%
cd ..\..
java -cp "%MIPA_CLASSPATH%" net.sourceforge.mipa.NewApplication