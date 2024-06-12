package whatsgpt.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Represents the User of the program, used to serialize and deserialize UserNames and API-Keys
 */
public class User {
    private String userName;
    private String apiKey;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public User(String name, String apiKey){
        this.userName = name;
        this.apiKey = apiKey;
    }

    /**
     * Serialized the data Inputted by the User into a .json file
     * Throws a RuntimeException if unable to find or create the needed file
     *
     * @param path The path, to which the User config will be saved
     *             Default = "user.home"
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
                tempFile= new File(path + "/user.json");
            }else {
                tempFile= new File(path.getParent()+"/user.json");
            }
            FileWriter fw = new FileWriter(tempFile, false);
            fw.write(gson.toJson(this));
            fw.close();
            //System.out.println(gson.toJson(this));
        }catch (IOException ioex){
            throw new RuntimeException(ioex);
        }
    }

    /**
     * Deserializes a preexisting User
     * (If able to find user.json file)
     *
     * @param path The path, to which the User config will be saved
     *              Default = "user.home"
     * @return The deserialized User
     */
    public static User deserialize(File path){
        if (path.exists()){
            try {
                User toReturn;
                Scanner sc = new Scanner(path);
                StringBuilder temp = new StringBuilder();
                while (sc.hasNextLine())
                    temp.append(sc.nextLine());
                sc.close();
                toReturn = gson.fromJson(temp.toString(), User.class);
                return toReturn;
                    //System.out.println(toReturn[i].toString()+"\n\n\n");
            } catch (IOException iox){
                throw new RuntimeException(iox);
            }
        }else {
            return null;
        }
    }

    public static File getPath(){
        return new File(System.getProperty("user.home") + "/whatsGPT/");
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        serialize(new File(getPath() + "/user.json"));
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        serialize(new File(getPath() + "/user.json"));
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", apiKey='" + apiKey + '\'' +
                '}';
    }
}
