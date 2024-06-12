package whatsgpt.view;

import com.google.gson.JsonElement;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import whatsgpt.controller.ProfileController;
import whatsgpt.controller.SettingsController;
import whatsgpt.model.Character;
import whatsgpt.model.User;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Hauptklasse der Anwendung, die das Hauptfenster und die Benutzeroberfläche initialisiert.
 */
public class Main extends Application {
    private final Map<String, VBox> chatWindows = new HashMap<>();
    private final Map<String, ObservableList<Message>> chatMessages = new HashMap<>();
    private VBox chatBox;
    private VBox leftPane;
    private TextField chatInput;
    private Label noChatSelectedLabel;
    private VBox rightPane;
    private ScrollPane scrollPane;
    private ListView<Chat> chatList;
    private ObservableList<Chat> items;
    private Button removeButton;
    private Scene mainScene, settingsScene, profileScene;
    private Stage primaryStage;
    private HashMap<String, Character> characterList;
    private Character character;
    public static User user = new User("User", "123456789");

    /**
     * Startmethode der JavaFX-Anwendung.
     *
     * @param stage Hauptbühne der Anwendung
     */
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        characterList = new HashMap<>();
        BorderPane root = new BorderPane();

        // Seitenleiste mit Schaltflächen
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(10));
        sidebar.setSpacing(10);
        sidebar.setStyle("-fx-background-color: #075E54;");

        // Profil-, Einstellungs- und andere Schaltflächen
        InputStream chatsIcon = getClass().getResourceAsStream("/whatsgpt/chatsicon.png");
        InputStream statusIcon = getClass().getResourceAsStream("/whatsgpt/statusicon.png");
        InputStream settingsIcon = getClass().getResourceAsStream("/whatsgpt/settings.png");
        InputStream appLogo = getClass().getResourceAsStream("/whatsgpt/whatsgpt-logo.png");


        Button chatsButton = createSidebarButton("Chats", chatsIcon);
        Button profileButton = createSidebarButton("Profile", statusIcon);
        Button settingsButton = createSidebarButton("Settings", settingsIcon);



        try {
            stage.getIcons().add(new Image(appLogo));
        }catch (NullPointerException nullpt){
            //we don't load anything
        }

        profileButton.setOnAction(e -> showProfile());
        settingsButton.setOnAction(e -> showSettings());

        // Chats-Button ganz oben, Profil- und Einstellungs-Button ganz unten
        sidebar.getChildren().addAll(chatsButton, new Spacer(), profileButton, settingsButton);

        User newUser = User.deserialize(new File(User.getPath() + "/user.json"));
        boolean isTheUserNew;
        if (newUser!=null){
            user = newUser;
            isTheUserNew = false;
        }else {
            isTheUserNew = true;
        }

        // Linke Seite - Liste der Chats, Suche, Add-Button und Remove-Button
        leftPane = new VBox();
        leftPane.setPadding(new Insets(10));
        leftPane.setSpacing(10);
        leftPane.setStyle("-fx-background-color: #ECE5DD;");

        chatList = new ListView<>();
        items = FXCollections.observableArrayList();
        chatList.setItems(items);
        chatList.setCellFactory(param -> new ChatListCell());
        VBox.setVgrow(chatList, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search or start new Chat");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            chatList.setItems(FXCollections.observableArrayList(
                    items.stream().filter(chat -> chat.getName().toLowerCase().contains(newValue.toLowerCase())).toList()
            ));
            chatList.getSelectionModel().clearSelection();  // Auswahl beim Klicken auf das Suchfeld löschen
        });

        Button addButton = new Button("+");
        addButton.setMaxWidth(Double.MAX_VALUE); // Make button full width
        HBox.setHgrow(addButton, Priority.ALWAYS); // Ensure it resizes with the search field

        String[] characterNames = Character.deserializeAllNames(Character.getPath());

        if (characterNames != null) {
            for (String name : characterNames) {
                Image profileImage = new Image(getClass().getResourceAsStream("/whatsgpt/profileicon.jpg"));
                Chat newChat = new Chat(name, profileImage, "...");
                items.add(newChat);
                chatWindows.put(name, new VBox());
            }
        }

        //Determines what happens whenever you press the "+" Character add Button
        addButton.setOnAction(event -> {
            Dialog<Map<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Add new Chat");

            // Set the button types
            ButtonType addButtonType = new ButtonType("Add Chat", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            // Create the chat name and message limit fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField chatNameField = new TextField();
            chatNameField.setPromptText("Chat-Name");
            TextField messageLimitField = new TextField();
            messageLimitField.setPromptText("Message memory depth");

            // Ensure only numbers are entered in the message limit field
            messageLimitField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    messageLimitField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });

            grid.add(new Label("Chat-Name:"), 0, 0);
            grid.add(chatNameField, 1, 0);
            grid.add(new Label("Message memory depth:"), 0, 1);
            grid.add(messageLimitField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // Convert the result to a chat name and message limit pair when the add button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    Map<String, String> result = new HashMap<>();
                    result.put("chatName", chatNameField.getText());
                    result.put("messageLimit", messageLimitField.getText());
                    return result;
                }
                return null;
            });

            Optional<Map<String, String>> result = dialog.showAndWait();

            result.ifPresent(chatData -> {
                String chatName = chatData.get("chatName");
                String messageLimitStr = chatData.get("messageLimit");
                int messageLimit = Integer.parseInt(messageLimitStr);
                if (user.getApiKey().equals("123456789")){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Missing API-Key!");
                    alert.setHeaderText("Placeholder API-Key found!");
                    alert.setContentText("Please enter a ChatGPT API-Key in the settings!");
                    alert.showAndWait();
                }else if (chatName != null && !chatName.trim().isEmpty() && messageLimit>5 && !messageLimitStr.trim().isEmpty() && !chatWindows.containsKey(chatName)) {
                    String imagePath = new File("src/main/resources/whatsgpt/profileicon.jpg").getAbsolutePath();
                    Image profileImage = new Image(imagePath);
                    Chat newChat = new Chat(chatName, profileImage, "Hey there! I am using WhatsGPT.");
                    items.add(newChat);
                    chatWindows.put(chatName, new VBox());
                    chatMessages.put(chatName, FXCollections.observableArrayList());
                    chatList.setItems(FXCollections.observableArrayList(items)); // Chatliste aktualisieren
                    characterList.put(chatName, new Character(chatName, null, messageLimit)); // Erstellt neuen Charakter mit der angegebenen Nachrichtenanzahl
                    this.character = characterList.get(chatName);

                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Input!");
                    alert.setHeaderText("Invalid Chat-Name or message limit");
                    alert.setContentText("Please enter a valid chat name and message limit (minimum of 6)." +
                            "\n(Duplicate Character names are invalid)");
                    alert.showAndWait();
                }
            });
        });

        removeButton = new Button("-");
        removeButton.setMaxWidth(Double.MAX_VALUE); // Button volle Breite machen
        removeButton.setDisable(true); // Initial deaktivieren
        HBox.setHgrow(removeButton, Priority.ALWAYS); // Sicherstellen, dass er mit dem Suchfeld skaliert

        removeButton.setOnAction(event -> {
            Chat selectedChat = chatList.getSelectionModel().getSelectedItem();
            if (selectedChat != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete chat");
                alert.setHeaderText("Are you sure you want to delete the selected chat?");
                alert.setContentText("Chat: " + selectedChat.getName());
                try{
                    Character.deleteChar(selectedChat.getName());
                }catch (RuntimeException rex){
                    //we ignore it
                }
                characterList.remove(selectedChat.getName());

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    items.remove(selectedChat);
                    chatWindows.remove(selectedChat.getName());
                    chatMessages.remove(selectedChat.getName());
                    chatList.setItems(FXCollections.observableArrayList(items)); // Chatliste aktualisieren
                    closeChatWindow();
                }
            }
        });

        chatList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            removeButton.setDisable(newValue == null);
        });

        VBox.setVgrow(chatList, Priority.ALWAYS);
        leftPane.getChildren().addAll(searchField, addButton, removeButton, chatList);

        // Rechte Seite - Chat Fenster und Eingabefeld
        rightPane = new VBox();
        rightPane.setPadding(new Insets(10));
        rightPane.setStyle("-fx-background-color: #ECE5DD;");
        rightPane.setMinWidth(600);
        rightPane.setMaxWidth(1400);

        scrollPane = new ScrollPane();
        chatBox = new VBox(10);  // Setze den Abstand zwischen Nachrichten
        chatBox.setPadding(new Insets(10));
        scrollPane.setContent(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #ECE5DD;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        chatInput = new TextField();
        chatInput.setPromptText("Enter message:");
        chatInput.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: black;");

        noChatSelectedLabel = new Label("No chat selected");
        noChatSelectedLabel.setStyle("-fx-text-fill: black; -fx-font-size: 16px;");
        noChatSelectedLabel.setAlignment(Pos.CENTER);
        noChatSelectedLabel.setPadding(new Insets(20));

        chatInput.setOnAction(event -> {
            String newMessage = chatInput.getText();
            if (!newMessage.isEmpty()) {
                try {
                    addMessageToCurrentChat(newMessage, true);
                    addMessageToCurrentChat(Character.runInFX(character, newMessage), false);
                }catch (RuntimeException rex){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("API Error!");
                    alert.setHeaderText("Attempt to get a connection with the API failed");
                    alert.setContentText("Please ensure you have entered a valid ChatGPT API-Key!\n" +
                            "(After entering a valid API-Key, please restart the program and try again)");
                    alert.showAndWait();
                }

                chatInput.clear();
                character.serialize(Character.getPath());

                // Nach dem Hinzufügen einer neuen Nachricht zum unteren Rand scrollen
                Timeline scrollTimeline = new Timeline(new KeyFrame(Duration.seconds(0.1), e -> scrollPane.setVvalue(1.0)));
                scrollTimeline.play();
            }
        });

        rightPane.getChildren().addAll(noChatSelectedLabel);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftPane, rightPane);
        splitPane.setDividerPositions(0.3);
        splitPane.setStyle("-fx-background-color: #ECE5DD;");




        root.setLeft(sidebar);
        root.setCenter(splitPane);

        chatList.setOnMouseClicked(event -> {
            Chat selectedChat = chatList.getSelectionModel().getSelectedItem();
            if (selectedChat != null) {
                loadChatWindow(selectedChat.getName());
                chatInput.requestFocus(); // Fokus auf Chat-Eingabefeld setzen, wenn Chat ausgewählt ist
            } else {
                closeChatWindow();
            }
        });

        searchField.setOnMouseClicked(event -> {
            chatList.getSelectionModel().clearSelection(); // Clear selection when search field is clicked
            closeChatWindow();
        });

        mainScene = new Scene(root, 1920, 1000);
        stage.setScene(mainScene);
        stage.setTitle("WhatsGPT");
        stage.show();

        if (isTheUserNew){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Welcome to WhatsGPT!");
            alert.setContentText("Please enter your name in the PROFIL tab\n" +
                    "Then, please enter a ChatGPT API-Key into the SETTINGS tab");
            alert.setHeaderText("A few steps before you are all set up:");
            alert.showAndWait();
        }

        // Initialisieren der Profile- und Einstellungsszenen
        settingsScene = new Scene(new SettingsController(this).getSettingsPane(), 1920, 1000);
        profileScene = new Scene(new ProfileController(this).getProfilePane(), 1920, 1000);
    }

    /**
     * Schließt das aktuelle Chatfenster.
     */
    private void closeChatWindow() {
        rightPane.getChildren().clear();
        rightPane.getChildren().add(noChatSelectedLabel);
    }

    /**
     * Erstellt eine Schaltfläche für die Seitenleiste mit einem Symbol und Text.
     *
     * @param text     Der Text der Schaltfläche
     * @param iconPath Pfad zum Symbolbild
     * @return Die erstellte Schaltfläche
     */
    private Button createSidebarButton(String text, InputStream iconPath) {
        Button button = new Button(text);
        ImageView icon = new ImageView(new Image(iconPath));
        icon.setFitHeight(20);
        icon.setFitWidth(20);
        button.setGraphic(icon);
        button.setStyle("-fx-background-color: #075E54; -fx-text-fill: white;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #128C7E; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #075E54; -fx-text-fill: white;"));
        return button;
    }

    /**
     * Lädt das Chatfenster für einen bestimmten Chat.
     *
     * @param chatName Der Name des Chats
     */
    private void loadChatWindow(String chatName) {
        chatBox.getChildren().clear();
        if (!characterList.containsKey(chatName)){
            loadCharacter(chatName);
        }
        ObservableList<Message> messages = chatMessages.get(chatName);
        for (Message message : messages) {
            addMessageToChatBox(message);
            loadCharacter(chatName);
            this.character = characterList.get(chatName);
            this.character.fillPromptAnew();
        }
        rightPane.getChildren().clear();
        rightPane.getChildren().addAll(scrollPane, chatInput);
    }

    /**
     * Fügt eine Nachricht zum aktuellen Chat hinzu.
     *
     * @param messageText Der Text der Nachricht
     * @param isSent      Gibt an, ob die Nachricht gesendet oder empfangen wurde
     */
    private void addMessageToCurrentChat(String messageText, boolean isSent) {
        Chat selectedChat = chatList.getSelectionModel().getSelectedItem();
        if (selectedChat != null) {
            String chatName = selectedChat.getName();
            Message newMessage = new Message(messageText, isSent);
            chatMessages.get(chatName).add(newMessage);
            addMessageToChatBox(newMessage);

            // Aktualisiere die Vorschau der letzten Nachricht
            selectedChat.setLastMessage(messageText);
            chatList.refresh();
        }
    }

    /**
     * Fügt eine Nachricht zur Chatbox hinzu.
     *
     * @param message Die Nachricht, die hinzugefügt werden soll
     */
    private void addMessageToChatBox(whatsgpt.view.Message message) {
        Text messageText = new Text(message.getText());
        messageText.setFill(Color.BLACK);
        messageText.setTextAlignment(TextAlignment.LEFT);

        TextFlow textFlow = new TextFlow(messageText);
        textFlow.setPadding(new Insets(8));
        textFlow.setStyle(message.isSent() ?
                "-fx-background-color: #DCF8C6; -fx-background-radius: 10;" :
                "-fx-background-color: #FFFFFF; -fx-background-radius: 10;");

        // Binding the wrapping width to the chatBox width minus some padding
        textFlow.maxWidthProperty().bind(chatBox.widthProperty().subtract(20));

        HBox messageBox = new HBox(textFlow);
        messageBox.setAlignment(message.isSent() ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        chatBox.getChildren().add(messageBox);
    }


    /**
     * Lädt einen Charakter basierend auf dem Namen.
     *
     * @param name Der Name des Charakters
     */
    public void loadCharacter(String name) {
        if (Character.checkIfPathExists()) {
            Character character = Character.deserialize(Character.getPath(), name);
            assert character != null;
            String chatName = character.getName();
            for (Chat current : items) {
                if (current.getName().equals(name)) {
                    current.setLastMessage(character.getLastMessage());
                    break;
                }
            }


            chatWindows.put(character.getName(), new VBox());
            chatMessages.put(chatName, convertToObservable(character.messages));
            chatList.setItems(FXCollections.observableArrayList(items)); // Refresh chat list
            characterList.put(chatName, character);
            this.character = character;

        }
    }

    /**
     * Zeigt die Einstellungsszene an.
     */
    public void showSettings() {
        primaryStage.setScene(settingsScene);
    }

    /**
     * Zeigt die Profilszene an.
     */
    public void showProfile() {
        primaryStage.setScene(profileScene);
    }

    /**
     * Zeigt die Hauptszene an.
     */
    public void showMainScene() {
        primaryStage.setScene(mainScene);
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Konvertiert eine Liste von JsonElementen in eine ObservableList von Nachrichten.
     *
     * @param list Die Liste von JsonElementen
     * @return Die ObservableList von Nachrichten
     */
    public static ObservableList<Message> convertToObservable(List<JsonElement> list){
        ObservableList<Message> toReturn = FXCollections.observableArrayList();
        list.remove(0);
        for (JsonElement message : list){
            boolean isSent;
            isSent = !message.getAsJsonObject().get("role").toString().equals("\"assistant\"");
            Message messages = new Message(message.getAsJsonObject().get("content").toString().substring(1, message.getAsJsonObject().get("content").toString().length()-1), isSent);
            toReturn.add(messages);
        }

        return toReturn;
    }

    /**
     * Hilfsklasse für Abstände.
     */
    static class Spacer extends Region {
        public Spacer() {
            javafx.scene.layout.VBox.setVgrow(this, Priority.ALWAYS);
        }
    }
}
