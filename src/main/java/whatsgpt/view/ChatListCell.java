package whatsgpt.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Benutzerdefinierte Listenzelle zur Anzeige von Chat-Informationen.
 */
public class ChatListCell extends ListCell<whatsgpt.view.Chat> {
    private final HBox content;
    private final ImageView profileImageView;
    private final VBox vbox;
    private final Label nameLabel;
    private final Label lastMessageLabel;

    public ChatListCell() {
        profileImageView = new ImageView();
        profileImageView.setFitWidth(50);
        profileImageView.setFitHeight(50);

        nameLabel = new Label();
        nameLabel.setStyle("-fx-font-weight: bold;");

        lastMessageLabel = new Label();

        vbox = new VBox(nameLabel, lastMessageLabel);
        vbox.setSpacing(5);

        content = new HBox(profileImageView, vbox);
        content.setSpacing(10);
        content.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(Chat item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
        } else {
            profileImageView.setImage(item.getProfileImage());
            nameLabel.setText(item.getName());
            lastMessageLabel.setText(item.getLastMessage());
            setGraphic(content);
        }
    }
}
