package net.theakingdom.ranks;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public final class Ranks extends JavaPlugin {
    MongoClient mongoClient;
    public MongoClient getMongoClient(){
        return mongoClient;
    }
    MongoDatabase db;
    public MongoCollection coll;
    public MongoCollection playerColl;

    public MongoCollection getColl(){
        return coll;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        File pwFile = new File("plugins/Ranks/password.txt");
        Scanner reader = null;
        try {
            reader = new Scanner(pwFile);
            String endData = "";
            while(reader.hasNextLine()){
                String data = reader.nextLine();
                endData += data;
            } //Fuck you mongodb
            mongoClient = MongoClients.create("mongodb://127.0.0.1:27017");
            db = mongoClient.getDatabase("theakingdom");
            coll = db.getCollection("ranks");
            playerColl = db.getCollection("players");
        } catch (FileNotFoundException e) {
            System.out.println("File password.txt was not found.");
        }
        getCommand("rank").setExecutor(new Rank(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
