import java.util.LinkedList;
import java.util.List;

public class QuarantineMailbox {

    // lista de mensajes actualmente en cuarentena
    private final List<Message> quarantined = new LinkedList<>();

    public synchronized void add(Message msg) {
        quarantined.add(msg);
        // no usamos notify porque el manejador hace polling cada segundo
    }

    /**
     * Llamado por el manejador de cuarentena una vez por "ciclo".
     * Hace:
     *  - Decrementar TTL
     *  - Decidir si descarta (malicioso)
     *  - Mover mensajes listos al buzón de entrega
     *  - Detectar END
     *
     * Devuelve la lista de mensajes que deben salir a buzón de entrega.
     * También devuelve ENDs, para que el manejador pueda saber que debe terminar.
     */
    public synchronized List<Message> reviewAndCollectReady() {
        List<Message> ready = new LinkedList<>();
        List<Message> toRemove = new LinkedList<>();

        for (Message m : quarantined) {
            // bajar TTL
            m.tickTTL();

            if (m.getType() == Message.MessageType.END) {
                // El manejador sabrá que esto es señal de fin
                ready.add(m);
                toRemove.add(m);
                continue;
            }

            // decidir descarte malicioso:
            // random 1..21, si múltiplo de 7 => descartar
            int r = 1 + (int)(Math.random() * 21);
            boolean discard = (r % 7 == 0);

            if (m.isTTLExpired()) {
                if (!discard) {
                    ready.add(m); // listo para pasar a entrega
                }
                toRemove.add(m);
            }
        }

        quarantined.removeAll(toRemove);
        return ready;
    }

    public synchronized boolean isEmpty() {
        return quarantined.isEmpty();
    }
}
