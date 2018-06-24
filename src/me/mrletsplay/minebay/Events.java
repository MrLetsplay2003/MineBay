package me.mrletsplay.minebay;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class Events implements Listener{
	
	public static HashMap<UUID, Integer> changeName = new HashMap<>();
	public static HashMap<UUID, Object[]> sellItem = new HashMap<>();
	public static HashMap<UUID, Integer> changeDescription = new HashMap<>();
	
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
				int roomID = (int) objs[0];
				ItemStack item = (ItemStack) objs[1];
				AuctionRoom r = AuctionRooms.getAuctionRoomByID(roomID);
				SellItem it = new SellItem(item, r, (Config.use_uuids?e.getPlayer().getUniqueId().toString():e.getPlayer().getName()), pr, r.getNewItemID());
				r.addSellItem(it);
				e.getPlayer().sendMessage(Config.replaceForSellItem(Config.getMessage("minebay.info.sell.success"), it, r));
				sellItem.remove(e.getPlayer().getUniqueId());
			}catch(NumberFormatException ex){
				e.getPlayer().sendMessage(Config.getMessage("minebay.info.sell.error.invalid-price"));
			}
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
	}
	
	@EventHandler
	public void onCMD(PlayerCommandPreprocessEvent e) {
		String[] spl = e.getMessage().split(" ");
		for(String a : Config.config.getStringList("minebay.general.command-aliases")) {
			if(spl[0].equalsIgnoreCase(a)) {
				e.getPlayer().chat("/minebay"+e.getMessage().substring(a.length()));
				e.setCancelled(true);
				return;
			}
		}
	}
	
}
