package dev.lone.scratchit.loots.data;

import dev.lone.LoneLibs.nbt.nbtapi.NBTCompound;
import dev.lone.LoneLibs.nbt.nbtapi.NBTEntity;
import dev.lone.LoneLibs.nbt.nbtapi.NBTTileEntity;
import dev.lone.LoneLibs.nbt.nbtapi.NBTType;
import dev.lone.scratchit.util.Msg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class NBTMatchRule implements IRule
{
    final String nbtPath;
    Object nbtValue;

    String[] nbtPathSplit;

    NBTType nbtValueType;

    public NBTMatchRule(String nbtPath, String nbtValueStr, String nbtValueTypeStr)
    {
        this.nbtPath = nbtPath;
        this.nbtValue = nbtValueStr;

        String nbtTypeFixed = "NBTTag" + StringUtils.capitalize(nbtValueTypeStr.toLowerCase());

        try
        {
            nbtValueType = NBTType.valueOf(nbtTypeFixed);
        }
        catch(IllegalArgumentException exc)
        {
            Msg.get().log(ChatColor.RED + "Unknown nbt.type '" + nbtValueTypeStr + "' for nbt path '" + nbtPath + "'." +
                    ChatColor.GRAY + " Allowed: string, int, float, double, byte, short");
            return;
        }
        switch (nbtValueType)
        {
            case NBTTagString:
                break;
            case NBTTagInt:
                nbtValue = Integer.parseInt(nbtValueStr);
                break;
            case NBTTagFloat:
                nbtValue = Float.parseFloat(nbtValueStr);
                break;
            case NBTTagDouble:
                nbtValue = Double.parseDouble(nbtValueStr);
                break;
            case NBTTagByte:
                nbtValue = Byte.parseByte(nbtValueStr);
            case NBTTagShort:
                nbtValue = Short.parseShort(nbtValueStr);
                break;
        }

        this.nbtPathSplit = nbtPath.split("\\.");
    }

    public String getNbtPath()
    {
        return nbtPath;
    }

    public Object getNbtValue()
    {
        return nbtValue;
    }

    @Override
    public boolean matchRule(Entity entity)
    {
        NBTEntity nbtEntity = new NBTEntity(entity);

        if(!nbtEntity.hasKey(nbtPathSplit[0]))
            return false;

        NBTCompound currentCompound = nbtEntity.getCompound(nbtPathSplit[0]);
        for(int i=1; i<nbtPathSplit.length-1; i++)
        {
            if(!currentCompound.hasKey(nbtPathSplit[i]))
                return false;
            currentCompound = currentCompound.getCompound(nbtPathSplit[i]);
        }
        if(!currentCompound.hasKey(nbtPathSplit[nbtPathSplit.length-1]))
            return false;

        NBTType nbtType = currentCompound.getType(nbtPathSplit[nbtPathSplit.length-1]);
        if(nbtType != nbtValueType)
            return false;

        if(nbtType == NBTType.NBTTagString)
            return (currentCompound.getString(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));
        else if(nbtType == NBTType.NBTTagInt)
            return (currentCompound.getInteger(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));
        else if(nbtType == NBTType.NBTTagDouble)
            return (currentCompound.getDouble(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));
        else if(nbtType == NBTType.NBTTagFloat)
            return (currentCompound.getFloat(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));
        else if(nbtType == NBTType.NBTTagByte)
            return (currentCompound.getByte(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));
        else if(nbtType == NBTType.NBTTagShort)
            return (currentCompound.getShort(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));

        return false;
    }

    @Override
    public boolean matchRule(Block block)
    {
        NBTTileEntity nbtEntity = new NBTTileEntity(block.getState());

        if(!nbtEntity.hasKey(nbtPathSplit[0]))
            return false;

        NBTCompound currentCompound = nbtEntity.getCompound(nbtPathSplit[0]);
        for(int i=1; i<nbtPathSplit.length-1; i++)
        {
            if(!currentCompound.hasKey(nbtPathSplit[i]))
                return false;
            currentCompound = currentCompound.getCompound(nbtPathSplit[i]);
        }
        if(!currentCompound.hasKey(nbtPathSplit[nbtPathSplit.length-1]))
            return false;

        NBTType nbtType = currentCompound.getType(nbtPathSplit[nbtPathSplit.length-1]);
        if(nbtType != nbtValueType)
            return false;

        if(nbtType == NBTType.NBTTagString)
            return (currentCompound.getString(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));
        else if(nbtType == NBTType.NBTTagInt)
            return (currentCompound.getInteger(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));
        else if(nbtType == NBTType.NBTTagDouble)
            return (currentCompound.getDouble(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));
        else if(nbtType == NBTType.NBTTagFloat)
            return (currentCompound.getFloat(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));
        else if(nbtType == NBTType.NBTTagByte)
            return (currentCompound.getByte(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));
        else if(nbtType == NBTType.NBTTagShort)
            return (currentCompound.getShort(nbtPathSplit[nbtPathSplit.length-1]).equals(nbtValue));

        return false;
    }
}
