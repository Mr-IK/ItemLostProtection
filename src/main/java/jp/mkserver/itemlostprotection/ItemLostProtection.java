package jp.mkserver.itemlostprotection;

import jp.mkserver.itemlostprotection.inv.KInventory;
import jp.mkserver.itemlostprotection.sql.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class ItemLostProtection extends JavaPlugin {

    final String prefix = "§b§l[§7ILP§b§l]§r";
    final String wC= "§c§l";
    final String eC = "§4§l";
    final String oC = "§a§l";


    private SQLManager sql;
    private ILPCommand com;
    private ILPListener lis;
    private VaultAPI vault;
    private FileConfiguration config;

    protected boolean power = true;
    private int fee = 100;
    private int clearsec = 300;
    private int marketday = 3;
    private List<Integer> warnlist;
    private List<String> noClearlist;

    private int countsec = 0;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = getConfig();
        power = config.getBoolean("power",true);
        fee = config.getInt("fee",100);
        clearsec = config.getInt("clear.sec",300);
        marketday = config.getInt("marketday",3);
        warnlist = new ArrayList<>();
        noClearlist = new ArrayList<>();
        if(clearsec>0){
            if(config.contains("clear.warn")){
                warnlist = config.getIntegerList("clear.warn");
            }
            if(config.contains("clear.noclear")){
                noClearlist = config.getStringList("clear.noclear");
            }
            countsec = clearsec;
            Bukkit.getScheduler().runTaskTimerAsynchronously(this,()->{
                if(clearsec<=0||!power){
                    return;
                }

                if(countsec==0){
                    clearAllItem();
                    countsec = clearsec;
                    return;
                }

                countsec--;
                if(warnlist.contains(countsec)){
                    Bukkit.broadcastMessage(prefix+"§c注意: 残り§e"+minIntToString(countsec)+"§cで落ちているアイテムが回収されます！");
                }
            },0,20);
        }

        sql = new SQLManager(this,"ILProtection");
        com = new ILPCommand(this);
        lis = new ILPListener(this);
        vault = new VaultAPI(this);

        KInventory.init(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void putLostedItem(ItemStack item, UUID droper){
        if(droper==null||item==null){
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(this,()->{
            sql.execute("INSERT INTO drop_items (uuid,item)  VALUES ('"+droper.toString()+"','"+itemToBase64(item)+"');");
        });
    }

    public String minIntToString(int sec){
        int minute = sec/60;
        int nsec = sec%60;
        if(nsec==0){
            return minute+"分";
        }else if(minute==0){
            return nsec+"秒";
        }
        return minute+"分"+nsec+"秒";
    }

    public void clearAllItem(){
        Bukkit.getScheduler().runTaskAsynchronously(this,()->{
            int cleared = 0;
            for(World world : Bukkit.getWorlds()){
                if(noClearlist.contains(world.getName())){
                    continue;
                }
                List<Entity> entList = world.getEntities();
                for(Entity current : entList) {
                    if (current instanceof Item) {
                        Item i = (Item)current;
                        UUID thrower = i.getThrower();
                        if(thrower!=null){
                            putLostedItem(i.getItemStack(),thrower);
                        }
                        current.remove();
                        cleared++;
                    }
                }
            }
            Bukkit.broadcastMessage(prefix+"§7お知らせ: §e"+cleared+"§7個のドロップアイテムを回収しました。");
        });
    }

    public void reload(){
        reloadConfig();
        config = getConfig();
        fee = config.getInt("fee",100);
        clearsec = config.getInt("clear.sec",300);
        marketday = config.getInt("marketday",3);
        if(config.contains("clear.warn")){
            warnlist = config.getIntegerList("clear.warn");
        }
        if(config.contains("clear.noclear")){
            noClearlist = config.getStringList("clear.noclear");
        }
        countsec = clearsec;
    }

    public void fReload(){
        reload();
        sql.init();
    }

    public boolean checkHasfee(Player p){
        double bal = vault.getBalance(p.getUniqueId());
        if(bal<fee){
            p.sendMessage(prefix+wC+"手数料§e$"+fee+wC+"を支払えません！");
            return false;
        }
        return true;
    }

    public boolean paymentFee(Player p){
        if(!checkHasfee(p)){
            return false;
        }
        vault.withdraw(p.getUniqueId(),fee);
        return true;
    }

    public void reciveLostItem(int id, Player p){
        Bukkit.getScheduler().runTaskAsynchronously(this,()->{
            LostedItem litem = getLostedItem(id);
            if(litem==null){
                p.sendMessage(prefix+wC+"ID:"+id+" の回収アイテムは存在しません。");
                return;
            }

            if(p.getInventory().firstEmpty()==-1){
                p.sendMessage(prefix+wC+"インベントリに空きがないためアイテム投入を中止しました");
                return;
            }

            sql.execute("DELETE FROM drop_items WHERE id = "+id+";");

            p.getInventory().addItem(litem.item);
        });
    }

    public LostedItem getLostedItem(int id){
        String exe = "SELECT * FROM drop_items WHERE id = " + id + ";";
        LostedItem litem = new LostedItem();
        SQLManager.Query qu = sql.query(exe);
        ResultSet rs = qu.getRs();
        if (rs != null) {
            try {
                if (rs.next()) {
                    ItemStack item = itemFromBase64(rs.getString("item"));
                    Date date = rs.getTimestamp("time");
                    UUID droper = UUID.fromString(rs.getString("uuid"));
                    litem.id = id;
                    litem.item = item;
                    litem.date = date;
                    litem.droper = droper;
                }
                qu.close();
                return litem;
            } catch (SQLException e) {
                e.printStackTrace();
                qu.close();
                return null;
            }
        }
        return null;
    }

    public List<LostedItem> getLostedItems(UUID droper){
        String exe = "SELECT * FROM drop_items WHERE uuid = '" + droper.toString() + "';";
        List<LostedItem> list = new ArrayList<>();
        SQLManager.Query qu = sql.query(exe);
        ResultSet rs = qu.getRs();
        if (rs != null) {
            try {
                while (rs.next()) {
                    ItemStack item = itemFromBase64(rs.getString("item"));
                    int id = rs.getInt("id");
                    Date date = rs.getTimestamp("time");
                    LostedItem litem = new LostedItem();
                    litem.id = id;
                    litem.item = item;
                    litem.date = date;
                    litem.droper = droper;
                    list.add(litem);
                }
                qu.close();
            } catch (SQLException e) {
                e.printStackTrace();
                qu.close();
                return list;
            }
        }
        return list;
    }

    public List<LostedItem> getLostedItemsLimit(UUID droper,int limit,int offset){
        String exe = "SELECT * FROM drop_items WHERE uuid = '" + droper.toString() + "' ORDER BY id LIMIT "+limit+" OFFSET "+offset+";";
        List<LostedItem> list = new ArrayList<>();
        SQLManager.Query qu = sql.query(exe);
        ResultSet rs = qu.getRs();
        if (rs != null) {
            try {
                while (rs.next()) {
                    ItemStack item = itemFromBase64(rs.getString("item"));
                    int id = rs.getInt("id");
                    Date date = rs.getTimestamp("time");
                    LostedItem litem = new LostedItem();
                    litem.id = id;
                    litem.item = item;
                    litem.date = date;
                    litem.droper = droper;
                    list.add(litem);
                }
                qu.close();
            } catch (SQLException e) {
                e.printStackTrace();
                qu.close();
                return list;
            }
        }
        return list;
    }

    public List<LostedItem> getAllLostedItems(){
        String exe = "SELECT * FROM drop_items ;";
        List<LostedItem> list = new ArrayList<>();
        SQLManager.Query qu = sql.query(exe);
        ResultSet rs = qu.getRs();
        if (rs != null) {
            try {
                while (rs.next()) {
                    ItemStack item = itemFromBase64(rs.getString("item"));
                    int id = rs.getInt("id");
                    Date date = rs.getTimestamp("time");
                    UUID droper = UUID.fromString(rs.getString("uuid"));
                    LostedItem litem = new LostedItem();
                    litem.id = id;
                    litem.item = item;
                    litem.date = date;
                    litem.droper = droper;
                    list.add(litem);
                }
                qu.close();
            } catch (SQLException e) {
                e.printStackTrace();
                qu.close();
                return list;
            }
        }
        return list;
    }

    public List<LostedItem> getAllLostedItemsLimit(int limit,int offset){
        String exe = "SELECT * FROM drop_items LIMIT "+limit+" OFFSET "+offset+" ;";
        List<LostedItem> list = new ArrayList<>();
        SQLManager.Query qu = sql.query(exe);
        ResultSet rs = qu.getRs();
        if (rs != null) {
            try {
                while (rs.next()) {
                    ItemStack item = itemFromBase64(rs.getString("item"));
                    int id = rs.getInt("id");
                    Date date = rs.getTimestamp("time");
                    UUID droper = UUID.fromString(rs.getString("uuid"));
                    LostedItem litem = new LostedItem();
                    litem.id = id;
                    litem.item = item;
                    litem.date = date;
                    litem.droper = droper;
                    list.add(litem);
                }
                qu.close();
            } catch (SQLException e) {
                e.printStackTrace();
                qu.close();
                return list;
            }
        }
        return list;
    }

    public List<LostedItem> getAllMarketOKItemsLimit(int limit,int offset){
        String exe = "SELECT * FROM drop_items WHERE time < now() - interval "+marketday+" day ORDER BY id LIMIT "+limit+" OFFSET "+offset+" ;";
        List<LostedItem> list = new ArrayList<>();
        SQLManager.Query qu = sql.query(exe);
        ResultSet rs = qu.getRs();
        if (rs != null) {
            try {
                while (rs.next()) {
                    ItemStack item = itemFromBase64(rs.getString("item"));
                    int id = rs.getInt("id");
                    Date date = rs.getTimestamp("time");
                    UUID droper = UUID.fromString(rs.getString("uuid"));
                    LostedItem litem = new LostedItem();
                    litem.id = id;
                    litem.item = item;
                    litem.date = date;
                    litem.droper = droper;
                    list.add(litem);
                }
                qu.close();
            } catch (SQLException e) {
                e.printStackTrace();
                qu.close();
                return list;
            }
        }
        return list;
    }

    public boolean checkLostItemIsMarketIn(LostedItem item){
        if(item==null){
            return false;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(item.date);
        cal.add(Calendar.DAY_OF_MONTH,marketday);
        return new Date().before(cal.getTime());
    }

    public static ItemStack itemFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items[0];
        } catch (Exception e) {
            return null;
        }
    }

    public static String itemToBase64(ItemStack item) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            ItemStack[] items = new ItemStack[1];
            items[0] = item;
            dataOutput.writeInt(items.length);

            for (ItemStack itemStack : items) {
                dataOutput.writeObject(itemStack);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static String textToDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH時mm分ss秒");
        return sdf.format(date);
    }

    public static String itemToName(ItemStack item){
        if(item.hasItemMeta()&&item.getItemMeta().hasDisplayName()){
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name();
    }

    class LostedItem{
        ItemStack item;
        int id;
        UUID droper;
        Date date;
    }
}
