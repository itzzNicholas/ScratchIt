package dev.lone.scratchit.gui;

import dev.lone.scratchit.Main;
import dev.lone.scratchit.card.CardData;
import dev.lone.scratchit.gui.generic.ItemViewer;
import dev.lone.scratchit.util.InvUtil;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CardInfoViewer extends ItemViewer
{
    private CardsListViewer cardsListViewer;

    private final CardData card;

    public CardInfoViewer(Player owner, ItemStack itemStack, CardsListViewer cardsListViewer)
    {
        super(owner, GUIsGlobalStuff.inst.settings_itemViewer.setTitle(InvUtil.getReadableName(itemStack)), itemStack);
        this.cardsListViewer = cardsListViewer;
        drawGui(fastInv);

        card = Main.inst.cardsStorage.getCard(CardData.getId(itemStack));
    }

    @Override
    protected void drawGui(FastInv fastInv)
    {
        fillInventory(fastInv, settings.backgroundIcon);

        fastInv.setItem(22, itemStack, inventoryClickEvent -> {
        });

        if(owner.hasPermission("scratchit.admin.get"))
        {
            fastInv.setItem(46, settings.getItemIcon, e -> {
                Bukkit.getServer().dispatchCommand(owner, "scratchit get " + card.getId());
            });
        }

        if(owner.hasPermission("scratchit.admin.config.create"))
        {
            fastInv.setItem(47, settings.openOnlineEditorIcon, e -> {
                Main.inst.cardsStorage.createNewWebEditor(card.getId(), owner);
                owner.closeInventory();
            });
        }

        if(cardsListViewer != null)
        {
            fastInv.setItem(45, settings.backIcon, e -> {
                cardsListViewer.show();
            });
        }
    }
}
