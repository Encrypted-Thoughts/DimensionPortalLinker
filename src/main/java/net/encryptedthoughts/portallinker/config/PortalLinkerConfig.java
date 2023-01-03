package net.encryptedthoughts.portallinker.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.encryptedthoughts.portallinker.DimensionInfo;
import net.encryptedthoughts.portallinker.PortalLinkerMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;

public class PortalLinkerConfig {
    public ArrayList<DimensionInfo> Dimensions = new ArrayList<>();

    public boolean ReadFromFile() {
        Path path = FabricLoader.getInstance().getConfigDir();
        var file = path.resolve("portal_linker/portal_linker.json").toFile();
        if (file.exists()) {
            var gson = new Gson();
            try {
                var temp = gson.fromJson(new FileReader(file), this.getClass());
                Dimensions = temp.Dimensions;
            } catch (FileNotFoundException e) {
                PortalLinkerMod.LOGGER.error("Failed to read a config file.");
                e.printStackTrace();
                return false;
            }
        } else
            return false;

        return true;
    }

    public void SaveToFile() {
        Path path = FabricLoader.getInstance().getConfigDir();
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            var directory = path.resolve("portal_linker/").toFile();
            if (!directory.exists())
                if (!directory.mkdirs()) throw new Exception("Unable to create directory to store config files.");
            try (PrintWriter writer = new PrintWriter(directory.getPath() + "/portal_linker.json")) {
                writer.println(gson.toJson(this));
            }
        } catch (Exception e) {
            PortalLinkerMod.LOGGER.error("Failed to save a config file.");
            e.printStackTrace();
        }
    }
}


