package me.mrletsplay.minebay.economy;

import java.math.BigDecimal;

import org.bukkit.OfflinePlayer;

import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;

import me.mrletsplay.minebay.Config;

public class TokenEnchantEconomy implements MineBayEconomy {

	public TokenEnchantAPI tapi;
	
	public TokenEnchantEconomy() {
		tapi = TokenEnchantAPI.getInstance();
	}

	@Override
	public MineBayEconomyResponse depositPlayer(OfflinePlayer player, BigDecimal amount) {
		tapi.addTokens(player, amount.doubleValue());
		return new TokenEnchantEconomyResponse(true, null);
	}

	@Override
	public MineBayEconomyResponse withdrawPlayer(OfflinePlayer player, BigDecimal amount) {
		if(tapi.getTokens(player) < amount.doubleValue())
			return new TokenEnchantEconomyResponse(false,
					Config.getMessage("minebay.economy.tokenenchant.insufficient-funds",
							"needed-balance", ""+amount,
							"current-balance", tapi.getTokensInString(player)));
		tapi.removeTokens(player, amount.doubleValue());
		return new TokenEnchantEconomyResponse(true, null);
	}

	@Override
	public String getCurrencyNameSingular() {
		return Config.getMessage("minebay.economy.tokenenchant.currency-name.singular");
	}

	@Override
	public String getCurrencyNamePlural() {
		return Config.getMessage("minebay.economy.tokenenchant.currency-name.plural");
	}
	
	public static class TokenEnchantEconomyResponse implements MineBayEconomyResponse {

		private boolean success;
		private String msg;
		
		public TokenEnchantEconomyResponse(boolean success, String msg) {
			this.success = success;
			this.msg = msg;
		}
		
		@Override
		public boolean isTransactionSuccess() {
			return success;
		}

		@Override
		public String getError() {
			return msg;
		}
		
	}

}
