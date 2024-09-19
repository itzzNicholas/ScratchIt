package dev.lone.scratchit.gui;

import dev.lone.scratchit.CardsStorage;
import dev.lone.scratchit.gui.generic.PagedViewer;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CardsListViewer extends PagedViewer
{
    private final CardsStorage cardsStorage;

    public CardsListViewer(Player owner, CardsStorage cardsStorage)
    {
        super(owner, GUIsGlobalStuff.inst.settings_cardsPagedViewer, cardsStorage.cardsItemsCache);
        this.cardsStorage = cardsStorage;
    }

    @Override
    public void itemClickEvent(InventoryClickEvent e)
    {
        new CardInfoViewer(owner, e.getCurrentItem(), this).show();
    }

    @Override
    public void handleShowItems(FastInv fastInv)
    {
        int j = 0;
        for (int i = currentIndex; i < currentIndex + 45 && i < items.size(); i++)
        {
            if(cardsStorage.cards.get(i).permissions.checkShowInListGuiPermission(owner))
            {
                fastInv.setItem(j, items.get(i), this::itemClickEvent);
                j++;
            }
        }
    }
}
