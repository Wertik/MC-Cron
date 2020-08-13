package me.tade.mccron.bungee;

import me.tade.mccron.bungee.commands.BungeeCronCommand;
import me.tade.mccron.bungee.commands.BungeeTimerCommand;
import me.tade.mccron.bungee.job.BungeeCronJob;
import me.tade.mccron.bungee.job.BungeeEventJob;
import me.tade.mccron.bungee.managers.EventManager;
import me.tade.mccron.EventType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This file was created by Tadeáš Drab on 14. 12. 2018
 */
public class BungeeCron extends Plugin {

    private File file;
    private Configuration config;

    private final List<String> startUpCommands = new ArrayList<>();

    private final HashMap<String, BungeeCronJob> jobs = new HashMap<>();
    private final HashMap<EventType, List<BungeeEventJob>> eventJobs = new HashMap<>();

    @Override
    public void onEnable() {
        log("Loading plugin...");
        log("Loading config...");

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        try {
            this.file = new File(getDataFolder(), "config.yml");
            if (!this.file.exists()) {
                log("Error: config.yml Not Found! Creating a new");
                copy(this.file, "config.yml");
            }
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.file);

            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        log("Loading commands...");
        getProxy().getPluginManager().registerCommand(this, new BungeeTimerCommand(this));
        getProxy().getPluginManager().registerCommand(this, new BungeeCronCommand(this));

        loadJobs();

        log("Loading managers...");
        new EventManager(this);

        ProxyServer.getInstance().getScheduler().schedule(this, (Runnable) () -> {
            log("Dispatching startup commands..");
            for (String commands : getStartUpCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commands);
            }
            log("Commands dispatched!");
        }, 2, TimeUnit.SECONDS);
    }

    public void loadJobs() {
        log("Loading cron jobs....");
        for (String s : config.getSection("jobs").getKeys()) {
            List<String> cmds = config.getStringList("jobs." + s + ".commands");
            String time = config.getString("jobs." + s + ".time");

            jobs.put(s, new BungeeCronJob(this, cmds, time, s));
            log("Created new job: " + s);
        }
        log("Total loaded jobs: " + jobs.size());
        log("Starting cron jobs...");
        for (BungeeCronJob j : new ArrayList<>(jobs.values())) {
            try {
                log("Starting job: " + j.getName());
                j.startJob();
            } catch (IllegalArgumentException ex) {
                log("Can't start job " + j.getName() + "! " + ex.getMessage());
            }
        }
        log("All jobs started!");

        for (String s : config.getSection("event-jobs").getKeys()) {
            EventType type = EventType.isEventJob(s);
            if (type != null) {
                List<BungeeEventJob> jobs = new ArrayList<>();
                for (String name : config.getSection("event-jobs." + s).getKeys()) {
                    int time = config.getInt("event-jobs." + s + "." + name + ".time");
                    List<String> cmds = config.getStringList("event-jobs." + s + "." + name + ".commands");
                    jobs.add(new BungeeEventJob(this, name, time, cmds, type));
                    log("Created new event job: " + name + " (" + type.getConfigName() + ")");
                }

                eventJobs.put(type, jobs);
            }
        }
        log("All event jobs registered!");

        List<String> cmds = config.getStringList("startup.commands");
        for (String command : cmds) {
            startUpCommands.add(command);
            log("Created new startup command: " + command);
        }
        log("All startup commands registered!");
    }

    @Override
    public void onDisable() {
    }

    public void log(String info) {
        getLogger().info(info);
        logCustom(info);
    }

    private void logCustom(String info) {
        try {
            File dataFolder = getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }

            File saveTo = new File(getDataFolder(), "log.txt");
            if (!saveTo.exists()) {
                saveTo.createNewFile();
            }

            FileWriter fw = new FileWriter(saveTo, true);
            PrintWriter pw = new PrintWriter(fw);

            pw.println("[" + new SimpleDateFormat("dd.MM.YYYY HH:mm:ss").format(new Date()) + "] " + info);
            pw.flush();
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, BungeeCronJob> getJobs() {
        return jobs;
    }

    public HashMap<EventType, List<BungeeEventJob>> getEventJobs() {
        return eventJobs;
    }

    private void copy(File file, String resource) {
        try {
            Files.copy(getResourceAsStream(resource), file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().warning("Could not copy " + resource + " file");
        }
    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getStartUpCommands() {
        return startUpCommands;
    }
}