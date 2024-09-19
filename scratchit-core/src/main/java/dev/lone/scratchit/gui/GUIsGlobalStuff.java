package dev.lone.scratchit.gui;

import dev.lone.LoneLibs.Mat;
import dev.lone.scratchit.Main;
import dev.lone.scratchit.gui.generic.GUISettings;
import dev.lone.scratchit.util.InvUtil;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class GUIsGlobalStuff
{
    public static GUIsGlobalStuff inst;

    public final ItemStack GET_ITEM;
    public final ItemStack BACK;
    public final ItemStack NEXT_PAGE;
    public final ItemStack PREV_PAGE;
    public final ItemStack OPEN_ONLINE_EDITOR;

    public final ItemStack ITEM_INFO_BACKGROUND;
    public final ItemStack LIST_BACKGROUND;
    public final ItemStack CREATE_ITEM;

    public GUISettings settings_cardsPagedViewer;
    public GUISettings settings_itemViewer;

    public GUIsGlobalStuff()
    {
        GET_ITEM = Mat.valueOf(Main.config.getString("gui.list.button_get_item")).getItemStack();
        BACK = Mat.valueOf(Main.config.getString("gui.list.button_back")).getItemStack();
        NEXT_PAGE = Mat.valueOf(Main.config.getString("gui.list.button_next_page")).getItemStack();
        PREV_PAGE = Mat.valueOf(Main.config.getString("gui.list.button_prev_page")).getItemStack();
        LIST_BACKGROUND = Mat.valueOf(Main.config.getString("gui.list.background")).getItemStack();
        CREATE_ITEM = Mat.valueOf(Main.config.getString("gui.list.button_create_item")).getItemStack();

        ITEM_INFO_BACKGROUND = Mat.valueOf(Main.config.getString("gui.item_info.background")).getItemStack();
        OPEN_ONLINE_EDITOR = Mat.valueOf(Main.config.getString("gui.item_info.button_open_online_editor")).getItemStack();


        InvUtil.renameItemStack(GET_ITEM, ChatColor.WHITE + Main.lang.getLocalized("button_get_item"));
        InvUtil.renameItemStack(BACK,ChatColor.WHITE +  Main.lang.getLocalized("button_back"));
        InvUtil.renameItemStack(NEXT_PAGE, ChatColor.WHITE + Main.lang.getLocalized("button_next_page"));
        InvUtil.renameItemStack(PREV_PAGE, ChatColor.WHITE + Main.lang.getLocalized("button_prev_page"));
        InvUtil.renameItemStack(CREATE_ITEM, ChatColor.WHITE + Main.lang.getLocalized("button_create_item"));

        InvUtil.renameItemStack(ITEM_INFO_BACKGROUND, " ");
        InvUtil.renameItemStack(LIST_BACKGROUND, " ");
        InvUtil.renameItemStack(OPEN_ONLINE_EDITOR, ChatColor.WHITE + Main.lang.getLocalized("button_open_online_editor"));

        settings_cardsPagedViewer = new GUISettings(
                Main.lang.getLocalized("cards_list_title"),
                GET_ITEM,
                BACK,
                PREV_PAGE,
                NEXT_PAGE,
                CREATE_ITEM,
                OPEN_ONLINE_EDITOR,
                LIST_BACKGROUND
        );

        settings_itemViewer = new GUISettings(
                "",
                GET_ITEM,
                BACK,
                PREV_PAGE,
                NEXT_PAGE,
                CREATE_ITEM,
                OPEN_ONLINE_EDITOR,
                ITEM_INFO_BACKGROUND
        );

    }

    public static void init()
    {
        inst = new GUIsGlobalStuff();
    }
}
