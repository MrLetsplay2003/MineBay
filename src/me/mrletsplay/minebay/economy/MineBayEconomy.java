package me.mrletsplay.minebay.economy;

import java.math.BigDecimal;

import org.bukkit.OfflinePlayer;

public interface MineBayEconomy {

	public MineBayEconomyResponse depositPlayer(OfflinePlayer player, BigDecimal amount);

	public MineBayEconomyResponse withdrawPlayer(OfflinePlayer player, BigDecimal amount);
	
	public String getCurrencyNameSingular();
	
	public String getCurrencyNamePlural();
	
	public static interface MineBayEconomyResponse {
		
		public boolean isTransactionSuccess();
		
		public String getError();
		
	}
	
}
