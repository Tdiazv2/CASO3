public class ClientSender extends Thread {

    private final int clientId;
    private final int messagesToGenerate;
    private final InputMailbox inputMailbox;

    public ClientSender(int clientId,
                        int messagesToGenerate,
                        InputMailbox inputMailbox) {
        super("ClientSender-" + clientId);
        this.clientId = clientId;
        this.messagesToGenerate = messagesToGenerate;
        this.inputMailbox = inputMailbox;
    }

    @Override
    public void run() {
        System.out.println(getName() + " inició envío.");

        // START
        Message startMsg = new Message(
                Message.MessageType.START,
                "cliente" + clientId + "-START",
                clientId,
                false
        );
        inputMailbox.put(startMsg);
        System.out.println(getName() + " envió START.");

        // correos normales
        for (int i = 1; i <= messagesToGenerate; i++) {
            boolean spamFlag = Math.random() < 0.3;
            Message normal = new Message(
                    Message.MessageType.NORMAL,
                    "cliente" + clientId + "-msg" + i,
                    clientId,
                    spamFlag
            );
            inputMailbox.put(normal);
            System.out.println(getName() + " envió " + normal);
            Thread.yield(); // ceder CPU para permitir que los filtros actúen
        }

        // END
        Message endMsg = new Message(
                Message.MessageType.END,
                "cliente" + clientId + "-END",
                clientId,
                false
        );
        inputMailbox.put(endMsg);
        System.out.println(getName() + " envió END y terminó.");
    }
}