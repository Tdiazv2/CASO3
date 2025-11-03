public class GlobalControl {

    private final int totalClients;
    private int finishedClients = 0;
    private boolean finalEndSentToDelivery = false;
    private boolean quarantineEnded = false;
    private boolean quarantineEndSent = false;

    public GlobalControl(int totalClients) {
        this.totalClients = totalClients;
    }

    public synchronized void registerClientEnd() {
        finishedClients++;
        notifyAll();
    }

    public synchronized boolean allClientsFinished() {
        return finishedClients >= totalClients;
    }

    public synchronized void markDeliveryFinalEndSent() {
        finalEndSentToDelivery = true;
        notifyAll();
    }

    public synchronized boolean isDeliveryFinalEndSent() {
        return finalEndSentToDelivery;
    }

    public synchronized void markQuarantineEnded() {
        quarantineEnded = true;
        notifyAll();

    }

    public synchronized boolean isQuarantineEnded() {
        return quarantineEnded;
    }

    // condición de "ya todo acabó"
    public synchronized boolean systemCanShutdown() {
        return allClientsFinished()
                && finalEndSentToDelivery
                && quarantineEnded;
    }

    public synchronized boolean isQuarantineEndSent() {
        return quarantineEndSent;
    }

    public synchronized void markQuarantineEndSent() {
        quarantineEndSent = true;
    }
}

