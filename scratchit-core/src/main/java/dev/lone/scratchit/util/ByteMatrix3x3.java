package dev.lone.scratchit.util;

import java.util.Arrays;

public class ByteMatrix3x3
{
    public final byte[] internal;
    public final int width;
    public final int height;

    public ByteMatrix3x3(byte[] arr, int width, int height, int depth)
    {
        this.internal = new byte[width * height * depth];
        this.width = width;
        this.height = height;
        System.arraycopy(arr, 0, internal, 0, arr.length);
    }

    public ByteMatrix3x3(int width, int height, int depth)
    {
        this.internal = new byte[width * height * depth];
        this.width = width;
        this.height = height;
    }

    public byte get(int x, int y, int z)
    {
        return internal[x + width * y + width * height * z];
    }

    public void set(int x, int y, int z, byte value)
    {
        internal[x + width * y + width * height * z] = value;
    }

    public void getInternalCopy(byte[] copy)
    {
        System.arraycopy(internal, 0, copy, 0, internal.length);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(internal);
    }
}
