@echo off
rem Startet StartPassengers.ps1 ohne Aenderung der Execution Policy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0StartPassengers.ps1" %*
