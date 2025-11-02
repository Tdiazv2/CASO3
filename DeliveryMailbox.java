import java.util.LinkedList;
import java.util.Queue;

public class DeliveryMailbox {

    private final Queue<Message> queue = new LinkedList<>();
    private final int capacity;

    private boolean broadcastFinalEnd = false; // para saber si ya se metió el END global

    public DeliveryMailbox(int capacity) {
        this.capacity = capacity;
    }

    // Productores: Filtro de spam y Manejador de cuarentena
    public synchronized void put(Message msg) {
        while (queue.size() >= capacity) {
            // espera SEMIACTIVA:
            // en este buzón no nos dijeron explícitamente pasiva,
            // así que usamos wait con timeout corto para "reintentar"
            try {
                wait(10); // <= semiactiva, no dormimos eternamente
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        queue.add(msg);
        notifyAll(); // avisar a delivery servers
    }

    // Consumidor: Servidor de entrega (espera ACTIVA afuera)
    public synchronized Message takeNonBlocking() {
        if (queue.isEmpty()) {
            return null;
        }
        return queue.poll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void markBroadcastFinalEnd() {
        broadcastFinalEnd = true;
        notifyAll();
    }

    public synchronized boolean isBroadcastFinalEnd() {
        return broadcastFinalEnd;
    }

    public int getCapacity() {
        return capacity;
    }
}
