package dev.lone.scratchit.loots.data;

import dev.lone.itemsadder.api.CustomBlock;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public class CustomBlockLootEntry extends BlockLootEntry
{
    final String blockNamespacedId;

    public CustomBlockLootEntry(CustomBlock blockItem)
    {
        this.blockNamespacedId = blockItem.getNamespacedID();
    }

    public String getBlockNamespacedId()
    {
        return blockNamespacedId;
    }
}
