package dev.lone.scratchit.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundWrapper
{
    String soundStr;
    XSound bukkitSound;
    Sound defaultBukkitSound;

    protected float volume;
    protected float pitch;

    public SoundWrapper(String str, Sound defaultBukkitSound)
    {
        this.defaultBukkitSound = defaultBukkitSound;
        this.pitch = 1;
        this.volume = 1;

        if(str == null)
            return;

        try
        {
            bukkitSound = XSound.valueOf(str);
        }
        catch(Exception e)
        {
            soundStr = str;
        }
    }

    public float getVolume()
    {
        return volume;
    }

    public void setVolume(float volume)
    {
        this.volume = volume;
    }

    public float getPitch()
    {
        return pitch;
    }

    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }

    public void play(Player player, Location location)
    {
        if(bukkitSound != null)
            player.playSound(location, bukkitSound.getBukkitSound(), volume, pitch);
        else if(soundStr != null)
            player.playSound(location, soundStr, volume, pitch);
        else if(defaultBukkitSound != null)
            player.playSound(location, defaultBukkitSound, volume, pitch);
    }

    public boolean hasAnyValidSound()
    {
        return bukkitSound != null || soundStr != null || defaultBukkitSound != null;
    }
}
