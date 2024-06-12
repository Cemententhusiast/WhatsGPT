package whatsgpt.view;

/**
 * Repräsentiert eine Nachricht in einem Chat.
 */
public class Message {
    private final String text;
    private final boolean isSent;

    /**
     * Erstellt eine neue Instanz von Message.
     *
     * @param text   der Text der Nachricht
     * @param isSent ob die Nachricht vom Benutzer gesendet wurde
     */
    public Message(String text, boolean isSent) {
        this.text = text;
        this.isSent = isSent;
    }

    public String getText() {
        return text;
    }

    public boolean isSent() {
        return isSent;
    }
}
