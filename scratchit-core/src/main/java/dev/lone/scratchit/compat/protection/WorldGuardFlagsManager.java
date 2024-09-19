package dev.lone.scratchit.compat.protection;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class WorldGuardFlagsManager
{
    private static boolean areFlagsRegistered;

    private static FlagRegistry registry;


    public static void registerFlags()
    {
        try
        {
            if (areFlagsRegistered)
                return;

            areFlagsRegistered = true;
            registry = WorldGuard.getInstance().getFlagRegistry();

            Flags.CAN_USE_CARD = registerFlag("scratch-it-can-use-card");
        }
        catch (IllegalStateException ignored) {}
    }

    static StateFlag registerFlag(String name)
    {
        return registerFlag(name, true);
    }

    static StateFlag registerFlag(String name, boolean def)
    {
        Flag<?> existing = registry.get(name);
        if (existing instanceof StateFlag)
        {
            return (StateFlag) existing;
        }
        else
        {
            // types don't match - this is bad news! some other plugin conflicts with you
            // hopefully this never actually happens
        }

        StateFlag flag = new StateFlag(name, def);
        registry.register(flag);
        return flag;
    }

    public static boolean hasBypass(LocalPlayer localPlayer, Location location)
    {
        return WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(location.getWorld()));
    }

    public static FlagResult testEntityFlag(StateFlag stateFlag, Player player, Entity entity)
    {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location target = BukkitAdapter.adapt(entity).getLocation();

        if (query.getApplicableRegions(target).getRegions().isEmpty())
            return FlagResult.NO_REGION;
        if (hasBypass(localPlayer, entity.getLocation()))
            return FlagResult.BYPASS;
        if (query.testState(target, localPlayer, stateFlag))
        {
            return FlagResult.ALLOW;
        }
        return FlagResult.DENY;
    }

    public static FlagResult testBlockFlag(StateFlag stateFlag, Player player, Block block)
    {
        Location loc;
        if (block == null)
            loc = player.getLocation();
        else
            loc = block.getLocation();

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location target = BukkitAdapter.adapt(loc);

        if (query.getApplicableRegions(target).getRegions().isEmpty())
            return FlagResult.NO_REGION;
        if (hasBypass(localPlayer, loc))
            return FlagResult.BYPASS;
        if (query.testState(target, localPlayer, stateFlag))
        {
            return FlagResult.ALLOW;
        }
        return FlagResult.DENY;
    }

    public static class Flags
    {
        public static StateFlag CAN_USE_CARD;
    }

    public enum FlagResult
    {
        ALLOW,
        DENY,
        NO_REGION,
        BYPASS
    }
}
