#!/usr/bin/env bash
# Startet m Passagier-Prozesse (Java) im Hintergrund.
# Nutzung:  ./StartPassengers.sh <stationHost> <anzahl>
# Beispiel: ./StartPassengers.sh localhost 6
set -euo pipefail
cd "$(dirname "$0")"

HOST=${1:-localhost}
COUNT=${2:-6}
PORT=9090
CP="build:lib/*"   # kompilierte Klassen + libthrift + slf4j

mkdir -p logs
for i in $(seq 1 "$COUNT"); do
  java -cp "$CP" rollerCoaster.PassengerClient "$HOST" "$PORT" "$i" > "logs/passenger_$i.log" 2>&1 &
  echo "Passagier $i gestartet (PID $!)"
done

echo "Alle Passagiere laufen. Stoppen mit: pkill -f PassengerClient"
