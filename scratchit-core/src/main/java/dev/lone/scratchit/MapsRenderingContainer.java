package dev.lone.scratchit;

import dev.lone.scratchit.card.CardData;
import dev.lone.scratchit.config.Settings;
import dev.lone.scratchit.map.PlayerMapRenderer;
import dev.lone.scratchit.map.card.CardRenderer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class MapsRenderingContainer
{
    HashMap<UUID, PlayerMapRenderer> players;
    private final HashMap<UUID, HashMap<String, Long>> playerLastUsage;
    private final HashMap<UUID, Long> playerLastUsageGlobal;

    //TODO: schedule a removal of cached cooldowns each X minutes for offline players, to avoid useless RAM usage

    public MapsRenderingContainer()
    {
        players = new HashMap<>();
        playerLastUsage = new HashMap<>();
        playerLastUsageGlobal = new HashMap<>();
    }

    public void addPlayer(Player player, PlayerMapRenderer playerMapRenderer)
    {
        players.put(player.getUniqueId(), playerMapRenderer);
    }

    public void addPlayer(Player player, CardData cardData)
    {
        players.put(player.getUniqueId(), cardData.getNewRenderer(player, this));
        setPlayerLastUsage(player, cardData);
    }

    public void removePlayer(Player player)
    {
        players.remove(player.getUniqueId());
    }

    public void removeAll()
    {
        players.clear();
        playerLastUsage.clear();
        playerLastUsageGlobal.clear();
    }

    public void stopAllPlayer()
    {
        new HashMap<>(players).forEach((uuid, playerMapRenderer) -> {
            CardRenderer cardRenderer = getCardRenderer(playerMapRenderer.player());
            if(cardRenderer != null)
                cardRenderer.stop(true);
        });
        removeAll();
    }

    public PlayerMapRenderer getRenderer(Player player)
    {
        return players.get(player.getUniqueId());
    }

    public CardRenderer getCardRenderer(Player player)
    {
        PlayerMapRenderer renderer = players.get(player.getUniqueId());
        if(renderer instanceof CardRenderer)
            return (CardRenderer) renderer;
        return null;
    }

    public boolean isRendering(Player player)
    {
        return players.containsKey(player.getUniqueId());
    }

    public boolean isRenderingCard(Player player)
    {
        return players.get(player.getUniqueId()) instanceof CardRenderer;
    }

    public long handleRemainingCooldown(Player player, CardData cardData, Consumer<Long> global, Consumer<Long> perItem)
    {
        if(!playerLastUsageGlobal.containsKey(player.getUniqueId()))
            return 0;

        long remaining;
        if(Settings.inst().COOLDOWN_MS_GLOBAL > 0)
        {
            remaining = Settings.inst().COOLDOWN_MS_GLOBAL - (System.currentTimeMillis() - getPlayerLastUsageGlobal(player));
            if(remaining > 0)
            {
                global.accept(remaining);
                return remaining;
            }
        }

        if(cardData.cooldownMs > 0)
        {
            remaining = cardData.cooldownMs - (System.currentTimeMillis() - getPlayerLastUsageGlobal(player));
            if(remaining > 0)
            {
                perItem.accept(remaining);
                return remaining;
            }
        }
        return 0;
    }

    public long getPlayerLastUsageGlobal(Player player)
    {
        if(!playerLastUsageGlobal.containsKey(player.getUniqueId()))
            return 0;
        return playerLastUsageGlobal.get(player.getUniqueId());
    }

    public long getPlayerLastUsage(Player player, CardData cardData)
    {
        if(!playerLastUsage.containsKey(player.getUniqueId()))
            return 0;

        if(!playerLastUsage.get(player.getUniqueId()).containsKey(cardData.getId()))
            return 0;

        return playerLastUsage.get(player.getUniqueId()).get(cardData.getId());
    }

    public void setPlayerLastUsage(Player player, CardData cardData)
    {
        if(!playerLastUsage.containsKey(player.getUniqueId()))
            playerLastUsage.put(player.getUniqueId(), new HashMap<>());

        playerLastUsage.get(player.getUniqueId()).put(cardData.getId(), System.currentTimeMillis());
        playerLastUsageGlobal.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
