package dev.lone.scratchit.util;

import java.util.Arrays;

public class ByteMatrix2x2
{
    public final byte[] internal;
    public final int width;
    public final int height;

    public ByteMatrix2x2(int width, int height)
    {
        internal = new byte[width * height];
        this.width = width;
        this.height = height;
    }

    public byte get(int x, int y)
    {
        return internal[x + y * width];
    }

    public void set(int x, int y, byte value)
    {
        internal[x + y * width] = value;
    }

    public void copyToArray(byte[] copy)
    {
        System.arraycopy(internal, 0, copy, 0, internal.length);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(internal);
    }
}
