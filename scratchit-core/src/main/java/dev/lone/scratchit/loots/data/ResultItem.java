package dev.lone.scratchit.loots.data;

import dev.lone.scratchit.util.InvUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ResultItem
{
    ItemStack itemStack;
    final int minAmount;
    final int maxAmount;
    final float chance;
    final boolean ignoreFortune;

    public ResultItem(ItemStack itemStack, int minAmount, int maxAmount, float chance, boolean ignoreFortune)
    {
        this.itemStack = itemStack;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.chance = chance;
        this.ignoreFortune = ignoreFortune;
    }

    public static int getDropCountBasedOnFortune(int fortune, int minDropCount, int maxDropCount)
    {
        Random random = new Random();
        if (fortune < 1) //no fortune enchant
            return random.nextInt((maxDropCount - minDropCount) + 1) + minDropCount;

        int i = random.nextInt(fortune + 2) - 1;
        if (i < 0)
            i = 0;
        return random.nextInt((maxDropCount - minDropCount) + 2) + minDropCount * (i + 1);
    }

    public int getDropCountBasedOnFortune(ItemStack toolUsed, int minDropCount, int maxDropCount)
    {
        int level = -1;
        if (!ignoreFortune)
        {
            if (toolUsed != null && toolUsed.getType() != Material.AIR)
                level = InvUtil.getEnchantLevel(toolUsed, Enchantment.FORTUNE);
        }
        return getDropCountBasedOnFortune(
                level,
                minDropCount,
                maxDropCount
        );
    }

    /**
     * Ritorna un clone dell'itemstack con amount random basato sui valori di configurazione
     *
     * @param randomizedAmount Se true ritorna un clone dell'item con amount random, calcolato con i valori impostati dal costruttore
     * @return Clone con amount random (calcolato in base ai parametri del costruttore) oppure itemstack originale
     */
    public ItemStack getItemStack(ItemStack toolUsed, boolean randomizedAmount)
    {
        if (randomizedAmount)
        {
            ItemStack clone = itemStack.clone();
            clone.setAmount(getDropCountBasedOnFortune(toolUsed, minAmount, maxAmount));
            return clone;
        }
        return this.itemStack;
    }

    /**
     * Ritorna un clone dell'itemstack con amount random basato sui valori di configurazione
     *
     * @param randomizedAmount Se true ritorna un clone dell'item con amount random, calcolato con i valori impostati dal costruttore
     * @return Clone con amount random (calcolato in base ai parametri del costruttore) oppure itemstack originale
     */
    public ItemStack getItemStack(boolean randomizedAmount)
    {
        return getItemStack(null, randomizedAmount);
    }

    public float getChance()
    {
        return chance;
    }
}
