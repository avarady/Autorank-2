package me.armar.plugins.autorank.backup;

import com.google.common.io.Files;
import me.armar.plugins.autorank.Autorank;
import me.armar.plugins.autorank.data.flatfile.FlatFileManager;
import me.armar.plugins.autorank.data.flatfile.FlatFileManager.TimeType;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is used to backup several data files of Autorank.
 *
 * @author Staartvin
 */
public class BackupManager {

    private final static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private final BackupDataManager backupDataManager;
    private final Autorank plugin;

    public BackupManager(final Autorank plugin) {
        this.plugin = plugin;
        backupDataManager = new BackupDataManager(plugin);

        backupDataManager.createNewFile();
    }

    /**
     * Backup a file to a folder.
     *
     * @param sourceFileName Path of file to backup
     * @param storePath      Path to backup the file to, can be null
     */
    public void backupFile(final String sourceFileName, final String storePath) {
        // CAN ONLY COPY YML
        final String folderPath = plugin.getDataFolder().getAbsolutePath() + File.separator;
        final File sourceFile = new File(folderPath + sourceFileName);

        File copyFile = null;

        String dateFormatForFiles = dateFormat.format(new Date());

        if (storePath == null) {
            copyFile = new File(
                    folderPath + sourceFileName.replace(".yml", "") + "-backup-" + dateFormatForFiles + ".yml");
        } else {
            copyFile = new File(storePath.replace(".yml", "") + "-backup-" + dateFormatForFiles + ".yml");
        }

        // Create folder if it doesn't exist.
        copyFile.getParentFile().mkdirs();

        try {
            Files.copy(sourceFile, copyFile);
            plugin.debugMessage("Made backup of '" + sourceFileName + "'!");
        } catch (final IOException e) {
            plugin.getServer().getConsoleSender().sendMessage(
                    "[Autorank] " + ChatColor.RED + "Was not able to back up " + sourceFileName + ", trying again in " +
                            "24 hours.");
        }

    }

    /**
     * Backup data files of either playerdata or regular time data.
     * It will notify the backup manager that it does not have to backup the files again within 24 hours.
     * @param dataType Type of data to backup (data or playerdata).
     */
    public void backupDataFolders(String dataType) {

        if (dataType.equalsIgnoreCase("data")) {
            plugin.debugMessage(ChatColor.GREEN + "Making a backup of all data files!");

            // For every type of time data file
            for (TimeType type : TimeType.values()) {

                // Get the path to the file and back it up!
                String path = FlatFileManager.dataTypePaths.get(type);

                plugin.getBackupManager().backupFile(path, plugin.getDataFolder().getAbsolutePath()
                        + File.separator + "backups" + File.separator + path.replace("/data/", ""));
            }

            // Update latest backup time so backup manager does not backup again within 24 hours.
            backupDataManager.getConfig().set("data", System.currentTimeMillis());

        } else if (dataType.equalsIgnoreCase("playerdata")) {
            plugin.debugMessage(ChatColor.GREEN + "Making a backup of PlayerData file!");

            // Before running, backup stuff.
            plugin.getBackupManager().backupFile("/playerdata/PlayerData.yml",
                    plugin.getDataFolder().getAbsolutePath() + File.separator + "backups" + File.separator
                            + "PlayerData.yml");

            // Update latest backup time so backup manager does not backup again within 24 hours.
            backupDataManager.getConfig().set("playerdata", System.currentTimeMillis());
        }
    }

    /**
     * Start the internal backup system of Autorank. This will make a backup of
     * each data file every 24 hours.
     */
    public void startBackupSystem() {
        // Makes a backup every day
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {

                // Older than a day
                if ((System.currentTimeMillis() - backupDataManager.getLatestBackup("data")) > 1000 * 60 * 60 * 24
                    /* One day (in ms)*/) {
                    backupDataFolders("data");
                } else {
                    plugin.debugMessage("Data files did not have to be backed up yet.");
                }

                // Older than a day
                if ((System.currentTimeMillis() - backupDataManager.getLatestBackup("playerdata")) > 1000 * 60 * 60 * 24
                    /* One day (in ms)*/) {
                    backupDataFolders("playerdata");
                } else {
                    plugin.debugMessage("Playerdata files did not have to be backed up yet.");
                }

                // Save config
                backupDataManager.saveConfig();
            }
        }, 0, 20 * 60 * 60 * 24 /* One day (in ticks) */);
    }

}
