import java.util.List;

public class QuarantineManager extends Thread {

    private final QuarantineMailbox quarantineMailbox;
    private final DeliveryMailbox deliveryMailbox;
    private final GlobalControl globalControl;

    public QuarantineManager(QuarantineMailbox quarantineMailbox,
                             DeliveryMailbox deliveryMailbox,
                             GlobalControl globalControl) {
        super("QuarantineManager");
        this.quarantineMailbox = quarantineMailbox;
        this.deliveryMailbox = deliveryMailbox;
        this.globalControl = globalControl;
    }

    @Override
    public void run() {
        boolean running = true;

        while (running) {

            // corre cada segundo
            try {
                Thread.sleep(1500); // espera semiactiva
                System.out.println(getName() + " revisa cuarentena...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // revisar cuarentena
            List<Message> ready = quarantineMailbox.reviewAndCollectReady();

            for (Message m : ready) {
                if (m.getType() == Message.MessageType.END) {
                    // señal de que cuarentena debe cerrar
                    System.out.println(getName() + " recibió END de cuarentena.");
                    globalControl.markQuarantineEnded();
                } else {
                    // pasar mensaje "limpio" al buzón de entrega
                    System.out.println(getName() + " libera " + m + " de cuarentena -> entrega.");
                    deliveryMailbox.put(m);
                }
            }

            // seguridad: si ya marcamos fin de cuarentena, apagamos
            //System.out.println(quarantineMailbox);
            if (globalControl.isQuarantineEnded() && quarantineMailbox.isEmpty()) {
                running = false;
            }


        }

        System.out.println(getName() + " terminó (manejador de cuarentena).");
    }
}
