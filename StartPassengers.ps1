# Startet m Passagier-Prozesse (Java) im Hintergrund - Windows-Pendant zu StartPassengers.sh.
# Nutzung:  .\StartPassengers.ps1 <stationHost> <anzahl>
# Beispiel: .\StartPassengers.ps1 localhost 6
param(
    [string]$StationHost = "localhost",
    [int]$Count = 6
)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$Port = 9090
$CP = "build;lib\*"   # unter Windows ';' statt ':' als Trennzeichen

New-Item -ItemType Directory -Force -Path logs | Out-Null
for ($i = 1; $i -le $Count; $i++) {
    $p = Start-Process -FilePath "java" `
        -ArgumentList "-cp", "`"$CP`"", "rollerCoaster.PassengerClient", $StationHost, $Port, $i `
        -RedirectStandardOutput "logs\passenger_$i.log" `
        -RedirectStandardError  "logs\passenger_$i.err.log" `
        -WindowStyle Hidden -PassThru
    Write-Host "Passagier $i gestartet (PID $($p.Id))"
}

Write-Host "Alle Passagiere laufen. Stoppen mit:"
Write-Host "  Get-CimInstance Win32_Process | ? CommandLine -like '*PassengerClient*' | % { Stop-Process -Id `$_.ProcessId }"
