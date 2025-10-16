package org.mcsci.efficiency.events;

import org.mcsci.efficiency.Efficiency;
import org.mcsci.efficiency.utils.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InventoryClickListener implements Listener {
    
    private Efficiency plugin;
    
    public InventoryClickListener(Efficiency plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();
        
        // 检查是否是传送请求GUI
        if (inventory.getTitle().equals("§6传送请求处理")) {
            event.setCancelled(true);
            handleTeleportGUI(player, clickedItem);
        }
        // 检查是否是商店GUI
        else if (inventory.getTitle().equals("§6效率商店")) {
            event.setCancelled(true);
            handleShopGUI(player, clickedItem, event.getSlot(), event.isLeftClick());
        }
    }
    
    private void handleTeleportGUI(Player player, ItemStack clickedItem) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        UUID playerUUID = player.getUniqueId();
        TeleportRequest request = plugin.getTeleportRequests().get(playerUUID);
        
        if (request == null || request.isExpired()) {
            player.sendMessage("§c传送请求已过期！");
            player.closeInventory();
            plugin.getTeleportRequests().remove(playerUUID);
            return;
        }
        
        Player requester = request.getRequesterPlayer();
        
        switch (clickedItem.getType()) {
            case EMERALD_BLOCK:
                // 接受请求
                if (requester != null && requester.isOnline()) {
                    requester.teleport(player.getLocation());
                    requester.sendMessage("§a你的传送请求已被 " + player.getName() + " 接受！");
                    player.sendMessage("§a你已接受 " + requester.getName() + " 的传送请求");
                    Bukkit.broadcastMessage("§e玩家 " + requester.getName() + " 传送到了 " + player.getName() + " 的位置");
                } else {
                    player.sendMessage("§c发送请求的玩家已离线！");
                }
                break;
                
            case REDSTONE_BLOCK:
                // 拒绝请求
                if (requester != null && requester.isOnline()) {
                    requester.sendMessage("§c你的传送请求已被 " + player.getName() + " 拒绝！");
                    player.sendMessage("§c你已拒绝 " + requester.getName() + " 的传送请求");
                    Bukkit.broadcastMessage("§e玩家 " + player.getName() + " 拒绝了 " + requester.getName() + " 的传送请求");
                } else {
                    player.sendMessage("§c发送请求的玩家已离线！");
                }
                break;
        }
        
        player.closeInventory();
        plugin.getTeleportRequests().remove(playerUUID);
    }
    
    private void handleShopGUI(Player player, ItemStack clickedItem, int slot, boolean isLeftClick) {
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        // 获取商店物品列表
        var shopItems = plugin.getShopManager().getShopItems();
        if (slot < 0 || slot >= shopItems.size()) {
            return;
        }
        
        // 左键购买指定数量，右键购买全部
        if (isLeftClick) {
            player.closeInventory();
            player.sendMessage("§e请输入购买数量: §a/ef shop in " + (slot + 1) + " <数量>");
        } else {
            // 右键购买全部
            boolean success = plugin.getShopManager().buyShopItem(player, slot, shopItems.get(slot).getRemainingQuantity());
            if (success) {
                player.closeInventory();
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // 可以在这里添加商店GUI关闭时的逻辑
    }
}