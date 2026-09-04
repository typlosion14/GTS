package org.pokesplash.gts.util;

import com.cobblemon.mod.common.pokemon.Pokemon;
import org.pokesplash.gts.Gts;

import java.lang.reflect.Method;

/**
 * Soft integration with the Cobbreeding mod.
 *
 * Cobbreeding keeps both the amount of eggs a Pokemon has produced and its neutered flag in the Pokemon's
 * persistent data, which travels with the listing, so GTS reads those straight off the Pokemon. The egg
 * quota is a server setting instead, so it is read reflectively from Cobbreeding's own accessor and GTS
 * falls back to showing a bare count whenever that can't be reached.
 */
public abstract class CobbreedingUtil {
	// Keys used by Cobbreeding's EggCountProperty and NeuterProperty.
	private static final String EGG_COUNT_KEY = "eggs_produced";
	private static final String NEUTERED_KEY = "neutered";

	private static final String EGG_COUNT_CLASS = "ludichat.cobbreeding.EggCountProperty";

	// Resolved once, on the first lookup.
	private static boolean resolved = false;
	private static boolean present = false;
	private static Object eggCountProperty; // The EggCountProperty Kotlin object.
	private static Method getMax; // EggCountProperty.getMax()

	/**
	 * @return true if Cobbreeding is installed.
	 */
	public static boolean isPresent() {
		resolve();
		return present;
	}

	/**
	 * @param pokemon The Pokemon to check.
	 * @return The amount of eggs the Pokemon has produced. Absent data reads as 0.
	 */
	public static int getEggCount(Pokemon pokemon) {
		return pokemon.getPersistentData().getInt(EGG_COUNT_KEY);
	}

	/**
	 * @return The amount of eggs a Pokemon may produce, or -1 when the quota is disabled or unreadable.
	 */
	public static int getMaxEggCount() {
		resolve();

		if (getMax == null) {
			return -1;
		}

		try {
			return (int) getMax.invoke(eggCountProperty);
		} catch (Throwable e) {
			return -1;
		}
	}

	/**
	 * @param pokemon The Pokemon to check.
	 * @return true if the Pokemon can no longer breed, either because it was neutered or because it has
	 *         used up its egg quota.
	 */
	public static boolean isSterile(Pokemon pokemon) {
		if (pokemon.getPersistentData().getBoolean(NEUTERED_KEY)) {
			return true;
		}

		int max = getMaxEggCount();
		return max >= 0 && getEggCount(pokemon) >= max;
	}

	/**
	 * Looks up Cobbreeding's quota accessor. A missing class means the mod isn't installed; a missing
	 * accessor only costs the quota, since the per Pokemon data is read from persistent data regardless.
	 */
	private static synchronized void resolve() {
		if (resolved) {
			return;
		}
		resolved = true;

		// Loaded without initialising, so a presence check never runs another mod's static setup.
		try {
			Class.forName(EGG_COUNT_CLASS, false, CobbreedingUtil.class.getClassLoader());
			present = true;
		} catch (Throwable e) {
			return;
		}

		try {
			Class<?> eggCount = Class.forName(EGG_COUNT_CLASS);
			eggCountProperty = eggCount.getField("INSTANCE").get(null);
			getMax = eggCount.getMethod("getMax");
		} catch (Throwable e) {
			Gts.LOGGER.info("Found Cobbreeding but could not read its egg quota, showing egg counts without it.");
		}
	}
}
