package dev.lone.scratchit.map.card.elements;

import dev.lone.scratchit.map.card.CardRenderer;
import dev.lone.scratchit.map.card.ParticleDot;
import dev.lone.scratchit.util.ByteMatrix2x2;
import dev.lone.scratchit.util.ByteMatrix3x3;

public class EraserRenderer extends CursorRenderer
{
    private final ByteMatrix3x3 eraser_bytes;
    boolean erasedAnything;

    public EraserRenderer(CardRenderer cr)
    {
        super(cr);
        eraser_bytes = cr.cardData.getEraser(false).byPalette(cr.paletteType());
    }

    @Override
    public boolean render(ByteMatrix2x2 mainBytes)
    {
        erasedAnything = false;
        changedAnything = super.render(mainBytes);

        if (cr.isScratching())
        {
            for (int i = 0; i < eraser_bytes.width; i++)
            {
                for (int j = 0; j < eraser_bytes.height; j++)
                {
                    if (eraser_bytes.get(i, j, 1) != -128) //not transparent
                    {
                        int k = (int) cr.eraserRenderer.x + i;
                        int m = (int) cr.eraserRenderer.y + j;
                        if (k < 0 || k >= 128 || m < 0 || m >= 128)
                            continue;
                        if(cr.overlayRenderer.deletedPixels.get(k, m))//already deleted
                            continue;
                        cr.overlayRenderer.deletedPixels.set(k, m, true);

                        if (cr.overlayRenderer.overlayBytes.get(k, m, 1) != -128)//not transparent
                        {
                            if(cr.eraseParticlesRenderer.enabled)
                            {
                                //copy the previous pixel color and make it fall: animated scratch particles
                                cr.eraseParticlesRenderer.addParticleDot(new ParticleDot(k, m, cr.overlayRenderer.overlayBytes.get(k, m, 0)));
                                changedAnything = true;
                            }
                            cr.overlayRenderer.erasedPixelsCounter++;
                            erasedAnything = true;
                        }
                    }
                }
            }

            if (erasedAnything && cr.cardData.hasCursorPressedSound())
                cr.cardData.getCursorPressedSound().tryPlay(cr.player());
//            if(erasedAnything)
//            {
//               //System.out.println("scratching " +  (cr.overlayRenderer.erasedPixelsCounter * 100 / cr.overlayRenderer.totalNonTransparentPixels) + "%");
//               //System.out.println(cr.overlayRenderer.erasedPixelsCounter);
//               //System.out.println(cr.overlayRenderer.totalNonTransparentPixels);
//            }
        }
        return changedAnything;
    }

    public boolean hasScratchedEnough()
    {
        //erased : total = x : 100
        return (double) (cr.overlayRenderer.erasedPixelsCounter * 100) / cr.overlayRenderer.totalNonTransparentPixels > cr.cardData.getNeededScratchPercentage();
    }

    public boolean hasScratchedSomeParts()
    {
        return cr.overlayRenderer.erasedPixelsCounter > 0;
    }
}
