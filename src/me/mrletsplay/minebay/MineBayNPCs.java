package me.mrletsplay.minebay;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class MineBayNPCs {

	public static void init() {
		Main.pl.getLogger().info("Enabling NPCs");
		Bukkit.getPluginManager().registerEvents(new NPCListener(), Main.pl);
	}
	
	public static class NPCListener implements Listener {
		
		@EventHandler
		public void onNPCClick(NPCRightClickEvent event) {
			NPC npc = event.getNPC();
			if(npc.data().has("minebay_roomid")) {
				int roomID = npc.data().get("minebay_roomid");
				event.getClicker().openInventory(GUIs.getAuctionRoomGUI(event.getClicker(), roomID, 0));
			}
		}
		
	}
	
}
