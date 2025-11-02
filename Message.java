public class Message {

    public enum MessageType {
        START,   // mensaje de inicio
        NORMAL,  // correo normal
        END      // mensaje de fin
    }

    private final MessageType type;
    private final String id;          // ejemplo: "cliente2-msg5"
    private final int senderClientId; // quién lo generó
    private final boolean isSpam;     // flag de spam
    private int quarantineTTL;        // tiempo restante en cuarentena

    public Message(MessageType type, String id, int senderClientId, boolean isSpam) {
        this.type = type;
        this.id = id;
        this.senderClientId = senderClientId;
        this.isSpam = isSpam;
        this.quarantineTTL = 0;
    }

    public MessageType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public int getSenderClientId() {
        return senderClientId;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public int getQuarantineTTL() {
        return quarantineTTL;
    }

    public void setQuarantineTTL(int quarantineTTL) {
        this.quarantineTTL = quarantineTTL;
    }

    public void tickTTL() {
        if (quarantineTTL > 0) quarantineTTL--;
    }

    public boolean isTTLExpired() {
        return quarantineTTL <= 0;
    }

    @Override
    public String toString() {
        return "[Message " + id +
               " type=" + type +
               " spam=" + isSpam +
               " ttl=" + quarantineTTL +
               "]";
    }
}
