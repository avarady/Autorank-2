package me.armar.plugins.autorank.hooks.statsapi;

import me.armar.plugins.autorank.Autorank;
import me.armar.plugins.autorank.hooks.DependencyHandler;
import nl.lolmewn.stats.Main;
import nl.lolmewn.stats.api.Stat;
import nl.lolmewn.stats.api.StatsAPI;
import nl.lolmewn.stats.player.StatData;
import nl.lolmewn.stats.player.StatsPlayer;

import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

/**
 * Handles all connections with Stats
 * <p>
 * Date created: 21:02:34 15 mrt. 2014
 * 
 * @author Staartvin
 * 
 */
public class StatsAPIHandler implements DependencyHandler {

	private StatsAPI api;
	private final Autorank plugin;

	public StatsAPIHandler(final Autorank instance) {
		plugin = instance;
	}

	public boolean areBetaFunctionsEnabled() {
		if (api != null) {
			return api.isUsingBetaFunctions();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see me.armar.plugins.autorank.hooks.DependencyHandler#get()
	 */
	@Override
	public Plugin get() {
		final Plugin plugin = this.plugin.getServer().getPluginManager()
				.getPlugin("Stats");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof Main)) {
			return null; // Maybe you want throw an exception instead
		}

		return plugin;
	}

	/**
	 * Gets the total blocks of a certain id and damage value placed/broken
	 * 
	 * @param playerName Player to check for
	 * @param id Item ID to check for
	 * @param damageValue Damage value to check for. (negative number to not
	 *            skip check)
	 * @param worldName World to check in. Null for global.
	 * @param statType Either "Block break" or "Block place"
	 * @return amount player placed/broke of a block
	 */
	public int getBlocksStat(final String playerName, final int id,
			final int damageValue, final String worldName, final String statType) {
		if (!isAvailable())
			return 0;

		final StatData blockStat = getStatType(statType, playerName, worldName);
		int value = 0;
		boolean checkDamageValue = false;

		if (damageValue > 0) {
			checkDamageValue = true;
		}

		for (final Object[] vars : blockStat.getAllVariables()) {

			if (checkDamageValue) {
				// VAR 0 = blockID, VAR 1 = damageValue, VAR 2 = (1 = break, 0 = place)
				final byte[] byteArray = (byte[]) vars[1];

				if ((Integer) vars[0] == id
						&& Integer.parseInt(new String(byteArray)) == damageValue) {
					value += blockStat.getValue(vars);
				}
			} else {
				if ((Integer) vars[0] == id) {
					value += blockStat.getValue(vars);
				}
			}
		}

		return value;
	}

	public EntityType getEntityType(final String entityName) {
		try {
			return EntityType.valueOf(entityName.toUpperCase());
		} catch (final Exception e) {
			return null;
		}
	}

	public int getNormalStat(final String playerName, final String statName,
			final String worldName) {
		if (!isAvailable())
			return 0;

		final StatData stat = getStatType(statName, playerName, worldName);

		int value = 0;

		for (final Object[] vars : stat.getAllVariables()) {
			value += stat.getValue(vars);
		}

		return value;
	}

	public Stat getStat(final String name) {
		return api.getStat(name);
	}

	public StatsPlayer getStats(final String playerName) {
		return api.getPlayer(playerName);
	}

	/**
	 * Get the stats of a player, a new stat will be created if it didn't exist
	 * yet.
	 * 
	 * @param statName Name of the stat to get
	 * @param playerName Player to get the stats of.
	 * @param worldName World to check for.
	 * @return Requested stat of the player
	 */
	public StatData getStatType(final String statName, final String playerName,
			final String worldName) {
		final StatsPlayer sPlayer = getStats(playerName);

		final Stat stat = getStat(statName);

		if (stat == null)
			throw new IllegalArgumentException("Unknown stat '" + statName
					+ "'!");

		StatData data = null;

		if (worldName != null) {
			data = sPlayer.getStatData(stat, worldName, true);
		} else {
			data = sPlayer.getGlobalStatData(stat);
		}

		return data;
	}

	public int getTotalBlocksBroken(final String playerName,
			final String worldName) {
		if (!isAvailable())
			return 0;

		if (worldName != null) {
			return (int) Math.round(api.getTotalBlocksBroken(playerName,
					worldName));
		} else {
			return (int) Math.round(api.getTotalBlocksBroken(playerName));
		}
	}

	public int getTotalBlocksMoved(final String playerName, final int type,
			final String worldName) {
		if (!isAvailable())
			return 0;

		final String statName = "Move";

		final StatData stat = getStatType(statName, playerName, worldName);

		int value = 0;

		for (final Object[] vars : stat.getAllVariables()) {
			if ((Integer) vars[0] == type) {
				value += stat.getValue(vars);
			}
		}

		return value;
	}

	public int getTotalBlocksPlaced(final String playerName,
			final String worldName) {
		if (!isAvailable())
			return 0;

		if (worldName != null) {
			return (int) Math.round(api.getTotalBlocksPlaced(playerName,
					worldName));
		} else {
			return (int) Math.round(api.getTotalBlocksPlaced(playerName));
		}
	}

	public int getTotalMobsKilled(final String playerName,
			final String mobName, final String worldName) {
		if (!isAvailable())
			return 0;

		final String statName = "Kill";

		final StatData data = getStatType(statName, playerName, worldName);

		final EntityType mob = getEntityType(mobName);
		boolean checkEntityType = false;
		int value = 0;

		if (mob != null) {
			checkEntityType = true;
		}

		for (final Object[] vars : data.getAllVariables()) {

			// var 0 is mob type

			if (checkEntityType) {
				if (getEntityType(vars[0].toString()) != null
						&& getEntityType(vars[0].toString()).equals(mob)) {
					value += data.getValue(vars);
				}
			} else {
				value += data.getValue(vars);
			}
		}

		return value;
	}

	public int getTotalPlayTime(final String playerName, final String worldName) {
		if (!isAvailable())
			return 0;

		if (worldName != null) {
			return (int) Math.round(api.getPlaytime(playerName, worldName));
		} else {
			return (int) Math.round(api.getPlaytime(playerName));
		}
	}

	/* (non-Javadoc)
	 * @see me.armar.plugins.autorank.hooks.DependencyHandler#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return api != null;
	}

	/* (non-Javadoc)
	 * @see me.armar.plugins.autorank.hooks.DependencyHandler#isInstalled()
	 */
	@Override
	public boolean isInstalled() {
		final Plugin plugin = get();

		return plugin != null && plugin.isEnabled();
	}

	/* (non-Javadoc)
	 * @see me.armar.plugins.autorank.hooks.DependencyHandler#setup()
	 */
	@Override
	public boolean setup(final boolean verbose) {
		if (!isInstalled()) {
			if (verbose) {
				plugin.getLogger().info("Stats has not been found!");
			}
			return false;
		} else {
			final Main stats = (Main) get();

			api = stats.getAPI();

			if (api != null) {
				if (verbose) {
					plugin.getLogger().info(
							"Stats has been found and can be used!");
				}
				return true;
			} else {
				if (verbose) {
					plugin.getLogger().info(
							"Stats has been found but cannot be used!");
				}
				return false;
			}
		}
	}
}
