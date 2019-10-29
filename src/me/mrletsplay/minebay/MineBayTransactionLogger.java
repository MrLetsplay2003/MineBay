package me.mrletsplay.minebay;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import me.mrletsplay.mrcore.bukkitimpl.config.BukkitConfigMappers;

public class MineBayTransactionLogger {

	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("'['z dd.MM.yyyy HH:mm:ss']' ");
	
	private static PrintWriter logWriter;
	
	static {
		try {
			logWriter = new PrintWriter(new FileWriter(new File(Main.pl.getDataFolder(), "transactions.log"), true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void logTransaction(OfflinePlayer seller, OfflinePlayer buyer, AuctionRoom room, BigDecimal price, ItemStack item) {
		logWriter.write(
				TIMESTAMP_FORMAT.format(Instant.now().atZone(ZoneId.systemDefault())) +
				seller.getName() + " (" + seller.getUniqueId() + ") -> " + buyer.getName() + " (" + buyer.getUniqueId() + ")" +
				" for " + price + " " + Main.econ.getCurrencyNamePlural() + " in Room #" + room.getID() + " (" + room.getName() + "): " +
				BukkitConfigMappers.ITEM_MAPPER.mapObject(Config.config, item).toString());
		logWriter.write(System.lineSeparator());
		logWriter.flush();
	}
	
}
