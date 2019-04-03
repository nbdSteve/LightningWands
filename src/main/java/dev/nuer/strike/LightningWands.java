package dev.nuer.strike;

import dev.nuer.strike.command.LightningCommand;
import dev.nuer.strike.event.GroundClick;
import dev.nuer.strike.event.MobClick;
import dev.nuer.strike.event.gui.GuiClick;
import dev.nuer.strike.file.LoadProvidedFiles;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

/**
 * Core class for the LightningWands plugin.
 */
public final class LightningWands extends JavaPlugin {
    //Economy variable for the plugin
    private static Economy econ;
    //New LoadProvidedFiles instance
    private LoadProvidedFiles lpf;
    //Create the cooldown hashmap
    private HashMap<UUID, Long> LightningCDT = new HashMap<>();

    /**
     * Method called when the plugin starts, register all events and commands in this method
     */
    @Override
    public void onEnable() {
        getLogger().info("Thanks for using LightningWands - nbdSteve");
        if (!setupEconomy()) {
            getLogger().severe("Vault.jar not found, disabling economy features.");
        }
        //Generate all of the provided files for the plugin
        this.lpf = new LoadProvidedFiles();
        //Register the commands for the plugin
        getCommand("lw").setExecutor(new LightningCommand(this));
        getCommand("lightning").setExecutor(new LightningCommand(this));
        //Register the events for the plugin
        getServer().getPluginManager().registerEvents(new GuiClick(), this);
        getServer().getPluginManager().registerEvents(new MobClick(), this);
        getServer().getPluginManager().registerEvents(new GroundClick(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Thanks for using LightningWands - nbdSteve");
    }

    //Set up the economy for the plugin
    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    /**
     * Get the LoadProvidedFiles instance that has been created
     *
     * @return LoadProvidedFiles instance
     */
    public LoadProvidedFiles getFiles() {
        return lpf;
    }

    /**
     * Get the servers economy
     *
     * @return econ
     */
    public static Economy getEconomy() {
        return econ;
    }

    public HashMap<UUID, Long> getLightningCDT() {
        return LightningCDT;
    }
}
