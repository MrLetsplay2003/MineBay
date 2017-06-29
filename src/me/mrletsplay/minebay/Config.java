package me.mrletsplay.minebay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import net.md_5.bungee.api.ChatColor;

public class Config {
	
	public static FileConfiguration Config = Main.pl.getConfig();
	
	public static void save(){
		try{
//			Config.save(ConfigFile);
			Main.pl.saveConfig();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void init(){
		Config.addDefault("minebay.prefix", "&8[&6Mine&bBay&8]");
		Config.addDefault("minebay.mbstring", "&6Mine&bBay");
		Config.addDefault("minebay.info.purchase.success", "%prefix% &aYou successfully bought &6%amount% %type% &afrom &6%seller% &afor &6%price% %currency%");
		Config.addDefault("minebay.info.purchase.error", "&cError: %error%");
		Config.addDefault("minebay.info.purchase.seller.success", "%prefix% &6%buyer% &ahas bought &6%amount% %type% &afor &6%price% %currency% &afrom you on %mbstring% &7(-%roomtax%% tax => You get %price2% %currency%)");
		Config.addDefault("minebay.info.purchase.room-owner.success", "%prefix% &6%buyer% &ahas bought &6%amount% %type% &afor &6%price% %currency% &ain your room on %mbstring% &7(-%roomtax%% tax => You get %price2% %currency%)");
		Config.addDefault("minebay.info.sell.success", "%prefix% &aSuccessfully put &6%amount% %type% &afor &6%price% %currency% &afor sale on %mbstring%");
		Config.addDefault("minebay.info.sell.type-in-price", "%prefix% &aType in the price for the item");
		Config.addDefault("minebay.info.sell.action-cancelled", "%prefix% &cOld sell action cancelled!");
		Config.addDefault("minebay.info.sell.error.invalid-price", "%prefix% &aType in another price");
		Config.addDefault("minebay.info.sell.error.noitem", "%prefix% &cYou need to hold an item in your hand");
		Config.addDefault("minebay.info.sell.error.toocheap", "%prefix% &cYou need to set a price higher than 0");
		Config.addDefault("minebay.info.sell.error.no-slots", "%prefix% &cAll slots are already occupied");
		Config.addDefault("minebay.info.sell.error.too-many-sold", "%prefix% &cYou have already sold too many items in that room");
		Config.addDefault("minebay.info.newname", "%prefix% &aType in a new name (Max. %maxchars% Characters)");
		Config.addDefault("minebay.info.newname-cancelled", "%prefix% &c Old rename action cancelled!");
		Config.addDefault("minebay.info.newname-applied", "%prefix% &aName changed to: %newname%");
		Config.addDefault("minebay.info.error.name-too-long", "%prefix% &cMaximum name length: %maxchars%");
		Config.addDefault("minebay.info.newicon-applied", "%prefix% &aRoom icon changed to: %type%");
		Config.addDefault("minebay.info.buy-icon.success", "%prefix% &aBought icon for %price% %currency%, room icon changed to: %type%");
		Config.addDefault("minebay.info.buy-icon.error", "%prefix% &cError: %error%");
		Config.addDefault("minebay.info.room-created", "%prefix% &aRoom &6\"%name%\" &acreated! &7(Properties: Tax: %taxshare%%, Slots: %slots%, Icon Material: %iconmaterial%, ID: %roomid%)");
		Config.addDefault("minebay.info.room-create.error.too-many-rooms", "%prefix% &cYou have already reached the room limit!");
		Config.addDefault("minebay.info.room-create.error.general", "%prefix% &cError: %error%");
		Config.addDefault("minebay.info.slot-buy.success", "%prefix% &aBought %slotamount% slot/s for %price% %currency%");
		Config.addDefault("minebay.info.slot-buy.error", "%prefix% &cError: %error%");
		Config.addDefault("minebay.info.slot-buy.toomanyslots", "%prefix% &cYou already have reached the maximum amount of slots");
		Config.addDefault("minebay.info.slot-sell.success", "%prefix% &aSold %slotamount% slot/s for %price% %currency%");
		Config.addDefault("minebay.info.slot-sell.not-allowed", "%prefix% &cSlot selling is not allowed");
		Config.addDefault("minebay.info.slot-sell.all-slots-occupied", "%prefix% &cAll slots are currently occupied");
		Config.addDefault("minebay.info.slot-sell.error", "%prefix% &cError: %error%");
		Config.addDefault("minebay.info.slot-sell.notenoughslots", "%prefix% &cYou already have reached the minimum amount of slots");
		Config.addDefault("minebay.info.tax.success", "%prefix% &aChanged the tax to %newtax%%");
		Config.addDefault("minebay.info.tax.toohigh", "%prefix% &cYou already have reached the maximum tax");
		Config.addDefault("minebay.info.tax.toolow", "%prefix% &cYou can't set the tax below 0%");
		Config.addDefault("minebay.info.sell-room.success", "%prefix% &aSuccessfully sold your room for %price% %currency%");
		Config.addDefault("minebay.info.sell-room.not-allowed", "%prefix% &cRoom selling is not allowed");
		Config.addDefault("minebay.info.sell-room.not-empty", "%prefix% &cThere are still offers in your room");
		Config.addDefault("minebay.info.sell-room.error", "%prefix% &cError: %error%");
		Config.addDefault("minebay.info.retract-sale.success", "%prefix% &aSuccessfully retracted your sale");
		Config.addDefault("minebay.info.user-rooms-disabled", "%prefix% &cUser rooms are disabled!");
		Config.addDefault("minebay.info.reload-complete", "%prefix% &aReload complete");
		Config.addDefault("minebay.info.reload-no-permission", "%prefix% &cNo permission");
		Config.addDefault("minebay.general.allow-drag-and-drop", true);
		Config.addDefault("minebay.general.enable-user-rooms", true);
		Config.addDefault("minebay.general.max-type-time-seconds", -1);
		Config.addDefault("minebay.general.user-rooms-settings.tax-notify", true);
		Config.addDefault("minebay.general.user-rooms-settings.slot-notify", true);
		Config.addDefault("minebay.general.allow-slot-selling", true);
		Config.addDefault("minebay.general.allow-room-selling", true);
		Config.addDefault("minebay.general.allow-room-creation", true);
		Config.addDefault("minebay.general.enable-update-check", true);
		Config.addDefault("minebay.general.update-check-on-join", true);
		Config.addDefault("minebay.general.update-check-on-command", true);
		Config.addDefault("minebay.default-auction-room.slots", -1);
		Config.addDefault("minebay.default-auction-room.taxshare", 5);
		Config.addDefault("minebay.default-auction-room.name", "Default Auction Room");
		Config.addDefault("minebay.default-auction-room.icon-material", "DIAMOND_BLOCK");
		Config.addDefault("minebay.default-auction-room.icon-material-damage", 0);
		Config.addDefault("minebay.default-auction-room.applySettings", false);
		Config.addDefault("minebay.user-rooms.room-price", 1000);
		Config.addDefault("minebay.user-rooms.room-sell-price", 900);
		Config.addDefault("minebay.user-rooms.slot-price", 100);
		Config.addDefault("minebay.user-rooms.slot-sell-price", 90);
		Config.addDefault("minebay.user-rooms.default-tax-percent", 5);
		Config.addDefault("minebay.user-rooms.max-tax-percent", 50);
		Config.addDefault("minebay.user-rooms.default-slot-number", 5);
		Config.addDefault("minebay.user-rooms.max-slots", 50);
		Config.addDefault("minebay.user-rooms.offers-per-slot", 5);
		Config.addDefault("minebay.user-rooms.max-name-length", 20);
		Config.addDefault("minebay.user-rooms.max-rooms", 3);
		Config.addDefault("minebay.user-rooms.default-icon-material", "GRASS");
		Config.addDefault("minebay.user-rooms.custom-icon-price", 100);
		List<String> perms = new ArrayList<>();
		perms.add("user.premium");
		perms.add("user.donator");
		Config.addDefault("room-perms", perms);
		Config.addDefault("room-perm.user.premium.max-rooms", 5);
		Config.addDefault("room-perm.user.premium.allow-colored-names", false);
		Config.addDefault("room-perm.user.donator.max-rooms", 7);
		Config.addDefault("room-perm.user.donator.allow-colored-names", true);
		Config.options().copyDefaults(true);
		save();
		/*try {
			loadConfigFile();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	//TODO:Will be added someday in the future
	@SuppressWarnings("unused")
	private static void loadConfigFile() throws IOException{
		System.out.println("LOAD!");
		URL u = Main.class.getResource("/res/default-config.yml");
		System.out.println(u.getFile());
		InputStream in = new FileInputStream(new File(u.getFile()));
		//File dFl = new File(Main.class.getClassLoader().getResource("./res/default-config.yml").getFile());
		//System.out.println(dFl.getPath());
		//FileInputStream in = new FileInputStream(dFl);
		File cFl = new File(Main.pl.getDataFolder(), "config.yml");
		if(!cFl.exists()){
			Main.pl.getLogger().info("Generating default config...");
			FileOutputStream out = new FileOutputStream(cFl);
			byte[] buf = new byte[1024];
			int curr;
			while((curr = in.read(buf))>0){
				out.write(buf, 0, curr);
			}
			out.close();
			Main.pl.saveDefaultConfig();
			try {
				Main.pl.getConfig().load(cFl);
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
		in.close();
	}
	
	public static String simpleReplace(String s){
		String currencyName = Main.econ.currencyNamePlural();
		s = ChatColor.translateAlternateColorCodes('&', s
				.replace("%prefix%", Config.getString("minebay.prefix"))
				.replace("%mbstring%", Config.getString("minebay.mbstring")))
				.replace("%maxchars%", ""+Config.getInt("minebay.user-rooms.max-name-length"))
				.replace("%slotprice%", ""+Config.getInt("minebay.user-rooms.slot-price"));
		if(currencyName!=null){
			s = s.replace("%currency%", Main.econ.currencyNamePlural());
		}else{
			s = s.replace("%currency%", "");
		}
		return s;
	}
	
	public static String replaceForSellItem(String s, SellItem it, AuctionRoom r){
		s = ChatColor.translateAlternateColorCodes('&', s
				.replace("%amount%", ""+it.getItem().getAmount())
				.replace("%type%", it.getItem().getType().toString().toLowerCase().replace("_", " "))
				.replace("%seller%", it.getSeller())
				.replace("%price%", ""+it.getPrice()))
				.replace("%roomtax%", ""+r.getTaxshare());
		return s;
	}
	
	public static String replaceForAuctionRoom(String s, AuctionRoom r){
		s = ChatColor.translateAlternateColorCodes('&', s
				.replace("%name%", ""+r.getName())
				.replace("%taxshare%", ""+r.getTaxshare())
				.replace("%slots%", ""+r.getSlots())
				.replace("%roomid%", ""+r.getRoomID())
				.replace("%iconmaterial%", ""+r.getIcon().getType().name().toLowerCase().replace("_", " ")));
		return s;
	}
	
	public static String onlyDigits(String s){
		StringBuilder b = new StringBuilder();
		for(char c : s.toCharArray()){
			if(Character.isDigit(c)){
				b.append(c);
			}
		}
		return b.toString();
	}
	
	public static String onlyDigitsNoColor(String s){
		s = s.replaceAll("§.", "");
		StringBuilder b = new StringBuilder();
		for(char c : s.toCharArray()){
			if(Character.isDigit(c)){
				b.append(c);
			}
		}
		return b.toString();
	}
	
}
