package dev.lone.scratchit.card;

import org.bukkit.entity.Player;

public class SoundDataInterval extends SoundData
{
    private Long lastPlayedNano;
    private final int intervalMs;

    public SoundDataInterval(String sound, float volume, float pitch, int intervalTicks)
    {
        super(sound, volume, pitch);
        //20 : 1000 = intervalTicks : X
        this.intervalMs = (intervalTicks * 1000 / 20) * 1_000_000;
        this.lastPlayedNano = 0L;
    }

    public void tryPlay(Player player)
    {
        if(System.nanoTime() - lastPlayedNano < intervalMs)
            return;
        super.play(player);
        lastPlayedNano = System.nanoTime();
    }
}
