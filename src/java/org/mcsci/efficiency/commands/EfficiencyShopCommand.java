package org.mcsci.efficiency.commands;

import org.mcsci.efficiency.Efficiency;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EfficiencyShopCommand implements CommandExecutor {
    
    private Efficiency plugin;
    
    public EfficiencyShopCommand(Efficiency plugin) {
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
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "shop":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /ef shop <in|out|list>");
                    return true;
                }
                
                switch (args[1].toLowerCase()) {
                    case "out":
                        handleSellItem(player, args);
                        break;
                    case "in":
                        handleBuyItem(player, args);
                        break;
                    case "list":
                        plugin.getShopManager().openShopGUI(player);
                        break;
                    default:
                        player.sendMessage("§c未知的子命令！使用: /ef shop <in|out|list>");
                        break;
                }
                break;
                
            case "help":
                sendHelp(player);
                break;
                
            default:
                player.sendMessage("§c未知命令！使用 /ef help 查看帮助");
                break;
        }
        
        return true;
    }
    
    private void handleSellItem(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§c用法: /ef shop out <价格> <数量>");
            player.sendMessage("§7示例: /ef shop out 5 32 - 以5钻石/个的价格卖出32个手中物品");
            return;
        }
        
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType().isAir()) {
            player.sendMessage("§c请手持要卖出的物品！");
            return;
        }
        
        try {
            double price = Double.parseDouble(args[2]);
            int quantity = Integer.parseInt(args[3]);
            
            if (price <= 0) {
                player.sendMessage("§c价格必须大于0！");
                return;
            }
            
            if (quantity <= 0) {
                player.sendMessage("§c数量必须大于0！");
                return;
            }
            
            boolean success = plugin.getShopManager().addShopItem(player, handItem, price, quantity);
            if (success) {
                plugin.getShopManager().saveShopData(); // 立即保存数据
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage("§c价格和数量必须是有效的数字！");
        }
    }
    
    private void handleBuyItem(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c用法: /ef shop in <商品序号> <数量>");
            player.sendMessage("§7使用 /ef shop list 查看商品列表");
            return;
        }
        
        try {
            int itemIndex = Integer.parseInt(args[2]) - 1; // 转换为0-based索引
            int quantity = args.length >= 4 ? Integer.parseInt(args[3]) : 1;
            
            boolean success = plugin.getShopManager().buyShopItem(player, itemIndex, quantity);
            if (success) {
                plugin.getShopManager().saveShopData(); // 立即保存数据
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage("§c商品序号和数量必须是有效的数字！");
        }
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6=== Efficiency插件帮助 ===");
        player.sendMessage("§a/ef shop out <价格> <数量> §7- 卖出手中物品");
        player.sendMessage("§a/ef shop in <序号> [数量] §7- 购买商店物品");
        player.sendMessage("§a/ef shop list §7- 查看商店列表");
        player.sendMessage("§a/cs <玩家名> §7- 发送传送请求");
        player.sendMessage("§a/qk open §7- 开启区块显示");
        player.sendMessage("§a/qk stop §7- 关闭区块显示");
    }
}