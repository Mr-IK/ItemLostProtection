package jp.mkserver.itemlostprotection.inv.multiver;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface UpdateTitle {

    public void sendTitleChangePacket(Player p,String title, Inventory inv);
}
