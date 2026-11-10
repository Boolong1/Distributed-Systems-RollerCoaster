import random
import sys
import time

from rollerCoaster import Station  # Modulname = Name der .thrift-Datei (bzw. "namespace py")
from thrift.protocol import TBinaryProtocol
from thrift.transport import TSocket, TTransport


def main():
    host = sys.argv[1]
    port = int(sys.argv[2])
    wagon_id = int(sys.argv[3])
    base_ride_time = int(sys.argv[4]) if len(sys.argv) > 4 else 3000

    socket = TSocket.TSocket(host, port)
    socket.setTimeout(None)  # Calls duerfen lange blockieren (arrive, ride_finished)
    transport = TTransport.TBufferedTransport(socket)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)  # muss zum Server passen
    station = Station.Client(protocol)
    transport.open()

    try:
        while True:
            print(f"[W{wagon_id}] fahre in den Bahnsteig ein")
            station.arrive(wagon_id)  # blockiert, bis ich dran UND voll bin

            station.departure(wagon_id)
            ride_time = base_ride_time + random.randint(0, 2000)
            print(f"[W{wagon_id}] Abfahrt, Fahrt dauert {ride_time} ms")
            time.sleep(ride_time / 1000)  # die eigentliche Fahrt

            station.ride_finished(wagon_id)  # blockiert, bis alle ausgestiegen sind
            print(f"[W{wagon_id}] leer, zurueck zum Bahnsteig")
    finally:
        transport.close()


if __name__ == "__main__":
    main()
