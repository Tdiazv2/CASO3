public class SpamFilter extends Thread {

    private final InputMailbox inputMailbox;
    private final QuarantineMailbox quarantineMailbox;
    private final DeliveryMailbox deliveryMailbox;
    private final GlobalControl globalControl;

    public SpamFilter(int id,
                      InputMailbox inputMailbox,
                      QuarantineMailbox quarantineMailbox,
                      DeliveryMailbox deliveryMailbox,
                      GlobalControl globalControl) {
        super("SpamFilter-" + id);
        this.inputMailbox = inputMailbox;
        this.quarantineMailbox = quarantineMailbox;
        this.deliveryMailbox = deliveryMailbox;
        this.globalControl = globalControl;
    }

    @Override
    public void run() {
        boolean running = true;

        while (running) {
            // tomar mensaje del buzón de entrada (bloquea si vacío)
            Message msg = inputMailbox.take();
            if (msg == null) {
                continue; // hilo pudo ser interrumpido
            }

            switch (msg.getType()) {
                case START:
                    // START no es spam
                    System.out.println(getName() +
                        " vio START de cliente " + msg.getSenderClientId());
                    break;

                case NORMAL:
                    handleNormal(msg);
                    break;

                case END:
                    handleEnd(msg);
                    break;
            }

            // ¿Podemos ya mandar el END global al buzón de entrega?
            maybeSendGlobalEnd();

            // ¿Ya podemos apagar este filtro?
            synchronized (globalControl) {
                if (globalControl.systemCanShutdown()) {
                    running = false;
                }
            }
        }

        System.out.println(getName() + " terminó (filtro de spam).");
    }

    private void handleNormal(Message msg) {
        if (msg.isSpam()) {
            // mensaje va a cuarentena con TTL aleatorio [10000..20000]
            msg.setQuarantineTTL(10000 + (int)(Math.random() * 10001));
            quarantineMailbox.add(msg);
        } else {
            // mensaje válido -> va al buzón de entrega
            deliveryMailbox.put(msg);
        }
    }

    private void handleEnd(Message msg) {
        // Registrar que este cliente ya terminó
        globalControl.registerClientEnd();

        // Enviar END a cuarentena para que eventualmente se cierre
        Message quarantineEnd = new Message(
                Message.MessageType.END,
                "END-from-cliente" + msg.getSenderClientId(),
                msg.getSenderClientId(),
                false
        );
        quarantineEnd.setQuarantineTTL(0);
        quarantineMailbox.add(quarantineEnd);
    }

    private void maybeSendGlobalEnd() {
        synchronized (globalControl) {
            if (!globalControl.allClientsFinished()) return;
            synchronized (inputMailbox) {
                synchronized (quarantineMailbox) {
                    synchronized (deliveryMailbox) {
                        if (!inputMailbox.isEmpty()) return;
                        if (!quarantineMailbox.isEmpty()) return;
                        if (globalControl.isDeliveryFinalEndSent()) return;

                        // crear END global para los servidores de entrega
                        Message finalEnd = new Message(
                                Message.MessageType.END,
                                "GLOBAL_END",
                                -1,
                                false
                        );
                        deliveryMailbox.put(finalEnd);

                        // marcar que ya enviamos el END global
                        globalControl.markDeliveryFinalEndSent();
                        deliveryMailbox.markBroadcastFinalEnd();
                    }
                }
            }
        }
    }
}
