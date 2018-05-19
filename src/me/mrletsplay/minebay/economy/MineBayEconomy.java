package me.mrletsplay.minebay.economy;

import org.bukkit.OfflinePlayer;

public interface MineBayEconomy {

	MineBayEconomyResponse depositPlayer(OfflinePlayer player, double amount);

	MineBayEconomyResponse withdrawPlayer(OfflinePlayer player, double amount);
	
	String getCurrencyNameSingular();
	
	String getCurrencyNamePlural();
	
	public static interface MineBayEconomyResponse {
		
		public boolean isTransactionSuccess();
		
		public String getError();
		
	}
	
}
