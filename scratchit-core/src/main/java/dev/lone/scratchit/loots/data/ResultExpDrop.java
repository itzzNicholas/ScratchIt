package dev.lone.scratchit.loots.data;

public class ResultExpDrop
{
    final int minAmount;
    final int maxAmount;
    final float chance;

    public ResultExpDrop(int minAmount, int maxAmount, float chance)
    {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.chance = chance;
    }

    public int getMinAmount()
    {
        return minAmount;
    }

    public int getMaxAmount()
    {
        return maxAmount;
    }

    public float getChance()
    {
        return chance;
    }
}
