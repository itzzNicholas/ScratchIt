package dev.lone.scratchit.map.card;

import dev.lone.scratchit.Main;
import dev.lone.scratchit.MapsRenderingContainer;
import dev.lone.scratchit.config.Settings;
import dev.lone.scratchit.api.events.ScratchCardFinishEvent;
import dev.lone.scratchit.card.ActionsGroup;
import dev.lone.scratchit.card.AnimationPlayer;
import dev.lone.scratchit.card.CardData;
import dev.lone.scratchit.map.GenericBackgroundRenderer;
import dev.lone.scratchit.map.PlayerMapRenderer;
import dev.lone.scratchit.map.card.elements.EraseParticlesRenderer;
import dev.lone.scratchit.map.card.elements.EraserRenderer;
import dev.lone.scratchit.map.card.elements.OverlayRenderer;
import dev.lone.scratchit.map.card.elements.ResultsGridRenderer;
import dev.lone.scratchit.util.EventsUtil;
import dev.lone.scratchit.util.Scheduler;
import dev.lone.scratchit.nms.Packets;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

/**
 * Per player card renderer
 */
public class CardRenderer extends PlayerMapRenderer
{
    private static final int ARMORSTAND_ID = 1337;

    public final MapsRenderingContainer mapsRenderingContainer;
    public CardData cardData;

    @Setter
    public boolean allowCancel = true;

    public GenericBackgroundRenderer bg;
    public GenericBackgroundRenderer bgWin;
    public GenericBackgroundRenderer bgLose;
    public EraserRenderer eraserRenderer;
    public OverlayRenderer overlayRenderer;
    public EraseParticlesRenderer eraseParticlesRenderer;
    public ResultsGridRenderer resultsGridRenderer;

    private long lastInteract = 0;

    public final boolean hasWon_calculated;

    @Getter
    private boolean hasEnded;
    private boolean forceEnded;

    private AnimationPlayer endAnimationPlayer;
    private GenericBackgroundRenderer currentBackground;

    ItemStack scratchItemInstance;

    ActionsGroup rolledActionsGroup;
    HashSet<ActionsGroup> actionsGroups_alwaysExecute;
    long endDurationBackground = 0;


    int changedElements = 0;
    long endDurationBackground_PassedTicks = 0;
    private volatile boolean runningWinLose;
    private int currentTick;

    public CardRenderer(Player player,
                        CardData cardData,
                        MapsRenderingContainer mapsRenderingContainer)
    {
        super(player);

        scratchItemInstance = player.getItemInHand().clone();
        scratchItemInstance.setAmount(1);

        this.cardData = cardData;
        this.mapsRenderingContainer = mapsRenderingContainer;

        hasWon_calculated = cardData.hasWon();

        bg = new GenericBackgroundRenderer(this, "background");
        bgWin = new GenericBackgroundRenderer(this, "background_win");
        bgLose = new GenericBackgroundRenderer(this, "background_lose");
        eraserRenderer = new EraserRenderer(this);
        overlayRenderer = new OverlayRenderer(this);
        eraseParticlesRenderer = new EraseParticlesRenderer(this);
        resultsGridRenderer = new ResultsGridRenderer(this);

        eraseParticlesRenderer.enabled = cardData.eraserParticlesEnabled;

        currentBackground = bg;

        if (hasWon_calculated)
        {
            rolledActionsGroup = cardData.winActions.roll(player);
            actionsGroups_alwaysExecute = cardData.winActions_alwaysExecute;
            endDurationBackground = cardData.win_backgroundDuration;
        }
        else
        {
            rolledActionsGroup = cardData.loseActions.roll(player);
            actionsGroups_alwaysExecute = cardData.loseActions_alwaysExecute;
            endDurationBackground = cardData.lose_backgroundDuration;
        }
    }

    @Override
    public void start()
    {
        super.start();
        // Remove it as soon as it's started
        player.getInventory().removeItem(scratchItemInstance);

        // Spawn fake entity
        spawnFakeInteraction();
        freezePlayer();

        refreshFakeItemInHand(0);

        if(allowCancel)
        {
            String methodsString = "";
            if (Settings.inst().CANCEL_METHOD_SHIFT_GLOBAL)
                methodsString += "SHIFT / ";
            if (Settings.inst().CANCEL_METHOD_F_GLOBAL)
                methodsString += "F / ";
            if (Settings.inst().CANCEL_METHOD_DROP_GLOBAL)
                methodsString += "Q / ";

            if(!methodsString.isEmpty())
            {
                methodsString = methodsString.substring(0, methodsString.length() - 3);
                player.sendMessage(Main.lang.getLocalized("press_to_cancel_usage").replace("{methods}", methodsString));
            }
        }
    }

    @Override
    public void tickSendMapPacket()
    {
        // Just in case the player moved somehow.
        if (currentTick % 5 == 0)
            Packets.get().sendTeleport(player, ARMORSTAND_ID, player.getEyeLocation().add(0, -0.8, 0));
        tickSendMapPacket(false);
        currentTick++;
    }

