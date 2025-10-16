package org.mcsci.efficiency.events;

import org.mcsci.efficiency.Efficiency;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    
    private Efficiency plugin;
    
    public PlayerMoveListener(Efficiency plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // 检查玩家是否启用了区块显示
        if (plugin.isChunkShowEnabled(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            
            // 如果玩家移动到了新的区块，显示区块信息
            if (from.getChunk().getX() != to.getChunk().getX() || 
                from.getChunk().getZ() != to.getChunk().getZ()) {
                
                Chunk currentChunk = to.getChunk();
                int chunkX = currentChunk.getX();
                int chunkZ = currentChunk.getZ();
                
                // 发送动作栏消息显示当前区块坐标
                sendActionBar(player, "§e当前区块: §aX:" + chunkX + " Z:" + chunkZ);
            }
        }
    }
    
    private void sendActionBar(Player player, String message) {
        // 兼容1.12+的动作栏发送方法
        try {
            player.sendTitle("", message, 0, 20, 0);
        } catch (Exception e) {
            player.sendMessage(message);
        }
    }
}