package org.mcsci.efficiency.commands;

import org.mcsci.efficiency.Efficiency;
import org.mcsci.efficiency.utils.TeleportRequest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class CustomTeleportCommand implements CommandExecutor {
    
    private Efficiency plugin;
    
    public CustomTeleportCommand(Efficiency plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用此命令！");
            return true;
        }
        
        Player requester = (Player) sender;
        
        if (args.length != 1) {
            requester.sendMessage("§c用法: /cs <玩家名>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            requester.sendMessage("§c玩家 " + args[0] + " 不在线或不存在！");
            return true;
        }
        
        if (requester.equals(target)) {
            requester.sendMessage("§c你不能向自己发送传送请求！");
            return true;
        }
        
        // 创建传送请求
        TeleportRequest request = new TeleportRequest(requester, target);
        plugin.getTeleportRequests().put(target.getUniqueId(), request);
        
        // 发送提示消息
        requester.sendMessage("§a已向玩家 " + target.getName() + " 发送传送请求！");
        target.sendMessage("§e玩家 " + requester.getName() + " 向你发送了传送请求！");
        target.sendMessage("§e输入 /csgui 来查看请求界面");
        
        // 为目标玩家打开GUI界面
        openTeleportGUI(target);
        
        return true;
    }
    
    private void openTeleportGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6传送请求处理");
        
        // 创建接受请求物品（绿宝石块）
        ItemStack acceptItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta acceptMeta = acceptItem.getItemMeta();
        acceptMeta.setDisplayName("§a接受传送请求");
        acceptMeta.setLore(Arrays.asList("§7点击接受玩家的传送请求", "§7接受后玩家将传送到你的位置"));
        acceptItem.setItemMeta(acceptMeta);
        gui.setItem(11, acceptItem);
        
        // 创建拒绝请求物品（红石块）
        ItemStack denyItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta denyMeta = denyItem.getItemMeta();
        denyMeta.setDisplayName("§c拒绝传送请求");
        denyMeta.setLore(Arrays.asList("§7点击拒绝玩家的传送请求", "§7拒绝后请求将被取消"));
        denyItem.setItemMeta(denyMeta);
        gui.setItem(15, denyItem);
        
        // 填充边界
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, border);
            }
        }
        
        player.openInventory(gui);
    }
}