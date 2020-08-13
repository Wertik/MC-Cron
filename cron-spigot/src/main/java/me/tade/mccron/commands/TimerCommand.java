package me.tade.mccron.commands;

import me.tade.mccron.Cron;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * @author The_TadeSK
 */
public class TimerCommand implements CommandExecutor {

    private final Cron cron;

    public TimerCommand(Cron cron) {
        this.cron = cron;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("timer")) {
            if (sender instanceof Player) {
                sender.sendMessage("§cOnly console can perform this command!");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("§aUse /timer <time> <command>");
                return true;
            } else if (args.length >= 2) {
                StringBuilder c = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    c.append(" ").append(args[i]);
                }
                c = new StringBuilder(c.substring(1));

                int time = Integer.parseInt(args[0]);
                if (time > 300) {
                    sender.sendMessage("§cMaximum is 300 seconds (5 minutes)!");
                    return true;
                }
                runCmd(c.toString(), time);
            }
        }
        return true;
    }

    public void runCmd(String cmd, int seconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }.runTaskLater(cron, seconds * 20);
    }
}