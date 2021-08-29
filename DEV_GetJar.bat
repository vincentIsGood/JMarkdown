@echo off

set jarname=jmarkdown-v3.0.0
set structure=com/vincentcodes/markdown/*

:: cp -r lib/com/ .

:: with Manifest (for command line)
cd classes
jar -cvfm %jarname%.jar Manifest.txt %structure%
mv %jarname%.jar ..

::rm -r ../com/

:: without Manifest
cd classes
jar -cvf %jarname%.jar %structure%
mv %jarname%.jar ..

cd ../src
jar -cvf %jarname%-sources.jar %structure%
mv %jarname%-sources.jar ..

pause