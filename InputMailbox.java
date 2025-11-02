import java.util.LinkedList;
import java.util.Queue;

public class InputMailbox {

    private final Queue<Message> queue = new LinkedList<>();
    private final int capacity;

    public InputMailbox(int capacity) {
        this.capacity = capacity;
    }

    // Productor: Cliente emisor
    public synchronized void put(Message msg) {
        while (queue.size() >= capacity) {
            // buzón lleno -> espera pasiva
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        queue.add(msg);
        // despierta a un consumidor (filtro)
        notify();
    }

    // Consumidor: Filtro
    public synchronized Message take() {
        while (queue.isEmpty()) {
            // buzón vacío -> espera pasiva
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        Message m = queue.poll();
        // despierta a un productor (cliente) si estaba bloqueado por capacidad
        notify();
        return m;
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public int getCapacity() {
        return capacity;
    }
}
