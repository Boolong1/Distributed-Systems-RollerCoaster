#!/usr/bin/env bash
# Startet n Wagen-Prozesse (Python) im Hintergrund.
# Nutzung:  ./StartWagons.sh <stationHost> <anzahl> [fahrzeitMs]
# Beispiel: ./StartWagons.sh 192.168.0.10 2 3000
set -euo pipefail
cd "$(dirname "$0")"

HOST=${1:-localhost}
COUNT=${2:-1}
RIDE=${3:-3000}
PORT=9090

PYTHON=.venv/bin/python
export PYTHONPATH="gen-py"   # Python-Pendant zu "java -cp": generierter Thrift-Code

mkdir -p logs
for i in $(seq 1 "$COUNT"); do
  "$PYTHON" -u src/python/wagon_client.py "$HOST" "$PORT" "$i" "$RIDE" > "logs/wagon_$i.log" 2>&1 &
  echo "Wagen $i gestartet (PID $!)"
  sleep 1   # kleine Pause, damit die Ankunftsreihenfolge W1, W2, ... ist
done

echo "Alle Wagen laufen. Stoppen mit: pkill -f wagon_client.py"
