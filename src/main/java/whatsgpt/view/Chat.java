package whatsgpt.view;

import javafx.scene.image.Image;

/**
 * Repräsentiert einen Chat mit einem Namen, Profilbild und der letzten Nachricht.
 */
public class Chat {
    private final String name;
    private final Image profileImage;
    private String lastMessage;

    /**
     * Erstellt eine neue Instanz von Chat.
     *
     * @param name         der Name des Chats
     * @param profileImage das Profilbild des Chats
     * @param lastMessage  die letzte Nachricht im Chat
     */
    public Chat(String name, Image profileImage, String lastMessage) {
        this.name = name;
        this.profileImage = profileImage;
        this.lastMessage = lastMessage;
    }

    public String getName() {
        return name;
    }

    public Image getProfileImage() {
        return profileImage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        if (lastMessage.length()>30){
            lastMessage = lastMessage.substring(0, 30)+"...";
        }
        this.lastMessage = lastMessage;
    }
}
