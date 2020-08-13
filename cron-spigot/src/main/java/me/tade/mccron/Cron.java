package me.tade.mccron;

import lombok.Getter;
import me.tade.mccron.commands.CronCommand;
import me.tade.mccron.commands.TimerCommand;
import me.tade.mccron.job.CronJob;
import me.tade.mccron.job.EventJob;
import me.tade.mccron.managers.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author The_TadeSK
 */
public class Cron extends JavaPlugin {

    @Getter
    private FileConfiguration config;

    @Getter
    private final HashMap<String, CronJob> jobs = new HashMap<>();
    @Getter
    private final HashMap<EventType, List<EventJob>> eventJobs = new HashMap<>();

    @Getter
    private final List<String> startUpCommands = new ArrayList<>();

    @Override
    public void onEnable() {
        log("Loading plugin...");
        log("Loading config...");

        loadConfig();
        loadJobs();

        log("Loading managers...");
        new EventManager(this);

        log("Loading commands...");
        getCommand("timer").setExecutor(new TimerCommand(this));
        getCommand("mccron").setExecutor(new CronCommand(this));

        new BukkitRunnable() {
            @Override
            public void run() {
                log("Dispatching startup commands..");
                for (String commands : getStartUpCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commands);
                }
                log("Commands dispatched!");
            }
        }.runTaskLater(this, 20);
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            try {
                saveResource("config.yml", false);
            } catch (IllegalArgumentException e) {
                log("Could not create resource config.yml");
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void loadJobs() {
        log("Loading cron jobs....");
        for (String s : getConfig().getConfigurationSection("jobs").getKeys(false)) {
            List<String> commands = getConfig().getStringList("jobs." + s + ".commands");
            String time = getConfig().getString("jobs." + s + ".time");

            jobs.put(s, new CronJob(this, commands, time, s));
            log("Created new job: " + s);
        }
        log("Total loaded jobs: " + jobs.size());
        log("Starting cron jobs...");
        for (CronJob j : new ArrayList<>(jobs.values())) {
            try {
                log("Starting job: " + j.getName());
                j.startJob();
            } catch (IllegalArgumentException ex) {
                log("Can't start job " + j.getName() + "! " + ex.getMessage());
            }
        }
        log("All jobs started!");

        for (String s : getConfig().getConfigurationSection("event-jobs").getKeys(false)) {
            EventType type = EventType.isEventJob(s);
            if (type != null) {
                List<EventJob> jobs = new ArrayList<>();
                for (String name : getConfig().getConfigurationSection("event-jobs." + s).getKeys(false)) {
                    int time = getConfig().getInt("event-jobs." + s + "." + name + ".time");
                    List<String> cmds = getConfig().getStringList("event-jobs." + s + "." + name + ".commands");
                    jobs.add(new EventJob(this, name, time, cmds, type));
                    log("Created new event job: " + name + " (" + type.getConfigName() + ")");
                }

                eventJobs.put(type, jobs);
            }
        }
        log("All event jobs registered!");

        List<String> commands = getConfig().getStringList("startup.commands");

        for (String command : commands) {
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

            pw.println("[" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()) + "] " + info);
            pw.flush();
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
