package dev.kastle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PlayerShopGUIUpgrader {
    public static void main(String[] args) {
        File dbFile = new File("database.db");
        if (!dbFile.exists()) {
            System.out.println("No database.db found in the current directory.");
            return;
        }

        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            Statement statement = connection.createStatement();
        ) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM shops WHERE shopItems != '[]'");
            while (resultSet.next()) {
                JsonElement items = JsonParser.parseString(resultSet.getString("shopItems"));
                if (!items.isJsonArray()) {
                    continue;
                }

                for (JsonElement item : items.getAsJsonArray()) {
                    if (!item.isJsonObject()) {
                        continue;
                    }

                    JsonObject itemObject = item.getAsJsonObject();
                    // get itemStack base64 string and do the stuff...
                }
                
                // statement.executeUpdate(...);
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace(System.err);
        } 
    }
}
