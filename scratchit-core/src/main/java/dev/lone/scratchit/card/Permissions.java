package dev.lone.scratchit.card;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class Permissions
{
    private String showInListGuiPermission;
    private String usePermission;

    public void setShowInListGuiPermission(String showInListGuiPermission)
    {
        if(showInListGuiPermission == null)
            return;
        this.showInListGuiPermission = "scratchit.card.show_in_list_gui." + showInListGuiPermission;
    }

    public void setUsePermission(String usePermission)
    {
        if(usePermission == null)
            return;
        this.usePermission = "scratchit.card.use." + usePermission;
    }

    public String getShowInListGuiPermission()
    {
        return showInListGuiPermission;
    }

    public String getUsePermission()
    {
        return usePermission;
    }

    public boolean checkShowInListGuiPermission(Player player)
    {
        return checkShowInListGuiPermission(player, null);
    }

    public boolean checkShowInListGuiPermission(Player player, Consumer<String> callback)
    {
        if(showInListGuiPermission == null)
            return true;

        if(callback != null)
            callback.accept(showInListGuiPermission);
        return player.hasPermission(showInListGuiPermission);
    }

    public boolean checkUsePermission(Player player)
    {
        return checkUsePermission(player, null);
    }

    public boolean checkUsePermission(Player player, Consumer<String> noPermissionCallback)
    {
        if(usePermission == null)
            return true;

        boolean has = player.hasPermission(usePermission);
        if(!has && noPermissionCallback != null)
            noPermissionCallback.accept(usePermission);
        return has;
    }
}
