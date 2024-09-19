package dev.lone.scratchit.loots.data;

import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder(toBuilder = true)
public class NBTLootEntry extends LootEntry
{
    final List<NBTMatchRule> nbtMatchRules;

    public NBTLootEntry()
    {
        this.nbtMatchRules = new ArrayList<>();
    }

    public void addNBTMatchRule(NBTMatchRule nbtMatchRule)
    {
        nbtMatchRules.add(nbtMatchRule);
    }

    public List<NBTMatchRule> getNBTMatchRules()
    {
        return nbtMatchRules;
    }

    public boolean hasNBTMatchRules()
    {
        return nbtMatchRules.size() > 0;
    }
}
