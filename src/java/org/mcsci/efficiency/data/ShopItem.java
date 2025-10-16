package org.mcsci.efficiency.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class ShopItem {
    private UUID seller;
    private String sellerName;
    private ItemStack item;
    private double pricePerItem; // 每个物品的价格（钻石）
    private int totalQuantity;
    private int remainingQuantity;
    private long createTime;
    
    public ShopItem(UUID seller, String sellerName, ItemStack item, double pricePerItem, int totalQuantity) {
        this.seller = seller;
        this.sellerName = sellerName;
        this.item = item.clone();
        this.item.setAmount(1); // 确保物品数量为1，便于计算
        this.pricePerItem = pricePerItem;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = totalQuantity;
        this.createTime = System.currentTimeMillis();
    }
    
    // Getters
    public UUID getSeller() { return seller; }
    public String getSellerName() { return sellerName; }
    public ItemStack getItem() { return item.clone(); }
    public double getPricePerItem() { return pricePerItem; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getRemainingQuantity() { return remainingQuantity; }
    public long getCreateTime() { return createTime; }
    
    // 减少剩余数量
    public void reduceQuantity(int amount) {
        this.remainingQuantity -= amount;
    }
    
    // 检查是否还有库存
    public boolean hasStock() {
        return remainingQuantity > 0;
    }
    
    // 获取显示物品（用于GUI）
    public ItemStack getDisplayItem(int index) {
        ItemStack displayItem = item.clone();
        ItemMeta meta = displayItem.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e" + (index + 1) + ". " + getItemDisplayName());
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add("§7卖家: " + sellerName);
            lore.add("§7价格: §b" + pricePerItem + " 钻石/个");
            lore.add("§7库存: §a" + remainingQuantity + "/" + totalQuantity + " 个");
            lore.add("§7总价: §b" + (pricePerItem * remainingQuantity) + " 钻石");
            lore.add("");
            lore.add("§a左键购买指定数量");
            lore.add("§6右键购买全部剩余数量");
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }
        
        return displayItem;
    }
    
    private String getItemDisplayName() {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().toString().toLowerCase().replace("_", " ");
    }
    
    // 检查是否过期（7天）
    public boolean isExpired() {
        return System.currentTimeMillis() - createTime > 7 * 24 * 60 * 60 * 1000L;
    }
}