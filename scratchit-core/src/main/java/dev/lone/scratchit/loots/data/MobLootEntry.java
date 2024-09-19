package dev.lone.scratchit.loots.data;

import lombok.experimental.SuperBuilder;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuperBuilder(toBuilder = true)
public class MobLootEntry extends NBTLootEntry
{
    final EntityType type;
    //TODO mythicmobs
    final List<IRule> rules;
    final List<MetadataMatchRule> metadataMatchRules;
    final boolean ignoreSpawnerMobs;

    public MobLootEntry(EntityType type, boolean ignoreSpawnerMobs)
    {
        super();
        this.type = type;
        this.ignoreSpawnerMobs = ignoreSpawnerMobs;
        this.rules = new ArrayList<>();
        this.metadataMatchRules = new ArrayList<>();
    }

    public EntityType getType()
    {
        return type;
    }

    public boolean isIgnoreSpawnerMobs()
    {
        return ignoreSpawnerMobs;
    }

    public boolean hasRules()
    {
        return rules.size() > 0;
    }

    public List<IRule> getRules()
    {
        return rules;
    }

    @Override
    public void addNBTMatchRule(NBTMatchRule nbtMatchRule)
    {
        rules.add(nbtMatchRule);
        super.addNBTMatchRule(nbtMatchRule);
    }

    public void addMetadataMatchRule(MetadataMatchRule nbtMatchRule)
    {
        rules.add(nbtMatchRule);
        metadataMatchRules.add(nbtMatchRule);
    }

    public List<MetadataMatchRule> getMetadataMatchRules()
    {
        return metadataMatchRules;
    }

    public boolean hasMetadataMatchRules()
    {
        return metadataMatchRules.size() > 0;
    }

    /**
     * Slow, use carefully
     * @return
     */
    @Deprecated
    public Optional<MetadataMatchRule> getMetadataMatchRule(String name)
    {
        if(!hasMetadataMatchRules())
            return Optional.empty();

        for (MetadataMatchRule entry : metadataMatchRules)
        {
            if(entry.name.equals(name))
                return Optional.of(entry);
        }
        return Optional.empty();
    }

    /*public static void containsEntry(List<MobLootEntry> list, )
    {
        for(MobLootEntry entry : list)
        {
            if(entry.getType() == )
        }
    }*/
}
