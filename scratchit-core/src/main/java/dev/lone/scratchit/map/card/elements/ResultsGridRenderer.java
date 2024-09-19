package dev.lone.scratchit.map.card.elements;

import dev.lone.scratchit.config.Settings;
import dev.lone.scratchit.map.IMapElementRenderer;
import dev.lone.scratchit.map.image.ImgData;
import dev.lone.scratchit.map.card.CardRenderer;
import dev.lone.scratchit.map.image.ImageBytes;
import dev.lone.scratchit.util.ByteMatrix2x2;
import dev.lone.scratchit.util.ByteMatrix3x3;
import dev.lone.scratchit.util.Utils;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class ResultsGridRenderer implements IMapElementRenderer
{
    CardRenderer cr;
    private final @Nullable ByteMatrix3x3 rightBytes;
    private final @Nullable ByteMatrix3x3 wrongBytes;

    private boolean[] wonSymbolsGrid;
    private boolean changed = false;

    boolean noIcons = false;

    public ResultsGridRenderer(CardRenderer cr)
    {
        this.cr = cr;

        ImgData tmpRight = cr.cardData.getRight(false);
        if(tmpRight != null)
            rightBytes = tmpRight.byPalette(cr.paletteType());
        else
            rightBytes = null;

        ImgData tmpLeft = cr.cardData.getWrong(false);
        if(tmpLeft != null)
            wrongBytes = tmpLeft.byPalette(cr.paletteType());
        else
            wrongBytes = null;

        if(rightBytes == null && wrongBytes == null)
        {
            noIcons = true;
            return;
        }
        wonSymbolsGrid = new boolean[cr.cardData.getIconsCoords().size()];

        //cr.player.sendMessage(cr.hasWon_calculated ? "won" : "you lose");

        int needed;
        if (cr.hasWon_calculated)
            //needed = KUtils.getRandomInt(cr.scratchCardData.getNeededToWin(), cr.scratchCardData.getIconsCoords().size());
            needed = cr.cardData.getNeededToWin();
        else
            needed = Utils.getRandomInt(0, cr.cardData.getNeededToWin() - 1);

        if(wonSymbolsGrid.length < needed)
            needed = wonSymbolsGrid.length;

        if(needed > 0 && wonSymbolsGrid.length > 0)
        {
            int counter = 0;
            while (counter < needed)
            {
                for (int i = 0; i < wonSymbolsGrid.length; i++)
                {
                    if (counter >= needed)
                        break;
                    if (Math.random() * 100 > 70 && !wonSymbolsGrid[i])
                    {
                        wonSymbolsGrid[i] = true;
                        counter++;
                    }
                }
            }
        }
    }

    @Override
    public boolean render(ByteMatrix2x2 mainBytes)
    {
        if(noIcons)
            return false;

        changed = false;
        int w = 0;
        @Nullable ByteMatrix3x3 icon;
        for (Vector iconCoords : cr.cardData.getIconsCoords())
        {
            if (wonSymbolsGrid[w])
                icon = rightBytes;
            else
                icon = wrongBytes;
            w++;

            if(icon == null) //no icon set in configuration
                return false;

            for (int i = 0; i < icon.width; i++)
            {
                for (int j = 0; j < icon.height; j++)
                {
                    if (icon.get(i, j, 1) != -128) //not transparent
                    {
                        int k = iconCoords.getBlockX() + i - 8;
                        int m = iconCoords.getBlockY() + j - 8;
                        if (k < 0 || k >= 128 || m < 0 || m >= 128)
                            continue;
                        if (cr.overlayRenderer.deletedPixels.get(k, m))
                        {
                            if (Settings.inst().FIX_SEMITRANSPARENT_PIXELS && icon.get(i, j, 1) < 127)
                            {
                                mainBytes.set(k, m, ImageBytes.blendByteColors(cr.paletteType(), mainBytes.get(k, m), icon.get(i, j, 0), icon.get(i, j, 1)));
                            }
                            else
                            {
                                mainBytes.set(k, m, icon.get(i, j, 0));
                            }
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }
}
