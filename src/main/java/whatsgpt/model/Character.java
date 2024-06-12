package whatsgpt.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import whatsgpt.view.Main;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Main class for Serialization and Handling of Characters
 */
public class Character {
    private String name;
    private transient JsonElement personalityPrompt;
    private int maxMessages;
    private static final String model = "gpt-3.5-turbo";
    private PromptObject prompt;
    public List<JsonElement> messages;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Character(String name, String personality, int maxMessages){
        setName(name);
        setPersonality(personality);
        setMaxMessages(maxMessages);

        this.messages = new LinkedList<>();
        messages.add(personalityPrompt);
        this.prompt = new PromptObject(model, messages);

    }

    /**
     * Middleman between the GUI and the API,
     * handles conversion of prompts into API-accepted Json formats,
     * as well as receiving and abstracting the responses
     *
     * @param character The character, which is currently selected within the GUI and is being communicated with
     * @param message The plaintext User-created message inputted into the GUI Textfield
     * @return The fully extracted response-message from the ChatGPT-API
     */
    public static String runInFX(Character character, String message){
        //converts the inputted message into the proper Json format with the role of User
        JsonElement newM = convertMessageToJsonElement(message, "user");
        character.addMessage(newM);

        //checks for the messages being doubled up (error that occurs when a character has been deserialized)
        boolean repeater = false;
        if (!character.prompt.getMessages().get(character.prompt.getMessages().size()-1).equals(newM)){
            character.prompt.addMessages(newM);
            repeater = true;
        }

        //generates the full Chat prompt to be sent
        String toSend = character.getNewPrompt();
        //sends the prompt, as well as receives the reply from the API in a mostly extracted Json format
        JsonElement response = MessageHandler.communication(toSend);
        //same doubling up issue as mentioned above
        character.addMessage(response);
        if (repeater){
            character.prompt.addMessages(response);
        }
        return response.getAsJsonObject().get("content").getAsString();
    }

    /**
     * Serializes the entire Character (as far as relevant) into a Json format
     *
     * @param path Location, where the serialized .json will be saved
     *             Default = "user.home + /whatsgpt/logs/"
     */
    public void serialize(File path){
        try {
            File tempFile;
            if (!getPath().exists()){
                if(!getPath().mkdirs()){
                    throw new RuntimeException("could not create file");
                }
            }
            if (path.isDirectory()){
                tempFile= new File(path + "/" + getName() + ".json");
            }else {
                tempFile= new File(path.getParent()+"/"+getName() + ".json");
            }
            FileWriter fw = new FileWriter(tempFile, false);
            fw.write(gson.toJson(this));
            fw.close();
        }catch (IOException ioex){
            throw new RuntimeException(ioex);
        }

    }

    /**
     * Used to deserialize only the Names of all the saves Characters by looping through the /logs/ directory and extracting the filenames
     * Only called for the previews on startup of the app (before clicking on a character)
     *
     * @param path Location of the whatsgpt/logs/ folder
     *             Default = "user.home"
     * @return String Array containing all filenames (excluding the .json attachment)
     */
    public static String[] deserializeAllNames(File path){
        if (path.isDirectory()){
            File[] fileArray = path.listFiles();
            String[] toReturn = new String[fileArray.length];
            for (int i = 0; i< fileArray.length; i++){
                File f = fileArray[i];
                toReturn[i] = f.getName().substring(0,f.getName().length()-5);
            }
            return toReturn;
        }else {
            return null;
        }
    }

    public static Character deserialize(File path, String name){
        if (path.isDirectory()){
            try {
                File[] fileArray = path.listFiles();
                Character toReturn;
                for (File file : fileArray) {
                    if (file.getName().substring(0,file.getName().length()-5).equals(name)) {
                        Scanner sc = new Scanner(file);
                        StringBuilder temp = new StringBuilder();
                        while (sc.hasNextLine())
                            temp.append(sc.nextLine());
                        sc.close();
                        toReturn = gson.fromJson(temp.toString(), Character.class);
                        return toReturn;
                    }
                }
            } catch (IOException iox){
                throw new RuntimeException(iox);
            }
        }else {
            return null;
        }
        return null;
    }

    /**
     * Used to delete a Character using the "-" Key in the GUI
     * Deletes the file from the system irrevocably.
     *
     * @param name Name of the character to be deleted
     */
    public static void deleteChar(String name){
        File toDel = new File(getPath()+"/"+name+".json");
        if (!toDel.delete()){
            throw new RuntimeException("Could not delete character");
        }
    }

    /**
     * Reformats the prompt array in order to adhere to the given maximum size of a message
     *
     * @return The Json formatted prompt of the given length to be used to be sent to the ChatGPT-API
     */
    public String getNewPrompt(){
        if (messages.size()>maxMessages){
            prompt.removeMessages(1, false);
            prompt.removeMessages(2, false);
        }
        return new Gson().toJson(prompt);
    }

    public void fillPromptAnew(){
        prompt.removeMessages(0, true);
        if (messages.size()>maxMessages){
            for (int i = 0; i < maxMessages; i++) {
                prompt.addMessages(messages.get(maxMessages-i));
            }
        }else {
            for (JsonElement message : messages){
                prompt.addMessages(message);
            }
        }
    }

    public void addMessage(JsonElement message){
        messages.add(message);
    }

    public static JsonElement convertMessageToJsonElement(String message, String role){
        return new Gson().toJsonTree(new MessageObject(role, message));
    }

    private void generatePersonality() {
        JsonElement message = new Gson().toJsonTree(new MessageObject("system", String.format("Your name is %s. Generate yourself a personality based on that name", getName())));
        String prompt = new Gson().toJson(new PromptObject(model, List.of(message)));
        String response = MessageHandler.communication(prompt).toString().split("\"content\":\"")[1];
        response = response.substring(0, response.length()-2);
        setPersonality(response);
    }

    private void setPersonality(String personality){
        if (personality == null || personality.isBlank()){
            generatePersonality();
        }else {
            this.personalityPrompt = new Gson().toJsonTree(new MessageObject("system", String.format("Your name is %s. Your are to behave in this manner, you pretend to be a real person who is friends with me (my name is %s) and not an AI: %s", getName(), Main.user.getUserName(), personality)));
        }
    }

    private void setMaxMessages(int max){
        if (max<=5){
            throw new RuntimeException("Too few messages to remember");
        }
        this.maxMessages=max;
    }

    private void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Bad name");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage(){
        String lastMessage = messages.get(messages.size()-1).getAsJsonObject().get("content").toString();
        if (lastMessage.length()>30){
            lastMessage = lastMessage.substring(1,30);
            return lastMessage+"...";
        }else {
            return lastMessage.substring(1,lastMessage.length()-1);
        }
    }

    public static boolean checkIfPathExists(){
        File homeDir = new File(System.getProperty("user.home") + "/whatsGPT/logs/");
        return homeDir.exists();
    }

    public static File getPath(){
        return new File(System.getProperty("user.home") + "/whatsGPT/logs/");
    }

    @Override
    public String toString() {
        return "Character{" +"\n" +
                "name='" + name + '\'' + "\n" +
                ", personalityPrompt=" + personalityPrompt +"\n" +
                ", maxMessages=" + maxMessages +"\n" +
                ", prompt=" + prompt +"\n" +
                ", messages=" + messages +"\n" +
                '}';
    }
}
