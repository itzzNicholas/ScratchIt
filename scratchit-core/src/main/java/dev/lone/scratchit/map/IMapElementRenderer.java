package dev.lone.scratchit.map;

import dev.lone.scratchit.util.ByteMatrix2x2;

public interface IMapElementRenderer
{
    /**
     * Processes this element pixels and edit the needed ones.
     * @param mainBytes
     * @return true if any pixel got edited.
     */
    boolean render(ByteMatrix2x2 mainBytes);
}
