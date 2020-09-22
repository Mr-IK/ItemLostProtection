package jp.mkserver.itemlostprotection.inv.multiver;

import net.minecraft.server.v1_16_R2.ChatMessage;
import net.minecraft.server.v1_16_R2.Containers;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.PacketPlayOutOpenWindow;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class UpdateTitle_1_16_2 implements UpdateTitle{
    @Override
    public void sendTitleChangePacket(Player p, String title, Inventory inv) {
        EntityPlayer ep = ((CraftPlayer)p).getHandle();
        Containers con = Containers.GENERIC_9X1;
        if(inv.getSize()==18){
            con = Containers.GENERIC_9X2;
        }else if(inv.getSize()==27){
            con = Containers.GENERIC_9X3;
        }else if(inv.getSize()==36){
            con = Containers.GENERIC_9X4;
        }else if(inv.getSize()==45){
            con = Containers.GENERIC_9X5;
        }else if(inv.getSize()==54){
            con = Containers.GENERIC_9X6;
        }
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(ep.activeContainer.windowId, con, new ChatMessage(title));
        ep.playerConnection.sendPacket(packet);
        ep.updateInventory(ep.activeContainer);
    }
}
