package jp.mkserver.itemlostprotection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

import java.util.UUID;

public class ILPListener implements Listener {

    ItemLostProtection plugin;

    public ILPListener(ItemLostProtection plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event){
        UUID thrower = event.getEntity().getThrower();
        if(thrower==null){
            return;
        }
        plugin.putLostedItem(event.getEntity().getItemStack(),thrower);
    }
}
