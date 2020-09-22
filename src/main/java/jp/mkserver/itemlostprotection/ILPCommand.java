package jp.mkserver.itemlostprotection;

import jp.mkserver.itemlostprotection.inv.InvListener;
import jp.mkserver.itemlostprotection.inv.InventoryAPI;
import jp.mkserver.itemlostprotection.inv.KInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ILPCommand implements CommandExecutor {

    ItemLostProtection plugin;

    public ILPCommand(ItemLostProtection plugin){
        this.plugin = plugin;
        plugin.getCommand("ilp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!plugin.power){
            return true;
        }
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("ilprotect.use")){
            p.sendMessage("Unknown command. Type \"/help\" for help.");
            return true;
        }
        if(args.length==0){
            openGUI(p,null,"main",new String[]{});
        }else if(args.length==1){
            if(args[0].equalsIgnoreCase("clear")){
                if(!p.hasPermission("ilprotect.forceclear")){
                    return true;
                }
                plugin.clearAllItem();
                return true;
            }else if(args[0].equalsIgnoreCase("reload")){
                if(!p.hasPermission("ilprotect.config")){
                    return true;
                }
                plugin.reload();
                p.sendMessage(plugin.prefix+plugin.oC+"リロードしました。");
                return true;
            }else if(args[0].equalsIgnoreCase("freload")){
                if(!p.hasPermission("ilprotect.freload")){
                    return true;
                }
                plugin.fReload();
                p.sendMessage(plugin.prefix+plugin.oC+"フォースリロードしました。");
                return true;
            }else if(args[0].equalsIgnoreCase("clearwarn")){
                if(!p.hasPermission("ilprotect.config")){
                    return true;
                }
                List<Integer> warnlist = plugin.getConfig().getIntegerList("clear.warn");
                warnlist.clear();
                plugin.getConfig().set("clear.warn",warnlist);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix+plugin.oC+"アイテム回収警告時間をクリアしました。/ilp reloadで反映されます。");
                return true;
            }else if(args[0].equalsIgnoreCase("clearworlds")){
                if(!p.hasPermission("ilprotect.config")){
                    return true;
                }
                List<Integer> nolist = plugin.getConfig().getIntegerList("clear.noclear");
                nolist.clear();
                plugin.getConfig().set("clear.noclear",nolist);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix+plugin.oC+"除外ワールドリストをクリアしました。/ilp reloadで反映されます。");
                return true;
            }else if(args[0].equalsIgnoreCase("help")) {
                if (!p.hasPermission("ilprotect.forceclear")) {
                    return true;
                }
                p.sendMessage(plugin.prefix+"§e§l====コンフィグ関連ヘルプ====");
                p.sendMessage(plugin.prefix+"§6/ilp reload : 設定変更を反映します。");
                p.sendMessage(plugin.prefix+"§c/ilp freload : SQL設定を含めた設定変更を反映します。");
                p.sendMessage(plugin.prefix+"§6/ilp setpower <true/false> : プラグインを再開/停止します。");
                p.sendMessage(plugin.prefix+"§6/ilp setfee <金額> : 手数料を変更します。");
                p.sendMessage(plugin.prefix+"§6/ilp setmarket <日数> : アイテムの保護期間を日単位で変更します。");
                p.sendMessage(plugin.prefix+"§6/ilp setclearsec <秒数> : アイテム回収間隔を秒単位で変更します。");
                p.sendMessage(plugin.prefix+"§6/ilp addwarnsec <秒数> : アイテム回収警告時間を追加します。");
                p.sendMessage(plugin.prefix+"§6/ilp remwarnsec <秒数> : アイテム回収警告時間を除外します。");
                p.sendMessage(plugin.prefix+"§6/ilp clearwarn : アイテム回収警告時間リストをクリアします。");
                p.sendMessage(plugin.prefix+"§6/ilp addnoclear <ワールド名> : アイテム回収除外ワールドを追加します。");
                p.sendMessage(plugin.prefix+"§6/ilp remnoclear <ワールド名> : アイテム回収除外ワールドを追加します。");
                p.sendMessage(plugin.prefix+"§6/ilp clearworlds : 回収除外ワールドリストをクリアします。");
                p.sendMessage(plugin.prefix+"§e§l=========================");
                return true;
            }
        }else if(args.length==2){
            if(args[0].equalsIgnoreCase("setfee")) {
                if (!p.hasPermission("ilprotect.config")) {
                    return true;
                }
                int fee = 100;
                try {
                    fee = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    p.sendMessage(plugin.prefix + plugin.wC + "数字で入力してください。");
                    return true;
                }
                plugin.getConfig().set("fee", fee);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix + plugin.oC + "手数料を上書きしました。/ilp reloadで反映されます。");
                return true;
            }else if(args[0].equalsIgnoreCase("setpower")){
                if(!p.hasPermission("ilprotect.config")){
                    return true;
                }
                if(args[1].equalsIgnoreCase("true")){
                    plugin.getConfig().set("power",true);
                }else if(args[1].equalsIgnoreCase("false")){
                    plugin.getConfig().set("power",false);
                }else{
                    p.sendMessage(plugin.prefix + plugin.wC + "true/falseのどちらかを入力してください。");
                    return true;
                }
                plugin.saveConfig();
                p.sendMessage(plugin.prefix+plugin.oC+"起動/停止を上書きしました。/ilp reloadで反映されます。");
                return true;
            }else if(args[0].equalsIgnoreCase("setmarket")){
                if(!p.hasPermission("ilprotect.config")){
                    return true;
                }
                int md = 3;
                try{
                    md = Integer.parseInt(args[1]);
                }catch (NumberFormatException e){
                    p.sendMessage(plugin.prefix+plugin.wC+"数字で入力してください。");
                    return true;
                }
                plugin.getConfig().set("marketday",md);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix+plugin.oC+"保護期間を上書きしました。/ilp reloadで反映されます。");
                return true;
            }else if(args[0].equalsIgnoreCase("setclearsec")){
                if(!p.hasPermission("ilprotect.config")){
                    return true;
                }
                int cs = 300;
                try{
                    cs = Integer.parseInt(args[1]);
                }catch (NumberFormatException e){
                    p.sendMessage(plugin.prefix+plugin.wC+"数字で入力してください。");
                    return true;
                }
                plugin.getConfig().set("clear.sec",cs);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix+plugin.oC+"アイテム回収間隔を上書きしました。/ilp reloadで反映されます。");
                return true;
            }else if(args[0].equalsIgnoreCase("addwarnsec")){
                if(!p.hasPermission("ilprotect.config")){
                    return true;
                }
                int ws = 300;
                try{
                    ws = Integer.parseInt(args[1]);
                }catch (NumberFormatException e){
                    p.sendMessage(plugin.prefix+plugin.wC+"数字で入力してください。");
                    return true;
                }
                List<Integer> warnlist = plugin.getConfig().getIntegerList("clear.warn");
                if(warnlist.contains(ws)){
                    p.sendMessage(plugin.prefix+plugin.wC+"既にこの数字は存在しています。");
                    return true;
                }
                warnlist.add(ws);
                plugin.getConfig().set("clear.warn",ws);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix+plugin.oC+"アイテム回収警告時間を追加しました。/ilp reloadで反映されます。");
                return true;
            }else if(args[0].equalsIgnoreCase("remwarnsec")){
                if(!p.hasPermission("ilprotect.config")){
                    return true;
                }
                int ws = 300;
                try{
                    ws = Integer.parseInt(args[1]);
                }catch (NumberFormatException e){
                    p.sendMessage(plugin.prefix+plugin.wC+"数字で入力してください。");
                    return true;
                }
                List<Integer> warnlist = plugin.getConfig().getIntegerList("clear.warn");
                if(!warnlist.contains(ws)){
                    p.sendMessage(plugin.prefix+plugin.wC+"この数字は存在していません。");
                    return true;
                }
                warnlist.remove(ws);
                plugin.getConfig().set("clear.warn",ws);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix+plugin.oC+"アイテム回収警告時間を除外しました。/ilp reloadで反映されます。");
                return true;
            }else if(args[0].equalsIgnoreCase("addnoclear")){
                if(!p.hasPermission("ilprotect.config")){
                    return true;
                }
                List<String> nolist = plugin.getConfig().getStringList("clear.noclear");
                if(nolist.contains(args[1])){
                    p.sendMessage(plugin.prefix+plugin.wC+"このワールドは既に追加されています。");
                    return true;
                }
                nolist.add(args[1]);
                plugin.getConfig().set("clear.noclear",nolist);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix+plugin.oC+"除外ワールドを追加しました。/ilp reloadで反映されます。");
                return true;
            }else if(args[0].equalsIgnoreCase("remnoclear")){
                if(!p.hasPermission("ilprotect.config")){
                    return true;
                }
                List<String> nolist = plugin.getConfig().getStringList("clear.noclear");
                if(!nolist.contains(args[1])){
                    p.sendMessage(plugin.prefix+plugin.wC+"このワールドはリストに存在しません。");
                    return true;
                }
                nolist.remove(args[1]);
                plugin.getConfig().set("clear.noclear",nolist);
                plugin.saveConfig();
                p.sendMessage(plugin.prefix+plugin.oC+"除外ワールドリストから除外しました。/ilp reloadで反映されます。");
                return true;
            }
        }
        return true;
    }

    public void sendLostItemMessage(Player p, ItemLostProtection.LostedItem litem){
        p.sendMessage(plugin.prefix+"§6[§a"+litem.id+"§6]§a"+
                ItemLostProtection.textToDate(litem.date)+" §e§l"+ItemLostProtection.itemToName(litem.item));
    }

    public void openGUI(Player p, InventoryAPI invs , String invname,String[] args){
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()-> {
            InventoryAPI inv = invs;
            if (invname.equalsIgnoreCase("main")) {
                inv = KInventory.get3_9Inv(p, inv, "§3§lアイテム収集局 §6§lメインページ");
                inv.setItem(11, inv.createUnbitem("§a§l自分の落とし物を確認する", new String[]{"§e落としてしまったアイテムの確認や回収はこちらから"},
                        Material.BUCKET, 0, false));
                inv.addOriginalListing(new InvListener(plugin, inv) {
                    @EventHandler
                    public void onClick(InventoryClickEvent e) {
                        if (!super.ClickCheck(e)) {
                            return;
                        }
                        if (e.getSlot() != 11) {
                            return;
                        }
                        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                        openGUI(p, inv, "mylostitem", new String[]{0 + ""});
                    }

                    @EventHandler
                    public void onClose(InventoryCloseEvent e) {
                        super.closeCheck(e);
                    }
                });
                inv.setItem(15, inv.createUnbitem("§a§l落とし物マーケット", new String[]{"§e保護期限切れのアイテムを安値で買うことができます！"},
                        Material.EMERALD, 0, false));
                inv.addOriginalListing(new InvListener(plugin, inv) {
                    @EventHandler
                    public void onClick(InventoryClickEvent e) {
                        if (!super.ClickCheck(e)) {
                            return;
                        }
                        if (e.getSlot() != 15) {
                            return;
                        }
                        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                        openGUI(p, inv, "freemarket", new String[]{0 + ""});
                    }

                    @EventHandler
                    public void onClose(InventoryCloseEvent e) {
                        super.closeCheck(e);
                    }
                });
                InventoryAPI finalInv = inv;
                Bukkit.getScheduler().runTask(plugin,()->{
                    finalInv.openInv(p);
                });
            } else if (invname.equalsIgnoreCase("mylostitem")) {
                int page = Integer.parseInt(args[0]);

                inv = KInventory.getListupInv(p, inv, "§3§lアイテム収集局 §9Page:" + page);
                inv.addOriginalListing(new InvListener(plugin, inv) {
                    @EventHandler
                    public void onClick(InventoryClickEvent e) {
                        if (!super.ClickCheck(e)) {
                            return;
                        }
                        if (e.getSlot() != 49) {
                            return;
                        }
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                        openGUI(p, inv, "main", new String[]{});
                    }

                    @EventHandler
                    public void onClose(InventoryCloseEvent e) {
                        super.closeCheck(e);
                    }
                });
                inv.addOriginalListing(new InvListener(plugin, inv) {
                    @EventHandler
                    public void onClick(InventoryClickEvent e) {
                        if (!super.ClickCheck(e)) {
                            return;
                        }
                        if (e.getSlot() == 45 || e.getSlot() == 46) {
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                            if (page == 0) {
                                return;
                            }
                            openGUI(p, inv, "mylostitem", new String[]{"" + (page - 1)});
                        } else if (e.getSlot() == 52 || e.getSlot() == 53) {
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                            openGUI(p, inv, "mylostitem", new String[]{"" + (page + 1)});
                        }
                    }

                    @EventHandler
                    public void onClose(InventoryCloseEvent e) {
                        super.closeCheck(e);
                    }
                });
                int ii = 0;
                for (ItemLostProtection.LostedItem litem : plugin.getLostedItemsLimit(p.getUniqueId(), 45, (page * 45))) {
                    if (ii == 45) {
                        break;
                    }
                    ItemStack it = inv.createUnbitem(ItemLostProtection.itemToName(litem.item), new String[]{"§eクリックで確認画面へ移行します！"},
                            litem.item.getType(), 0, false);
                    it.setAmount(litem.item.getAmount());
                    inv.setItem(ii, it);
                    int finalIi = ii;
                    inv.addOriginalListing(new InvListener(plugin, inv) {
                        @EventHandler
                        public void onClick(InventoryClickEvent e) {
                            if (!super.ClickCheck(e)) {
                                return;
                            }
                            if (e.getSlot() != finalIi) {
                                return;
                            }
                            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.9f);
                            if (plugin.getLostedItem(litem.id)==null) {
                                p.sendMessage(plugin.prefix + plugin.wC + "このアイテムは既に受け取られたようです。");
                                openGUI(p, inv, "mylostitem", new String[]{"" + (page)});
                                return;
                            }
                            openGUI(p, inv, "reciveCheck", new String[]{litem.id + "", "mylost", page + ""});
                        }

                        @EventHandler
                        public void onClose(InventoryCloseEvent e) {
                            super.closeCheck(e);
                        }
                    });
                    ii++;
                }
                InventoryAPI finalInv = inv;
                Bukkit.getScheduler().runTask(plugin,()->{
                    finalInv.openInv(p);
                });
            } else if (invname.equalsIgnoreCase("freemarket")) {
                int page = Integer.parseInt(args[0]);

                inv = KInventory.getListupInv(p, inv, "§3§lアイテム収集局 §6§lフリマ §9Page:" + page);
                inv.addOriginalListing(new InvListener(plugin, inv) {
                    @EventHandler
                    public void onClick(InventoryClickEvent e) {
                        if (!super.ClickCheck(e)) {
                            return;
                        }
                        if (e.getSlot() != 49) {
                            return;
                        }
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                        openGUI(p, inv, "main", new String[]{});
                    }

                    @EventHandler
                    public void onClose(InventoryCloseEvent e) {
                        super.closeCheck(e);
                    }
                });
                inv.addOriginalListing(new InvListener(plugin, inv) {
                    @EventHandler
                    public void onClick(InventoryClickEvent e) {
                        if (!super.ClickCheck(e)) {
                            return;
                        }
                        if (e.getSlot() == 45 || e.getSlot() == 46) {
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                            if (page == 0) {
                                return;
                            }
                            openGUI(p, inv, "freemarket", new String[]{"" + (page - 1)});
                        } else if (e.getSlot() == 52 || e.getSlot() == 53) {
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                            openGUI(p, inv, "freemarket", new String[]{"" + (page + 1)});
                        }
                    }

                    @EventHandler
                    public void onClose(InventoryCloseEvent e) {
                        super.closeCheck(e);
                    }
                });
                int ii = 0;
                for (ItemLostProtection.LostedItem litem : plugin.getAllMarketOKItemsLimit(45, (page * 45))) {
                    if (ii == 45) {
                        break;
                    }
                    ItemStack it = inv.createUnbitem(ItemLostProtection.itemToName(litem.item), new String[]{"§eクリックで確認画面へ移行します！"},
                            litem.item.getType(), 0, false);
                    it.setAmount(litem.item.getAmount());
                    inv.setItem(ii, it);
                    int finalIi = ii;
                    inv.addOriginalListing(new InvListener(plugin, inv) {
                        @EventHandler
                        public void onClick(InventoryClickEvent e) {
                            if (!super.ClickCheck(e)) {
                                return;
                            }
                            if (e.getSlot() != finalIi) {
                                return;
                            }
                            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.9f);
                            if (plugin.getLostedItem(litem.id)==null) {
                                p.sendMessage(plugin.prefix + plugin.wC + "このアイテムは既に受け取られたようです。");
                                openGUI(p, inv, "freemarket", new String[]{"" + (page)});
                                return;
                            }
                            openGUI(p, inv, "reciveCheck", new String[]{litem.id + "", "market", page + ""});
                        }

                        @EventHandler
                        public void onClose(InventoryCloseEvent e) {
                            super.closeCheck(e);
                        }
                    });
                    ii++;
                }

                InventoryAPI finalInv = inv;
                Bukkit.getScheduler().runTask(plugin,()->{
                    finalInv.openInv(p);
                });
            } else if (invname.equalsIgnoreCase("reciveCheck")) {
                inv = KInventory.get3_9Inv(p, inv, "§5§l受け取り確認画面");
                int id = Integer.parseInt(args[0]);
                String type = args[1];
                int page = Integer.parseInt(args[2]);
                ItemLostProtection.LostedItem litem = plugin.getLostedItem(id);

                if (litem == null) {
                    p.sendMessage(plugin.prefix + plugin.wC + "このアイテムは既に受け取られたようです。");
                    Bukkit.getScheduler().runTask(plugin, (Runnable) p::closeInventory);
                    return;
                }

                if (!litem.droper.toString().equals(p.getUniqueId().toString()) && !plugin.checkLostItemIsMarketIn(litem)) {
                    p.sendMessage(plugin.prefix + plugin.wC + "このアイテムは保護期間中です！");
                    Bukkit.getScheduler().runTask(plugin, (Runnable) p::closeInventory);
                    return;
                }

                ItemStack wall = inv.createUnbitem(" ", new String[]{}, Material.BLACK_STAINED_GLASS_PANE, 0, false);
                ItemStack ok = inv.createUnbitem("§r" + ItemLostProtection.itemToName(litem.item) + "§a§lを受け取る", new String[]{}, Material.GREEN_STAINED_GLASS_PANE, 0, false);
                ItemStack cancel = inv.createUnbitem("§4§lキャンセル", new String[]{}, Material.RED_STAINED_GLASS_PANE, 0, false);
                inv.setItems(new int[]{0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21}, ok);
                inv.setItems(new int[]{4, 22}, wall);
                inv.setItem(13, litem.item);
                inv.setItems(new int[]{5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26}, cancel);

                if (type.equalsIgnoreCase("market")) {
                    inv.addOriginalListing(new InvListener(plugin, inv) {
                        @EventHandler
                        public void onClick(InventoryClickEvent e) {
                            if (!super.ClickCheck(e)) {
                                return;
                            }
                            if (!((e.getSlot() >= 0 && e.getSlot() <= 3) || (e.getSlot() >= 9 && e.getSlot() <= 12) || (e.getSlot() >= 18 && e.getSlot() <= 21))) {
                                return;
                            }
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                            if (!plugin.paymentFee(p)) {
                                openGUI(p, inv, "freemarket", new String[]{page + ""});
                                return;
                            }
                            plugin.reciveLostItem(id, p);
                            p.sendMessage(plugin.prefix + "§e" + ItemLostProtection.itemToName(litem.item) + plugin.oC + "を受け取りました。");
                            openGUI(p, inv, "main", new String[]{});
                        }

                        @EventHandler
                        public void onClose(InventoryCloseEvent e) {
                            super.closeCheck(e);
                        }
                    });
                    inv.addOriginalListing(new InvListener(plugin, inv) {
                        @EventHandler
                        public void onClick(InventoryClickEvent e) {
                            if (!super.ClickCheck(e)) {
                                return;
                            }
                            if (!((e.getSlot() >= 5 && e.getSlot() <= 8) || (e.getSlot() >= 14 && e.getSlot() <= 17) || (e.getSlot() >= 23 && e.getSlot() <= 26))) {
                                return;
                            }
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                            openGUI(p, inv, "freemarket", new String[]{page + ""});
                        }

                        @EventHandler
                        public void onClose(InventoryCloseEvent e) {
                            super.closeCheck(e);
                        }
                    });
                } else if (type.equalsIgnoreCase("mylost")) {
                    inv.addOriginalListing(new InvListener(plugin, inv) {
                        @EventHandler
                        public void onClick(InventoryClickEvent e) {
                            if (!super.ClickCheck(e)) {
                                return;
                            }
                            if (!((e.getSlot() >= 0 && e.getSlot() <= 3) || (e.getSlot() >= 9 && e.getSlot() <= 12) || (e.getSlot() >= 18 && e.getSlot() <= 21))) {
                                return;
                            }
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                            if (!plugin.paymentFee(p)) {
                                openGUI(p, inv, "mylostitem", new String[]{page + ""});
                                return;
                            }
                            plugin.reciveLostItem(id, p);
                            p.sendMessage(plugin.prefix + "§e" + ItemLostProtection.itemToName(litem.item) + plugin.oC + "を受け取りました。");
                            openGUI(p, inv, "main", new String[]{});
                        }

                        @EventHandler
                        public void onClose(InventoryCloseEvent e) {
                            super.closeCheck(e);
                        }
                    });
                    inv.addOriginalListing(new InvListener(plugin, inv) {
                        @EventHandler
                        public void onClick(InventoryClickEvent e) {
                            if (!super.ClickCheck(e)) {
                                return;
                            }
                            if (!((e.getSlot() >= 5 && e.getSlot() <= 8) || (e.getSlot() >= 14 && e.getSlot() <= 17) || (e.getSlot() >= 23 && e.getSlot() <= 26))) {
                                return;
                            }
                            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                            openGUI(p, inv, "mylostitem", new String[]{page + ""});
                        }

                        @EventHandler
                        public void onClose(InventoryCloseEvent e) {
                            super.closeCheck(e);
                        }
                    });
                }

                InventoryAPI finalInv = inv;
                Bukkit.getScheduler().runTask(plugin,()->{
                    finalInv.openInv(p);
                });
            }
        });
    }
}
