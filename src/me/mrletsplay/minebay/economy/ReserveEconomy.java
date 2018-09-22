package me.mrletsplay.minebay.economy;

import java.math.BigDecimal;

import org.bukkit.OfflinePlayer;

import me.mrletsplay.minebay.Config;
import net.tnemc.core.Reserve;

public class ReserveEconomy implements MineBayEconomy {

	@Override
	public MineBayEconomyResponse depositPlayer(OfflinePlayer player, double amount) {
		Reserve.instance().economy().addHoldings(player.getUniqueId(), new BigDecimal(amount));
		return new ReserveEconomyResponse(true, null);
	}

	@Override
	public MineBayEconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
		if(!Reserve.instance().economy().canRemoveHoldings(player.getUniqueId(), new BigDecimal(amount)))
			return new ReserveEconomyResponse(false,
					Config.getMessage("minebay.economy.reserve.insufficient-funds",
							"needed-balance", ""+amount,
							"current-balance", Reserve.instance().economy().getHoldings(player.getUniqueId()).toString(),
							"currency-name-singular", Reserve.instance().economy().currencyDefaultSingular(),
							"currency-name-plural", Reserve.instance().economy().currencyDefaultPlural()));
		Reserve.instance().economy().removeHoldings(player.getUniqueId(), new BigDecimal(amount));
		return new ReserveEconomyResponse(true, null);
	}

	@Override
	public String getCurrencyNameSingular() {
		return Reserve.instance().economy().currencyDefaultSingular();
	}

	@Override
	public String getCurrencyNamePlural() {
		return Reserve.instance().economy().currencyDefaultPlural();
	}
	
	public static class ReserveEconomyResponse implements MineBayEconomyResponse {

		private boolean success;
		private String msg;
		
		public ReserveEconomyResponse(boolean success, String msg) {
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
