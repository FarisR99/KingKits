package com.faris.kingkits.guis;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.hooks.Plugin;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public abstract class GuiKingKits implements Listener {
    public static Map<String, GuiKitMenu> guiKitMenuMap = new HashMap<String, GuiKitMenu>();
    public static Map<String, GuiPreviewKit> guiPreviewKitMap = new HashMap<String, GuiPreviewKit>();

    private KingKits plugin = null;
    private Player player = null;
    private String playerName = null;
    protected Inventory guiInventory = null;

    /**
     * Create a new GUI instance.
     *
     * @param player - The player that is using the menu
     * @param inventory - The inventory.
     */
    public GuiKingKits(Player player, Inventory inventory) {
        Validate.notNull(player);
        this.plugin = Plugin.getPlugin();

        this.player = player;
        this.playerName = this.player.getName();

        this.guiInventory = inventory;
        if (this.guiInventory == null)
            this.guiInventory = this.player.getServer().createInventory(this.player, InventoryType.CHEST);

        if (Plugin.isInitialised()) this.player.getServer().getPluginManager().registerEvents(this, this.plugin);
        else
            this.player.getServer().getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("KingKits"));
    }

    public boolean openMenu() {
        this.closeMenu(false, true);
        if (this.plugin == null) this.plugin = Plugin.getPlugin();
        if (!guiPreviewKitMap.containsKey(this.playerName)) {
            if (this.getPlayer() != null) {
                this.guiInventory.clear();
                this.fillInventory();
                this.getPlayer().openInventory(this.guiInventory);
                return true;
            }
        }
        return false;
    }

    protected abstract void fillInventory();

    public void closeMenu(boolean unregisterEvents, boolean closeInventory) {
        if (this.guiInventory != null) this.guiInventory.clear();
        if (unregisterEvents) HandlerList.unregisterAll(this);
        if (closeInventory && this.player != null) this.player.closeInventory();
        this.plugin = null;
    }

    @EventHandler
    protected abstract void onPlayerClickInventory(InventoryClickEvent event);

    @EventHandler(priority = EventPriority.LOW)
    protected void onPlayerCloseInventory(InventoryCloseEvent event) {
        try {
            if (this.guiInventory != null && event.getInventory() != null) {
                if (event.getPlayer() instanceof Player) {
                    if (this.playerName.equals(event.getPlayer().getName())) this.closeMenu(true, false);
                }
            }
        } catch (Exception ex) {
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    protected String getPlayerName() {
        return this.playerName;
    }

    protected KingKits getPlugin() {
        return this.plugin;
    }

}
