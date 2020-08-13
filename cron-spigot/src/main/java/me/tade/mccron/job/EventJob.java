package me.tade.mccron.job;

import lombok.Getter;
import me.tade.mccron.Cron;
import me.tade.mccron.EventType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class EventJob {

    private final Cron cron;

    @Getter
    private final String name;
    @Getter
    private final int time;
    @Getter
    private final List<String> commands;
    @Getter
    private final EventType eventType;

    public EventJob(Cron cron, String name, int time, List<String> commands, EventType eventType) {
        this.cron = cron;
        this.name = name;
        this.time = time;
        this.commands = commands;
        this.eventType = eventType;
    }

    public void performJob(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (eventType == EventType.JOIN_EVENT && !player.isOnline())
                    return;

                for (String command : commands) {
                    command = command.replace("{player}", player.getName());

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }.runTaskLater(cron, time * 20);
    }
}