    public void tickSendMapPacket(boolean skipRunningWinLoseCheck)
    {
        if (!skipRunningWinLoseCheck && runningWinLose)
            return;

        changedElements = 0;

        if (!hasEnded)
        {
            currentBackground.render(mapData());
            overlayRenderer.render(mapData());
            resultsGridRenderer.render(mapData());
            if (eraseParticlesRenderer.enabled && eraseParticlesRenderer.render(mapData()))
                changedElements++;
            if (eraserRenderer.render(mapData()))
                changedElements++;

            hasEnded = forceEnded || (!isScratching() && eraserRenderer.hasScratchedEnough());
            if (hasEnded)
            {
                endAnimationPlayer = new AnimationPlayer(this, hasWon_calculated ? cardData.getWinAnimation() : cardData.getLoseAnimation());
                currentBackground = hasWon_calculated ? bgWin : bgLose;

                // I just have to call the event here, no need to call it also on the "else", because stop() is called so the "else"
                // is not reached.
                if(EventsUtil.call(new ScratchCardFinishEvent(player(), scratchItemInstance, cardData.getId(), hasWon_calculated)))
                {
                    if (rolledActionsGroup != null && (rolledActionsGroup.executeInstantly || skipRunningWinLoseCheck))
                        rolledActionsGroup.executeAllActionsSyncThread(player);
                    if (actionsGroups_alwaysExecute != null)
                    {
                        actionsGroups_alwaysExecute.forEach(actionsGroup -> {
                            if (actionsGroup.executeInstantly)
                                actionsGroup.executeAllActionsSyncThread(player);
                        });
                    }
                }
                else
                {
                    stop(true);
                }
            }
        }
        else // Is ended, so I have to play the end animation and give the prizes
        {
            if (currentBackground.hasImage()) //no win/lose background set
                currentBackground.render(mapData());

            if (endAnimationPlayer.hasAnimation())
            {
                if (endAnimationPlayer.render(mapData()))
                    changedElements++;
            }

            // No win/lose animation set or finished playing win/lose animation
            if (!endAnimationPlayer.hasAnimation() || endAnimationPlayer.hasFinishedAnimating())
            {
                if (endDurationBackground == 0 || endDurationBackground_PassedTicks == endDurationBackground)
                {
                    stop(true);

                    if (rolledActionsGroup != null && !rolledActionsGroup.executeInstantly)
                        rolledActionsGroup.executeAllActionsSyncThread(player);
                    if (actionsGroups_alwaysExecute != null)
                    {
                        actionsGroups_alwaysExecute.forEach(actionsGroup -> {
                            if (!actionsGroup.executeInstantly)
                                actionsGroup.executeAllActionsSyncThread(player);
                        });
                    }
                    return;
                }
                else
                {
                    if (currentBackground.hasImage()) //no win/lose background set
                    {
                        currentBackground.render(mapData());
                        changedElements++;
                    }
                    endDurationBackground_PassedTicks += renderFrequencyTicks;
                }
            }
        }

        // If not changed do not send the packet again, to avoid too much network usage
        if (changedElements > 0)
            sendMapDataToPlayer();
    }

    @Override
    public void stop(boolean useMainThread)
    {
        super.stop(useMainThread);
        mapsRenderingContainer.removePlayer(player);
        destroyFakeArmorstand();

        player.updateInventory();

        if(useMainThread)
            Scheduler.sync(this::unfreezePlayer);
        else
            unfreezePlayer();
    }

    public void stop()
    {
        stop(false);
    }

    /**
     * Armorstand used to catch interact event (to scratch)
     */
    private void spawnFakeInteraction()
    {
        Location location = player.getEyeLocation().add(0, -0.8, 0);
        Packets.get().sendSpawnInvisibleArmorStand(player, ARMORSTAND_ID, location);
    }

    private void destroyFakeArmorstand()
    {
        Packets.get().sendDestroyEntityPacket(player, ARMORSTAND_ID);
    }

    public void forceHandleWinLose()
    {
        if(runningWinLose)
            return;

        runningWinLose = true;

        if(eraserRenderer.hasScratchedSomeParts())
        {
            forceEnded = true;
            Scheduler.async(() -> tickSendMapPacket(true));
        }
        else
        {
            player.getInventory().addItem(scratchItemInstance);

            if(allowCancel)
                player.sendMessage(Main.lang.getLocalized("cancelled_usage"));
        }

        stop();
    }

    public boolean isScratching()
    {
        //TODO: IDK IF THIS COMMENT IS STILL VALID SINCE I CHANGED FROM MS TO NANOSECOND:

        //TODO: may not be precise on high ping player / low TPS server
        //TODO: test on a low TPS ( < 18) server or with high ping player connection.
        //TODO: maybe scale the value (250) based on the TPS and player ping?

        return System.nanoTime() - lastInteract < 250_000_000; //250ms
    }

    public boolean isCursorMoving()
    {
        return eraserRenderer.isMoving();
    }

    public void updateLastInteract()
    {
        lastInteract = System.nanoTime();
    }
}
