package me.mrletsplay.minebay;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CancelTask implements Runnable{

	private Player p;
	
	public CancelTask(Player pl) {
		p = pl;
	}
	
	@Override
	public void run() {
		cancelForPlayer(p);
	}
	
	public static void cancelForPlayer(Player p){
		if(Events.changeName.containsKey(p.getUniqueId())){
			p.sendMessage(Config.getMessage("minebay.info.newname-cancelled"));
			Events.changeName.remove(p.getUniqueId());
		}else if(Events.sellItem.containsKey(p.getUniqueId())){
			HashMap<Integer,ItemStack> excess = p.getInventory().addItem((ItemStack)Events.sellItem.get(p.getUniqueId())[1]);
			for(Map.Entry<Integer, ItemStack> me : excess.entrySet()){
				p.getWorld().dropItem(p.getLocation(), me.getValue());
			}
			p.sendMessage(Config.getMessage("minebay.info.sell.action-cancelled"));
			Events.sellItem.remove(p.getUniqueId());
		}else if(Events.changeDescription.containsKey(p.getUniqueId())) {
			p.sendMessage(Config.getMessage("minebay.info.newdescription-cancelled"));
			Events.changeDescription.remove(p.getUniqueId());
		}else if(Events.addPlayer.containsKey(p.getUniqueId())) {
			p.sendMessage(Config.getMessage("minebay.info.addplayer-cancelled"));
			Events.addPlayer.remove(p.getUniqueId());
		}
	}

}
