echo off

set scriptpath=%~dp0
set packagepath=%scriptpath%
set modulespath=%packagepath%\modules

set "JAVA_CMD=%packagepath%jdk\bin\java"
set "BANALYTICS_HOME=%packagepath%"

echo Java command: %JAVA_CMD%
echo Banalytics Home: %BANALYICS_HOME%

"%JAVA_CMD%" --enable-preview -XX:VMOptionsFile=config/banalytics.vmoptions -Dorg.bytedeco.javacpp.nopointergc=true -cp "%packagepath%banalytics-box.jar" -Dloader.path="%modulespath%" -Dfile.encoding=UTF-8 org.springframework.boot.loader.PropertiesLauncher
