package dev.lone.scratchit.card.actions;

import dev.lone.scratchit.card.SoundData;
import org.bukkit.entity.Player;

public class ActionPlaySound extends ActionDelay
{
    private SoundData soundData;

    public ActionPlaySound(String sound, float volume, float pitch, long delay)
    {
        super(delay);
        soundData = new SoundData(sound, volume, pitch);
    }

    @Override
    public void execute(Player player)
    {
        execute(() -> doExecute(player));
    }

    private void doExecute(Player player)
    {
        soundData.play(player);
    }
}
