package dev.lone.scratchit.map;

public interface IMapRenderer
{
    /**
     * This function is executed each X ms.
     * It does various rendering calculations and then decides if it's needed to send map packet to the player
     * to avoid high network bandwidth usage.
     */
    void tickSendMapPacket();
}
