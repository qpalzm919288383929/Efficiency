package org.mcsci.efficiency.managers;

import org.mcsci.efficiency.Efficiency;
import org.mcsci.efficiency.data.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ShopManager {
    private Efficiency plugin;
    private List<ShopItem> shopItems;
    private File shopFile;
    private FileConfiguration shopConfig;
    
    public ShopManager(Efficiency plugin) {
        this.plugin = plugin;
        this.shopItems = new ArrayList<>();
        this.shopFile = new File(plugin.getDataFolder(), "shop.yml");
        this.shopConfig = YamlConfiguration.loadConfiguration(shopFile);
    }
    
    public void loadShopData() {
        if (!shopFile.exists()) {
            return;
        }
        
        shopItems.clear();
        List<?> itemsList = shopConfig.getList("shop-items", new ArrayList<>());
        
        for (Object obj : itemsList) {
            if (obj instanceof Map) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) obj;
                    
                    UUID seller = UUID.fromString((String) map.get("seller"));
                    String sellerName = (String) map.get("sellerName");
                    ItemStack item = (ItemStack) map.get("item");
                    double pricePerItem = (double) map.get("pricePerItem");
                    int totalQuantity = (int) map.get("totalQuantity");
                    int remainingQuantity = (int) map.get("remainingQuantity");
                    
                    ShopItem shopItem = new ShopItem(seller, sellerName, item, pricePerItem, totalQuantity);
                    // 手动设置剩余数量
                    for (int i = 0; i < totalQuantity - remainingQuantity; i++) {
                        shopItem.reduceQuantity(1);
                    }
                    
                    shopItems.add(shopItem);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "加载商店物品时出错: " + e.getMessage());
                }
            }
        }
        
        plugin.getLogger().info("已加载 " + shopItems.size() + " 个商店物品");
    }
    
    public void saveShopData() {
        // 清理过期物品
        cleanExpiredItems();
        
        List<Map<String, Object>> itemsList = new ArrayList<>();
        
        for (ShopItem shopItem : shopItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("seller", shopItem.getSeller().toString());
            map.put("sellerName", shopItem.getSellerName());
            map.put("item", shopItem.getItem());
            map.put("pricePerItem", shopItem.getPricePerItem());
            map.put("totalQuantity", shopItem.getTotalQuantity());
            map.put("remainingQuantity", shopItem.getRemainingQuantity());
            
            itemsList.add(map);
        }
        
        shopConfig.set("shop-items", itemsList);
        
        try {
            shopConfig.save(shopFile);
            plugin.getLogger().info("已保存 " + shopItems.size() + " 个商店物品");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存商店数据时出错: " + e.getMessage());
        }
    }
    
    public boolean addShopItem(Player seller, ItemStack item, double pricePerItem, int quantity) {
        // 检查玩家是否有足够的物品
        int playerQuantity = countPlayerItems(seller, item);
        if (playerQuantity < quantity) {
            seller.sendMessage("§c你只有 " + playerQuantity + " 个该物品，无法卖出 " + quantity + " 个！");
            return false;
        }
        
        // 移除玩家物品
        removePlayerItems(seller, item, quantity);
        
        // 创建商店物品
        ShopItem shopItem = new ShopItem(seller.getUniqueId(), seller.getName(), item, pricePerItem, quantity);
        shopItems.add(shopItem);
        
        seller.sendMessage("§a成功上架物品！价格: §b" + pricePerItem + " 钻石/个 §a数量: " + quantity + " 个");
        return true;
    }
    
    public boolean buyShopItem(Player buyer, int itemIndex, int quantity) {
        if (itemIndex < 0 || itemIndex >= shopItems.size()) {
            buyer.sendMessage("§c商品序号无效！");
            return false;
        }
        
        ShopItem shopItem = shopItems.get(itemIndex);
        
        if (!shopItem.hasStock()) {
            buyer.sendMessage("§c该商品已售罄！");
            return false;
        }
        
        if (quantity <= 0 || quantity > shopItem.getRemainingQuantity()) {
            buyer.sendMessage("§c购买数量无效！可用数量: " + shopItem.getRemainingQuantity());
            return false;
        }
        
        double totalPrice = shopItem.getPricePerItem() * quantity;
        
        // 检查买家是否有足够的钻石
        int diamondCount = countPlayerDiamonds(buyer);
        if (diamondCount < totalPrice) {
            buyer.sendMessage("§c钻石不足！需要: " + totalPrice + " 钻石，你只有: " + diamondCount + " 钻石");
            return false;
        }
        
        // 移除买家钻石
        removePlayerDiamonds(buyer, (int) totalPrice);
        
        // 给买家物品
        ItemStack boughtItem = shopItem.getItem().clone();
        boughtItem.setAmount(quantity);
        givePlayerItem(buyer, boughtItem);
        
        // 给卖家钻石
        Player seller = Bukkit.getPlayer(shopItem.getSeller());
        if (seller != null && seller.isOnline()) {
            seller.sendMessage("§a你的 " + shopItem.getItem().getType() + " 已售出 " + quantity + " 个，获得 " + totalPrice + " 钻石！");
            givePlayerDiamonds(seller, (int) totalPrice);
        } else {
            // 如果卖家不在线，保存到离线数据（简化版，实际需要更复杂的离线经济系统）
            // 这里可以扩展为保存到数据库或文件
        }
        
        // 更新库存
        shopItem.reduceQuantity(quantity);
        
        buyer.sendMessage("§a成功购买 " + quantity + " 个 " + getItemDisplayName(shopItem.getItem()) + "！花费: " + totalPrice + " 钻石");
        
        // 如果库存为0，移除物品
        if (!shopItem.hasStock()) {
            shopItems.remove(itemIndex);
        }
        
        return true;
    }
    
    public void openShopGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6效率商店");
        
        for (int i = 0; i < shopItems.size() && i < 54; i++) {
            gui.setItem(i, shopItems.get(i).getDisplayItem(i));
        }
        
        player.openInventory(gui);
    }
    
    public List<ShopItem> getShopItems() {
        return new ArrayList<>(shopItems);
    }
    
    private int countPlayerItems(Player player, ItemStack targetItem) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(targetItem)) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    private void removePlayerItems(Player player, ItemStack targetItem, int quantity) {
        int remaining = quantity;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(targetItem)) {
                int removeAmount = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - removeAmount);
                remaining -= removeAmount;
                
                if (remaining <= 0) break;
            }
        }
        player.updateInventory();
    }
    
    private int countPlayerDiamonds(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.DIAMOND) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    private void removePlayerDiamonds(Player player, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.DIAMOND) {
                int removeAmount = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - removeAmount);
                remaining -= removeAmount;
                
                if (remaining <= 0) break;
            }
        }
        player.updateInventory();
    }
    
    private void givePlayerItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            // 如果背包满了，掉落物品
            for (ItemStack left : leftover.values()) {
                player.getWorld().dropItem(player.getLocation(), left);
            }
            player.sendMessage("§e背包已满，部分物品已掉落在地面上！");
        }
    }
    
    private void givePlayerDiamonds(Player player, int amount) {
        ItemStack diamonds = new ItemStack(Material.DIAMOND, amount);
        givePlayerItem(player, diamonds);
    }
    
    private String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().toString().toLowerCase().replace("_", " ");
    }
    
    private void cleanExpiredItems() {
        shopItems.removeIf(ShopItem::isExpired);
    }
}