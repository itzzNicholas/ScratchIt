package dev.lone.scratchit.map;

import dev.lone.scratchit.config.Settings;
import dev.lone.scratchit.map.image.ImageBytes;
import dev.lone.scratchit.map.card.CardRenderer;
import dev.lone.scratchit.map.image.ImgCache;
import dev.lone.scratchit.map.image.ImgData;
import dev.lone.scratchit.map.image.PaletteType;
import dev.lone.scratchit.util.ByteMatrix2x2;
import dev.lone.scratchit.util.ByteMatrix3x3;

import java.util.ArrayList;
import java.util.List;

public class AnimationFrames
{
    List<ImgData> frames;
    int shiftX;
    int shiftY;
    private ImgCache imgCache;

    public AnimationFrames(ImgCache imgCache, int shiftX, int shiftY)
    {
        this.imgCache = imgCache;
        this.frames = new ArrayList<>();
        this.shiftX = shiftX;
        this.shiftY = shiftY;
    }

    public void addFrame(String filePath, boolean isReload)
    {
        if(isReload)
            imgCache.checkForFileChanges(filePath);

        if (imgCache.has(filePath))
        {
            frames.add(imgCache.get(filePath));
            return;
        }

        ImgData imgData = new ImgData(filePath, 
                                      ImageBytes.imageToByteMatrix(PaletteType.MC1_8, filePath),
                                      ImageBytes.imageToByteMatrix(PaletteType.MC1_12, filePath),
                                      ImageBytes.imageToByteMatrix(PaletteType.MC1_16, filePath)
        );
        imgCache.save(filePath, imgData);
        frames.add(imgData);
    }
    
    public void addFrame(String filePath, ByteMatrix3x3 texture18, ByteMatrix3x3 texture112, ByteMatrix3x3 texture116)
    {
        ImgData imgData = new ImgData(filePath, texture18, texture112, texture116);
        imgCache.save(filePath, imgData);
        frames.add(imgData);
    }

    public ImgData getFrame(int index)
    {
        return frames.get(index);
    }

    public boolean render(CardRenderer cr, int index, ByteMatrix2x2 mainBytes, int prevIndex)
    {
        if (hasFinishedAnimating(index))
            return false;

        int xCentered = 0;
        int yCentered = 0;
        ImgData frame = getFrame(index);
        if (prevIndex != -1 && frame.getImageHashCode() == getFrame(prevIndex).getImageHashCode())
            return false;

        for (int x = 0; x < frame.getWidth(); x++)
        {
            for (int y = 0; y < frame.getHeight(); y++)
            {
                //(128 - larghezzaFrame) / 2 = ottengo shift da sinistra
                xCentered = ((128 - frame.getWidth()) / 2) + x + shiftX;
                yCentered = ((128 - frame.getHeight()) / 2) + y + shiftY;

                if (!frame.isTransparent(x, y))
                {
                    if (Settings.inst().FIX_SEMITRANSPARENT_PIXELS && frame.isSemiTransparent(x, y))
                    {
                        mainBytes.set(xCentered, yCentered, ImageBytes.blendByteColors(cr.paletteType(), mainBytes.get(x, y), frame.byPalette(cr.paletteType()).get(x, y, 0), frame.byPalette(cr.paletteType()).get(x, y, 1)));
                    }
                    else
                    {
                        mainBytes.set(xCentered, yCentered, frame.byPalette(cr.paletteType()).get(x, y, 0));
                    }
                }
            }
        }
        return true;
    }

    public int getFramesCount()
    {
        return frames.size();
    }

    public boolean hasFinishedAnimating(int index)
    {
        return (getFramesCount() == 0 || index > getFramesCount() - 1);
    }

    public int getShiftX()
    {
        return shiftX;
    }

    public int getShiftY()
    {
        return shiftY;
    }
}
