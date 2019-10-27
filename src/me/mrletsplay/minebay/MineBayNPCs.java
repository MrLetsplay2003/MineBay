package me.mrletsplay.minebay;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;

public class MineBayNPCs {

	public static void init() {
		Main.pl.getLogger().info("Enabling NPCs");
		Bukkit.getPluginManager().registerEvents(new NPCListener(), Main.pl);
	}
	
	public static void spawnAuctionRoomNPC(AuctionRoom room, Location location) {
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Auctioneer");
		npc.data().setPersistent("minebay_roomid", room.getID());
		npc.spawn(location);
		
		SkinnableEntity en = (SkinnableEntity) npc.getEntity();
		en.setSkinName(Config.config.getString("minebay.npc.skin-name"));
		en.getSkinTracker().notifySkinChange(true);
	}
	
	public static class NPCListener implements Listener {
		
		@EventHandler
		public void onNPCClick(NPCRightClickEvent event) {
			NPC npc = event.getNPC();
			if(npc.data().has("minebay_roomid")) {
				int roomID = npc.data().get("minebay_roomid");
				event.getClicker().openInventory(GUIs.getAuctionRoomGUI(event.getClicker(), roomID, 0, true));
			}
		}
		
	}
	
}
