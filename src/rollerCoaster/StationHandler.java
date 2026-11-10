package rollerCoaster;
import org.apache.thrift.TException;
import java.util.*;

/**
 * Zentrale Station (Monitor). Alle Methoden sind synchronized auf dieselbe Instanz.
 * Clients werden blockiert, indem ihr Thrift-Call in wait() haengt, bis ihre Bedingung gilt.
 * notifyAll() weckt nach jeder Zustandsaenderung alle Wartenden, die ihre Bedingung neu pruefen.
 */
public class StationHandler implements Station.Iface {

    private final int capacity; // c

    // Passagiere, die auf eine Fahrt warten (FIFO -> keine Starvation, faire Reihenfolge)
    private final Deque<Integer> waitingPassengers = new ArrayDeque<>();

    // Wagen am Bahnsteig in Ankunftsreihenfolge; nur der vorderste darf beladen werden
    private final Deque<Integer> platformQueue = new ArrayDeque<>();

    // Wagen auf der Strecke in Abfahrtsreihenfolge (eine Strecke -> kein Ueberholen)
    private final Deque<Integer> track = new ArrayDeque<>();

    // Welche Passagiere sitzen in welchem Wagen
    private final Map<Integer, List<Integer>> seats = new HashMap<>();
    private final Map<Integer, Integer> passengerWagon = new HashMap<>();

    // Wagen, deren Fahrt beendet ist und die gerade entladen werden
    private final Set<Integer> rideOver = new HashSet<>();

    // Nur fuer die Demo: Fairness sichtbar machen
    private final Map<Integer, Integer> rideCount = new TreeMap<>();

    public StationHandler(int capacity) {
        this.capacity = capacity;
    }

    // ---------------- Passagiere ----------------

    @Override
    public synchronized int i_am_boarding(int passengerId) throws TException {
        waitingPassengers.addLast(passengerId);
        log("P" + passengerId + " wartet  | Queue: " + waitingPassengers);

        // Warten, bis: ein Wagen beladen wird, er noch Platz hat UND ich vorne in der Schlange bin
        while (!(isLoadingWagonFree() && Integer.valueOf(passengerId).equals(waitingPassengers.peekFirst()))) {
            waitUninterruptibly();
        }

        waitingPassengers.pollFirst();
        int wagonId = platformQueue.peekFirst();
        seats.get(wagonId).add(passengerId);
        passengerWagon.put(passengerId, wagonId);
        log("P" + passengerId + " steigt in W" + wagonId + " ein " + seats.get(wagonId));

        notifyAll(); // naechster Passagier oder volle-Wagen-Bedingung pruefen
        return wagonId;
    }

    @Override
    public synchronized void wait_for_ride_end(int passengerId) throws TException {
        int wagonId = passengerWagon.get(passengerId);
        while (!rideOver.contains(wagonId)) {
            waitUninterruptibly();
        }
    }

    @Override
    public synchronized void i_am_disembarking(int passengerId) throws TException {
        int wagonId = passengerWagon.remove(passengerId);
        seats.get(wagonId).remove(Integer.valueOf(passengerId));
        rideCount.merge(passengerId, 1, Integer::sum);
        log("P" + passengerId + " steigt aus W" + wagonId + " aus | Fahrten: " + rideCount);
        notifyAll(); // Wagen wartet evtl. darauf, leer zu sein
    }

    // ---------------- Wagen ----------------

    @Override
    public synchronized void arrive(int wagonId) throws TException {
        seats.put(wagonId, new ArrayList<>());
        platformQueue.addLast(wagonId);
        log("W" + wagonId + " am Bahnsteig | Wagen-Queue: " + platformQueue);
        notifyAll(); // wartende Passagiere koennen evtl. einsteigen

        // Warten, bis ich vorne am Bahnsteig bin UND voll
        while (!(Integer.valueOf(wagonId).equals(platformQueue.peekFirst()) && seats.get(wagonId).size() == capacity)) {
            waitUninterruptibly();
        }
    }

    @Override
    public synchronized void departure(int wagonId) throws TException {
        platformQueue.pollFirst();   // Bahnsteig frei fuer den naechsten Wagen
        track.addLast(wagonId);      // Abfahrtsreihenfolge merken
        log("W" + wagonId + " faehrt ab mit " + seats.get(wagonId) + " | Strecke: " + track);
        notifyAll(); // naechster Wagen darf jetzt beladen werden
    }

    @Override
    public synchronized void ride_finished(int wagonId) throws TException {
        // Kein Ueberholen: erst entladen, wenn alle vorher abgefahrenen Wagen entladen sind
        while (!Integer.valueOf(wagonId).equals(track.peekFirst())) {
            waitUninterruptibly();
        }

        rideOver.add(wagonId);
        log("W" + wagonId + " zurueck, Passagiere steigen aus");
        notifyAll(); // weckt die Passagiere in wait_for_ride_end

        // Warten, bis alle c Passagiere ausgestiegen sind
        while (!seats.get(wagonId).isEmpty()) {
            waitUninterruptibly();
        }

        rideOver.remove(wagonId);
        track.pollFirst();
        log("W" + wagonId + " leer");
        notifyAll(); // naechster Wagen auf der Strecke darf entladen
    }

    // ---------------- Hilfsmethoden ----------------

    private boolean isLoadingWagonFree() {
        Integer w = platformQueue.peekFirst();
        return w != null && seats.get(w).size() < capacity;
    }

    private void waitUninterruptibly() {
        try {
            wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void log(String msg) {
        System.out.println("[Station] " + msg);
    }
}