package rollerCoaster;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * Passagier-Knoten. Start: java PassengerClient <stationHost> <port> <passengerId>
 * Mehrere Passagiere = mehrere Prozesse mit unterschiedlicher ID (alle auf Maschine A).
 */
public class PassengerClient {
    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int id = Integer.parseInt(args[2]);

        TTransport transport = new TSocket(host, port);
        transport.open();
        Station.Client station = new Station.Client(new TBinaryProtocol(transport));

        int rides = 0;
        while (true) {
            System.out.println("[P" + id + "] moechte fahren");
            int wagonId = station.i_am_boarding(id);        // blockiert, bis ich einen Platz habe
            System.out.println("[P" + id + "] sitze in W" + wagonId);

            station.wait_for_ride_end(id);                  // blockiert waehrend der Fahrt
            station.i_am_disembarking(id);
            rides++;
            System.out.println("[P" + id + "] ausgestiegen, Fahrten: " + rides);

            Thread.sleep(500);                              // kurz verschnaufen, dann wieder anstellen
        }
    }
}