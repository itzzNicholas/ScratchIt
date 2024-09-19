package dev.lone.scratchit.card;

import dev.lone.scratchit.util.SoundWrapper;
import org.bukkit.entity.Player;

public class SoundData extends SoundWrapper
{
    public SoundData(String soundName, float volume, float pitch)
    {
        super(soundName, null);
        this.volume = volume;
        this.pitch = pitch;
    }

    public void play(Player player)
    {
        play(player, player.getLocation());
    }
}
