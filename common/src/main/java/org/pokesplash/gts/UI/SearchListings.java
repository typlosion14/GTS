package org.pokesplash.gts.UI;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ItemLore;
import org.pokesplash.gts.Gts;
import org.pokesplash.gts.Listing.PokemonListing;
import org.pokesplash.gts.UI.button.ManageListings;
import org.pokesplash.gts.UI.button.*;
import org.pokesplash.gts.util.ColorUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * UI that lists every species currently for sale, so a player can pick one instead of typing a name.
 */
public class SearchListings {

	// Item stacks can't show a number above this, and the count is repeated in the lore anyway.
	private static final int MAX_STACK_SIZE = 64;

	/**
	 * Method that returns the page.
	 * @return The search page.
	 */
	public Page getPage() {

		PlaceholderButton placeholder = new PlaceholderButton();

		List<Button> buttons = new ArrayList<>();

		for (Listed listed : getListedSpecies()) {

			List<Component> lore = new ArrayList<>();
			lore.add(ColorUtil.parse(Gts.language.getSearchListingCount() + listed.count));

			Button button = GooeyButton.builder()
					// The plain species icon, so a shiny or a regional form doesn't stand in for the species.
					.display(PokemonItem.from(listed.species, new String[0],
							Math.min(listed.count, MAX_STACK_SIZE)))
					.with(DataComponents.CUSTOM_NAME, listed.species.getTranslatedName())
					.with(DataComponents.LORE, new ItemLore(lore))
					.onClick((action) -> {
						ServerPlayer sender = action.getPlayer();
						Page page = new FilteredListings().getPage(listed.species);
						UIManager.openUIForcefully(sender, page);
					})
					.build();

			buttons.add(button);
		}

		ChestTemplate template = ChestTemplate.builder(6)
				.rectangle(0, 0, 5, 9, placeholder)
				.fill(Filler.getButton())
				.set(48, SeePokemonListings.getButton())
				.set(49, ManageListings.getButton())
				.set(50, SeeItemListings.getButton())
				.set(53, NextPage.getButton())
				.set(45, PreviousPage.getButton())
				.set(52, RelistAll.getButton())
				.build();

		LinkedPage page = PaginationHelper.createPagesFromPlaceholders(template, buttons, null);

		page.setTitle(Gts.language.getSearchTitle());

		setPageTitle(page);

		return page;
	}

	/**
	 * Groups the active Pokemon listings by species.
	 * @return Every listed species, sorted by name, with how many listings it has.
	 */
	private List<Listed> getListedSpecies() {

		LinkedHashMap<ResourceLocation, Listed> species = new LinkedHashMap<>();

		for (PokemonListing listing : Gts.listings.getPokemonListings()) {
			Species listed = listing.getListing().getSpecies();

			species.computeIfAbsent(listed.getResourceIdentifier(), key -> new Listed(listed)).count++;
		}

		List<Listed> output = new ArrayList<>(species.values());
		output.sort(Comparator.comparing(listed -> listed.species.getName().toLowerCase()));

		return output;
	}

	private void setPageTitle(LinkedPage page) {
		LinkedPage next = page.getNext();
		if (next != null) {
			next.setTitle(Gts.language.getSearchTitle());
			setPageTitle(next);
		}
	}

	/**
	 * A species that has at least one listing, and how many listings it has.
	 */
	private static class Listed {
		private final Species species;
		private int count;

		private Listed(Species species) {
			this.species = species;
		}
	}
}
