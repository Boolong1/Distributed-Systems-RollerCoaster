@echo off
rem Startet StartWagons.ps1 ohne Aenderung der Execution Policy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0StartWagons.ps1" %*
