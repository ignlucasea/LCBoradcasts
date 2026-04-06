package com.spaleforce.lcbroadcasts;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class LCBroadcasts extends JavaPlugin {

    private static LCBroadcasts instance;
    private BukkitRunnable broadcastTask;
    private List<String> messages;
    private int currentIndex;
    private Random random;
    private long intervalSeconds;
    private boolean randomOrder;

    @Override
    public void onEnable() {
        instance = this;
        currentIndex = 0;
        random = new Random();
        
        saveDefaultConfig();
        loadConfig();
        startBroadcastTask();
        
        getCommand("lcbroadcast").setExecutor(new ReloadCommand(this));
        
        getLogger().info("LCBroadcasts has been enabled!");
    }

    @Override
    public void onDisable() {
        stopBroadcastTask();
        getLogger().info("LCBroadcasts has been disabled!");
    }

    public static LCBroadcasts getInstance() {
        return instance;
    }

    public void loadConfig() {
        reloadConfig();
        messages = getConfig().getStringList("messages");
        intervalSeconds = getConfig().getLong("interval-seconds", 300);
        randomOrder = getConfig().getBoolean("random-order", false);
        
        if (messages.isEmpty()) {
            getLogger().warning("No messages configured in config.yml!");
        }
        
        getLogger().info("Loaded " + messages.size() + " broadcast messages.");
        getLogger().info("Broadcast interval: " + intervalSeconds + " seconds");
        getLogger().info("Random order: " + randomOrder);
    }

    public void reload() {
        stopBroadcastTask();
        currentIndex = 0;
        loadConfig();
        startBroadcastTask();
    }

    private void startBroadcastTask() {
        if (messages == null || messages.isEmpty()) {
            getLogger().warning("Cannot start broadcast task - no messages configured.");
            return;
        }

        long intervalTicks = intervalSeconds * 20;

        broadcastTask = new BukkitRunnable() {
            @Override
            public void run() {
                broadcastNextMessage();
            }
        };

        broadcastTask.runTaskTimer(this, intervalTicks, intervalTicks);
    }

    private void stopBroadcastTask() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
    }

    private void broadcastNextMessage() {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        String message;
        
        if (randomOrder) {
            message = messages.get(random.nextInt(messages.size()));
        } else {
            message = messages.get(currentIndex);
            currentIndex = (currentIndex + 1) % messages.size();
        }

        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
        Bukkit.broadcastMessage(coloredMessage);
    }

    public List<String> getMessages() {
        return messages;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public boolean isRandomOrder() {
        return randomOrder;
    }
}
