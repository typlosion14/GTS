package org.pokesplash.gts.UI.button;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.Page;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import org.pokesplash.gts.Gts;
import org.pokesplash.gts.UI.SearchListings;
import org.pokesplash.gts.util.ColorUtil;

public abstract class SearchPokemon {
    public static Button getButton() {
        return GooeyButton.builder()
                .display(Gts.language.getSearchButtonItem())
                .with(DataComponents.CUSTOM_NAME,
                        ColorUtil.parse(Gts.language.getSearchButtonLabel()))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick((action) -> {
                    ServerPlayer sender = action.getPlayer();

                    // The pages this button sits on are built without a player, so the search
                    // permission is checked on click rather than by leaving the button out.
                    if (!Gts.permissions.hasPermission(sender, "search")) {
                        return;
                    }

                    Page page = new SearchListings().getPage();
                    UIManager.openUIForcefully(sender, page);
                })
                .build();
    }
}
