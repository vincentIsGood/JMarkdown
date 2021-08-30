@echo off

set cmdjarname=jmarkdown-cmdutil-v2.0.0
set jarname=jmarkdown-v3.0.2
set structure=*

:: cp -r lib/com/ .

:: with Manifest (for command line)
:: cd classes
:: jar -cvfm %cmdjarname%.jar Manifest.txt %structure%
:: mv %cmdjarname%.jar ..

::rm -r ../com/

:: without Manifest
cd classes
jar -cvf %jarname%.jar %structure%
mv %jarname%.jar ..

cd ../src
jar -cvf %jarname%-sources.jar %structure%
mv %jarname%-sources.jar ..

pause