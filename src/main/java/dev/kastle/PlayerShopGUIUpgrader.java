package dev.kastle;

import ca.spottedleaf.dataconverter.minecraft.MCDataConverter;
import ca.spottedleaf.dataconverter.minecraft.MCVersions;
import ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtFormatException;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

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
            SharedConstants.tryDetectVersion();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM shops WHERE shopItems != '[]'");
            while (resultSet.next()) {
                JsonElement items = JsonParser.parseString(resultSet.getString("shopItems"));
                if (!items.isJsonArray()) {
                    continue;
                }


                boolean updated = false;
                for (JsonElement item : items.getAsJsonArray()) {
                    if (!item.isJsonObject()) {
                        continue;
                    }

                    JsonObject itemObject = item.getAsJsonObject();
                    if (!itemObject.has("itemStack")) {
                        continue;
                    }
                    if (!itemObject.get("itemStack").isJsonPrimitive()) {
                        continue;
                    }
                    if (!itemObject.get("itemStack").getAsJsonPrimitive().isString()) {
                        continue;
                    }

                    byte[] decodedBytes = Base64.getDecoder().decode(itemObject.get("itemStack").getAsString());
                    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(decodedBytes));
                    CompoundTag itemStack;

                    try {
                        itemStack = NbtIo.readCompressed(dataInputStream, NbtAccounter.unlimitedHeap());
                    } catch (IOException | NbtFormatException e) {
                        System.err.println("Failed to read item stack NBT data.");
                        StringBuilder hexDump = new StringBuilder();
                        for (byte b : decodedBytes) {
                            hexDump.append(String.format("%02X ", b));
                        }
                        System.out.println("Base64 string of failed item stack: " + itemObject.get("itemStack").getAsString());
                        System.out.println("Hex dump of failed item stack: " + hexDump.toString());
                        e.printStackTrace(System.err);
                        continue;
                    }

                    CompoundTag converted = MCDataConverter.convertTag(MCTypeRegistry.ITEM_STACK, itemStack, MCVersions.V1_20_1, MCVersions.V1_21_4);
                    if (converted == null) {
                        continue;
                    }

                    // System.out.println("Converted item stack: " + converted.getAsString());

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    try {
                        NbtIo.writeCompressed(converted, outputStream);
                    } catch (IOException e) {
                        System.err.println("Failed to write converted item stack NBT data.");
                        e.printStackTrace(System.err);
                        continue;
                    }

                    String base64String = Base64.getEncoder().encodeToString(outputStream.toByteArray());
                    itemObject.addProperty("itemStack", base64String);
                    updated = true;
                }

                if (!updated) {
                    continue;
                }

                String updateQuery = "UPDATE shops SET shopItems = ? WHERE id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setString(1, items.toString());
                    preparedStatement.setInt(2, resultSet.getInt("id"));
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Failed to update shopItems in the database.");
                    e.printStackTrace(System.err);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace(System.err);
        } 
    }
}
