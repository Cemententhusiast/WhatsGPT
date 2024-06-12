package whatsgpt.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import whatsgpt.view.Main;

/**
 * Controller für die Einstellungenansicht.
 */
public class SettingsController {
    private VBox settingsPane;
    private Main mainController;
    private TextField apiKeyInput;
    private Label apiKeyLabel;

    public SettingsController(Main mainController) {
        this.mainController = mainController;
        initUI();
    }

    /**
     * Opens the UI of the Settings page, contains all interactive elements of it
     */
    private void initUI() {
        apiKeyLabel = new Label("API Key:");
        apiKeyInput = new TextField(Main.user.getApiKey());
        apiKeyInput.setPromptText("Please enter your API-Key");
        apiKeyInput.setMaxWidth(400); // Set the maximum width of the input field
        apiKeyInput.setEditable(false); // Initially not editable

        // Make the TextField editable when clicked
        apiKeyInput.setOnMouseClicked(e -> apiKeyInput.setEditable(true));

        // Handle Enter key press to save the API key
        apiKeyInput.setOnAction(e -> saveApiKey());

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> mainController.showMainScene());

        settingsPane = new VBox(10, apiKeyLabel, apiKeyInput, backButton);
        settingsPane.setAlignment(Pos.CENTER);
        settingsPane.setPadding(new Insets(20));
    }

    /**
     * Deserializes a preexisting User
     * (If able to find user.json file)
     *
     * @param path The path, to which the User config will be saved
     *              Default = "user.home"
     * @return The deserialized User
     */
    private void saveApiKey() {
        String apiKey = apiKeyInput.getText();
        if (isValidApiKey(apiKey)) {
            // Hier können Sie den API Key speichern oder weiterverarbeiten
            //System.out.println("API Key gespeichert: " + apiKey);
            // Zum Beispiel können Sie den API Key im Hauptcontroller aktualisieren oder speichern
            // mainController.updateApiKey(apiKey);
            apiKeyInput.setEditable(false); // Make the field non-editable again after saving
            Main.user.setApiKey(apiKey);
        } else {
            // Fehlerbehandlung, falls der API Key ungültig ist
            System.out.println("Invalid API-Key");
            showInvalidApiKeyAlert(); // Show an alert for invalid API key
            apiKeyInput.setText(""); // Reset if invalid
            apiKeyInput.setEditable(false); // Make the field non-editable again
        }
    }

    /**
     * Checks the validity of an API-Key
     * (Very barebones, to be expanded eventually)
     *
     * @param apiKey The unvalidated Key entered by the User
     * @return True = The key passes the checks and appears to be valid
     *          False = The key does not pass the checks and will likely be invalid
     */
    private boolean isValidApiKey(String apiKey) {
        // Einfache Validierung, dass der API Key nicht leer ist und eine bestimmte Länge hat
        // Hier können Sie Ihre eigene Validierungslogik einfügen
        return apiKey != null && apiKey.length() > 10 && !apiKey.contains(" ");
    }

    /**
     * Shows a JavaFX-visible Alert to show the invalidity of the given API-Key
     * (Only triggers if called by saveApiKey())
     */
    private void showInvalidApiKeyAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid API-Key");
        alert.setHeaderText("The entered API-Key may likely be invalid");
        alert.setContentText("Please enter a valid ChatGPT API-Key to be able to use the program");
        alert.showAndWait();
    }

    public VBox getSettingsPane() {
        return settingsPane;
    }
}
