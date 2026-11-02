@echo off
set "JAVA_HOME=C:\Users\Prashant\tools\jdk-17.0.20.1+1"
set "PATH=%JAVA_HOME%\bin;C:\Users\Prashant\tools\apache-maven-3.9.6\bin;%PATH%"
call "C:\Users\Prashant\tools\apache-maven-3.9.6\bin\mvn.cmd" %*
