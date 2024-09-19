package dev.lone.scratchit.loots.data;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public interface IRule
{
    boolean matchRule(Entity entity);

    boolean matchRule(Block block);
}
