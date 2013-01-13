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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class LagLocateCommandExecutor implements CommandExecutor {
    String[] helpMsg;
    double   distance;
    boolean  tp;
    int maxResults = 5;
    ArrayList<List<Entity>> greatEntityGroup = null;
    String   type;

    private static Comparator<List<Entity>> sizeComparator = new Comparator<List<Entity>>() {
		@Override
		public int compare(List<Entity> l1, List<Entity> l2) {
			return Integer.compare(l2.size(), l1.size());
		}
    };
    
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
    	if (cmd.getName().equalsIgnoreCase("lltp")) {
    		
    		if (!(sender instanceof Player)) {
    			sender.sendMessage("This command must be run by a player.");
    			return true;
    		}
    		Player player = (Player)sender;
    		if (!sender.hasPermission("LagLocate.teleport")) {
    			sender.sendMessage("You don't have permission to teleport.");
    			return false;
    		}
    		if (greatEntityGroup == null || greatEntityGroup.size() == 0) {
    			sender.sendMessage("There are no results to teleport to, try another search.");
    			return true;
    		}
    		int num = 1;
    		if (args.length == 1) {
    			try {
    				num = Integer.parseInt(args[0]);
    			} catch (NumberFormatException ex) {
    				sender.sendMessage("Invalid number: " + args[0]);
    				return true;
    			}
    		}
    		num -= 1;
    		if (num < 0 || num >= greatEntityGroup.size()) {
    			sender.sendMessage("please select a number between 1 and " + greatEntityGroup.size());
    			return true;
    		}
    		player.teleport(greatEntityGroup.get(num).get(0).getLocation());
    		return true;
    	}
    	
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
                greatEntityGroup = new ArrayList<List<Entity>>();
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
                    if (greatEntityGroup.size() < maxResults) {
                    	greatEntityGroup.add(nearEntities);
                    	Collections.sort(greatEntityGroup, sizeComparator);
                    } else {
	                    for (int i=0;i<greatEntityGroup.size();i++) {
	                    	if (nearEntities.size() > greatEntityGroup.get(i).size()) {
	                    		greatEntityGroup.add(i, nearEntities);
	                    		break;
	                    	}
	                    }
	                    if (greatEntityGroup.size() > maxResults) {
	                    	greatEntityGroup.remove(greatEntityGroup.size()-1);
	                    }
                    }
                }
                Iterator<List<Entity>> it = greatEntityGroup.iterator();
                HashSet<Location> locSet = new HashSet<Location>();
                Location loc;
                while (it.hasNext()) {
                	loc = it.next().get(0).getLocation().getBlock().getLocation();
                	if (locSet.contains(loc)) {
                		it.remove();
                		continue;
                	} else {
                		locSet.add(loc);
                	}
                }
                
                if (greatEntityGroup.size() > 0) {
                	for (int i=0;i<greatEntityGroup.size();i++) {
                		center = greatEntityGroup.get(i).get(0).getLocation();
                		sender.sendMessage((i+1) + ") X: " + center.getBlockX() + " Y: " + center.getBlockY() + " Z: " + center.getBlockZ());	
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
