package me.faris.kingkits.listeners.event.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerKitEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private String kitName = "";
    private String oldKit = "";
    private List<ItemStack> kitContents = new ArrayList<ItemStack>(), armourItems = new ArrayList<ItemStack>();
    private List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
    private List<String> kitCommands = new ArrayList<String>();

    private boolean isCancelled = false;

    // Cancelled
    // Canceled

    /**
     * Create a new PlayerKitEvent instance.
     *
     * @param player - The player.
     * @param kitName - The new kit.
     */
    public PlayerKitEvent(Player player, String kitName) {
        super(player);
        this.kitName = kitName;
    }

    /**
     * Create a new PlayerKitEvent instance.
     *
     * @param player - The player.
     * @param kitName - The new kit.
     * @param oldKit - The previous kit the player was.
     */
    public PlayerKitEvent(Player player, String kitName, String oldKit) {
        super(player);
        this.kitName = kitName;
        this.oldKit = oldKit;
    }

    /**
     * Create a new PlayerKitEvent instance.
     *
     * @param player - The player.
     * @param kitName - The new kit.
     * @param oldKit - The previous kit the player was.
     * @param newKitItems - The kit items.
     * @param armourItems - The kit's armour items.
     */
    public PlayerKitEvent(Player player, String kitName, String oldKit, List<ItemStack> newKitItems, List<ItemStack> armourItems) {
        super(player);
        this.kitName = kitName;
        this.oldKit = oldKit;
        this.kitContents = newKitItems;
        this.armourItems = armourItems;
    }

    /**
     * Create a new PlayerKitEvent instance.
     *
     * @param player - The player.
     * @param kitName - The new kit.
     * @param oldKit - The previous kit the player was.
     * @param newKitItems - The kit items.
     * @param armourItems - The kit's armour items.
     */
    public PlayerKitEvent(Player player, String kitName, String oldKit, List<ItemStack> newKitItems, List<ItemStack> armourItems, List<PotionEffect> potionEffects) {
        super(player);
        this.kitName = kitName;
        this.oldKit = oldKit;
        this.kitContents = newKitItems;
        this.armourItems = armourItems;
        this.potionEffects = potionEffects;
    }

    /**
     * @return An unmodifiable List of all the kit-specific-commands run.
     */
    public List<String> getCommands() {
        return Collections.unmodifiableList(this.kitCommands);
    }

    /**
     * @return The kit's name.
     */
    public String getKit() {
        return this.kitName;
    }

    /**
     * @return An unmodifiable List of the armour items in the kit.
     */
    public List<ItemStack> getKitArmour() {
        return Collections.unmodifiableList(this.armourItems);
    }

    /**
     * @return An unmodifiable List of items in the kit.
     */
    public List<ItemStack> getKitContents() {
        return Collections.unmodifiableList(this.kitContents);
    }

    /**
     * @return An unmodifiable List of all the potion effects.
     */
    public List<PotionEffect> getPotionEffects() {
        return Collections.unmodifiableList(this.potionEffects);
    }

    /**
     * @return The player's old kit name.
     */
    public String getOldKit() {
        return this.oldKit;
    }

    public void setCommands(List<String> commands) {
        this.kitCommands = commands != null ? commands : new ArrayList<String>();
    }

    /**
     * Set the armour contents in the kit *
     */
    public void setKitArmour(List<ItemStack> armourContents) {
        this.armourItems = armourContents != null ? armourContents : new ArrayList<ItemStack>();
    }

    /**
     * Set the item contents of the kit *
     */
    public void setKitContents(List<ItemStack> kitContents) {
        this.kitContents = kitContents != null ? kitContents : new ArrayList<ItemStack>();
    }

    /**
     * Set the potion effects gained when using the kit *
     */
    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects != null ? potionEffects : new ArrayList<PotionEffect>();
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean flag) {
        this.isCancelled = flag;
    }
}
