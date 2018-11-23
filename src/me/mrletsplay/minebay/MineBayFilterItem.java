package me.mrletsplay.minebay;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils.ComparisonParameter;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils.ComparisonResult;
import me.mrletsplay.mrcore.bukkitimpl.config.BukkitConfigMappers;
import me.mrletsplay.mrcore.config.mapper.JSONObjectMapper;
import me.mrletsplay.mrcore.config.mapper.builder.JSONMapperBuilder;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.misc.Complex;

public class MineBayFilterItem {
	
	public static final JSONObjectMapper<MineBayFilterItem> MAPPER = new JSONMapperBuilder<>(MineBayFilterItem.class,
			(s, j) -> {
				return new MineBayFilterItem(
					s.castType(j.getJSONObject("item"), ItemStack.class, Complex.value(ItemStack.class)).get(),
					j.getJSONArray("ignored-parameters").stream().map(p -> ComparisonParameter.valueOf((String)p)).collect(Collectors.toList())
				);
			}
			)
			.mapJSONObject("item", (s, i) -> BukkitConfigMappers.ITEM_MAPPER.mapObject(s, i.getItem()), null).then()
			.mapJSONArray("ignored-parameters", i -> new JSONArray(i.getIgnoredParameters().stream().map(ComparisonParameter::name).collect(Collectors.toList())), null).then()
			.create();

	private ItemStack item;
	private List<ComparisonParameter> ignoredParameters;
	
	public MineBayFilterItem(ItemStack item, List<ComparisonParameter> ignoredParams) {
		this.item = item;
		this.ignoredParameters = ignoredParams;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public List<ComparisonParameter> getIgnoredParameters() {
		return ignoredParameters;
	}
	
	public boolean matches(ItemStack item) {
		ComparisonResult res = ItemUtils.compareItems(item, this.item);
		return res.matches(Arrays.stream(ComparisonParameter.values()).filter(c -> {
			return !c.isParameterCollection &&
					!c.equals(ComparisonParameter.AMOUNT) &&
					!ignoredParameters.contains(c);
		}).toArray(ComparisonParameter[]::new));
	}
	
}
