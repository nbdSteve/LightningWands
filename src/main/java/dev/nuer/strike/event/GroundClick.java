package dev.nuer.strike.event;

import dev.nuer.strike.LightningWands;
import dev.nuer.strike.file.LoadProvidedFiles;
import dev.nuer.strike.support.Factions;
import dev.nuer.strike.support.MassiveCore;
import dev.nuer.strike.support.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Event called when the player right clicks a block, most of the code is not executed unless they are using the
 * tool. The tool check is done first to reduce memory usage.
 */
public class GroundClick implements Listener {
    //Register the main class
    private Plugin pl = LightningWands.getPlugin(LightningWands.class);
    //Register LoadProvidedFiles class
    private LoadProvidedFiles lpf = ((LightningWands) pl).getFiles();
    //Get the cooldown hashmap from the main class
    private HashMap<UUID, Long> toolCDT = ((LightningWands) pl).getLightningCDT();

    /**
     * All code for the event is stored in this method.
     *
     * @param e the event, cannot be null.
     */
    @EventHandler
    public void onGroundClick(PlayerInteractEvent e) {
        //Get the player
        Player p = e.getPlayer();
        //Check that it is the right event
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            //Check that the player has the lightningwand in their hand
            if (p.getInventory().getItemInHand().hasItemMeta()) {
                if (p.getInventory().getItemInHand().getItemMeta().hasLore()) {
                    ItemMeta toolMeta = p.getInventory().getItemInHand().getItemMeta();
                    List<String> toolLore = toolMeta.getLore();
                    String toolType = null;
                    //Get the level of lightning from the tool lore
                    for (int i = 1; i < 10; i++) {
                        String tool = "lightning-wand-" + i;
                        try {
                            lpf.getLightning().getString(tool + ".unique");
                            if (toolLore.contains(ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString(tool + ".unique")))) {
                                toolType = tool;
                            }
                        } catch (Exception ex) {
                            //Do nothing, this tool isn't active or doesn't exist
                        }
                    }
                    if (toolType == null) {
                        return;
                    }
                    //Figure out which plugins are being used and what to support
                    if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
                        if (!WorldGuard.allowsBreak(e.getClickedBlock().getLocation())) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (Bukkit.getPluginManager().getPlugin("MassiveCore") != null) {
                        MassiveCore.canBreakBlock(p, e.getClickedBlock());
                        if (!MassiveCore.canBreakBlock(p, e.getClickedBlock())) {
                            e.setCancelled(true);
                            return;
                        }
                    } else if (Bukkit.getServer().getPluginManager().getPlugin("Factions") != null) {
                        if (!Factions.canBreakBlock(p, e.getClickedBlock())) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                    int cooldown = lpf.getLightning().getInt(toolType + ".cooldown");
                    if (cooldown >= 0) {
                        if (toolCDT.containsKey(p.getUniqueId())) {
                            long CDT = ((toolCDT.get(p.getUniqueId()) / 1000) + cooldown)
                                    - (System.currentTimeMillis() / 1000);
                            if (CDT > 0) {
                                for (String line : lpf.getMessages().getStringList("cooldown")) {
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', line)
                                            .replace("%cooldown%", String.valueOf(CDT)));
                                }
                            } else {
                                toolCDT.remove(p.getUniqueId());
                            }
                            e.setCancelled(true);
                            return;
                        } else {
                            toolCDT.put(p.getUniqueId(), System.currentTimeMillis());
                        }
                    }
                    //Strike lightning at that location
                    e.getClickedBlock().getLocation().getWorld().strikeLightningEffect(e.getClickedBlock().getLocation());
                    //Get the id for the uses line
                    String uID = ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString(toolType + ".uses-line-id"));
                    try {
                        for (int i = 0; i < toolMeta.getLore().size(); i++) {
                            String l = toolMeta.getLore().get(i);
                            if (l.contains(uID)) {
                                String uses = "";
                                for (int m = 0; m < toolLore.get(i).length(); m++) {
                                    if (Character.isDigit(toolLore.get(i).charAt(m))) {
                                        if (m != 0) {
                                            if (toolLore.get(i).charAt(m - 1) != ChatColor.COLOR_CHAR) {
                                                uses += toolLore.get(i).charAt(m);
                                            }
                                        } else {
                                            uses += toolLore.get(i).charAt(m);
                                        }
                                    }
                                }
                                int temp = Integer.parseInt(uses) - 1;
                                if (temp <= 0) {
                                    p.getInventory().removeItem(p.getItemInHand());
                                    for (String message : lpf.getMessages().getStringList("break")) {
                                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                                    }
                                } else {
                                    String uI = ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString(toolType + ".uses-id").replace("%uses%", String.valueOf(temp)));
                                    toolLore.set(i, (uID + " " + uI));
                                    toolMeta.setLore(toolLore);
                                    p.getItemInHand().setItemMeta(toolMeta);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        //Do nothing, tool isn't recording uses
                    }
                }
            }
        }
    }
}