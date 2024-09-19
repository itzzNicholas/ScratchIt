package dev.lone.scratchit.map.image;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

public abstract class Palette
{
    Color[] byteToColor;
    int[] intColorValues;
    final Color[] byteToColorDarker = new Color[256];

    protected int ci(int r, int g, int b)
    {
        return new Color(r, g, b).getRGB();
    }

    protected void cb()
    {
        for (int i = 0; i < byteToColor.length; i++)
            byteToColorDarker[i] = byteToColor[i].darker();
    }

    protected Color c(int r, int g, int b)
    {
        return new Color(r, g, b);
    }

    public byte matchColor(@NotNull Color color)
    {
        if (color.getAlpha() < 128)
        {
            return 0;
        }
        else
        {
            int index = 0;
            double best = -1.0D;

            for (int i = 4; i < byteToColor.length; ++i)
            {
                double distance = getDistance(color, byteToColor[i]);
                if (distance < best || best == -1.0D)
                {
                    best = distance;
                    index = i;
                }
            }

            return (byte) (index < 128 ? index : -129 + (index - 127));
        }
    }

    private static double getDistance(@NotNull Color c1, @NotNull Color c2)
    {
        double rmean = (double) (c1.getRed() + c2.getRed()) / 2.0D;
        double r = (double) (c1.getRed() - c2.getRed());
        double g = (double) (c1.getGreen() - c2.getGreen());
        int b = c1.getBlue() - c2.getBlue();
        double weightR = 2.0D + rmean / 256.0D;
        double weightG = 4.0D;
        double weightB = 2.0D + (255.0D - rmean) / 256.0D;
        return weightR * r * r + weightG * g * g + weightB * (double) b * (double) b;
    }
}
