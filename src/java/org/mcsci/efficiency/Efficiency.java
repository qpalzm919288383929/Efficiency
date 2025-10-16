package org.mcsci.efficiency;

import org.mcsci.efficiency.commands.ChunkShowCommand;
import org.mcsci.efficiency.commands.CustomTeleportCommand;
import org.mcsci.efficiency.commands.EfficiencyShopCommand;
import org.mcsci.efficiency.events.InventoryClickListener;
import org.mcsci.efficiency.events.PlayerMoveListener;
import org.mcsci.efficiency.managers.ShopManager;
import org.mcsci.efficiency.utils.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Efficiency extends JavaPlugin {
    
    private static Efficiency instance;
    private HashMap<UUID, TeleportRequest> teleportRequests;
    private Set<UUID> chunkShowPlayers;
    private ShopManager shopManager;
    
    @Override
    public void onEnable() {
        instance = this;
        teleportRequests = new HashMap<>();
        chunkShowPlayers = new HashSet<>();
        shopManager = new ShopManager(this);
        
        // 注册命令
        getCommand("cs").setExecutor(new CustomTeleportCommand(this));
        getCommand("qk").setExecutor(new ChunkShowCommand(this));
        getCommand("ef").setExecutor(new EfficiencyShopCommand(this));
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        
        // 加载商店数据
        shopManager.loadShopData();
        
        // 保存默认配置
        saveDefaultConfig();
        
        getLogger().info("Efficiency插件已成功加载！");
    }
    
    @Override
    public void onDisable() {
        // 保存商店数据
        shopManager.saveShopData();
        getLogger().info("Efficiency插件已卸载！");
    }
    
    public static Efficiency getInstance() {
        return instance;
    }
    
    public HashMap<UUID, TeleportRequest> getTeleportRequests() {
        return teleportRequests;
    }
    
    public Set<UUID> getChunkShowPlayers() {
        return chunkShowPlayers;
    }
    
    public ShopManager getShopManager() {
        return shopManager;
    }
    
    public void addChunkShowPlayer(Player player) {
        chunkShowPlayers.add(player.getUniqueId());
    }
    
    public void removeChunkShowPlayer(Player player) {
        chunkShowPlayers.remove(player.getUniqueId());
    }
    
    public boolean isChunkShowEnabled(Player player) {
        return chunkShowPlayers.contains(player.getUniqueId());
    }
}