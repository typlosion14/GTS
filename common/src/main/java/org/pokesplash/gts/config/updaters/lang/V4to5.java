package org.pokesplash.gts.config.updaters.lang;

import com.google.gson.JsonObject;
import org.pokesplash.gts.util.CodecUtils;
import org.pokesplash.gts.util.JsonFileUpdater;
import com.cobblemon.mod.common.CobblemonItems;
import net.minecraft.world.item.ItemStack;

public class V4to5 implements JsonFileUpdater {
    @Override
    public int from() {
        return 4;
    }

    @Override
    public int to() {
        return 5;
    }

    @Override
    public JsonObject update(JsonObject in) {
        JsonObject out = in.deepCopy();

        out.addProperty("searchTitle", "§3Gts - Search");
        out.addProperty("searchButtonLabel", "§bSearch Pokemon");
        out.add("searchButtonItem", CodecUtils.encodeItem(new ItemStack(CobblemonItems.WIDE_LENS)));
        out.addProperty("searchListingCount", "§9Listings: §b");
        out.addProperty("version", 5);
        return out;
    }

    @Override
    public boolean validate(JsonObject json) {

        String[] properties = new String[]{"version", "searchTitle", "searchButtonLabel", "searchButtonItem",
                "searchListingCount"};

        for (String property : properties) {
            if (!json.has(property)) {
                System.out.println(property);
                return false;
            }
        }

        return true;
    }
}
