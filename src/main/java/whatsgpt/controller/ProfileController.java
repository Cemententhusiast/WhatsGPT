package whatsgpt.controller;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import whatsgpt.view.Main;

import java.io.File;

/**
 * Controller für die Profilansicht.
 */
public class ProfileController {
    private VBox profilePane;
    private ImageView profileImageView;
    private Main mainController;
    private TextField nameInput;
    private Label nameLabel;
    private String userName = "User";

    public ProfileController(Main mainController) {
        this.mainController = mainController;
        initUI();
    }

    /**
     * Creates the UI for the Profile editing page, as well as handling all the interactive elements inside
     */
    private void initUI() {
        profileImageView = new ImageView();
        profileImageView.setFitHeight(100);
        profileImageView.setFitWidth(100);

        Button changeProfileButton = new Button("Change profile image");
        changeProfileButton.setOnAction(e -> changeProfilePicture());

        nameLabel = new Label("Name:");
        nameInput = new TextField(Main.user.getUserName());
        nameInput.setPromptText("Enter your name");
        nameInput.setMaxWidth(200); // Set the maximum width of the input field
        nameInput.setEditable(false); // Initially not editable

        // Make the TextField editable when clicked
        nameInput.setOnMouseClicked(e -> nameInput.setEditable(true));

        // Handle Enter key press to save the name
        nameInput.setOnAction(e -> saveProfileName());

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> mainController.showMainScene());

        profilePane = new VBox(10, profileImageView, changeProfileButton, nameLabel, nameInput, backButton);
        profilePane.setAlignment(Pos.CENTER);
        profilePane.setPadding(new Insets(20));
    }

    /**
     * Handles changing of the Profile picture through a FileChooser-dialogue field
     */
    private void changeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a new profile image");
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            profileImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    /**
     * Takes the inputted new UserName and calls to save it
     * If empty => defaults to "User"
     */
    private void saveProfileName() {
        String newName = nameInput.getText();
        if (newName != null && !newName.trim().isEmpty()) {
            // Hier können Sie den neuen Namen speichern oder weiterverarbeiten
            //System.out.println("Neuer Name: " + newName);
            // Zum Beispiel können Sie den Namen des Benutzers im Hauptcontroller aktualisieren oder speichern
            // mainController.updateProfileName(newName);
            nameInput.setEditable(false); // Make the field non-editable again after saving
            this.userName = newName;
            Main.user.setUserName(userName);
        } else {
            // Fehlerbehandlung, falls der Name leer ist
            nameInput.setText("User"); // Reset to default if empty
            nameInput.setEditable(false); // Make the field non-editable again
        }
    }

    public VBox getProfilePane() {
        return profilePane;
    }
}
