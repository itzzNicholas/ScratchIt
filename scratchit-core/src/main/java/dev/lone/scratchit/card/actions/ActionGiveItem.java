package dev.lone.scratchit.card.actions;

import dev.lone.scratchit.util.InvUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Gives item to player and if player has no space in inventory it will drop the items on the ground
 */
public class ActionGiveItem extends ActionDelay
{
    ItemStack itemStack;

    public ActionGiveItem(ItemStack itemStack, int amount, int damage, long delay)
    {
        super(delay);
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(amount);
        // TODO use modern API.
        this.itemStack.setDurability((short) damage);
    }

    @Override
    public void execute(Player player)
    {
        execute(() -> doExecute(player));
    }

    private void doExecute(Player player)
    {
        InvUtil.giveItem(player, itemStack);
    }
}
