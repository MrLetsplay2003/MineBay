package me.mrletsplay.minebay;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Tools {

	public static ItemStack createItem(Material m, int am, int dam, String name, String... lore){
		ItemStack i = new ItemStack(m, am, (short)dam);
		ItemMeta me = i.getItemMeta();
		me.setDisplayName(name);
		List<String> s = new ArrayList<>();
		for(String l:lore){
			s.add(l);
		}
		me.setLore(s);
		i.setItemMeta(me);
		return i;
	}
	
	public static ItemStack createItem(ItemStack it, String name, String... lore){
		ItemStack i = new ItemStack(it);
		ItemMeta me = i.getItemMeta();
		me.setDisplayName(name);
		List<String> s = new ArrayList<>();
		for(String l:lore){
			s.add(l);
		}
		me.setLore(s);
		i.setItemMeta(me);
		return i;
	}
	
	public static ItemStack arrowRight(){
		ItemStack i = new ItemStack(Material.BANNER);
		BannerMeta m = (BannerMeta)i.getItemMeta();
		m.setBaseColor(DyeColor.WHITE);
		m.addPattern(new Pattern(DyeColor.BLACK, PatternType.RHOMBUS_MIDDLE));
		m.addPattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_LEFT));
		m.addPattern(new Pattern(DyeColor.WHITE, PatternType.SQUARE_TOP_LEFT));
		m.addPattern(new Pattern(DyeColor.WHITE, PatternType.SQUARE_BOTTOM_LEFT));
		i.setItemMeta(m);
		return i;
	}
	
	public static ItemStack arrowLeft(){
		ItemStack i = new ItemStack(Material.BANNER);
		BannerMeta m = (BannerMeta)i.getItemMeta();
		m.setBaseColor(DyeColor.WHITE);
		m.addPattern(new Pattern(DyeColor.BLACK, PatternType.RHOMBUS_MIDDLE));
		m.addPattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_RIGHT));
		m.addPattern(new Pattern(DyeColor.WHITE, PatternType.SQUARE_TOP_RIGHT));
		m.addPattern(new Pattern(DyeColor.WHITE, PatternType.SQUARE_BOTTOM_RIGHT));
		i.setItemMeta(m);
		return i;
	}
	
}
