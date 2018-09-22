package me.mrletsplay.minebay.economy;

import org.bukkit.OfflinePlayer;

public interface MineBayEconomy {

	public MineBayEconomyResponse depositPlayer(OfflinePlayer player, double amount);

	public MineBayEconomyResponse withdrawPlayer(OfflinePlayer player, double amount);
	
	public String getCurrencyNameSingular();
	
	public String getCurrencyNamePlural();
	
	public static interface MineBayEconomyResponse {
		
		public boolean isTransactionSuccess();
		
		public String getError();
		
	}
	
}
