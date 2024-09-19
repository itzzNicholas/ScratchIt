package dev.lone.scratchit.loots.data;

import lombok.experimental.SuperBuilder;
import org.bukkit.Material;

@SuperBuilder(toBuilder = true)
public class VanillaBlockLootEntry extends BlockLootEntry
{
    final Material material;

    public VanillaBlockLootEntry(Material material)
    {
        this.material = material;
    }

    public Material getMaterial()
    {
        return material;
    }
}
