package com.github.mwerschy.LagLocate;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public class LagLocate extends JavaPlugin {
    Entity[] items;
    Entity[] creatures;
    LagLocateCommandExecutor executor;
    @Override
    public void onEnable() {
        getLogger().info("Enabled LagLocate");
        executor = new LagLocateCommandExecutor();
        getCommand("LagLocate").setExecutor(executor);
        getCommand("LLTP").setExecutor(executor);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled LagLocate");
    }
}
