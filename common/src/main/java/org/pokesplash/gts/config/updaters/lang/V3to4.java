package org.pokesplash.gts.config.updaters.lang;

import com.google.gson.JsonObject;
import org.pokesplash.gts.util.JsonFileUpdater;

public class V3to4 implements JsonFileUpdater {
    @Override
    public int from() {
        return 3;
    }

    @Override
    public int to() {
        return 4;
    }

    @Override
    public JsonObject update(JsonObject in) {
        JsonObject out = in.deepCopy();

        out.addProperty("eggCount", "§2Eggs: ");
        out.addProperty("sterile", "§cSterile");
        out.addProperty("fertile", "§aFertile");
        out.addProperty("version", 4);
        return out;
    }

    @Override
    public boolean validate(JsonObject json) {

        String[] properties = new String[]{"version", "eggCount", "sterile", "fertile"};

        for (String property : properties) {
            if (!json.has(property)) {
                System.out.println(property);
                return false;
            }
        }

        return true;
    }
}
