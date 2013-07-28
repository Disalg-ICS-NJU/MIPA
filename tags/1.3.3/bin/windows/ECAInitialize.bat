set MIPA_HOME=.
set MIPA_CLASSPATH=%MIPA_HOME%\build\classes\;%MIPA_HOME%\lib\log4j-1.2.17.jar;%MIPA_HOME%\lib\automaton.jar;%MIPA_HOME%\lib\org.apache.commons.collections_3.2.0.v201005080500.jar;%MIPA_HOME%\lib\org.apache.commons.lang_2.6.0.v201205030909.jar;%MIPA_HOME%\lib\org.eclipse.jface_3.8.101.v20120817-083647.jar;%MIPA_HOME%\lib\org.eclipse.swt.win32.win32.x86_64_3.100.1.v4234e.jar;%CLASSPATH%
cd ..\..
java -cp "%MIPA_CLASSPATH%" net.sourceforge.mipa.ECAInitialize
