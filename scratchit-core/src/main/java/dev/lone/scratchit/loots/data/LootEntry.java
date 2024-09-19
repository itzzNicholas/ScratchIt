package dev.lone.scratchit.loots.data;

import dev.lone.scratchit.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@SuperBuilder(toBuilder = true)
public class LootEntry
{
    @Getter
    protected final List<ResultItem> resultItems = new ArrayList<>();
    @Getter
    protected final List<ResultExpDrop> resultExpDrops = new ArrayList<>();
    protected final HashSet<Biome> biomes = new HashSet<>();
    @Getter
    @Setter
    protected boolean dropOnlyFirst;

    public LootEntry(){}

    public void addResultItem(ResultItem resultItem)
    {
        this.resultItems.add(resultItem);
    }

    public ResultExpDrop addResultExpDrop(ResultExpDrop resultItem)
    {
        this.resultExpDrops.add(resultItem);
        return resultItem;
    }

    public void dropResultItemsWithChance(Location location)
    {
        for(ResultItem resultItem : resultItems)
        {
            double rnd = Math.random();
            if (rnd * 100 > resultItem.getChance())
                continue;
            location.getWorld().dropItemNaturally(location, resultItem.getItemStack(true));
        }
    }

    public void dropResultItemsWithChance(ItemStack toolUsed, Location location)
    {
        if(!isValidBiome(location.getBlock()))
            return;

        for(ResultItem resultItem : resultItems)
        {
            double rnd = Math.random();
            if (rnd * 100 > resultItem.getChance())
                continue;
            ItemStack item = resultItem.getItemStack(toolUsed, true);
            if(item.getAmount() > 0)
            {
                location.getWorld().dropItemNaturally(location, item);
                if(isDropOnlyFirst())
                    return;
            }
        }
    }

    public List<ItemStack> getResultItemsWithChance(ItemStack toolUsed, Location location)
    {
        List<ItemStack> loot = new ArrayList<>();

        if(!isValidBiome(location.getBlock()))
            return loot;

        for(ResultItem resultItem : resultItems)
        {
            double rnd = Math.random();
            if (rnd * 100 > resultItem.getChance())
                continue;
            ItemStack item = resultItem.getItemStack(toolUsed, true);
            if(item.getAmount() > 0)
                loot.add(item);
        }
        return loot;
    }

    public void dropResultExpWithChance(Location location)
    {
        if(!isValidBiome(location.getBlock()))
            return;

        for(ResultExpDrop resultExpDrop : resultExpDrops)
        {
            double rnd = Math.random();
            if (rnd * 100 > resultExpDrop.getChance())
                continue;

            location.getWorld().spawn(location, ExperienceOrb.class).setExperience(Utils.getRandomInt(resultExpDrop.getMinAmount(), resultExpDrop.getMaxAmount()));
        }
    }

    public void addBiome(Biome biome)
    {
        biomes.add(biome);
    }

    public boolean isValidBiome(Block block)
    {
        if(biomes.isEmpty())
            return true;
        try
        {
            return biomes.contains(block.getBiome());
        }catch(NullPointerException ignored){} // TODO: Handle custom biomes!
        return false;
    }
}
