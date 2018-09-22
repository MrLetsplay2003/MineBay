package me.mrletsplay.minebay;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.mrletsplay.mrcore.bukkitimpl.ChatUI.ItemSupplier;
import me.mrletsplay.mrcore.bukkitimpl.ChatUI.StaticUIElement;
import me.mrletsplay.mrcore.bukkitimpl.ChatUI.UIBuilderMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.ChatUI.UIElement;
import me.mrletsplay.mrcore.bukkitimpl.ChatUI.UILayoutMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.ChatUI.UIMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.ExtraChatComponents.ItemStackComponent;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils.ComparisonParameter;
import me.mrletsplay.mrcore.bukkitimpl.ItemUtils.ComparisonResult;
import me.mrletsplay.mrcore.config.ConfigExpansions.ExpandableCustomConfig.ObjectMapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class MineBayFilter {
	
	public static UIMultiPage<MineBayFilterItem> filterUI = new UIBuilderMultiPage<MineBayFilterItem>()
			.addNextPageElement("next_page", new StaticUIElement("§8Next page").setHoverText("Click to go to the next page"))
			.addPreviousPageElement("previous_page", new StaticUIElement("§8Previous Page").setHoverText("Click to go to the previous page"))
			.setLayout(new UILayoutMultiPage()
					.addText(Config.getMessage("minebay.info.filter.header"))
					.newLine()
					.addElement("previous_page")
					.addText(" ")
					.addElement("next_page")
					.addPageElements(10, true))
			.setSupplier(new ItemSupplier<MineBayFilter.MineBayFilterItem>() {
				
				@Override
				public UIElement toUIElement(Player p, MineBayFilterItem it) {
					return new UIElement() {
						
						@Override
						public BaseComponent[] getLayout(Player p) {
							ItemStack item = it.getItem();
							TextComponent it2 = new TextComponent(Config.getMessage("minebay.info.filter.line",
									"type-or-name", item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : Config.getFriendlyTypeName(item.getType()),
									"type", Config.getFriendlyTypeName(item.getType())));
							it2.setHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ItemStackComponent(it.getItem()).toBase()));
							return new BaseComponent[] {it2};
						}
					};
				}
				
				@Override
				public List<MineBayFilterItem> getItems() {
					return Config.itemFilter;
				}
			})
			.build();
	
	public static boolean contains(ItemStack item) {
		return Config.itemFilter.stream().anyMatch(i -> i.matches(item));
	}
	
	public static class MineBayFilterObjectMapper extends ObjectMapper<MineBayFilterItem>{

		public MineBayFilterObjectMapper() {
			super(MineBayFilterItem.class);
		}

		@Override
		public Map<String, Object> mapObject(MineBayFilterItem object) {
			Map<String, Object> map = new HashMap<>();
			map.put("item", object.item);
			map.put("ignored-parameters", object.ignoredParameters.stream().map(p -> p.name()).collect(Collectors.toList()));
			return map;
		}

		@SuppressWarnings("unchecked")
		@Override
		public MineBayFilterItem constructObject(Map<String, Object> map) {
			if(!requireKeys(map, "item", "ignored-parameters")) return null;
			ItemStack it = castGeneric(map.get("item"), ItemStack.class);
			List<ComparisonParameter> params = ((List<String>) map.get("ignored-parameters")).stream().map(s -> ComparisonParameter.valueOf(s.toUpperCase())).collect(Collectors.toList());
			return new MineBayFilterItem(it, params);
		}
		
	}
	
	public static class MineBayFilterItem {
		
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

}
