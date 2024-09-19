package dev.lone.scratchit.map.card.elements;

import dev.lone.scratchit.map.IMapElementRenderer;
import dev.lone.scratchit.map.card.CardRenderer;
import dev.lone.scratchit.util.ByteMatrix2x2;
import dev.lone.scratchit.util.ByteMatrix3x3;
import lombok.Getter;
import org.bukkit.Location;

import static org.bukkit.Location.normalizeYaw;

public class CursorRenderer implements IMapElementRenderer
{
    protected CardRenderer cr;

    protected final ByteMatrix3x3 cursor_bytes;
    protected ByteMatrix3x3 pressedCursor_bytes;

    protected int prevX;
    protected int prevY;

    protected double x;
    protected double y;

    protected double targetX;
    protected double targetY;

    public double xOffset;
    public double yOffset;
    public float startingYav;
    public float startingPitch;

    protected double cursorSpeed;
    protected ByteMatrix3x3 coinToDrawBytes;

    protected boolean changedAnything = false;

    @Getter
    protected boolean isMoving = false;
    protected int wasScratching = 0;

    public CursorRenderer(CardRenderer cr)
    {
        this.cr = cr;

        cursor_bytes = cr.cardData.getCoin(false).byPalette(cr.paletteType());
        pressedCursor_bytes = cr.cardData.getPressedCoin(false).byPalette(cr.paletteType());
        cursorSpeed = cr.cardData.getCoinSpeed();

        xOffset = ((double) 128 / 2) - ((double) cursor_bytes.width / 2);
        yOffset = ((double) 128 / 2) - ((double) cursor_bytes.height / 2);
        startingYav = cr.startYav;
        startingPitch = cr.startPitch;

        prevX = 0;
        prevY = 0;

        x = xOffset;
        y = yOffset;
    }

    @Override
    public boolean render(ByteMatrix2x2 mainBytes)
    {
        // Update coin coords
        //TODO: fix the fact that at 180 tto -179.9 there is a little jump that makes the cursor go back to the center.
        targetX = (cursorSpeed * (Location.normalizeYaw(cr.player().getLocation().getYaw()) - startingYav) + xOffset) % 127;
        targetY = cursorSpeed * (cr.player().getLocation().getPitch() - startingPitch) + yOffset;

        double lerpFactor = 0.8;
        x = lerp(x, targetX, lerpFactor);
        y = lerp(y, targetY, lerpFactor);

        isMoving = false;

//       //System.out.println(x + " | " + y + " (" + ((int) cr.player.getLocation().getYaw()) + " | " + ((int) cr.player.getLocation().getPitch()));

        // Decide the cursor icon based if it's pressed or not
        if (cr.isScratching())
        {
            coinToDrawBytes = pressedCursor_bytes;
        }
        else
        {
            if(!cr.eraseParticlesRenderer.changedAnything && x == prevX && y == prevY)
                return false;
            coinToDrawBytes = cursor_bytes;
        }

        changedAnything = false;

        // Actually render the cursor with wrapping
        for (int i = 0; i < coinToDrawBytes.width; i++)
        {
            for (int j = 0; j < coinToDrawBytes.height; j++)
            {
                // Not transparent
                if (coinToDrawBytes.get(i, j, 1) != -128)
                {
                    int k = (int) (x + i);
                    int m = (int) (y + j);

                    // Apply wrapping logic for horizontal and vertical
                    if (k < 0)
                        k = 128 + k; // Warp from left to right
                    else if (k >= 128)
                        k = k - 128; // Warp from right to left

                    if (m < 0)
                        m = 128 + m; // Warp from top to bottom
                    else if (m >= 128)
                        m = m - 128; // Warp from bottom to top

                    // Ensure the pixel is within bounds before setting it
                    if (k >= 0 && k < 128 && m >= 0 && m < 128)
                    {
                        mainBytes.set(k, m, coinToDrawBytes.get(i, j, 0));
                        changedAnything = true;
                        isMoving = true;  // Consider as moving
                    }
                }
            }
        }

        prevX = (int) x;
        prevY = (int) y;

        return changedAnything;
    }

    private double lerp(double start, double end, double factor)
    {
        return start + factor * (end - start);
    }
}
