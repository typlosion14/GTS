package org.pokesplash.gts.UI;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ItemLore;
import org.pokesplash.gts.Gts;
import org.pokesplash.gts.Listing.Listing;
import org.pokesplash.gts.Listing.PokemonListing;
import org.pokesplash.gts.UI.button.ManageListings;
import org.pokesplash.gts.UI.button.*;
import org.pokesplash.gts.UI.module.ListingInfo;
import org.pokesplash.gts.UI.module.PokemonInfo;
import org.pokesplash.gts.api.provider.ListingAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * UI of the filtered Listings page.
 */
public class FilteredListings {

	/**
	 * Method that returns the page.
	 * @param searchValue The text to look for in the listing and seller names.
	 * @return Pokemon Listings page.
	 */
	public Page getPage(String searchValue) {

		List<Listing> matches = new ArrayList<>();

		for (Listing listing : getListings()) {

			if (listing.getListingName().toLowerCase(Locale.ROOT).contains(searchValue.toLowerCase(Locale.ROOT))
				|| listing.getSellerName().toLowerCase(Locale.ROOT).contains(searchValue.toLowerCase(Locale.ROOT))) {
				matches.add(listing);
			}
		}

		return build(matches, searchValue);
	}

	/**
	 * Method that returns the page for a single species.
	 *
	 * Unlike the text search this matches the species exactly, so picking Mew from the search page
	 * doesn't also bring up Mewtwo.
	 *
	 * @param species The species to show the listings of.
	 * @return Pokemon Listings page.
	 */
	public Page getPage(Species species) {

		List<Listing> matches = new ArrayList<>();

		for (Listing listing : getListings()) {

			if (!listing.isPokemon()) {
				continue;
			}

			Species listed = ((PokemonListing) listing).getListing().getSpecies();

			if (listed.getResourceIdentifier().equals(species.getResourceIdentifier())) {
				matches.add(listing);
			}
		}

		return build(matches, species.getTranslatedName().getString());
	}

	/**
	 * @return All active listings, deep cloned when an external provider is registered.
	 */
	private List<Listing> getListings() {
		return ListingAPI.getHighestPriority() == null ? Gts.listings.getListings() :
				Gts.listings.getListings().stream().map(Listing::deepClone).toList();
	}

	/**
	 * Builds the page from listings that have already been filtered.
	 * @param listings The listings to show.
	 * @param searchValue The value to write into the page title.
	 * @return The page.
	 */
	private Page build(List<Listing> listings, String searchValue) {

		PlaceholderButton placeholder = new PlaceholderButton();

		List<Button> buttons = new ArrayList<>();

		for (Listing listing : listings) {
			List<Component> lore = ListingInfo.parse(listing);

			if (listing.isPokemon()) {
				lore.addAll(PokemonInfo.parse((PokemonListing) listing));
			}

			Button button = GooeyButton.builder()
					.display(listing.getIcon())
					.with(DataComponents.CUSTOM_NAME, listing.getDisplayName())
					.with(DataComponents.LORE, new ItemLore(lore))
					.onClick((action) -> {
						ServerPlayer sender = action.getPlayer();
						Page page = new SingleListing().getPage(sender, listing);
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

		page.setTitle(Gts.language.getFilteredListingsTitle().replaceAll("%search%", searchValue));

		setPageTitle(page, searchValue);

		return page;
	}

	private void setPageTitle(LinkedPage page, String searchValue) {
		LinkedPage next = page.getNext();
		if (next != null) {
			next.setTitle(Gts.language.getFilteredListingsTitle().replaceAll("%search%", searchValue));
			setPageTitle(next, searchValue);
		}
	}
}
