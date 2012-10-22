package com.github.mwerschy.LagLocate;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class LagLocate extends JavaPlugin {
    Entity[] items;
    Entity[] creatures;

    @Override
    public void onEnable() {
        getLogger().info("Enabled LagLocate");
        getCommand("LagLocate").setExecutor(new LagLocateCommandExecutor());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled LagLocate");
    }
}
