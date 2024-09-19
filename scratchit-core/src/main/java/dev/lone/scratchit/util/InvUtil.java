package dev.lone.scratchit.util;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InvUtil
{
    public static boolean hasEnchant(ItemStack tool, Enchantment enchantment)
    {
        if(!tool.hasItemMeta() || !tool.getItemMeta().hasEnchants())
            return false;
        return tool.getItemMeta().hasEnchant(enchantment);
    }

    public static int getEnchantLevel(ItemStack tool, Enchantment enchantment)
    {
        return tool.getItemMeta().getEnchantLevel(enchantment);
    }

    public static void renameItemStack(ItemStack item, String name)
    {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    public static void giveItem(Player player, ItemStack itemStack) {
        player.getInventory().addItem(itemStack).forEach((index, overflow) -> {
            Item item = player.getWorld().dropItem(player.getLocation(), overflow);
            try
            {
                item.setOwner(player.getUniqueId());
            }
            catch(NoSuchMethodError ignored){}
            item.setPickupDelay(0);
        });
    }

    public static String getReadableName(ItemStack item)
    {
        String name;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            name = item.getItemMeta().getDisplayName();
        else
            name = WordUtils.capitalizeFully(item.getType().toString()).replace("_", " ");

        if(!name.startsWith(ChatColor.WHITE.toString()))
            return ChatColor.stripColor(name);
        return name;
    }
}
