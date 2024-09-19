package dev.lone.scratchit.compat.protection;

import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProtectionPlugins
{
    private static boolean registered;
    private static boolean hasWorldGuard;

    public static void register()
    {
        if(registered)
            return;
        registered = true;

        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            hasWorldGuard = true;
            WorldGuardFlagsManager.registerFlags();
        } catch (ClassNotFoundException ignored) {}
    }

    public static boolean canScratchCard(PlayerInteractEvent e)
    {
        return canInteract_block(e, WorldGuardFlagsManager.Flags.CAN_USE_CARD);
    }

    private static boolean canInteract_block(PlayerInteractEvent e, StateFlag stateFlag)
    {
        if(hasWorldGuard)
        {
            WorldGuardFlagsManager.FlagResult flagResult = WorldGuardFlagsManager.testBlockFlag(stateFlag, e.getPlayer(), e.getClickedBlock());
            if(flagResult == WorldGuardFlagsManager.FlagResult.ALLOW)
                return true;
            if(flagResult == WorldGuardFlagsManager.FlagResult.DENY)
                return false;
            if(flagResult == WorldGuardFlagsManager.FlagResult.BYPASS)
                return true;
            if(flagResult == WorldGuardFlagsManager.FlagResult.NO_REGION)
                return true;
        }

        return e.getClickedBlock() == null || !e.isCancelled();
    }
}
