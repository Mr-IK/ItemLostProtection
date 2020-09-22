package jp.mkserver.itemlostprotection.inv;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class KInventory {

    private static JavaPlugin plugin;

    public static void init(JavaPlugin plugin){
        KInventory.plugin = plugin;
    }

    public static InventoryAPI get3_9Inv(Player p, InventoryAPI invs, String name){
        if(invs!=null&&invs.getSize()==27){
            invs.regenerateID();
            invs.allunregistRunnable();
            invs.updateTitle(p,name);
            invs.clear();
            invs.addOriginalListing(new InvListener(plugin, invs) {
                @EventHandler
                public void onClick(InventoryClickEvent e) {
                    if (!super.ClickCheck(e)) {
                        return;
                    }
                    e.setCancelled(true);
                }

                @EventHandler
                public void onClose(InventoryCloseEvent e) {
                    super.closeCheck(e);
                }
            });
            ItemStack wall = invs.createUnbitem(" ", new String[]{}, Material.BLACK_STAINED_GLASS_PANE, 0, false);
            invs.fillInv(wall);
            invs.setOpenType(true);
            return invs;
        }else {
            InventoryAPI inv = new InventoryAPI(plugin, name, 27);
            inv.addOriginalListing(new InvListener(plugin, inv) {
                @EventHandler
                public void onClick(InventoryClickEvent e) {
                    if (!super.ClickCheck(e)) {
                        return;
                    }
                    e.setCancelled(true);
                }

                @EventHandler
                public void onClose(InventoryCloseEvent e) {
                    super.closeCheck(e);
                }
            });
            ItemStack wall = inv.createUnbitem(" ", new String[]{}, Material.BLACK_STAINED_GLASS_PANE, 0, false);
            inv.fillInv(wall);
            return inv;
        }
    }

    public static InventoryAPI get6_9Inv(Player p,InventoryAPI invs,String name){
        if(invs!=null&&invs.getSize()==54){
            invs.regenerateID();
            invs.allunregistRunnable();
            invs.updateTitle(p,name);
            invs.clear();
            invs.addOriginalListing(new InvListener(plugin, invs) {
                @EventHandler
                public void onClick(InventoryClickEvent e) {
                    if (!super.ClickCheck(e)) {
                        return;
                    }
                    e.setCancelled(true);
                }

                @EventHandler
                public void onClose(InventoryCloseEvent e) {
                    super.closeCheck(e);
                }
            });
            invs.setOpenType(true);
            return invs;
        }else {
            InventoryAPI inv = new InventoryAPI(plugin, name, 54);
            inv.addOriginalListing(new InvListener(plugin, inv) {
                @EventHandler
                public void onClick(InventoryClickEvent e) {
                    if (!super.ClickCheck(e)) {
                        return;
                    }
                    e.setCancelled(true);
                }

                @EventHandler
                public void onClose(InventoryCloseEvent e) {
                    super.closeCheck(e);
                }
            });
            return inv;
        }
    }

    public static InventoryAPI getListupInv(Player p,InventoryAPI invs,String name){
        InventoryAPI inv = get6_9Inv(p,invs,name);
        ItemStack wall = inv.createUnbitem(" ",new String[]{}, Material.BLACK_STAINED_GLASS_PANE,0,false);
        ItemStack back = inv.createUnbitem("§f§l§o前のページへ",new String[]{}, Material.WHITE_STAINED_GLASS_PANE,0,false);
        ItemStack walk = inv.createUnbitem("§f§l§o次のページへ",new String[]{}, Material.WHITE_STAINED_GLASS_PANE,0,false);
        inv.setItems(new int[]{45,46},back);
        inv.setItems(new int[]{47,48,50,51},wall);
        inv.setItems(new int[]{52,53},walk);
        inv.setItem(49,inv.createUnbitem("§c§l戻る",new String[]{"§e前のページに戻ります。"},
                Material.DARK_OAK_DOOR,0,false));
        return inv;
    }
}
