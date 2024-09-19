package dev.lone.scratchit.map.card.elements;

import dev.lone.scratchit.config.Settings;
import dev.lone.scratchit.map.image.ImageBytes;
import dev.lone.scratchit.map.IMapElementRenderer;
import dev.lone.scratchit.map.card.CardRenderer;
import dev.lone.scratchit.util.BoolMatrix2x2;
import dev.lone.scratchit.util.ByteMatrix2x2;
import dev.lone.scratchit.util.ByteMatrix3x3;

public class OverlayRenderer implements IMapElementRenderer
{
    CardRenderer cr;

    public final ByteMatrix3x3 overlayBytes;
    protected BoolMatrix2x2 deletedPixels;
    public int erasedPixelsCounter;
    public int totalNonTransparentPixels;
    private boolean changed;

    public OverlayRenderer(CardRenderer cr)
    {
        this.cr = cr;
        overlayBytes = cr.cardData.getOverlay(false).byPalette(cr.paletteType());

        deletedPixels = new BoolMatrix2x2(128, 128);
        //Arrays.fill(deletedPixels.internal, false);

        for (int i = 0; i < 128; i++)
            for (int j = 0; j < 128; j++)
                if (overlayBytes.get(i, j, 1) != -128) //not transparent
                    totalNonTransparentPixels++;
    }

    @Override
    public boolean render(ByteMatrix2x2 mainBytes)
    {
        changed = false;
        //draw overlay
        for (int x = 0; x < 128; x++)
        {
            for (int y = 0; y < 128; y++)
            {
                if (overlayBytes.get(x, y, 1) != -128) //not transparent
                {
                    //draw only not deleted pixels
                    if (!deletedPixels.get(x, y))
                    {
                        if (Settings.inst().FIX_SEMITRANSPARENT_PIXELS && overlayBytes.get(x, y, 1) < 127)
                        {
                            mainBytes.set(x, y, ImageBytes.blendByteColors(cr.paletteType(), mainBytes.get(x, y), overlayBytes.get(x, y, 0), overlayBytes.get(x, y, 1)));
                        }
                        else
                            mainBytes.set(x, y, overlayBytes.get(x, y, 0));
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }
}
