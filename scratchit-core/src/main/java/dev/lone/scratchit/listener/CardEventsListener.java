package dev.lone.scratchit.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import dev.lone.scratchit.Main;
import dev.lone.scratchit.compat.protection.ProtectionPlugins;
import dev.lone.scratchit.api.events.ScratchCardUseEvent;
import dev.lone.scratchit.card.CardData;
import dev.lone.scratchit.config.Settings;
import dev.lone.scratchit.map.PlayerMapRenderer;
import dev.lone.scratchit.map.card.CardRenderer;
import dev.lone.scratchit.util.EventsUtil;
import dev.lone.scratchit.util.Msg;
import dev.lone.scratchit.util.Scheduler;
import dev.lone.scratchit.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

public class CardEventsListener implements Listener
{
    private DamageListeners damageListeners;

    public void register(Plugin plugin)
    {
        EventsUtil.registerEventOnce(this, plugin);

        this.damageListeners = new DamageListeners();
        damageListeners.register(plugin);

        //drop fake item or left click
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                Main.inst, ListenerPriority.MONITOR, PacketType.Play.Client.ARM_ANIMATION)
        {
            @Override
            public void onPacketReceiving(PacketEvent e)
            {
                PlayerMapRenderer renderer = Main.inst.mapsRenderingContainer.getRenderer(e.getPlayer());
                if (renderer == null)
                    return;
                renderer.refreshFakeItemInHand(2L);
            }
        });

        //left click
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                Main.inst, ListenerPriority.MONITOR, PacketType.Play.Client.USE_ENTITY)
        {
            @Override
            public void onPacketReceiving(PacketEvent e)
            {
                CardRenderer renderer = Main.inst.mapsRenderingContainer.getCardRenderer(e.getPlayer());
                if (renderer == null)
                    return;

                if (isAttack(e))
                {
                    if (renderer.cardData.allowLeftClick)
                    {
                        renderer.refreshFakeItemInHand(0);
                        //wait the attack anim to end
                        Bukkit.getScheduler().runTaskLater(Main.inst, renderer::updateLastInteract, 5L);
                    }
                }
                else
                {
                    renderer.updateLastInteract();
                }
            }
        });
    }

    public void unregister()
    {
        EventsUtil.unregisterEvent(this);
        damageListeners.unregister();
    }

    private static boolean isAttack(PacketEvent e)
    {
        if (Main.is_v17_or_greather)
        {
            WrappedEnumEntityUseAction useAction = e.getPacket().getEnumEntityUseActions().read(0);
            return useAction.getAction() == EnumWrappers.EntityUseAction.ATTACK;
        }
        else
        {
            return e.getPacket().getEntityUseActions().getValues().get(0) == EnumWrappers.EntityUseAction.ATTACK;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockPlaceEvent_scratch(BlockPlaceEvent e)
    {
        if (!CardData.isScratchCard(e.getItemInHand()))
            return;
        e.setCancelled(true);

        PlayerMapRenderer renderer = Main.inst.mapsRenderingContainer.getRenderer(e.getPlayer());
        if (renderer == null)
            return;
        renderer.refreshFakeItemInHand(2L);
    }

    @EventHandler
    private void onInteract_startUsing(PlayerInteractEvent e)
    {
        if (e.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)
        {
            //already scratching
            PlayerMapRenderer tmp = Main.inst.mapsRenderingContainer.getRenderer(e.getPlayer());
            if (tmp != null)
                return;

            //check if has card in hand
            if (!CardData.isScratchCard(e.getItem()))
                return;

            if (damageListeners.hasLastAttackerNear(e.getPlayer()) && damageListeners.hasPlayerRecentlyGotAttack(e.getPlayer()))
            {
                e.getPlayer().sendMessage(Main.lang.getLocalized("recently_attacked"));
                e.setCancelled(true);
                return;
            }

            CardData card = Main.inst.cardsStorage.getCard(CardData.getId(e.getItem()));
            if (card == null)
            {
                if (Main.inst.cardsStorage.isLoading())
                {
                    e.getPlayer().sendMessage(Main.lang.getLocalized("items_still_loading_on_usage"));
                    e.setCancelled(true);
                    return;
                }
                e.getPlayer().sendMessage(Main.lang.getLocalized("removed_from_config"));
                e.setCancelled(true);
                return;
            }

            if (!card.permissions.checkUsePermission(e.getPlayer(), s -> {
                e.getPlayer().sendMessage(Main.lang.getLocalized("no_permission").replace("{permission}", s));
            }))
            {
                e.setCancelled(true);
                return;
            }

            if(!ProtectionPlugins.canScratchCard(e))
            {
                e.getPlayer().sendMessage(Main.lang.getLocalized("cant_use_this_item_in_this_region"));
                e.setCancelled(true);
                return;
            }

            e.setCancelled(true);
            long remainingCooldown = Main.inst.mapsRenderingContainer.handleRemainingCooldown(
                    e.getPlayer(), card,
                    remaining -> {
                        Msg.get().send(e.getPlayer(), Main.lang.getLocalized("card_please_wait_cooldown_global").replace("{time}", Utils.formatReadableTime(remaining)));
                    },
                    remaining -> {
                        Msg.get().send(e.getPlayer(), Main.lang.getLocalized("card_please_wait_cooldown").replace("{time}", Utils.formatReadableTime(remaining)));
                    }
            );
            if (remainingCooldown > 0)
                return;

            if(EventsUtil.call(new ScratchCardUseEvent(e.getPlayer(), e.getItem(), card.getId())))
            {
                Main.inst.mapsRenderingContainer.addPlayer(e.getPlayer(), card);

                PlayerMapRenderer renderer = Main.inst.mapsRenderingContainer.getRenderer(e.getPlayer());
                renderer.start();
                renderer.refreshFakeItemInHand(0);
            }
        }
        else //left click
        {
            PlayerMapRenderer renderer = Main.inst.mapsRenderingContainer.getRenderer(e.getPlayer());
            if (renderer == null)
                return;
            renderer.refreshFakeItemInHand(2L);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onEntityDamage(EntityDamageEvent e)
    {
        if (e.getEntity().getType() != EntityType.PLAYER)
            return;
        CardRenderer renderer = Main.inst.mapsRenderingContainer.getCardRenderer((Player) e.getEntity());
        if (renderer == null)
            return;
        //e.setCancelled(true);
        renderer.forceHandleWinLose();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e)
    {
        PlayerMapRenderer renderer = Main.inst.mapsRenderingContainer.getRenderer(e.getPlayer());
        if (renderer == null)
            return;
        e.setCancelled(true);
        e.getPlayer().updateInventory();
        renderer.refreshFakeItemInHand(0);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerItemHeld(PlayerItemHeldEvent e)
    {
        PlayerMapRenderer renderer = Main.inst.mapsRenderingContainer.getRenderer(e.getPlayer());
        if (renderer == null)
            return;
        e.setCancelled(true);
        e.getPlayer().updateInventory();
        renderer.refreshFakeItemInHand(0);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerChangedWorldEvent(PlayerChangedWorldEvent e)
    {
        CardRenderer renderer = Main.inst.mapsRenderingContainer.getCardRenderer(e.getPlayer());
        if (renderer == null)
            return;
        renderer.forceHandleWinLose();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerQuitEvent(PlayerQuitEvent e)
    {
        CardRenderer renderer = Main.inst.mapsRenderingContainer.getCardRenderer(e.getPlayer());
        if (renderer == null)
            return;
        renderer.forceHandleWinLose();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerTeleportEvent(PlayerTeleportEvent e)
    {
        // Ignore if player is in the same location and only rotated, this probably was caused by my plugin.
        if(e.getFrom().getX() == e.getTo().getX() && e.getFrom().getY() == e.getTo().getY() && e.getFrom().getZ() == e.getTo().getZ())
            return;

        CardRenderer renderer = Main.inst.mapsRenderingContainer.getCardRenderer(e.getPlayer());
        if (renderer == null)
            return;

        Scheduler.async(renderer::forceHandleWinLose, 10L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerDeathEvent(PlayerDeathEvent e)
    {
        CardRenderer renderer = Main.inst.mapsRenderingContainer.getCardRenderer(e.getEntity());
        if (renderer == null)
            return;
        renderer.forceHandleWinLose();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent e)
    {
        if(!Settings.inst().CANCEL_METHOD_F_GLOBAL)
            return;

        CardRenderer renderer = Main.inst.mapsRenderingContainer.getCardRenderer(e.getPlayer());
        if (renderer == null || !renderer.allowCancel)
            return;
        renderer.forceHandleWinLose();
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e)
    {
        if(!Settings.inst().CANCEL_METHOD_SHIFT_GLOBAL)
            return;

        CardRenderer scratch = Main.inst.mapsRenderingContainer.getCardRenderer(e.getPlayer());
        if (scratch == null || !scratch.allowCancel)
            return;

        if(e.isSneaking())
            scratch.forceHandleWinLose();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerDropItem(PlayerDropItemEvent e)
    {
        //TODO: use packets instead since this method won't be triggered if player has no real item in hand
        // (because the scratch card is now removed when scratching starts, in the new update I use a fake item, server doesn't know about it).
        // Do that also for the item swap event (F).
        PlayerMapRenderer renderer = Main.inst.mapsRenderingContainer.getRenderer(e.getPlayer());
        if (renderer == null)
            return;

        e.setCancelled(true);
        renderer.refreshFakeItemInHand(2L);

        if(renderer instanceof CardRenderer cardRenderer)
        {
            if (!Settings.inst().CANCEL_METHOD_DROP_GLOBAL || !cardRenderer.allowCancel)
            {
                //e.getPlayer().updateInventory();
                return;
            }
            cardRenderer.forceHandleWinLose();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onInventoryClick(InventoryClickEvent e)
    {
        if(!(e.getWhoClicked() instanceof Player))
            return;

        PlayerMapRenderer renderer = Main.inst.mapsRenderingContainer.getRenderer((Player) e.getWhoClicked());
        if (renderer == null)
            return;

        e.setCancelled(true);
        renderer.refreshFakeItemInHand(2L);
    }
}
