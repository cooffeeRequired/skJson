@echo off
setlocal EnableDelayedExpansion

rem Získání parametru souboru
set "FilePath=%~1"

rem Kontrola, zda soubor existuje
if not exist "!FilePath!" (
    echo Soubor nenalezen: !FilePath!
    exit /b
)

rem Výpočet hashe souboru pomocí PowerShellového příkazu Get-FileHash
for /f "delims=" %%A in ('powershell -Command "Get-FileHash -Path \"!FilePath!\" | Select-Object -ExpandProperty Hash"') do (
    set "Hash=%%A"
)

rem Výstup hashe
echo Hash souboru '!FilePath!': !Hash!
