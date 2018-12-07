//
// SmartCart copyright 2015 Ian Clark
//
// Distributed under the MIT License
// http://opensource.org/licenses/MIT
//
package net.f85.SmartCart;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

public class SmartCart extends JavaPlugin {


    private static SmartCartListener listener;
    static SmartCartUtil util;
    static FileConfiguration config;
    static Logger logger;


    @Override
    public void onEnable() {
        boolean success = true;
        getLogger().info("Starting up SmartCart");
        //plugin = this;

        // Generate the default config file
        this.saveDefaultConfig();

        config = this.getConfig();

        listener = new SmartCartListener(this);
        util = new SmartCartUtil(this);
        logger = getLogger();
        getLogger().info("Loading materials");
        try {
            BlockMaterial.ElevatorBlock.setMaterial(config.getString("elevator_block_material"));
            BlockMaterial.IntersectionBlock.setMaterial(config.getString("intersection_block_material"));
            BlockMaterial.KillBlock.setMaterial(config.getString("kill_block_material"));
            BlockMaterial.SlowBlock.setMaterial(config.getString("slow_block_material"));
            BlockMaterial.SpawnBlock.setMaterial(config.getString("spawn_block_material"));
            getLogger().info("done");
        }
        catch(Exception e) {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                sb.append(element).append("\n");
            }
            getLogger().severe("Error loading materials\n" + sb.toString());
            success = false;
        }
        // Set up command executor
        //commandExecutor = new CommandExecutorUtil(this);
        getLogger().info("Loading commands");
        this.getCommand("sc").setExecutor(new CommandExecutorUtil(this));
        this.getCommand("scSetTag").setExecutor(new CommandSetTag());
        getLogger().info("done");
        getLogger().info(success ? "Successfully activated SmartCart" : "Error loading SmartCart. Check the config");
    }


    @Override
    public void onDisable() {
        getLogger().info("Successfully deactivated SmartCart");
    }
}
