package me.mrletsplay.minebay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import me.mrletsplay.mrcore.config.CustomConfig;
import me.mrletsplay.mrcore.config.CustomConfig.ConfigSaveProperty;
import me.mrletsplay.mrcore.config.CustomConfig.InvalidConfigException;
import net.md_5.bungee.api.ChatColor;

public class Config {
	
	public static CustomConfig config = new CustomConfig(new File(Main.pl.getDataFolder(), "config.yml"), false, ConfigSaveProperty.SORT_ALPHABETICALLY).loadConfigSafely(),
							   messages;
	
	public static boolean use_uuids;
	
	public static String prefix, mbString;
	
	public static void saveConfig(){
		try{
			config.saveConfig();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void saveMessages(){
		try{
			messages.saveConfig();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void init(){
		config.addDefault("minebay.general.allow-drag-and-drop", true);
		config.addDefault("minebay.general.enable-user-rooms", true);
		config.addDefault("minebay.general.max-type-time-seconds", -1);
		config.addDefault("minebay.general.user-rooms-settings.tax-notify", true);
		config.addDefault("minebay.general.user-rooms-settings.slot-notify", true);
		config.addDefault("minebay.general.allow-slot-selling", true);
		config.addDefault("minebay.general.allow-room-selling", true);
		config.addDefault("minebay.general.allow-room-creation", true);
		config.addDefault("minebay.general.enable-update-check", true);
		config.addDefault("minebay.general.update-check-on-join", true);
		config.addDefault("minebay.general.update-check-on-command", true);
		config.addDefault("minebay.general.user-rooms-settings.change-icon-remove-item", true);
		config.addDefault("minebay.general.use-uuids", true);
		List<String> aliases = new ArrayList<>();
		aliases.add("/market");
		aliases.add("/mb");
		config.addDefault("minebay.general.command-aliases", aliases);
		config.addDefault("minebay.user-rooms.room-price", 1000);
		config.addDefault("minebay.user-rooms.room-sell-price", 900);
		config.addDefault("minebay.user-rooms.slot-price", 100);
		config.addDefault("minebay.user-rooms.slot-sell-price", 90);
		config.addDefault("minebay.user-rooms.default-tax-percent", 5);
		config.addDefault("minebay.user-rooms.max-tax-percent", 50);
		config.addDefault("minebay.user-rooms.default-slot-number", 5);
		config.addDefault("minebay.user-rooms.max-slots", 50);
		config.addDefault("minebay.user-rooms.offers-per-slot", 5);
		config.addDefault("minebay.user-rooms.max-name-length", 20);
		config.addDefault("minebay.user-rooms.max-rooms", 3);
		config.addDefault("minebay.user-rooms.default-icon-material", "GRASS");
		config.addDefault("minebay.user-rooms.custom-icon-price", 100);
		List<String> perms = new ArrayList<>();
		perms.add("user.premium");
		perms.add("user.donator");
		config.addDefault("room-perms", perms);
		config.addDefault("room-perm.user.premium.max-rooms", 5);
		config.addDefault("room-perm.user.premium.allow-colored-names", false);
		config.addDefault("room-perm.user.premium.allow-colored-descriptions", true);
		config.addDefault("room-perm.user.donator.max-rooms", 7);
		config.addDefault("room-perm.user.donator.allow-colored-names", true);
		config.addDefault("room-perm.user.donator.allow-colored-descriptions", true);
		
		config.applyDefaults(true);
		
		use_uuids = config.getBoolean("minebay.general.use-uuids")&&Bukkit.getOnlineMode();
		prefix = config.getString("minebay.prefix", "§8[§6Mine§bBay§8]", true);
		mbString = config.getString("minebay.mbstring", "§6Mine§bBay", true);
		saveConfig();
		
		messages = loadMessageConfig(new File(Main.pl.getDataFolder(), "lang/en.yml"));
		saveMessages();
	}
	
	private static CustomConfig loadMessageConfig(File f) {
		CustomConfig cc;
		try {
			cc = new CustomConfig(f, false).loadConfig();
			cc.addDefault("minebay.info.purchase.success", "%prefix% §aYou successfully bought §6%amount% %type% §afrom §6%seller% §afor §6%price% %currency%");
			cc.addDefault("minebay.info.purchase.error", "§cError: %error%");
			cc.addDefault("minebay.info.purchase.seller.success", "%prefix% §6%buyer% §ahas bought §6%amount% %type% §afor §6%price% %currency% §afrom you on %mbstring% §7(-%roomtax%% tax => You get %price2% %currency%)");
			cc.addDefault("minebay.info.purchase.room-owner.success", "%prefix% §6%buyer% §ahas bought §6%amount% %type% §afor §6%price% %currency% §ain your room on %mbstring% §7(-%roomtax%% tax => You get %price2% %currency%)");
			cc.addDefault("minebay.info.sell.success", "%prefix% §aSuccessfully put §6%amount% %type% §afor §6%price% %currency% §afor sale on %mbstring%");
			cc.addDefault("minebay.info.sell.type-in-price", "%prefix% §aType in the price for the item");
			cc.addDefault("minebay.info.sell.action-cancelled", "%prefix% §cOld sell action cancelled!");
			cc.addDefault("minebay.info.sell.error.invalid-price", "%prefix% §aType in another price");
			cc.addDefault("minebay.info.sell.error.noitem", "%prefix% §cYou need to hold an item in your hand");
			cc.addDefault("minebay.info.sell.error.toocheap", "%prefix% §cYou need to set a price higher than 0");
			cc.addDefault("minebay.info.sell.error.no-slots", "%prefix% §cAll slots are already occupied");
			cc.addDefault("minebay.info.sell.error.too-many-sold", "%prefix% §cYou have already sold too many items in that room");
			cc.addDefault("minebay.info.newname", "%prefix% §aType in a new name (Max. %maxchars% Characters)");
			cc.addDefault("minebay.info.newname-cancelled", "%prefix% §cOld rename action cancelled!");
			cc.addDefault("minebay.info.newname-applied", "%prefix% §aName changed to: %newname%");
			cc.addDefault("minebay.info.newdescription", "%prefix% §aType in a new description");
			cc.addDefault("minebay.info.newdescription-cancelled", "%prefix% §c Old description change action cancelled!");
			cc.addDefault("minebay.info.newdescription-applied", "%prefix% §aDescription changed to: %newdescription%");
			cc.addDefault("minebay.info.error.name-too-long", "%prefix% §cMaximum name length: %maxchars%");
			cc.addDefault("minebay.info.newicon-applied", "%prefix% §aRoom icon changed to: %type%");
			cc.addDefault("minebay.info.buy-icon.success", "%prefix% §aBought icon for %price% %currency%, room icon changed to: %type%");
			cc.addDefault("minebay.info.buy-icon.error", "%prefix% §cError: %error%");
			cc.addDefault("minebay.info.room-created", "%prefix% §aRoom §6\"%name%\" §acreated! §7(Properties: Tax: %taxshare%%, Slots: %slots%, Icon Material: %iconmaterial%, ID: %roomid%)");
			cc.addDefault("minebay.info.room-create.error.too-many-rooms", "%prefix% §cYou have already reached the room limit!");
			cc.addDefault("minebay.info.room-create.error.general", "%prefix% §cError: %error%");
			cc.addDefault("minebay.info.slot-buy.success", "%prefix% §aBought %slotamount% slot/s for %price% %currency%");
			cc.addDefault("minebay.info.slot-buy.error", "%prefix% §cError: %error%");
			cc.addDefault("minebay.info.slot-buy.toomanyslots", "%prefix% §cYou already have reached the maximum amount of slots");
			cc.addDefault("minebay.info.slot-buy.is-default", "%prefix% §cYou can't buy slots for auction room as it is a default auction room");
			cc.addDefault("minebay.info.slot-sell.success", "%prefix% §aSold %slotamount% slot/s for %price% %currency%");
			cc.addDefault("minebay.info.slot-sell.not-allowed", "%prefix% §cSlot selling is not allowed");
			cc.addDefault("minebay.info.slot-sell.all-slots-occupied", "%prefix% §cAll slots are currently occupied");
			cc.addDefault("minebay.info.slot-sell.error", "%prefix% §cError: %error%");
			cc.addDefault("minebay.info.slot-sell.notenoughslots", "%prefix% §cYou already have reached the minimum amount of slots");
			cc.addDefault("minebay.info.slot-sell.is-default", "%prefix% §cYou can't sell slots of auction room as it is a default auction room");
			cc.addDefault("minebay.info.tax.success", "%prefix% §aChanged the tax to %newtax%%");
			cc.addDefault("minebay.info.tax.toohigh", "%prefix% §cYou already have reached the maximum tax");
			cc.addDefault("minebay.info.tax.toolow", "%prefix% §cYou can't set the tax below 0%");
			cc.addDefault("minebay.info.sell-room.success", "%prefix% §aSuccessfully sold your room for %price% %currency%");
			cc.addDefault("minebay.info.sell-room.not-allowed", "%prefix% §cRoom selling is not allowed");
			cc.addDefault("minebay.info.sell-room.not-empty", "%prefix% §cThere are still offers in your room");
			cc.addDefault("minebay.info.sell-room.is-default", "%prefix% §cYou can't sell this auction room as it is the default auction room");
			cc.addDefault("minebay.info.sell-room.error", "%prefix% §cError: %error%");
			cc.addDefault("minebay.info.retract-sale.success", "%prefix% §aSuccessfully retracted your sale");
			cc.addDefault("minebay.info.user-rooms-disabled", "%prefix% §cUser rooms are disabled!");
			cc.addDefault("minebay.info.reload-complete", "%prefix% §aReload complete");
			cc.addDefault("minebay.info.reload-no-permission", "%prefix% §cNo permission");
			
			cc.addDefault("minebay.gui.item-confirm.name", "§8Confirm purchase");
			cc.addDefault("minebay.gui.item-confirm.confirm", "§aConfirm");
			cc.addDefault("minebay.gui.item-confirm.cancel", "§cCancel");
			
			cc.addDefault("minebay.gui.rooms.create-room", "§aCreate new room");
			cc.addDefault("minebay.gui.rooms.list-all", "§7All rooms");
			cc.addDefault("minebay.gui.rooms.list-self", "§7Your rooms");
			cc.addDefault("minebay.gui.misc.previous-page", "§7Previous page");
			cc.addDefault("minebay.gui.misc.next-page", "§7Next page");
			cc.addDefault("minebay.gui.misc.back", "§cBack");
			cc.addDefault("minebay.gui.misc.none", "§8None");
			cc.addDefault("minebay.gui.room-settings.delete", "§cDelete Room");
			cc.addDefault("minebay.gui.room-settings.name-desc.name", "§7Name");
			cc.addDefault("minebay.gui.room-settings.name-desc.name-lore", Arrays.asList(
																				  "§8Currently: §7%name%",
																				  "§7Description",
																				  "§8Currently: §7%description%"));
			cc.addDefault("minebay.gui.room-settings.name-desc.name-lore-linebreak-color", "§7");
			cc.addDefault("minebay.gui.room-settings.name-desc.change-name", "§7Change Name");
			cc.addDefault("minebay.gui.room-settings.name-desc.change-description", "§7Change Description");
			cc.addDefault("minebay.gui.room-settings.block.name", "§7Block");
			cc.addDefault("minebay.gui.room-settings.block.lore", Arrays.asList("§8Currently: §7%type%"));
			cc.addDefault("minebay.gui.room-settings.block-change.name", "§7Change Block");
			cc.addDefault("minebay.gui.room-settings.slots.name", "§7Slots");
			cc.addDefault("minebay.gui.room-settings.slots.lore", Arrays.asList("§8Currently: §7%slots%"));
			cc.addDefault("minebay.gui.room-settings.slots-buy.name", "§7Buy Slot/s");
			cc.addDefault("minebay.gui.room-settings.slots-buy.lore", Arrays.asList(
																			"§8Left click to buy 1 slot",
																			"§8Shift-left click to buy 5 slots"));
			cc.addDefault("minebay.gui.room-settings.slots-sell.name", "§7Sell slot/s");
			cc.addDefault("minebay.gui.room-settings.slots-sell.lore", Arrays.asList(
																			"§8Left click to sell 1 slot",
																			"§8Shift-left click to sell 5 slots"));
			cc.addDefault("minebay.gui.room-settings.tax.name", "§7Tax");
			cc.addDefault("minebay.gui.room-settings.tax.lore", Arrays.asList("§8Currently: §7%tax%"));
			cc.addDefault("minebay.gui.room-settings.tax-increase.name", "§7Increase Tax");
			cc.addDefault("minebay.gui.room-settings.tax-increase.lore", Arrays.asList(
																			"§8Left click to increase tax by 1%",
																			"§8Shift-left click to increase tax by 10%"));
			cc.addDefault("minebay.gui.room-settings.tax-decrease.name", "§7Decrease Tax");
			cc.addDefault("minebay.gui.room-settings.tax-decrease.lore", Arrays.asList(
																			"§8Left click to decrease tax by 1%",
																			"§8Shift-left click to decrease tax by 10%"));
			
			
			
			cc.addDefault("minebay.gui.confirm.room-create.name", "§8Buy Auction Room");
			cc.addDefault("minebay.gui.confirm.room-create.lore", Arrays.asList(
																			"§8Price: §7%price%"));
			
//			cc.addDefault("minebay.gui.room-settings.delete", "");
//			cc.addDefault("minebay.gui.room-settings.delete", "");
//			cc.addDefault("minebay.gui.room-settings.delete", "");
//			cc.addDefault("minebay.gui.room-settings.delete", "");
//			cc.addDefault("minebay.gui.room-settings.delete", "");
//			cc.addDefault("minebay.gui.room-settings.delete", "");
//			cc.addDefault("minebay.gui.room-settings.delete", "");
//			cc.addDefault("minebay.gui.room-settings.delete", "");
//			cc.addDefault("minebay.gui.room-settings.delete", "");
			
			cc.applyDefaults(true);
			
			return cc;
		} catch (InvalidConfigException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	private static void importLangFile(String lang) {
//		if(!new File(Main.pl.getDataFolder(), "/lang/"+lang+".yml").exists()) {
//			Main.pl.saveResource("lang/"+lang+".yml", false);
//		}
//	}
	
	public static String getAndTranslate(String file, String path) {
		String msg = null;
		if(file.equalsIgnoreCase("messages")) {
			msg = messages.getString(path);
		}else if(file.equalsIgnoreCase("config")) {
			msg = config.getString(path);
		}
		if(msg == null) msg = path;
		return simpleReplace(msg);
	}
	
	public static String getMessage(String msg) {
		return getAndTranslate("messages", msg);
	}
	
	public static String getMessage(String msg, String... params) {
		if(params.length%2!=0) return null;
		String msg2 = getAndTranslate("messages", msg);
		for(int i = 0; i < params.length; i+=2) {
			msg2 = msg2.replace("%"+params[i]+"%", params[i+1]);
		}
		return msg2;
	}
	
	public static List<String> getMessageList(String msg, String... params) {
		if(params.length%2!=0) return null;
		List<String> msg2 = messages.getStringList(msg, Arrays.asList(msg), false);
		List<String> msgf = new ArrayList<>();
		for(String s : msg2) {
			for(int i = 0; i < params.length; i+=2) {
				s = s.replace("%"+params[i]+"%", params[i+1]);
			}
			msgf.add(s);
		}
		return msgf.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
	}
	
	public static String simpleReplace(String s){
		String currencyName = Main.econ.currencyNamePlural();
		s = ChatColor.translateAlternateColorCodes('&', s
				.replace("%prefix%", config.getString("minebay.prefix"))
				.replace("%mbstring%", config.getString("minebay.mbstring")))
				.replace("%maxchars%", ""+config.getInt("minebay.user-rooms.max-name-length"))
				.replace("%slotprice%", ""+config.getInt("minebay.user-rooms.slot-price"));
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
				.replace("%seller%", it.getSellerName())
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
			if(Character.isDigit(c) || c == '.'){
				b.append(c);
			}
		}
		String so = b.toString();
		so = so.replaceAll("^\\.+", "");
		so = so.replaceAll("\\.+$", "");
		return so;
	}
	
}
