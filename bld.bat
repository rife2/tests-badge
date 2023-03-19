@echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
java -jar "%DIRNAME%/lib/bld/bld-wrapper.jar" "%0" src/bld/java/com/uwyn/testsbadge/TestsBadgeBuild.java %*