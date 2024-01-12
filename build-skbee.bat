@echo off
REM CHMOD 777
setlocal enabledelayedexpansion
REM Clone the repository from GitHub
git clone https://github.com/ShaneBeee/SkBee.git
REM Change to the repository directory
cd SkBee
REM Run Gradle Wrapper to build the project
call gradlew clean build
REM Get the name of the JAR file from the libs directory
for /f %%i in ('dir /b /o-d /a-d .\build\libs\*.jar') do set "jarFileName=%%i"
REM Set the delimiter
set "delimiter=SkBee-"
REM Initialize the counter
set "count=0"
REM Loop to split the string
for /f "tokens=1,* delims=%delimiter%" %%a in ("!jarFileName!") do (
    set "token[!count!]=%%a"
    set /a count+=1
    set "inputString=%%b"
)
set "Dversion=!token[0]:~0,-4!"
REM Install the JAR file into the local Maven repository
call mvn install:install-file -Dfile=build/libs/%jarFileName% -DgroupId=com.shanebeestudios.skbee -DartifactId=skbee -Dversion=!Dversion! -Dpackaging=jar
REM Delete the SkBee directory
cd ..
rd /s /q SkBee
endlocal