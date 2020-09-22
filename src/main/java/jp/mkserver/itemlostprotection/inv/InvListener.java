package jp.mkserver.itemlostprotection.inv;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class InvListener implements Listener {
    private JavaPlugin plugin;
    protected InventoryAPI inv;
    protected UUID player;
    private String unique;

    public InvListener(JavaPlugin plugin, InventoryAPI inv){
        this.plugin = plugin;
        this.inv = inv;
        this.unique = inv.getInvUniqueID();
    }

    protected void register(UUID uuid){
        this.player = uuid;
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    protected boolean ClickCheck(InventoryClickEvent e){
        if(e.getClickedInventory() == null){
            return false;
        }
        if(e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
            return false;
        }
        if(player==null||!e.getWhoClicked().getUniqueId().equals(player)){
            return false;
        }
        return checkUnique();
    }

    protected void closeCheck(InventoryCloseEvent e){
        if(e.getPlayer().getUniqueId()==player){
            unregister();
        }
    }

    protected boolean checkUnique(){
        return inv.getName().contains(unique);
    }

    protected void unregisterInv(){
        HandlerList.unregisterAll(this);
        this.player = null;
    }

    protected void unregister(){
        HandlerList.unregisterAll(this);
        this.player = null;
        inv.unregistRunnable(this);
    }
}