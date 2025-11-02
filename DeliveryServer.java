public class DeliveryServer extends Thread {

    private final int serverId;
    private final DeliveryMailbox deliveryMailbox;
    private boolean running = true;

    public DeliveryServer(int serverId,
                          DeliveryMailbox deliveryMailbox) {
        super("DeliveryServer-" + serverId);
        this.serverId = serverId;
        this.deliveryMailbox = deliveryMailbox;
    }

    @Override
    public void run() {

        while (running) {
            Message msg = null;

           

            // Espera ACTIVA pura: hacemos polling hasta que haya algo
            while (msg == null) {
                synchronized (deliveryMailbox) {
                    msg = deliveryMailbox.takeNonBlocking();
                }
                // aquí NO hacemos yield(), NO dormimos;
                // el hilo se queda pegado preguntando
            }

        
            switch (msg.getType()) {
                case START:
                    System.out.println(getName() + " recibió START");
                    break;

                case NORMAL:
                    processNormal(msg);
                    break;

                case END:
                    System.out.println(getName() + " recibió END -> termina");
                    running = false;
                    break;
            }
        }

        System.out.println(getName() + " terminó (servidor de entrega).");
    }

    private void processNormal(Message m) {
        // simula la entrega con retardo aleatorio
        int delay = 50 + (int)(Math.random() * 200); // 50..250ms
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(getName() + " entregó " + m);
    }
}
