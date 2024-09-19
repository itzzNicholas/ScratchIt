package dev.lone.scratchit.util;

import java.util.Arrays;

public class BoolMatrix2x2
{
    public final boolean[] internal;
    public final int width;
    public final int height;

    public BoolMatrix2x2(int width, int height)
    {
        internal = new boolean[width * height];
        this.width = width;
        this.height = height;
    }

    public boolean get(int x, int y)
    {
        return internal[x + y * width];
    }

    public void set(int x, int y, boolean value)
    {
        internal[x + y * width] = value;
    }

    public void getInternalCopy(boolean[] copy)
    {
        System.arraycopy(internal, 0, copy, 0, internal.length);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(internal);
    }
}
