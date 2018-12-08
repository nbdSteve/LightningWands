package com.nbdsteve.lightningwands.event;

import com.nbdsteve.lightningwands.LightningWands;
import com.nbdsteve.lightningwands.file.LoadProvidedFiles;
import com.nbdsteve.lightningwands.support.Factions;
import com.nbdsteve.lightningwands.support.MassiveCore;
import com.nbdsteve.lightningwands.support.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Event called when the player right clicks a mob, most of the code is not executed unless they are using the
 * tool. The tool check is done first to reduce memory usage.
 */
public class MobClick implements Listener {
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
    public void onMobClick(PlayerInteractEntityEvent e) {
        //Get the player
        Player p = e.getPlayer();
        //Check that it is the right event
        if (e.getRightClicked().getType().equals(EntityType.CREEPER)) {
            //Store the creeper
            Creeper mob = (Creeper) e.getRightClicked();
            //Check that the player has the lightningwand in their hand
            if (p.getInventory().getItemInHand().hasItemMeta()) {
                if (p.getInventory().getItemInHand().getItemMeta().hasLore()) {
                    ItemMeta toolMeta = p.getInventory().getItemInHand().getItemMeta();
                    List<String> toolLore = toolMeta.getLore();
                    String toolType;
                    //Get the level of lightning from the tool lore
                    if (toolLore.contains(
                            ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString("lightning-wand-1.unique")))) {
                        toolType = "lightning-wand-1";
                    } else if (toolLore.contains(
                            ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString("lightning-wand-2.unique")))) {
                        toolType = "lightning-wand-2";
                    } else if (toolLore.contains(
                            ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString("lightning-wand-3.unique")))) {
                        toolType = "lightning-wand-3";
                    } else if (toolLore.contains(
                            ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString("lightning-wand-4.unique")))) {
                        toolType = "lightning-wand-4";
                    } else if (toolLore.contains(
                            ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString("lightning-wand-5.unique")))) {
                        toolType = "lightning-wand-5";
                    } else if (toolLore.contains(
                            ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString("lightning-wand-6.unique")))) {
                        toolType = "lightning-wand-6";
                    } else if (toolLore.contains(
                            ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString("lightning-wand-7.unique")))) {
                        toolType = "lightning-wand-7";
                    } else if (toolLore.contains(
                            ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString("lightning-wand-8.unique")))) {
                        toolType = "lightning-wand-8";
                    } else if (toolLore.contains(
                            ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString("lightning-wand-9.unique")))) {
                        toolType = "lightning-wand-9";
                    } else {
                        return;
                    }
                    boolean wg = false;
                    boolean fac = false;
                    //Figure out which plugins are being used and what to support
                    if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
                        wg = true;
                        if (!WorldGuard.allowsBreak(e.getRightClicked().getLocation())) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (Bukkit.getPluginManager().getPlugin("MassiveCore") != null) {
                        MassiveCore.canBreakBlock(p, e.getRightClicked().getLocation().getBlock());
                        fac = true;
                        if (!MassiveCore.canBreakBlock(p, e.getRightClicked().getLocation().getBlock())) {
                            e.setCancelled(true);
                            return;
                        }
                    } else if (Bukkit.getServer().getPluginManager().getPlugin("Factions") != null) {
                        fac = true;
                        if (!Factions.canBreakBlock(p, e.getRightClicked().getLocation().getBlock())) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                    int cooldown = lpf.getLightning().getInt(toolType + ".cooldown");
                    if (cooldown != -1 && cooldown >= 0) {
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
                        } else {
                            toolCDT.put(p.getUniqueId(), System.currentTimeMillis());
                        }
                    }
                    //Strike lightning at that location
                    e.getRightClicked().getLocation().getWorld().strikeLightningEffect(e.getRightClicked().getLocation());
                    if (mob.isPowered()) {
                        for (String line : lpf.getMessages().getStringList("already-powered")) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                        }
                    } else {
                        mob.setPowered(true);
                    }
                    //Get the id for the uses line
                    String uID = ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString(toolType + ".uses-unique-line-id"));
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
                                    String uI = ChatColor.translateAlternateColorCodes('&', lpf.getLightning().getString(toolType + ".uses-increment-id").replace("%uses%", String.valueOf(temp)));
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