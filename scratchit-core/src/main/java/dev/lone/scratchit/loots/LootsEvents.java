package dev.lone.scratchit.loots;

import dev.lone.scratchit.loots.data.FishingLootEntry;
import dev.lone.scratchit.loots.data.IRule;
import dev.lone.scratchit.loots.data.MobLootEntry;
import dev.lone.scratchit.loots.data.VanillaBlockLootEntry;
import dev.lone.scratchit.Main;
import dev.lone.scratchit.util.WeakList;
import dev.lone.scratchit.util.EventsUtil;
import dev.lone.scratchit.util.Utils;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class LootsEvents implements Listener
{
    private final WeakList<Entity> spawnedFromSpawner = new WeakList<>();
    private IALootsEvents iaLootsEvents;

    public LootsEvents()
    {

    }

    public void registerEvents()
    {
        EventsUtil.registerEventOnce(this, Main.inst);
        if(Main.hasItemsAdder)
        {
            if(iaLootsEvents != null)
                EventsUtil.unregisterEvent(iaLootsEvents);
            iaLootsEvents = new IALootsEvents();
            EventsUtil.registerEventOnce(iaLootsEvents, Main.inst);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onKill(EntityDeathEvent e)
    {
        //if(e.isCancelled())
        //    return;

        Entity killed = e.getEntity();
        if(e.getEntity().getKiller() == null || e.getEntity().getKiller().getType() != EntityType.PLAYER)
            return;

        if(!Main.inst.cardsStorage.lootsManager.mobLootEntries.containsKey(killed.getType()))
            return;

        for(MobLootEntry lootEntry : Main.inst.cardsStorage.lootsManager.mobLootEntries.get(killed.getType()))
        {
            if(lootEntry.isIgnoreSpawnerMobs() && spawnedFromSpawner.contains(e.getEntity()))
                return;

            boolean matched = true;
            if(lootEntry.hasRules())
            {
                for(IRule rule : lootEntry.getRules())
                {
                    if(!rule.matchRule(killed))
                    {
                        matched = false;
                        break;
                    }
                }
            }

            if(matched)
            {
                lootEntry.dropResultItemsWithChance(e.getEntity().getKiller().getItemInHand(), e.getEntity().getLocation());
                lootEntry.dropResultExpWithChance(e.getEntity().getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void blockBreak(BlockBreakEvent e)
    {
        if(e.isCancelled())
            return;

        if(e.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        Block block = e.getBlock();
        //if(!e.getPlayer().hasPermission(Permissions.BYPASSBLOCKPLACELOOT) && block.hasMetadata("IAJustPlaced"))
        //    return;

        if(!Main.inst.cardsStorage.lootsManager.blockLootEntries.containsKey(block.getType()))
            return;

        for(VanillaBlockLootEntry entry : Main.inst.cardsStorage.lootsManager.blockLootEntries.get(block.getType()))
        {
            if(!entry.canToolGetUnbrokenBlock(e.getPlayer().getItemInHand(), block.getType()))
            {
                boolean can = true;
                if(entry.hasNBTMatchRules())
                {
                    for(IRule rule : entry.getNBTMatchRules())
                    {
                        if(!rule.matchRule(block))
                        {
                            can = false;
                            break;
                        }
                    }
                }

                if(can)
                {
                    entry.dropResultItemsWithChance(e.getPlayer().getItemInHand(), block.getLocation());
                    entry.dropResultExpWithChance(block.getLocation());
                }
            }
            //break; //non devo uscire, potrebbero esserci altre lootEntries registrate per lo stesso blocco
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void fishing(PlayerFishEvent e)
    {
        //FishingUtil.getInstance().get().setBiteTime(e.getHook(), 5);

        if(e.getState() != PlayerFishEvent.State.CAUGHT_ENTITY && e.getState() != PlayerFishEvent.State.CAUGHT_FISH)
            return;

        if(e.getCaught() == null && e.getCaught().getTicksLived() > 3)//> 3 significa che è un drop a terra
            return;

        if(e.getCaught().getType() != EntityType.ITEM)
            return;

        if(!((Item) e.getCaught()).getItemStack().getType().isEdible())
            return;

        if(Main.inst.cardsStorage.lootsManager.fishingLootEntries.size() > 0)
        {
            int rndLootEntry = Utils.getRandomInt(0, Main.inst.cardsStorage.lootsManager.fishingLootEntries.size() - 1);
            FishingLootEntry entry = Main.inst.cardsStorage.lootsManager.fishingLootEntries.get(rndLootEntry);

            entry.replaceFishWithChance((Item) e.getCaught(), e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void entitySpawn(SpawnerSpawnEvent e)
    {
        spawnedFromSpawner.add(e.getEntity());
    }
}
