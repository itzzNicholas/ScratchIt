package dev.lone.scratchit.listener;

import dev.lone.scratchit.util.EventsUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.WeakHashMap;

public class DamageListeners implements Listener
{
    private final WeakHashMap<Player, AttackData> attackDataByDamaged = new WeakHashMap<>();
    private final WeakHashMap<Entity, AttackData> attackDataByAttacker = new WeakHashMap<>();

    public void register(Plugin plugin)
    {
        EventsUtil.registerEventOnce(this, plugin);
    }

    public void unregister()
    {
        EventsUtil.unregisterEvent(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDeath(EntityDeathEvent e)
    {
        remove(e.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void damage(EntityDamageByEntityEvent e)
    {
        if(e.getEntity().getType() != EntityType.PLAYER)
            return;

        AttackData data =  new AttackData(
                (Player) e.getEntity(), e.getDamager()
        );

        attackDataByDamaged.put((Player) e.getEntity(), data);
        attackDataByAttacker.put(e.getDamager(), data);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerQuit(PlayerQuitEvent e)
    {
        remove(e.getPlayer());
    }

    public boolean hasLastAttackerNear(Player player)
    {
        if(!attackDataByDamaged.containsKey(player))
            return false;

        AttackData data = attackDataByDamaged.get(player);
        return (data.isNearAttacker(player));
    }

    public boolean hasPlayerRecentlyGotAttack(Player player)
    {
        if(!attackDataByDamaged.containsKey(player))
            return false;

        AttackData data = attackDataByDamaged.get(player);
        return (data.hasRecently());
    }

    private void remove(LivingEntity entity)
    {
        AttackData data = attackDataByAttacker.get(entity);
        if(data == null)
            return;

        attackDataByDamaged.remove(data.player);
        attackDataByAttacker.remove(entity);
    }
}


