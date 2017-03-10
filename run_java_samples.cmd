@ECHO OFF

ECHO Select demo to run:
ECHO.
ECHO  [1] DisplayMessages.Main
ECHO  [2] SendMessageToDevice.Main
ECHO.

SET /P opt="Enter number: "

cd samples-java

if "%opt%"=="1" mvn clean compile exec:java -Dexec.mainClass="DisplayMessages.Main"
if "%opt%"=="2" mvn clean compile exec:java -Dexec.mainClass="SendMessageToDevice.Main"


