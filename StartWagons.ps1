# Startet n Wagen-Prozesse (Python) im Hintergrund - Windows-Pendant zu StartWagons.sh.
# Nutzung:  .\StartWagons.ps1 <stationHost> <anzahl> [fahrzeitMs]
# Beispiel: .\StartWagons.ps1 192.168.0.10 2 3000
param(
    [string]$StationHost = "localhost",
    [int]$Count = 1,
    [int]$Ride = 3000
)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$Port = 9090
$Python = ".venv\Scripts\python.exe"
$env:PYTHONPATH = "gen-py"   # generierter Thrift-Code (wird an die Kindprozesse vererbt)

New-Item -ItemType Directory -Force -Path logs | Out-Null
for ($i = 1; $i -le $Count; $i++) {
    $p = Start-Process -FilePath $Python `
        -ArgumentList "-u", "src\python\wagon_client.py", $StationHost, $Port, $i, $Ride `
        -RedirectStandardOutput "logs\wagon_$i.log" `
        -RedirectStandardError  "logs\wagon_$i.err.log" `
        -WindowStyle Hidden -PassThru
    Write-Host "Wagen $i gestartet (PID $($p.Id))"
    Start-Sleep -Seconds 1   # kleine Pause, damit die Ankunftsreihenfolge W1, W2, ... ist
}

Write-Host "Alle Wagen laufen. Stoppen mit:"
Write-Host "  Get-CimInstance Win32_Process | ? CommandLine -like '*wagon_client.py*' | % { Stop-Process -Id `$_.ProcessId }"
