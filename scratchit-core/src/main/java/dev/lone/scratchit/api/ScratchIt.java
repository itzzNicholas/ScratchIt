package dev.lone.scratchit.api;

import dev.lone.scratchit.Main;
import dev.lone.scratchit.card.CardData;
import dev.lone.scratchit.map.card.CardRenderer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ScratchIt
{
    /**
     * Show a card to a player by its id.
     *
     * @param cardId      ID of the card, found in your config file
     * @param allowCancel Allow the player to cancel the usage using defined methods in config.yml of this plugin.
     * @throws IllegalAccessException if plugin is still loading
     * @throws NullPointerException   if card doesn't exist
     */
    public static void showCard(Player player, String cardId, boolean allowCancel) throws IllegalAccessException, NullPointerException
    {
        CardRenderer tmp = Main.inst.mapsRenderingContainer.getCardRenderer(player);
        //already scratching
        if (tmp != null)
        {
            tmp.forceHandleWinLose();
        }

        CardData card = Main.inst.cardsStorage.getCard(cardId);
        if (card == null)
        {
            if (Main.inst.cardsStorage.isLoading())
            {
                throw new IllegalAccessException(Main.inst.getName() + " is still loading.");
            }
            throw new NullPointerException("Card with ID " + cardId + " doesn't exists.");
        }

        Main.inst.mapsRenderingContainer.addPlayer(player, card);
        CardRenderer scratch = Main.inst.mapsRenderingContainer.getCardRenderer(player);
        scratch.setAllowCancel(allowCancel);
        scratch.start();
        scratch.refreshFakeItemInHand(0);
    }

    /**
     * Cancel scratching of a card.
     *
     * @param givePrize Decide if you want to give prizes to the player if they revealed most part of the card contents.
     */
    public static void cancelCardUsage(Player player, boolean givePrize)
    {
        CardRenderer scratch = Main.inst.mapsRenderingContainer.getCardRenderer(player);
        if (scratch == null)
            return;
        if (givePrize)
            scratch.forceHandleWinLose();
        else
            scratch.stop();
    }

    /**
     * Get the current card for this player.
     *
     * @return null if player is not scratching a card
     */
    @Nullable
    public static String getCurrentCard(Player player)
    {
        CardRenderer scratch = Main.inst.mapsRenderingContainer.getCardRenderer(player);
        if (scratch == null)
            return null;
        return scratch.cardData.getId();
    }
}
