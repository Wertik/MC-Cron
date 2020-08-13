package me.tade.mccron.bungee.job;

import me.tade.mccron.bungee.BungeeCron;
import me.tade.mccron.EventType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BungeeEventJob {

    private final BungeeCron cron;
    private final String name;
    private final int time;
    private final List<String> commands;
    private final EventType eventType;

    public BungeeEventJob(BungeeCron cron, String name, int time, List<String> commands, EventType eventType) {
        this.cron = cron;
        this.name = name;
        this.time = time;
        this.commands = commands;
        this.eventType = eventType;
    }

    public void performJob(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().schedule(cron, () -> {
            if (eventType == EventType.JOIN_EVENT && !player.isConnected())
                return;

            for (String command : commands) {
                command = command.replace("{player}", player.getName());

                ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
            }
        }, time, TimeUnit.SECONDS);
    }

    public String getName() {
        return name;
    }

    public EventType getEventType() {
        return eventType;
    }
}
