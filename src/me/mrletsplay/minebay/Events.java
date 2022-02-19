package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import me.mrletsplay.minebay.notifications.OfflineNotification;
import me.mrletsplay.minebay.notifications.PlayerData;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;
import net.md_5.bungee.api.ChatColor;

public class Events implements Listener{
	
	public static HashMap<UUID, Integer> changeName = new HashMap<>();
	public static HashMap<UUID, Object[]> sellItem = new HashMap<>();
	public static HashMap<UUID, Integer> changeDescription = new HashMap<>();
	public static HashMap<UUID, Integer> addPlayer = new HashMap<>();
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		if(changeName.containsKey(e.getPlayer().getUniqueId())){
			String nName = e.getMessage();
			int room = changeName.get(e.getPlayer().getUniqueId());
			if(nName.length()<=Config.config.getInt("minebay.user-rooms.max-name-length")){
				changeName.remove(e.getPlayer().getUniqueId());
				AuctionRoom r = AuctionRooms.getAuctionRoomByID(room);
				if(MineBay.hasPermissionForColoredNames(e.getPlayer())){
					nName = ChatColor.translateAlternateColorCodes('&', nName);
				}
				r.setName(nName);
				r.saveAllSettings();
				r.updateSettings();
				MineBay.updateRoomSelection();
				e.getPlayer().sendMessage(Config.getMessage("minebay.info.newname-applied").replace("%newname%", nName));
			}else{
				e.getPlayer().sendMessage(Config.getMessage("minebay.info.error.name-too-long"));
			}
			e.setCancelled(true);
		}else if(changeDescription.containsKey(e.getPlayer().getUniqueId())) {
			String nName = e.getMessage();
			int room = changeDescription.get(e.getPlayer().getUniqueId());
			changeDescription.remove(e.getPlayer().getUniqueId());
			AuctionRoom r = AuctionRooms.getAuctionRoomByID(room);
			if(MineBay.hasPermissionForColoredDescriptions(e.getPlayer())){
				nName = ChatColor.translateAlternateColorCodes('&', nName);
			}
			r.setDescription(nName);
			r.saveAllSettings();
			r.updateSettings();
			MineBay.updateRoomSelection();
			e.getPlayer().sendMessage(Config.getMessage("minebay.info.newdescription-applied").replace("%newdescription%", nName));
			e.setCancelled(true);
		}else if(sellItem.containsKey(e.getPlayer().getUniqueId())){
			String price = e.getMessage();
			try{
				BigDecimal pr = new BigDecimal(price);
				Object[] objs = sellItem.get(e.getPlayer().getUniqueId());
				sellItem.remove(e.getPlayer().getUniqueId());
				int roomID = (int) objs[0];
				ItemStack item = (ItemStack) objs[1];
				Bukkit.getScheduler().runTask(Main.pl, () -> e.getPlayer().openInventory(GUIs.getAuctionRoomGUI(e.getPlayer(), roomID, 0)));
				BigDecimal minPrice = Config.getMinimumPrice(item);
				if(minPrice.compareTo(BigDecimal.ZERO) == 1 && pr.compareTo(minPrice.multiply(BigDecimal.valueOf(item.getAmount()))) == -1) { // minPrice > 0 && pr < x * minPrice
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell.error.below-min-price", "min-price", minPrice.toString(), "total-min-price", minPrice.multiply(BigDecimal.valueOf(item.getAmount())).toString()));
					ItemUtils.addItemOrDrop(e.getPlayer(), item);
					e.setCancelled(true);
					return;
				}
				BigDecimal maxPrice = Config.getMaximumPrice(item);
				if(maxPrice.compareTo(BigDecimal.ZERO) == 1 && pr.compareTo(maxPrice.multiply(BigDecimal.valueOf(item.getAmount()))) == 1) { // maxPrice > 0 && pr > x * maxPrice
					e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell.error.above-max-price", "max-price", maxPrice.toString(), "total-max-price", maxPrice.multiply(BigDecimal.valueOf(item.getAmount())).toString()));
					ItemUtils.addItemOrDrop(e.getPlayer(), item);
					e.setCancelled(true);
					return;
				}
				AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
				SellItem it = new SellItem(item, r, (Config.useUUIDs?e.getPlayer().getUniqueId().toString() : e.getPlayer().getName()), pr, r.getNewItemID());
				r.addSellItem(it);
				e.getPlayer().sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.sell.success"), it, r));
			}catch(NumberFormatException ex){
				e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell.error.invalid-price"));
			}
			e.setCancelled(true);
		}else if(addPlayer.containsKey(e.getPlayer().getUniqueId())) {
			String nName = e.getMessage();
			int room = addPlayer.get(e.getPlayer().getUniqueId());
			addPlayer.remove(e.getPlayer().getUniqueId());
			AuctionRoom r = AuctionRooms.getAuctionRoomByID(room);
			OfflinePlayer pl = Bukkit.getOfflinePlayer(nName);
			if(!pl.hasPlayedBefore()) {
				e.getPlayer().sendMessage(Config.getMessage("minebay.info.addplayer-not-played"));
				e.setCancelled(true);
				return;
			}
			if(r.isPlayerOnList(pl)) {
				e.getPlayer().sendMessage(Config.getMessage("minebay.info.addplayer-already-on-list"));
				e.setCancelled(true);
				return;
			}
			r.addPlayerToList(pl);
			r.saveAllSettings();
			r.updatePlayerList();
			MineBay.updateRoomSelection();
			e.getPlayer().sendMessage(Config.getMessage("minebay.info.addplayer-applied", "player", pl.getName()));
			Bukkit.getScheduler().runTask(Main.pl, () -> e.getPlayer().openInventory(GUIs.getAuctionRoomPlayerListGUI(e.getPlayer(), room, 0)));
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		if(e.getPlayer().hasPermission("minebay.notify-update")){
			if(Config.config.getBoolean("minebay.general.enable-update-check") && Config.config.getBoolean("minebay.general.update-check-on-join")){
				UpdateChecker.checkForUpdate(e.getPlayer());
			}
		}
		
		List<OfflineNotification> offn = PlayerData.getOfflineNotifications(e.getPlayer());
		if(!offn.isEmpty()) {
			PlayerData.resetOfflineNotifications(e.getPlayer());
			offn.forEach(n -> n.send(e.getPlayer()));
		}
	}
	
}
