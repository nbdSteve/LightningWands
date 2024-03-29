package dev.nuer.strike.file;

import dev.nuer.strike.LightningWands;
import dev.nuer.strike.file.providedfile.GenerateProvidedFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

/**
 * Class to load a specified file, to create a new file simply add the name of it to the ENUM.
 * Then add another line to the LoadProvidedFiles method and create a getter for that file.
 */
public class LoadProvidedFiles {
    //Register the main class
    private Plugin pl = LightningWands.getPlugin(LightningWands.class);
    //HashMap to store the files
    private HashMap<Files, GenerateProvidedFile> fileList;

    /**
     * Enum to store each file, this is public so we can call methods on these
     */
    public enum Files {
        CONFIG, MESSAGES, LIGHTNING
    }

    /**
     * Generate all of the files in the enum
     */
    public LoadProvidedFiles() {
        fileList = new HashMap<>();
        fileList.put(Files.CONFIG, new GenerateProvidedFile("config.yml"));
        fileList.put(Files.MESSAGES, new GenerateProvidedFile("messages.yml"));
        fileList.put(Files.LIGHTNING, new GenerateProvidedFile("lightning.yml"));
        pl.getLogger().info("Loading provided files...");
    }

    public FileConfiguration getConfig() {
        return fileList.get(Files.CONFIG).get();
    }

    public FileConfiguration getMessages() {
        return fileList.get(Files.MESSAGES).get();
    }

    public FileConfiguration getLightning() {
        return fileList.get(Files.LIGHTNING).get();
    }

    public void reload() {
        for (Files file : Files.values()) {
            fileList.get(file).reload();
        }
        pl.getLogger().info("Reloading provided files...");
    }
}
