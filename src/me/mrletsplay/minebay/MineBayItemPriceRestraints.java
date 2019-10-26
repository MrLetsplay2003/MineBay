package me.mrletsplay.minebay;

import java.math.BigDecimal;

import me.mrletsplay.mrcore.config.mapper.JSONObjectMapper;
import me.mrletsplay.mrcore.config.mapper.builder.JSONMapperBuilder;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.misc.Complex;

public class MineBayItemPriceRestraints implements JSONConvertible {

	public static final JSONObjectMapper<MineBayItemPriceRestraints> MAPPER = new JSONMapperBuilder<>(MineBayItemPriceRestraints.class,
			(s, j) -> {
				MineBayFilterItem f = s.castType(j.getJSONObject("item"), MineBayFilterItem.class, Complex.value(MineBayFilterItem.class)).get();
				return new MineBayItemPriceRestraints(f, new BigDecimal(j.getString("min-price")), new BigDecimal(j.getString("max-price")));
			})
			.mapJSONObject("item", (s, i) -> MineBayFilterItem.MAPPER.mapObject(s, i.getItem()), null).then()
			.mapString("min-price", i -> i.getMinPrice().toString(), null).then()
			.mapString("max-price", i -> i.getMaxPrice().toString(), null).then()
			.create();
	
	private MineBayFilterItem item;
	private BigDecimal minPrice, maxPrice;
	
	public MineBayItemPriceRestraints(MineBayFilterItem item, BigDecimal minPrice, BigDecimal maxPrice) {
		this.item = item;
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
	}
	
	public MineBayFilterItem getItem() {
		return item;
	}
	
	public BigDecimal getMinPrice() {
		return minPrice;
	}
	
	public BigDecimal getMaxPrice() {
		return maxPrice;
	}
	
}
