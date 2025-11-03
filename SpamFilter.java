public class SpamFilter extends Thread {

    private final InputMailbox inputMailbox;
    private final QuarantineMailbox quarantineMailbox;
    private final DeliveryMailbox deliveryMailbox;
    private final GlobalControl globalControl;
    private final int numDeliveryServers;

    public SpamFilter(int id,
                      InputMailbox inputMailbox,
                      QuarantineMailbox quarantineMailbox,
                      DeliveryMailbox deliveryMailbox,
                      GlobalControl globalControl,
                      int numDeliveryServers) {
        super("SpamFilter-" + id);
        this.inputMailbox = inputMailbox;
        this.quarantineMailbox = quarantineMailbox;
        this.deliveryMailbox = deliveryMailbox;
        this.globalControl = globalControl;
        this.numDeliveryServers = numDeliveryServers;
    }

    @Override
    public void run() {
        boolean running = true;

        while (running) {
            // tomar mensaje del buzón de entrada (bloquea si vacío)
            Message msg = inputMailbox.take(); // este take devuelve null si todo terminó
            if (msg != null) {
                switch (msg.getType()) {
                    case START:
                        System.out.println(getName() +
                                " vio START de cliente " + msg.getSenderClientId());
                        deliveryMailbox.put(msg); // START va directo a entrega
                        break;

                    case NORMAL:
                        handleNormal(msg);
                        break;

                    case END:
                        handleEnd(msg);
                        break;
                }
            }

            // Intentar enviar END global (solo una vez)
            maybeSendGlobalEnd();

            // Verificar si ya se puede apagar el filtro
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
            msg.setQuarantineTTL(10000 + (int) (Math.random() * 10001)); // TTL 10000-20000
            System.out.println(getName() + " detectó SPAM " + msg + " (TTL=" + msg.getQuarantineTTL() + ")");
            quarantineMailbox.add(msg);
        } else {
            System.out.println(getName() + " aprobó " + msg + " -> entrega");
            deliveryMailbox.put(msg);
        }
    }

    private void handleEnd(Message msg) {
        globalControl.registerClientEnd();

        // enviar END a cuarentena para que eventualmente se cierre
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
        synchronized (globalControl) { // sincronizamos solo el estado global
            if (globalControl.isDeliveryFinalEndSent() || !globalControl.allClientsFinished()) {
                return; // ya enviado o no todos los clientes terminaron
            }

            // Verificamos buzones
            if (inputMailbox.isEmpty()){// && quarantineMailbox.isEmpty()) {
                Message finalEnd = new Message(
                        Message.MessageType.END,
                        "GLOBAL_END",
                        -1,
                        false
                );
                for (int i = 0; i < numDeliveryServers; i++) {
                    deliveryMailbox.put(finalEnd);
                }

                // marcar que ya enviamos el END global
                globalControl.markDeliveryFinalEndSent();
                deliveryMailbox.markBroadcastFinalEnd();
                System.out.println(getName() + " envió GLOBAL_END al buzón de entrega");
            }
        }
    }
}
