package org.mcsci.efficiency.commands;

import org.mcsci.efficiency.Efficiency;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChunkShowCommand implements CommandExecutor {
    
    private Efficiency plugin;
    
    public ChunkShowCommand(Efficiency plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("§c用法: /qk open - 开启区块显示");
            player.sendMessage("§c用法: /qk stop - 关闭区块显示");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "open":
            case "start":
            case "enable":
                plugin.addChunkShowPlayer(player);
                player.sendMessage("§a区块显示已开启！你现在可以看到所在区块的边界。");
                break;
                
            case "stop":
            case "close":
            case "disable":
                plugin.removeChunkShowPlayer(player);
                player.sendMessage("§c区块显示已关闭！");
                break;
                
            default:
                player.sendMessage("§c未知参数！使用: /qk open 或 /qk stop");
                break;
        }
        
        return true;
    }
}