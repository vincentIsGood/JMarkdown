@echo off

set first=src/com/vincentcodes/markdown/*.java src/com/vincentcodes/markdown/renderer/*.java
:: .java files are in encoding UTF-8
javac -encoding UTF-8 -d classes -cp ./lib/*;./src/ %first%

pause