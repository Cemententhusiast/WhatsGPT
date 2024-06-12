package whatsgpt.model;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import whatsgpt.view.Main;

/**
 * Handles all communication between the app and the ChatGPT API
 */
public class MessageHandler {
    private static final String url = "https://api.openai.com/v1/chat/completions";
    private static final String apiKey = Main.user.getApiKey();

    /**
     * Handles the sending and receiving of ChatGPT messages in Form of HTTP requests and returns the reply with the relevant message part extracted
     *
     * @param prompt The formed prompt to be sent to ChatGPT. Includes the model, API-Key, as well as previous messages with the character and their personality
     * @return The extracted message part of the ChatGPT-API reply.
     */
    public static JsonElement communication(String prompt){
        try {
            //Starts the connection with ChatGPT
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            //Sends our formed Request
            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            //System.out.println("chatgpt--------------\n" + prompt);
            writer.write(prompt);
            writer.flush();
            writer.close();

            // Response from ChatGPT
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;

            StringBuilder response = new StringBuilder();

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            //Close the scanner
            br.close();

            System.out.println(JsonParser.parseString(response.toString()).getAsJsonObject().get("usage"));
            //Get the required object from the above created object
            return JsonParser
                    .parseString(response.toString())
                    .getAsJsonObject()
                    .get("choices")
                    .getAsJsonArray()
                    .get(0)
                    .getAsJsonObject()
                    .get("message");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
