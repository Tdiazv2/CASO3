import java.util.LinkedList;
import java.util.Queue;

public class InputMailbox {

    private final Queue<Message> queue = new LinkedList<>();
    private final int capacity;

    private final GlobalControl globalControl;

    public InputMailbox(int capacity, GlobalControl globalControl) {
        this.capacity = capacity;
        this.globalControl = globalControl;
    }

    // Productor: Cliente emisor
    public synchronized void put(Message msg) {
        while (queue.size() >= capacity) {
            try {
                System.out.println(Thread.currentThread().getName() + " espera (buzón lleno).");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        queue.add(msg);
        System.out.println(Thread.currentThread().getName() + " insertó " + msg);
        notifyAll();
    }

    // Consumidor: Filtro
    public synchronized Message take() {
        while (queue.isEmpty() && !globalControl.allClientsFinished()) {
            try {
                System.out.println(Thread.currentThread().getName() + " espera (buzón vacío).");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        if (queue.isEmpty()) {
            // no hay más mensajes y el sistema terminó
            return null;
        }

        Message m = queue.poll();
        System.out.println(Thread.currentThread().getName() + " tomó " + m);
        notifyAll();
        return m;
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public int getCapacity() {
        return capacity;
    }
}
