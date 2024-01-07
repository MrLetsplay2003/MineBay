package me.mrletsplay.minebay;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils.ComparisonParameter;
import me.mrletsplay.mrcore.bukkitimpl.config.BukkitCustomConfig;
import me.mrletsplay.mrcore.bukkitimpl.versioned.VersionedMaterial;
import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.misc.Complex;
import net.md_5.bungee.api.ChatColor;

public class Config {

	public static File
			configFile = new File(Main.pl.getDataFolder(), "config.yml"),
			pricesFile = new File(Main.pl.getDataFolder(), "prices.yml");

	public static BukkitCustomConfig
			config = ConfigLoader.loadConfigFromFile(new BukkitCustomConfig(configFile), configFile),
			messages,
			prices = ConfigLoader.loadConfigFromFile(new BukkitCustomConfig(pricesFile), pricesFile);

	public static boolean
			useUUIDs,
			allowTaxChange,
			enableNPCs,
			enableTransactionLog;

	public static String
			prefix,
			mbString,
			economy;

	public static String
			openPermission,
			buyPermission,
			sellPermission,
			createPermission;

	public static List<MineBayFilterItem> itemFilter;
	public static List<MineBayItemPriceRestraints> itemPriceRestraints;

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
		List<String> aliases = new ArrayList<>();
		aliases.add("market");
		aliases.add("mb");
		config.addDefault("minebay.general.command-aliases", aliases);
		config.addDefault("minebay.general.max-default-room-sales", 5);
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
		config.addDefault("minebay.user-rooms.default-icon-material", VersionedMaterial.GRASS_BLOCK.getCurrentMaterialDefinition().getMaterialName());
		config.addDefault("minebay.user-rooms.default-name", "%player%'s Auction Room");
		config.addDefault("minebay.user-rooms.custom-icon-price", 100);
		config.addDefault("minebay.npc.price", 5000);
		config.addDefault("minebay.npc.skin-name", "TraderNPC");
		List<String> perms = new ArrayList<>();
		perms.add("user.premium");
		perms.add("user.donator");
		config.addDefault("room-perms", perms);
		config.addDefault("room-perm.user.premium.max-rooms", 5);
		config.addDefault("room-perm.user.premium.allow-colored-names", false);
		config.addDefault("room-perm.user.premium.allow-colored-descriptions", true);
		config.addDefault("room-perm.user.premium.max-default-room-sales", 10);
		config.addDefault("room-perm.user.premium.max-slots", 60);
		config.addDefault("room-perm.user.donator.max-rooms", 7);
		config.addDefault("room-perm.user.donator.allow-colored-names", true);
		config.addDefault("room-perm.user.donator.allow-colored-descriptions", true);
		config.addDefault("room-perm.user.donator.max-default-room-sales", 15);
		config.addDefault("room-perm.user.donator.max-slots", 70);

		config.applyDefaults();

		useUUIDs = config.getBoolean("minebay.general.use-uuids", true, true) && Bukkit.getOnlineMode();
		enableNPCs = config.getBoolean("minebay.general.enable-npcs", false, true);
		enableTransactionLog = config.getBoolean("minebay.general.enable-transaction-log", false, true);
		prefix = config.getString("minebay.prefix", "§8[§6Mine§bBay§8]", true);
		mbString = config.getString("minebay.mbstring", "§6Mine§bBay", true);
		economy = config.getString("minebay.general.economy", "Vault", true);
		allowTaxChange = config.getBoolean("minebay.general.allow-tax-changing", true, true);
		openPermission = config.getString("minebay.general.permission.open", "none", true);
		buyPermission = config.getString("minebay.general.permission.buy", "none", true);
		sellPermission = config.getString("minebay.general.permission.sell", "none", true);
		createPermission = config.getString("minebay.general.permission.create", "none", true);
		config.setComment("minebay.general.economy", "Possible economies: Vault, TokenEnchant, Reserve");
		config.saveToFile();

