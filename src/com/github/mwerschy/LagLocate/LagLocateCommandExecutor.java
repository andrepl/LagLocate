package com.github.mwerschy.LagLocate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LagLocateCommandExecutor implements CommandExecutor {
    String[] helpMsg;
    double   distance;
    boolean  tp;
    String   type;

    LagLocateCommandExecutor() {
        helpMsg = new String[7];
        helpMsg[0] = "Welcome to the LagLocate Help System";
        helpMsg[1] = "Usage:";
        helpMsg[2] = "  /LagLocate <distance> [items|creatures|all] [(true|false)tp]";
        helpMsg[3] = "    Returns the coordinates of the largest stack of found";
        helpMsg[4] = "    [items|creatures|all] (default: items) in the loaded chunks,";
        helpMsg[5] = "    within <distance> blocks of each other.";
        helpMsg[6] = "    Optionally teleports you there. (default: false)";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("LagLocate")) {
            if (args.length == 0) {
                sender.sendMessage(helpMsg);
                return true;
            } else {
                if (args.length > 3) {
                    sender.sendMessage("Too many arguments.");
                    return false;
                } else {
                    try {
                        distance = Double.parseDouble(args[0]);
                    } catch (Exception e) {
                        sender.sendMessage("Argument <distance> cannot be cast to Integer.");
                        return false;
                    }
                    if (args.length > 1) {
                        type = args[1];
                        if (!type.toLowerCase().contains("item") && !type.toLowerCase().contains("creature") && !type.toLowerCase().contains("all")) {
                            sender.sendMessage("Invalid type.");
                            return false;
                        }
                    } else {
                        type = "item";
                    }
                    if (args.length > 2) {
                        tp = Boolean.parseBoolean(args[2]);
                    }
                }
                sender.sendMessage("Searching for lag inducing setups:");
                sender.sendMessage("  Distance: " + args[0]);
                sender.sendMessage("  Type: " + (args.length > 1 ? args[1] : "Items (default)"));
                if (tp && sender.hasPermission("LagLocate.teleport")) {
                    sender.sendMessage("  Teleport: Yes");
                } else if (tp && !sender.hasPermission("LagLocate.teleport")) {
                    sender.sendMessage("  Teleport: No (No Permission)");
                } else {
                    sender.sendMessage("  Teleport: No (Default)");
                }
                Player commandSender = Bukkit.getPlayer(sender.getName());
                List<Entity> entities = commandSender.getWorld().getEntities();
                List<Entity> filteredEntities = new ArrayList<Entity>();
                List<Entity> greatEntityGroup = new ArrayList<Entity>();
                List<Entity> nearEntities;
                Location center = new Location(commandSender.getWorld(), 0, 0, 0);

                if (type.toLowerCase().contains("item")) {
                    for (Entity entity : entities) {
                        if (entity instanceof Item) {
                            filteredEntities.add(entity);
                        }
                    }
                } else if (type.toLowerCase().contains("creature")) {
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity) {
                            filteredEntities.add(entity);
                        }
                    }
                } else if (type.toLowerCase().contains("all")) {
                    filteredEntities = entities;
                }
                if (filteredEntities.size() != 0) {
                    center = filteredEntities.get(0).getLocation();
                }
                
                for (Entity entity : filteredEntities) {
                    nearEntities = new ArrayList<Entity>();
                    if (type.toLowerCase().contains("item")) {
                    	for (Entity e: entity.getNearbyEntities(distance, distance, distance)) {
                    		if (e instanceof Item) {
                    			nearEntities.add(e);
                    		}
                    	}
                    } else if (type.toLowerCase().contains("creature")) {
                        for (Entity e : entity.getNearbyEntities(distance, distance, distance)) {
                            if (e instanceof LivingEntity) {
                                nearEntities.add(e);
                            }
                        }
                    } else if (type.toLowerCase().contains("all")) {
                        nearEntities = entity.getNearbyEntities(distance, distance, distance);
                    }
                    greatEntityGroup = (nearEntities.size() > greatEntityGroup.size() ? nearEntities : greatEntityGroup);
                    center = (nearEntities.size() > greatEntityGroup.size() ? entity.getLocation() : center);
                }
                if (greatEntityGroup.size() > 0) {
                    sender.sendMessage("X: " + center.getBlockX() + " Y: " + center.getBlockY() + " Z: " + center.getBlockZ());
                    if (tp && sender.hasPermission("LagLocate.teleport")) {
                        commandSender.teleport(center);
                    }
                } else {
                    sender.sendMessage("No entities found with specified parameters.");
                }
                return true;
            }
        }
        return false;
    }
}
