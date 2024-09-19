package dev.lone.scratchit.loots.data;

import dev.lone.scratchit.util.InvUtil;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@SuperBuilder(toBuilder = true)
public class BlockLootEntry extends NBTLootEntry
{
    public BlockLootEntry()
    {
        super();
    }

    /**
     * Usato per prevenire duplicazione con silktouch
     */
    public boolean canToolGetUnbrokenBlock(ItemStack tool, Material material)
    {
        if(tool.getType() == Material.SHEARS)
        {
            switch (material)
            {
                case ACACIA_LEAVES:
                case BIRCH_LEAVES:
                case DARK_OAK_LEAVES:
                case JUNGLE_LEAVES:
                case OAK_LEAVES:
                case SPRUCE_LEAVES:
                    return true;
            }
        }
        else return InvUtil.hasEnchant(tool, Enchantment.SILK_TOUCH);

        return false;
    }
}
