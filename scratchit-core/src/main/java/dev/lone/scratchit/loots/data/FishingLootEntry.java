package dev.lone.scratchit.loots.data;

import lombok.experimental.SuperBuilder;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

@SuperBuilder(toBuilder = true)
public class FishingLootEntry extends LootEntry
{
    public FishingLootEntry()
    {
    }

    public void replaceFishWithChance(Item caughtFish, Player player)
    {
        for(ResultItem resultItem : getResultItems())
        {
            double rnd = Math.random();
            if (rnd * 100 > resultItem.getChance())
                continue;
            caughtFish.setItemStack(resultItem.getItemStack(player.getItemInHand(), true));
        }
    }
}
