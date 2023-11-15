@echo off
setlocal ENABLEDELAYEDEXPANSION

set PRE_CLASSPATH=
for %%i in (lib\*.jar) do (
 set jarfile=%%i
 call :setclass
)  

echo on
java -Xmx256m -cp classes;%PRE_CLASSPATH% europeana.rnd.iiif.discovery.demo.EdmAggregationDemonstrator %1 %2 %3 %4 %5 %6 %7 %8 %9
@echo off

:setclass
set PRE_CLASSPATH=%jarfile%;%PRE_CLASSPATH%
set jarfile=