		messages = loadMessageConfig(new File(Main.pl.getDataFolder(), "lang/en.yml"));
		messages.saveToFile();

		config.registerMapper(MineBayFilterItem.MAPPER);
		itemFilter = config.getComplex("minebay.general.item-filter", Complex.list(MineBayFilterItem.class), Arrays.asList(
					new MineBayFilterItem(ItemUtils.createItem(VersionedMaterial.GOLDEN_AXE, 1, "§cTest", "§6Test!"), Arrays.asList(ItemUtils.ComparisonParameter.DURABILITY))
				), true);

		prices.registerMapper(MineBayItemPriceRestraints.MAPPER);
		prices.registerMapper(MineBayFilterItem.MAPPER);
		prices.setHeader(
				" You can define minimum and maximum prices for different items here\n" +
				" \"-1\" for min or max price is equivalent to no limit");
		prices.setComment("type", " Type names are Bukkit material names (refer to https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html)");
		prices.setComment("item", " Valid values for 'ignored-parameters' can be found at https://github.com/MrLetsplay2003/MrCore/blob/2.0/src/me/mrletsplay/mrcore/bukkitimpl/ItemUtils.java#L338");

		for(Material m : Material.values()) {
			if(m.name().startsWith("LEGACY_")) continue; // Skip legacy Materials (1.13+)
			prices.addDefault("type." + m.name() + ".min-price", "-1");
			prices.addDefault("type." + m.name() + ".max-price", "-1");
		}

		List<MineBayItemPriceRestraints> defaultRestraints = Arrays.asList(
					new MineBayItemPriceRestraints(
							new MineBayFilterItem(
									ItemUtils.createItem(ItemUtils.arrowLeft(), "§5§lExample Banner", "§8Testing", "§8Testing Line 2"),
									Arrays.asList(ComparisonParameter.LORE)),
							BigDecimal.valueOf(100000),
							BigDecimal.valueOf(100001))
				);

		prices.addDefault("items", defaultRestraints);

		if(prices.isEmpty()) prices.applyDefaults();
		prices.saveToFile();

