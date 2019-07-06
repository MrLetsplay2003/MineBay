package me.mrletsplay.minebay.notifications;

import org.bukkit.entity.Player;

import me.mrletsplay.mrcore.json.converter.JSONConvertible;

public interface OfflineNotification extends JSONConvertible {

	public String getMessage();
	
	public void onSent(Player p);
	
	public default void send(Player p) {
		p.sendMessage(getMessage());
		onSent(p);
	}
	
}
