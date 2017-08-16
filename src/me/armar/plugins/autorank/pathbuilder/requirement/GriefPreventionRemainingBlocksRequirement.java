package me.armar.plugins.autorank.pathbuilder.requirement;

import me.armar.plugins.autorank.language.Lang;
import me.staartvin.plugins.pluginlibrary.Library;
import me.staartvin.plugins.pluginlibrary.hooks.GriefPreventionHook;
import org.bukkit.entity.Player;

public class GriefPreventionRemainingBlocksRequirement extends Requirement {

    private GriefPreventionHook handler = null;
    int remainingBlocks = -1;

    @Override
    public String getDescription() {
        return Lang.GRIEF_PREVENTION_REMAINING_BLOCKS_REQUIREMENT.getConfigValue(remainingBlocks);
    }

    @Override
    public String getProgress(final Player player) {
        final int level = handler.getNumberOfRemainingBlocks(player.getUniqueId());

        return level + "/" + remainingBlocks;
    }

    @Override
    public boolean meetsRequirement(final Player player) {

        if (!handler.isAvailable())
            return false;

        final int level = handler.getNumberOfRemainingBlocks(player.getUniqueId());

        return level >= remainingBlocks;
    }

    @Override
    public boolean setOptions(final String[] options) {

        handler = (GriefPreventionHook) this.getDependencyManager()
                .getLibraryHook(Library.GRIEFPREVENTION);

        if (options.length > 0) {
            remainingBlocks = Integer.parseInt(options[0]);
        }

        return remainingBlocks != -1 && handler != null;
    }
}
