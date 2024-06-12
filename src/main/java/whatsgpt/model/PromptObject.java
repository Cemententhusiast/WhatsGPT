package whatsgpt.model;

import com.google.gson.JsonElement;

import java.util.List;

public class PromptObject {
    private String model;
    private List<JsonElement> messages;

    public PromptObject(String model, List<JsonElement> messages) {
        setMessages(messages);
        setModel(model);
    }

    /**
     * Adds a message to the PromptObject.messages List in the form of a JsonElement
     * Throws a RuntimeException if null
     *
     * @param message The message to be added (JsonElement)
     */
    public void addMessages(JsonElement message){
        if (message == null){
            throw new RuntimeException("Invalid message");
        }
        messages.add(message);
    }

    /**
     * Removes messages from the PromptObject.messages List by the given index.
     *
     * @param index The index of the Element to be deleted
     *              (leave 0 if planning to delete all)
     * @param all Indicates if you wish to clear the entire List (except for the personality) or not
     *            true = list will be cleared (index ignored)
     *            false = only the given index will be cleared
     */
    public void removeMessages(int index, boolean all){
        if (all){
            JsonElement pers = messages.get(0);
            messages.clear();
            messages.add(pers);
        }else if (index > 0 && index<messages.size()){
            messages.remove(index);
        }else {
            throw new RuntimeException("Attempting to remove illegaly");
        }
    }

    public List<JsonElement> getMessages() {
        return messages;
    }

    public void setMessages(List<JsonElement> messages) {
        this.messages = messages;
    }

    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Converts the Object into a human-readable abstract String
     * (Debugging purposes only)
     * @return String containing the abstracted Object
     */
    @Override
    public String toString() {
        return "\nPromptObject{" +
                "model='" + model + '\'' +
                ", messages=" + messages +
                "}\n";
    }
}
