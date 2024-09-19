package dev.lone.scratchit.loots.data;

import dev.lone.LoneLibs.nbt.nbtapi.NBTType;
import dev.lone.scratchit.util.Msg;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class MetadataMatchRule implements IRule
{
    @Getter
    String name;
    @Getter
    Object value;

    NBTType nbtValueType;

    public MetadataMatchRule(String name, Object value, String nbtValueTypeStr)
    {
        this.name = name;
        this.value = value;

        String nbtTypeFixed = "NBTTag" + StringUtils.capitalize(nbtValueTypeStr.toLowerCase());

        try
        {
            nbtValueType = NBTType.valueOf(nbtTypeFixed);
        }
        catch(IllegalArgumentException exc)
        {
            Msg.get().warn(ChatColor.RED + "Unknown metadata.type '" + nbtValueTypeStr + "' for metadata of name '" + name + "'." +  ChatColor.GRAY + " Allowed: string, int, float, double, byte, short");
        }
    }

    @Override
    public boolean matchRule(Entity entity)
    {
        if(!entity.hasMetadata(name))
            return false;
        //System.out.println(entity.getMetadata(name).get(0).value().equals(value) + " " + entity.getMetadata(name).get(0).value() + " " + value);
        return entity.getMetadata(name).get(0).value().equals(value);
    }

    @Override
    public boolean matchRule(Block block)
    {
        return false;
    }
}
