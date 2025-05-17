package com.example.kutowerdefense;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Enemy {
    private int goldReward;
    private String enemyType;

    public Enemy(String enemyType) {
        this.enemyType = enemyType;
        setGoldReward();
    }

    private void setGoldReward() {
        try {
            // Read the options.json file
            String jsonContent = new String(Files.readAllBytes(Paths.get("resources/Options/options.json")));
            JsonObject options = JsonParser.parseString(jsonContent).getAsJsonObject();
            
            // Get the enemy stats from the options
            JsonObject enemyStats = options.getAsJsonObject("enemyStats");
            
            // Get the gold reward for this specific enemy type
            if (enemyStats.has(enemyType)) {
                JsonObject enemyTypeStats = enemyStats.getAsJsonObject(enemyType);
                this.goldReward = enemyTypeStats.get("goldReward").getAsInt();
            } else {
                System.err.println("Warning: No gold reward found for enemy type: " + enemyType);
                this.goldReward = 0;
            }
        } catch (IOException e) {
            System.err.println("Error reading options.json: " + e.getMessage());
            this.goldReward = 0;
        }
    }

    public int getGoldReward() {
        return goldReward;
    }
} 