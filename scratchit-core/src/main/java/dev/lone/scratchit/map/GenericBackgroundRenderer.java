package dev.lone.scratchit.map;

import dev.lone.scratchit.map.card.CardRenderer;
import dev.lone.scratchit.util.ByteMatrix2x2;
import dev.lone.scratchit.util.ByteMatrix3x3;

public class GenericBackgroundRenderer implements IMapElementRenderer
{
    private final ByteMatrix3x3 image;

    public GenericBackgroundRenderer(CardRenderer cr, String backgroundFileName)
    {
        image = cr.cardData.getImgData(backgroundFileName, false).byPalette(cr.paletteType());
    }

    @Override
    public boolean render(ByteMatrix2x2 mainBytes)
    {
        for (int x = 0; x < 128; x++)
        {
            for (int y = 0; y < 128; y++)
            {
                mainBytes.set(x, y, image.get(x, y, 0));
            }
        }
        return true;
    }

    public boolean hasImage()
    {
        return image != null;
    }
}
