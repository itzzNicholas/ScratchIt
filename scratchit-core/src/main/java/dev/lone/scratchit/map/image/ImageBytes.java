package dev.lone.scratchit.map.image;

import dev.lone.scratchit.Main;
import dev.lone.scratchit.util.ByteMatrix3x3;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

public class ImageBytes
{
    static final HashMap<PaletteType, HashMap<Integer, Byte>> cached = new HashMap<>();

    @Nullable
    public static ByteMatrix3x3 imageToByteMatrix(PaletteType paletteType, String filePath)
    {
        //dirty
        if(paletteType == PaletteType.MC1_16 && !Main.config.getBoolean("graphics.viaversion.v1_16"))
            return null;

        if(paletteType == PaletteType.MC1_12 && !Main.config.getBoolean("graphics.viaversion.v1_12"))
            return null;

        if(paletteType == PaletteType.MC1_8 && !Main.config.getBoolean("graphics.viaversion.v1_8"))
            return null;

        File file = new File(filePath);
        if(!file.exists())
            return null;
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        try
        {
            BufferedImage buffer = getBufferImage(file);
            ByteMatrix3x3 result = new ByteMatrix3x3(buffer.getWidth(), buffer.getHeight(), 2);
            for (int i = 0; i < buffer.getHeight(); i++)
            {
                for (int j = 0; j < buffer.getWidth(); j++)
                {
                    Color tmp = new Color(buffer.getRGB(j, i), true);
                    result.set(j, i, 0, paletteType.palette.matchColor(tmp));
                    result.set(j, i, 1, (byte) (tmp.getAlpha() - 128));//byte can store only -128 to 128
                }
            }
            //System.out.println(Arrays.deepToString(result));
            return result;
        } catch (Exception localException1)
        {
            localException1.printStackTrace();
        }
        return null;
    }

    private static BufferedImage getBufferImage(File file)
    {
        BufferedImage buffer = null;
        if ((file != null) && (file.exists()))
        {
            try
            {
                buffer = ImageIO.read(file);
            } catch (Exception localException)
            {
                buffer = null;
            }
        }
        return buffer;
    }

    public static int blendIntColors(PaletteType paletteType, byte color1, byte color2, int alpha)
    {
        return blendIntColors(byteColorToInt(paletteType, color1), byteColorToInt(paletteType, color2), alpha);
    }

    public static int byteColorToInt(PaletteType paletteType, byte index)
    {
        if ((index <= -21 || index >= 0) && index <= 127)
        {
            return paletteType.palette.intColorValues[index >= 0 ? index : index + 256];
        }
        else
        {
            throw new IndexOutOfBoundsException();
        }
    }

    public static byte intColorToByte(PaletteType paletteType, int color)
    {
        if(cached.containsKey(paletteType))
        {
            Byte bb = cached.get(paletteType).get(color);
            if(bb != null)
                return bb;
        }

        int index = 0;
        double best = -1.0D;

        for (int i = 4; i < paletteType.palette.intColorValues.length; ++i)
        {
            double distance = getDistanceInt(color, paletteType.palette.intColorValues[i]);
            if (distance < best || best == -1.0D)
            {
                best = distance;
                index = i;
            }
        }

        byte b = (byte) (index < 128 ? index : -129 + (index - 127));
        cached.computeIfAbsent(paletteType, paletteType1 -> new HashMap<>()).putIfAbsent(color, b);
        return b;
    }

    public static byte darkerByteColor(PaletteType paletteType, byte color)
    {
        return intColorToByte(paletteType, paletteType.palette.byteToColorDarker[color >= 0 ? color : color + 256].getRGB());
    }

    public static byte blendByteColors(PaletteType paletteType, byte color1, byte color2, byte alpha)
    {
        return intColorToByte(
                paletteType, blendIntColors(byteColorToInt(paletteType, color1), byteColorToInt(paletteType, color2), alpha + 128)
        );
    }

    public static int blendIntColors(int color1, int color2, int alpha)
    {
        int rb = color1 & 0xff00ff;
        int g = color1 & 0x00ff00;
        rb += ((color2 & 0xff00ff) - rb) * alpha >> 8;
        g += ((color2 & 0x00ff00) - g) * alpha >> 8;
        return 0xff000000 | (rb & 0xff00ff) | (g & 0xff00);
    }

//    public static int blendColors(int color1, int color2, int alpha)
//    {
//        int rb = color1 & 0xff00ff;
//        int g = color1 & 0x00ff00;
//        rb += ((color2 & 0xff00ff) - rb) * alpha >> 8;
//        g += ((color2 & 0x00ff00) - g) * alpha >> 8;
//        return (rb & 0xff00ff) | (g & 0xff00);
//    }

    private static double getDistanceInt(int c1, int c2)
    {
        int red_1 = (c1 >> 0) & 255;
        int green_1 = (c1 >> 8) & 255;
        int blue_1 = (c1 >> 16) & 255;

        int red_2 = (c2 >> 0) & 255;
        int green_2 = (c2 >> 8) & 255;
        int blue_2 = (c2 >> 16) & 255;

        double rmean = (double) (red_1 + red_2) / 2.0D;
        double r = red_1 - red_2;
        double g = green_1 - green_2;
        int b = blue_1 - blue_2;
        double weightR = 2.0D + rmean / 256.0D;
        double weightG = 4.0D;
        double weightB = 2.0D + (255.0D - rmean) / 256.0D;
        return weightR * r * r + weightG * g * g + weightB * (double) b * (double) b;
    }
}