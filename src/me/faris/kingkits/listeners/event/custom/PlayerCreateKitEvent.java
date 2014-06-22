package me.faris.kingkits.listeners.event.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerCreateKitEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private String kitName = "";
    private List<ItemStack> kitContents = new ArrayList<ItemStack>(), armourContents = new ArrayList<ItemStack>();

    private boolean isCanceled = false;

    /**
     * Create a new PlayerCreateKitEvent instance.
     *
     * @param player - The player.
     * @param kitName - The new kit.
     * @param newKitItems - The kit items.
     */
    public PlayerCreateKitEvent(Player player, String kitName, List<ItemStack> newKitItems, List<ItemStack> armourContents) {
        super(player);
        this.kitName = kitName;
        this.kitContents = newKitItems;
        this.armourContents = armourContents;
    }

    /**
     * Returns the kit's name *
     */
    public String getKit() {
        return this.kitName;
    }

    /**
     * Returns an unmodifiable List of armour in the new kit *
     */
    public List<ItemStack> getKitArmour() {
        return Collections.unmodifiableList(this.armourContents);
    }

    /**
     * Returns an unmodifiable List of items in the new kit *
     */
    public List<ItemStack> getKitContents() {
        return Collections.unmodifiableList(this.kitContents);
    }

    /**
     * Set the item contents of the new kit *
     */
    public void setArmourContents(List<ItemStack> armourContents) {
        if (armourContents != null) this.armourContents = armourContents;
    }

    /**
     * Set the item contents of the new kit *
     */
    public void setKitContents(List<ItemStack> kitContents) {
        if (kitContents != null) this.kitContents = kitContents;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCanceled;
    }

    @Override
    public void setCancelled(boolean flag) {
        this.isCanceled = flag;
    }

}