		itemPriceRestraints = prices.getComplex("items", Complex.list(MineBayItemPriceRestraints.class));
	}

	private static BukkitCustomConfig loadMessageConfig(File f) {
		BukkitCustomConfig cc = ConfigLoader.loadConfigFromFile(new BukkitCustomConfig(f), f);
		cc.addDefault("minebay.info.purchase.success", "%prefix% §aYou successfully bought §6%amount%x %type% §afrom §6%seller% §afor §6%price% %currency%");
		cc.addDefault("minebay.info.purchase.error", "§cError: %error%");
		cc.addDefault("minebay.info.purchase.seller.success", "%prefix% §6%buyer% §ahas bought §6%amount%x %type% §afor §6%price% %currency% §afrom you on %mbstring% §7(-%roomtax%% tax => You get %price2% %currency%)");
		cc.addDefault("minebay.info.purchase.room-owner.success", "%prefix% §6%buyer% §ahas bought §6%amount%x %type% §afor §6%price% %currency% §ain your room on %mbstring% §7(-%roomtax%% tax => You get %price2% %currency%)");
		cc.addDefault("minebay.info.sell.success", "%prefix% §aSuccessfully put §6%amount%x %type% §afor §6%price% %currency% §afor sale on %mbstring%");
		cc.addDefault("minebay.info.sell.type-in-price", "%prefix% §aType in the price for the item");
		cc.addDefault("minebay.info.sell.action-cancelled", "%prefix% §cOld sell action cancelled!");
		cc.addDefault("minebay.info.sell.error.invalid-price", "%prefix% §aType in another price");
		cc.addDefault("minebay.info.sell.error.noitem", "%prefix% §cYou need to hold an item in your hand");
		cc.addDefault("minebay.info.sell.error.toocheap", "%prefix% §cYou need to set a price higher than 0");
		cc.addDefault("minebay.info.sell.error.below-min-price", "%prefix% §cPrice is below the set miminum price of §6%min-price% %currency%/item §c(Total max: §6%total-min-price% %currency%§c)");
		cc.addDefault("minebay.info.sell.error.above-max-price", "%prefix% §cPrice is above the set maximum price of §6%max-price% %currency%/item §c(Total max: §6%total-max-price% %currency%§c)");
		cc.addDefault("minebay.info.sell.error.no-slots", "%prefix% §cAll slots are already occupied");
		cc.addDefault("minebay.info.sell.error.too-many-sold", "%prefix% §cYou have already sold too many items in that room");
		cc.addDefault("minebay.info.sell.error.missing-access", "%prefix% §cYou're not allowed to sell items in this room");
		cc.addDefault("minebay.info.offer-cancelled", "%prefix% §aSuccessfully removed the offer by §7%seller%");
		cc.addDefault("minebay.info.notification.offer-cancelled", "%prefix% §cYour offer in the auction room §7%room% §cwas removed by the room owner");
		cc.addDefault("minebay.info.newname", "%prefix% §aType in a new name (Max. %maxchars% Characters)");
		cc.addDefault("minebay.info.newname-cancelled", "%prefix% §cOld rename action cancelled!");
		cc.addDefault("minebay.info.newname-applied", "%prefix% §aName changed to: %newname%");
		cc.addDefault("minebay.info.newdescription", "%prefix% §aType in a new description");
		cc.addDefault("minebay.info.newdescription-cancelled", "%prefix% §c Old description change action cancelled!");
		cc.addDefault("minebay.info.newdescription-applied", "%prefix% §aDescription changed to: %newdescription%");
		cc.addDefault("minebay.info.addplayer", "%prefix% §aType in a player");
		cc.addDefault("minebay.info.addplayer-cancelled", "%prefix% §cOld add player action cancelled!");
		cc.addDefault("minebay.info.addplayer-not-played", "%prefix% §cThat player hasn't played on this server before");
		cc.addDefault("minebay.info.addplayer-already-on-list", "%prefix% §cThat player is already on the list");
		cc.addDefault("minebay.info.addplayer-applied", "%prefix% §aAdded player: %player%");
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
		cc.addDefault("minebay.info.tax.toohigh", "%prefix% §cYou have already reached the maximum tax");
		cc.addDefault("minebay.info.tax.toolow", "%prefix% §cYou can't set the tax lower than 0%");
		cc.addDefault("minebay.info.sell-room.success", "%prefix% §aSuccessfully sold your room for %price% %currency%");
		cc.addDefault("minebay.info.sell-room.not-allowed", "%prefix% §cRoom selling is not allowed");
		cc.addDefault("minebay.info.sell-room.not-empty", "%prefix% §cThere are still offers in your room");
		cc.addDefault("minebay.info.sell-room.is-default", "%prefix% §cYou can't sell this auction room as it is the default auction room");
		cc.addDefault("minebay.info.sell-room.error", "%prefix% §cError: %error%");
		cc.addDefault("minebay.info.retract-sale.success", "%prefix% §aSuccessfully retracted your sale");
		cc.addDefault("minebay.info.user-rooms-disabled", "%prefix% §cUser rooms are disabled!");
		cc.addDefault("minebay.info.reload-complete", "%prefix% §aReload complete");
		cc.addDefault("minebay.info.reload-no-permission", "%prefix% §cNo permission");
		cc.addDefault("minebay.info.filter.header", "%prefix% §7§lCurrent filter:");
		cc.addDefault("minebay.info.filter.line", "§8- §7%type-or-name% §r(%type%)");
		cc.addDefault("minebay.info.tax-changing-disabled", "§cTax changing is currently disabled");
		cc.addDefault("minebay.info.permission-missing.open", "%prefix% §cYou're not allowed to open the MineBay GUI");
		cc.addDefault("minebay.info.permission-missing.buy", "%prefix% §cYou're not allowed to buy items");
		cc.addDefault("minebay.info.permission-missing.sell", "%prefix% §cYou're not allowed to sell items");
		cc.addDefault("minebay.info.permission-missing.create", "%prefix% §cYou're not allowed to create/edit a room");
		cc.addDefault("minebay.info.npcs-disabled", "%prefix% §cNPCs are disabled");
		cc.addDefault("minebay.info.spawn-npc.error.general", "%prefix% §cError: %error%");
		cc.addDefault("minebay.info.spawn-npc.success", "%prefix% §aSpawned an auctioneer NPC");

		cc.addDefault("minebay.gui.item-confirm.name", "§8Confirm purchase");
		cc.addDefault("minebay.gui.item-confirm.confirm", "§aConfirm");
		cc.addDefault("minebay.gui.item-confirm.cancel", "§cCancel");

		cc.addDefault("minebay.gui.rooms.create-room", "§aCreate new room");
		cc.addDefault("minebay.gui.rooms.list-all", "§7All rooms");
		cc.addDefault("minebay.gui.rooms.list-self", "§7Your rooms");

		cc.addDefault("minebay.gui.room.sold-item.lore", Arrays.asList(
																	"§8Price: §7%price%",
																	"§8Seller: §7%seller-name%",
																	"§8Product ID: §7%item-id%",
																	"%retract-sale%",
																	"%remove-sale%"
																));
		cc.addDefault("minebay.gui.room.sold-item.retract-sale", "§7Click to retract sale");
		cc.addDefault("minebay.gui.room.sold-item.remove-sale", "§7Right-Click to remove sale");
		cc.addDefault("minebay.gui.misc.previous-page", "§7Previous page");
		cc.addDefault("minebay.gui.misc.next-page", "§7Next page");
		cc.addDefault("minebay.gui.misc.back", "§cBack");
		cc.addDefault("minebay.gui.misc.none", "None");
		cc.addDefault("minebay.gui.room-settings.delete", "§cDelete Room");
		cc.addDefault("minebay.gui.room-settings.name-desc.name", "§7Name");
		cc.addDefault("minebay.gui.room-settings.name-desc.name-lore", Arrays.asList(
																			  "§8Currently: §7%name%",
																			  "",
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
		cc.addDefault("minebay.gui.room-settings.tax-increase.disabled", "§cChanging your tax is currently not allowed");
		cc.addDefault("minebay.gui.room-settings.tax-increase.name", "§7Increase Tax");
		cc.addDefault("minebay.gui.room-settings.tax-increase.lore", Arrays.asList(
																		"§8Left click to increase tax by 1%",
																		"§8Shift-left click to increase tax by 10%",
																		"%tax-changing-disabled%"));
		cc.addDefault("minebay.gui.room-settings.tax-decrease.disabled", "§cChanging your tax is currently not allowed");
		cc.addDefault("minebay.gui.room-settings.tax-decrease.name", "§7Decrease Tax");
		cc.addDefault("minebay.gui.room-settings.tax-decrease.lore", Arrays.asList(
																		"§8Left click to decrease tax by 1%",
																		"§8Shift-left click to decrease tax by 10%",
																		"%tax-changing-disabled%"));
		cc.addDefault("minebay.gui.room-settings.room-delete.name", "§cDelete Room");
		cc.addDefault("minebay.gui.room-settings.room-delete.lore", Arrays.asList(
																		"§8Worth: §7%worth%",
																		"§8Room ID: §7%room-id%"));
		cc.addDefault("minebay.gui.room-settings.custom-icon.name", "§6Custom block/item");
		cc.addDefault("minebay.gui.room-settings.custom-icon.lore", Arrays.asList(
																		"§8Price: %price%"));

		cc.addDefault("minebay.gui.room-settings.custom-icon.item-drop.name", "§7Drop item here");
		cc.addDefault("minebay.gui.room-settings.custom-icon.item-drop.lore", Arrays.asList(
																		"§7Drop your item here"));

		cc.addDefault("minebay.gui.room-settings.private-room.private.name", "§cRoom is private");
		cc.addDefault("minebay.gui.room-settings.private-room.private.lore", Arrays.asList(
																		"§7Nobody can sell items in this room except you"));

		cc.addDefault("minebay.gui.room-settings.private-room.public.name", "§aRoom is public");
		cc.addDefault("minebay.gui.room-settings.private-room.public.lore", Arrays.asList(
																		"§7Everyone is allowed to sell items in this room"));

		cc.addDefault("minebay.gui.room-settings.private-room.blacklist.name", "§cBlacklist");
		cc.addDefault("minebay.gui.room-settings.private-room.blacklist.lore", Arrays.asList(
																		"§7These users aren't allowed to sell items in this room"));

		cc.addDefault("minebay.gui.room-settings.private-room.whitelist.name", "§aWhitelist");
		cc.addDefault("minebay.gui.room-settings.private-room.whitelist.lore", Arrays.asList(
																		"§7Only these users are allowed to sell items in this room"));

		cc.addDefault("minebay.gui.rooms.room-item.name", "§7%room-name%");
		cc.addDefault("minebay.gui.rooms.room-item.slots-unlimited", "unlimited");
		cc.addDefault("minebay.gui.rooms.room-item.description-linebreak-color", "§7");
		cc.addDefault("minebay.gui.rooms.room-item.is-banned", "§cYou're banned from making sales");
		cc.addDefault("minebay.gui.rooms.room-item.is-private-permission", "§aPrivate room");
		cc.addDefault("minebay.gui.rooms.room-item.is-private-no-permission", "§cPrivate room");
		cc.addDefault("minebay.gui.rooms.room-item.can-edit", "§7Right-click for settings");
		cc.addDefault("minebay.gui.rooms.room-item.lore", Arrays.asList(
																		"%is-private%",
																		"%is-banned%",
																		"§8Owner: §7%owner%",
																		"§8Slots: §7%slots-occupied%/%slots-limit%",
																		"§8Tax: §7%tax%%",
																		"§8ID: §7%room-id%",
																		"§8Description: §7%description%",
																		"%can-edit%"
																	));

		cc.addDefault("minebay.gui.player-list.item.name", "§r%player%");
		cc.addDefault("minebay.gui.player-list.item.lore", Arrays.asList("§7Click to remove from list"));


		cc.addDefault("minebay.gui.confirm.room-create.name", "§8Buy Auction Room");
		cc.addDefault("minebay.gui.confirm.room-create.lore", Arrays.asList(
																		"§8Price: §7%price%"));


		cc.addDefault("minebay.gui.confirm.slots-buy.name", "§8Buy Slot(s)");
		cc.addDefault("minebay.gui.confirm.slots-buy.lore", Arrays.asList(
																		"§8Price: §7%price%",
																		"§8Room ID: §7%room-id%",
																		"§8Amount: §7%amount%"));

		cc.addDefault("minebay.gui.confirm.slots-sell.name", "§8Sell Slot(s)");
		cc.addDefault("minebay.gui.confirm.slots-sell.lore", Arrays.asList(
																		"§8Worth: §7%price%",
																		"§8Room ID: §7%room-id%",
																		"§8Amount: §7%amount%"));

		cc.addDefault("minebay.gui.confirm.room-sell.name", "§8Sell Room");
		cc.addDefault("minebay.gui.confirm.room-sell.lore", Arrays.asList(
																		"§8Worth: §7%price%",
																		"§8Room ID: §7%room-id%"));

		cc.addDefault("minebay.gui.confirm.confirm.name", "§aConfirm");
		cc.addDefault("minebay.gui.confirm.confirm.lore", Arrays.asList(
																		"§7This will confirm the current action"));

		cc.addDefault("minebay.gui.confirm.cancel.name", "§cCancel");
		cc.addDefault("minebay.gui.confirm.cancel.lore", Arrays.asList(
																		"§7This will cancel the current action"));

		cc.addDefault("minebay.gui.confirm.buy-item.info.name", "§eInfo");
		cc.addDefault("minebay.gui.confirm.buy-item.info.lore", Arrays.asList(
																		"§8Price: §7%price%",
																		"§8Seller: §7%seller%",
																		"§8Product ID: §7%item-id%",
																		"§8Auction Room: §7%room-id%"));

		cc.addDefault("minebay.economy.tokenenchant.insufficient-funds", "§cInsufficient funds (Current balance: %current-balance% token(s), needed: %needed-balance% token(s))");
		cc.addDefault("minebay.economy.tokenenchant.currency-name.singular", "token");
		cc.addDefault("minebay.economy.tokenenchant.currency-name.plural", "tokens");

		cc.addDefault("minebay.economy.reserve.insufficient-funds", "§cInsufficient funds (Current balance: %current-balance% %currency-name-plural%, needed: %needed-balance% %currency-name-plural%)");

		cc.applyDefaults();
		return cc;
	}

	public static String getFriendlyTypeName(Material material) {
		return material.name().toLowerCase().replace("_", " "); // TODO
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
		String currencyName = Main.econ.getCurrencyNamePlural();
		s = ChatColor.translateAlternateColorCodes('&', s
				.replace("%prefix%", config.getString("minebay.prefix"))
				.replace("%mbstring%", config.getString("minebay.mbstring")))
				.replace("%maxchars%", ""+config.getInt("minebay.user-rooms.max-name-length"))
				.replace("%slotprice%", ""+config.getInt("minebay.user-rooms.slot-price"));
		if(currencyName!=null){
			s = s.replace("%currency%", Main.econ.getCurrencyNamePlural());
		}else{
			s = s.replace("%currency%", "");
		}
		return s;
	}

	public static String replaceForSellItem(String s, SellItem it, AuctionRoom r){
		s = ChatColor.translateAlternateColorCodes('&', s
				.replace("%amount%", ""+it.getItem().getAmount())
				.replace("%type%", it.getItem().hasItemMeta() && it.getItem().getItemMeta().hasDisplayName() ? it.getItem().getItemMeta().getDisplayName() : Config.getFriendlyTypeName(it.getItem().getType()))
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
				.replace("%roomid%", ""+r.getID())
				.replace("%iconmaterial%", Config.getFriendlyTypeName(r.getIcon().getType())));
		return s;
	}

	public static BigDecimal getMinimumPrice(ItemStack item) {
		BigDecimal m = getMinimumPriceStrict(item);
		if(m.compareTo(BigDecimal.ZERO) == 1) return m;
		return getMinimumPrice(item.getType());
	}

	public static BigDecimal getMaximumPrice(ItemStack item) {
		BigDecimal m = getMaximumPriceStrict(item);
		if(m.compareTo(BigDecimal.ZERO) == 1) return m;
		return getMaximumPrice(item.getType());
	}

	public static BigDecimal getMinimumPrice(Material type) {
		return new BigDecimal(prices.getString("type." + type.name() + ".min-price", "-1", false));
	}

	public static BigDecimal getMaximumPrice(Material type) {
		return new BigDecimal(prices.getString("type." + type.name() + ".max-price", "-1", false));
	}

	public static BigDecimal getMinimumPriceStrict(ItemStack item) {
		return itemPriceRestraints.stream()
				.filter(f -> f.getItem().matches(item))
				.map(MineBayItemPriceRestraints::getMinPrice)
				.filter(i -> i.compareTo(BigDecimal.ZERO) == 1) // > 0
				.sorted(Comparator.reverseOrder())
				.findFirst().orElse(BigDecimal.valueOf(-1));
	}

	public static BigDecimal getMaximumPriceStrict(ItemStack item) {
		return itemPriceRestraints.stream()
				.filter(f -> f.getItem().matches(item))
				.map(MineBayItemPriceRestraints::getMaxPrice)
				.filter(i -> i.compareTo(BigDecimal.ZERO) == 1) // > 0
				.sorted()
				.findFirst().orElse(BigDecimal.valueOf(-1));
	}

	public static void reload() {
		config.clear();
		config.loadFromFile();
	}

}
