package dev.lone.scratchit.card;

import dev.lone.scratchit.map.AnimationFrames;
import dev.lone.scratchit.map.card.CardRenderer;
import dev.lone.scratchit.util.ByteMatrix2x2;
import org.jetbrains.annotations.Nullable;

public class AnimationPlayer
{
    private static final int DELAY_BETWEEN_FRAMES = 0;

    private CardRenderer cr;
    @Nullable
    AnimationFrames animationFrames;
    private int index;
    private int currentDelay;

    public AnimationPlayer(CardRenderer cr, AnimationFrames animationFrames)
    {
        this.cr = cr;
        this.animationFrames = animationFrames;
        this.index = 0;
        this.currentDelay = DELAY_BETWEEN_FRAMES + 1;
    }

    public boolean render(ByteMatrix2x2 mainBytes)
    {
        boolean res = false;
        if (DELAY_BETWEEN_FRAMES > 0)
        {
            currentDelay++;
            if (currentDelay > DELAY_BETWEEN_FRAMES)
            {
                res = animationFrames.render(cr, index, mainBytes, index - 1);
                index++;
                currentDelay = 0;
            }
        }
        else
        {
            res = animationFrames.render(cr, index, mainBytes, index - 1);
            index++;
        }
        return res;
    }

    public boolean hasFinishedAnimating()
    {
        if (animationFrames == null)
            return true;
        return animationFrames.hasFinishedAnimating(index);
    }

    public boolean hasAnimation()
    {
        return animationFrames != null;
    }
}
