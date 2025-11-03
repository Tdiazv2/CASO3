import java.util.LinkedList;
import java.util.List;

/**
 * Buzón de cuarentena para mensajes SPAM.
 *
 * Este método revisa todos los mensajes actualmente en cuarentena y decide:
 * 1. Reducir el TTL de cada mensaje.
 * 2. Si el mensaje ya expiró (TTL ≤ 0), enviarlo a entrega o descartarlo.
 * 3. Si el mensaje es de tipo END, también debe ser enviado.
 * 4. Remover todos los mensajes procesados del buzón.
 *
 * Con esto se garantiza que el buzón quede vacío después de procesar todos los mensajes listos.
 */
public class QuarantineMailbox {

    private final List<Message> quarantined = new LinkedList<>();

    // Añadir mensaje a cuarentena
    public synchronized void add(Message msg) {
        quarantined.add(msg);
    }

    /**
     * Revisa la cuarentena y devuelve los mensajes listos para salir.
     *
     * @return lista de mensajes listos para enviar al buzón de entrega
     */
    public synchronized List<Message> reviewAndCollectReady() {
        List<Message> ready = new LinkedList<>();  // mensajes listos para entrega
        List<Message> toRemove = new LinkedList<>(); // mensajes a eliminar de cuarentena

        for (Message m : quarantined) {

            // 1. Reducir TTL
            m.tickTTL();

            // 2. Mensajes de tipo END se agregan a la lista de salida
            if (m.getType() == Message.MessageType.END) {
                ready.add(m);

                // 3. Mensajes normales o SPAM expiran
            } else if (m.isTTLExpired()) {
                // Decidir si descartar mensaje (aleatorio para simular malicioso)
                int r = 1 + (int)(Math.random() * 21);
                boolean discard = (r % 7 == 0);

                if (!discard) {
                    ready.add(m); // mensaje listo para entrega
                } else {
                    System.out.println("Mensaje descartado: " + m);
                }
            }

            // 4. En cualquier caso, el mensaje ya fue procesado -> se elimina del buzón
            toRemove.add(m);
        }

        // Eliminar todos los mensajes procesados de la cuarentena
        quarantined.removeAll(toRemove);

        return ready;
    }

    public synchronized boolean isEmpty() {
        return quarantined.isEmpty();
    }
}
