package dev.lone.scratchit.map.card.elements;

import dev.lone.scratchit.map.image.ImageBytes;
import dev.lone.scratchit.map.IMapElementRenderer;
import dev.lone.scratchit.map.card.CardRenderer;
import dev.lone.scratchit.map.card.ParticleDot;
import dev.lone.scratchit.util.ByteMatrix2x2;
import dev.lone.scratchit.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class EraseParticlesRenderer implements IMapElementRenderer
{
    public boolean enabled;
    CardRenderer cr;

    private List<ParticleDot> particleDots;
    private ListIterator<ParticleDot> iter;

    public float particlesSpeed = 4;
    public boolean changedAnything = false;

    public EraseParticlesRenderer(CardRenderer cr)
    {
        this.cr = cr;
        particleDots = new ArrayList<>();
        iter = particleDots.listIterator();
    }

    @Override
    public boolean render(ByteMatrix2x2 mainBytes)
    {
        if (particleDots.size() == 0)
            return false;

        changedAnything = false;
        iter = particleDots.listIterator();
        while (iter.hasNext())
        {
            ParticleDot particle = iter.next();
            //outside of the screen, underground
            if (particle.y > 127 || particle.x > 127 || particle.x < 0 || particle.y < 0)
            {
                iter.remove();
                continue;
            }
            byte b2 = ImageBytes.darkerByteColor(cr.paletteType(), particle.color);
            mainBytes.set((int) particle.x, (int) particle.y, b2);
            changedAnything = true;

            particle.y += particlesSpeed;
            particle.x -= Utils.randomNumber(-1, 1) * (particlesSpeed + Utils.randomNumber(0f, 0.5f));
        }

        return changedAnything;
    }

    public void addParticleDot(ParticleDot p)
    {
        if (particleDots.size() > 250)
        {
            particleDots.subList(particleDots.size() - 20, particleDots.size() - 1).clear();
        }
        particleDots.add(p);
    }
}
