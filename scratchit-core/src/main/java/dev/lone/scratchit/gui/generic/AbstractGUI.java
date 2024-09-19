package dev.lone.scratchit.gui.generic;

import dev.lone.scratchit.util.InvUtil;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractGUI
{
    protected void fillInventory(FastInv fastInv, @Nullable ItemStack itemStack)
    {
        if(itemStack == null)
            return;
        InvUtil.renameItemStack(itemStack, " ");
        for (int i = 0; i < 54; i++)
            fastInv.addItem(itemStack);
    }
}
