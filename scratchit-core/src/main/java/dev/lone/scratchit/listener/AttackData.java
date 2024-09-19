package dev.lone.scratchit.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

class AttackData
{
    public Player player;
    public Entity enemy;
    public long ms;

    public AttackData(Player player, Entity enemy)
    {
        this.player = player;
        this.enemy = enemy;
        this.ms = System.currentTimeMillis();
    }

    public boolean hasRecently()
    {
        return (System.currentTimeMillis() - ms) < 60 * 1000;//TODO: dynamic
    }

    public boolean isNearAttacker(Player player)
    {
        if (!enemy.getLocation().getWorld().getName().equals(player.getLocation().getWorld().getName()))
            return false;
        return enemy.getLocation().distance(player.getLocation()) < 128;//TODO: dynamic
    }
}
