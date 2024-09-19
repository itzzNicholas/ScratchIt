package dev.lone.scratchit.gui.generic;

import de.rapha149.signgui.SignGUI;
import dev.lone.scratchit.Main;
import dev.lone.scratchit.commands.MainCommand;
import dev.lone.scratchit.util.Utils;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public abstract class PagedViewer extends AbstractGUI implements IPagedView
{
    static final int MAX_ITEMS_PER_PAGE = 45;
    private final FastInv fastInv;

    protected Player owner;
    GUISettings settings;

    protected final List<ItemStack> items;
    private final int pagesCount;

    int currentPage;
    protected int currentIndex;

    public PagedViewer(Player owner, GUISettings guiSettings, List<ItemStack> items)
    {
        this.owner = owner;
        this.settings = guiSettings;
        this.items = items;
        this.pagesCount = Utils.ceilDivision(items.size(), MAX_ITEMS_PER_PAGE);
        this.fastInv = new FastInv(6 * 9, settings.title);
    }

    public void show()
    {
        currentIndex = currentPage * MAX_ITEMS_PER_PAGE;
        for (int i = 0; i < fastInv.getInventory().getSize(); i++)
            fastInv.removeItem(i);

        fillInventory(fastInv, settings.backgroundIcon);

        handleShowItems(fastInv);
        handlePages(fastInv);
    }

    public void handleShowItems(FastInv fastInv)
    {
        int j = 0;
        for (int i = currentIndex; i < currentIndex + 45 && i < items.size(); i++)
        {
            fastInv.setItem(j, items.get(i), this::itemClickEvent);
            j++;
        }
    }

    private void handlePages(FastInv fastInv)
    {
        if (MainCommand.checkPermission(owner, "scratchit.admin.config.create"))
        {
            fastInv.setItem(49, settings.createItemIcon, inventoryClickEvent -> {

                SignGUI gui = SignGUI.builder()
                        .setHandler((player, result) -> {
                            StringBuilder cardName = new StringBuilder();
                            for (String str : result.getLines())
                                cardName.append(str);
                            try
                            {
                                Main.inst.cardsStorage.createNewCard(owner, cardName.toString());
                            }
                            catch (IOException e)
                            {
                                inventoryClickEvent.getWhoClicked().sendMessage("Error while creating the card. Check console for more info. " + e.getMessage());
                                e.printStackTrace();
                            }

                            return Collections.emptyList();
                        }).build();
                gui.open((Player) inventoryClickEvent.getWhoClicked());
            });
        }

        if (currentPage > 0)
        {
            fastInv.setItem(52, settings.prevIcon, inventoryClickEvent -> {
                if (currentPage - 1 < 0)
                {
                    currentPage--;
                    this.show();
                }
            });
        }

        if (currentPage + 1 < pagesCount)
        //if((currentPage * maxItemsPerPage) < (items.size() - 1))
        {
            fastInv.setItem(53, settings.nextIcon, inventoryClickEvent -> {
                if (currentPage + 1 < items.size())
                {
                    currentPage++;
                    this.show();
                }
            });
        }

        fastInv.open(owner);
    }
}
