package org.mcsci.efficiency.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeleportRequest {
    private UUID requester;
    private UUID target;
    private long timestamp;
    
    public TeleportRequest(Player requester, Player target) {
        this.requester = requester.getUniqueId();
        this.target = target.getUniqueId();
        this.timestamp = System.currentTimeMillis();
    }
    
    public UUID getRequester() {
        return requester;
    }
    
    public UUID getTarget() {
        return target;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public Player getRequesterPlayer() {
        return Bukkit.getPlayer(requester);
    }
    
    public Player getTargetPlayer() {
        return Bukkit.getPlayer(target);
    }
    
    public boolean isExpired() {
        // 请求30秒后过期
        return System.currentTimeMillis() - timestamp > 30000;
    }
}