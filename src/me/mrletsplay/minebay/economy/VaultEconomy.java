package me.mrletsplay.minebay.economy;

import org.bukkit.OfflinePlayer;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class VaultEconomy implements MineBayEconomy {

	public Economy vaultEcon;
	
	public VaultEconomy(Economy vaultEcon) {
		this.vaultEcon = vaultEcon;
	}
	
	@Override
	public MineBayEconomyResponse depositPlayer(OfflinePlayer player, double amount) {
		return new VaultEconomyResponse(vaultEcon.depositPlayer(player, amount));
	}

	@Override
	public MineBayEconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
		return new VaultEconomyResponse(vaultEcon.withdrawPlayer(player, amount));
	}

	@Override
	public String getCurrencyNameSingular() {
		return vaultEcon.currencyNameSingular();
	}

	@Override
	public String getCurrencyNamePlural() {
		return vaultEcon.currencyNamePlural();
	}
	
	public static class VaultEconomyResponse implements MineBayEconomyResponse {

		public EconomyResponse vaultResponse;
		
		public VaultEconomyResponse(EconomyResponse vaultResponse) {
			this.vaultResponse = vaultResponse;
		}
		
		@Override
		public boolean isTransactionSuccess() {
			return vaultResponse.transactionSuccess();
		}

		@Override
		public String getError() {
			return vaultResponse.errorMessage;
		}
		
	}
	
}
