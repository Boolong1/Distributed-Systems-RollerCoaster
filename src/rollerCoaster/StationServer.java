package rollerCoaster;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;

public class StationServer {
    public static void main(String[] args) throws Exception {
        int port = 9090;
        int capacity = args.length > 0 ? Integer.parseInt(args[0]) : 4; // c

        StationHandler handler = new StationHandler(capacity);     // EINE Instanz fuer alle Clients
        Station.Processor<StationHandler> processor = new Station.Processor<>(handler);

        TServerSocket socket = new TServerSocket(port);
        TServer server = new TThreadPoolServer(
                new TThreadPoolServer.Args(socket)
                        .processor(processor)
                        .minWorkerThreads(16)); // ein Thread pro Client-Verbindung (m + n)

        System.out.println("[Station] laeuft auf Port " + port + ", c = " + capacity);
        server.serve();
    }
}