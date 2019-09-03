package me.mrletsplay.minebay;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import me.mrletsplay.mrcore.bukkitimpl.ExtraChatComponents.ItemStackComponent;
import me.mrletsplay.mrcore.bukkitimpl.ui.StaticUIElement;
import me.mrletsplay.mrcore.bukkitimpl.ui.UIBuilderMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.ui.UIElement;
import me.mrletsplay.mrcore.bukkitimpl.ui.UIItemSupplier;
import me.mrletsplay.mrcore.bukkitimpl.ui.UILayoutMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.ui.UIMultiPage;
import me.mrletsplay.mrcore.bukkitimpl.ui.event.UIBuildEvent;
import me.mrletsplay.mrcore.bukkitimpl.ui.event.UIBuildPageItemEvent;
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
			.setSupplier(new UIItemSupplier<MineBayFilterItem>() {
				
				@Override
				public UIElement toUIElement(UIBuildPageItemEvent<MineBayFilterItem> event, MineBayFilterItem it) {
					return new UIElement() {
						
						@Override
						public BaseComponent[] getLayout(UIBuildEvent e) {
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
	
}