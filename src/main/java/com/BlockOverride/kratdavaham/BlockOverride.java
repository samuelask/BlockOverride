package com.BlockOverride.kratdavaham;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.BlockOverride.kratdavaham.command.CommandBlockOverride;
import com.BlockOverride.kratdavaham.proxy.CommonProxy;
import com.BlockOverride.kratdavaham.util.reference;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = reference.MOD_ID, version = reference.VERSION, name = reference.NAME, acceptableRemoteVersions = reference.AceptRVer)
public class BlockOverride 
{
	private static Logger logger;
	private File configDir;
	
	@Instance
	public static BlockOverride instance;
	
	@SidedProxy(clientSide = reference.CLIENT_PROXY_CLASS, serverSide = reference.COMMON_PROXY_CLASS)
	public static CommonProxy proxy;
	
	public Map<String, float[]> blockOverrides = new HashMap<>();
	
	public final Map<Block, float[]> originalValues = new HashMap<>();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		configDir = event.getModConfigurationDirectory();
	    reloadConfig();
	}

	@EventHandler
	public void Init(FMLInitializationEvent event)
	{
		logger.info("Mod initlialised :" + reference.NAME);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	    for (Map.Entry<String, float[]> entry : blockOverrides.entrySet()) {
	        modifyBlock(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
	    }
	}
	
	public void modifyBlock(String id, float hardnessOverride, float resistanceOverride) {
	    Block block = Block.REGISTRY.getObject(new ResourceLocation(id));
	    if (block != null) {
	        // Save original values only once
	        if (!originalValues.containsKey(block)) {
	            IBlockState defaultState = block.getDefaultState();
	            float originalHardness = block.getBlockHardness(defaultState, null, BlockPos.ORIGIN);
	            float originalResistance = block.getExplosionResistance(null);
	            originalValues.put(block, new float[] { originalHardness, originalResistance });
	        }

	        block.setHardness(hardnessOverride);
	        block.setResistance(resistanceOverride);
	        logger.info("Modified block: " + id + " | Hardness: " + hardnessOverride + " | Resistance: " + resistanceOverride);
	    } else {
	        logger.warn("Block not found: " + id);
	    }
	}
	
	public void removeBlockFromConfig(String blockId) {
	    File configFile = new File(configDir, "blockoverride.cfg");
	    Configuration config = new Configuration(configFile);
	    config.load();

	    Property prop = config.get("settings", "blockSettings", new String[0]);
	    List<String> entries = new ArrayList<>(Arrays.asList(prop.getStringList()));

	    entries.removeIf(line -> line.startsWith(blockId + "="));

	    prop.set(entries.toArray(new String[0]));
	    config.save();
	}
	
	public void addOrUpdateBlockInConfig(String blockId, float hardness, float resistance) {
	    File configFile = new File(configDir, "blockoverride.cfg");
	    Configuration config = new Configuration(configFile);
	    config.load();

	    Property prop = config.get("settings", "blockSettings", new String[0]);
	    List<String> entries = new ArrayList<>(Arrays.asList(prop.getStringList()));

	    // Remove existing entry for block
	    entries.removeIf(line -> line.startsWith(blockId + "="));

	    // Add new entry
	    entries.add(blockId + "=" + hardness + "," + resistance);

	    prop.set(entries.toArray(new String[0]));

	    config.save();

	    blockOverrides.put(blockId, new float[]{hardness, resistance});
	}
	
	public void reloadConfig() {
		File configFile = new File(configDir, "blockoverride.cfg");
	    Configuration config = new Configuration(configFile);
	    config.load();

	    if (config.hasCategory("blocks")) {
	        logger.info("Migrating config category 'blocks' → 'settings'...");

	        String[] oldBlockSettings = config.get("blocks", "blockSettings", new String[0]).getStringList();

	        config.get("settings", "blockSettings", oldBlockSettings,
	            "List of blocks to override.\n" +
	            "Format: modid:blockname=hardness,resistance\n" +
	            "Example: minecraft:obsidian=5.0,1200.0\n" +
	            "Note: Bedrock has a hardness of -1.0 by default, and resistance of 3600000.0\n" +
	            "Setting hardness to -1.0 may make blocks unbreakable (like bedrock)."
	        ).set(oldBlockSettings);

	        config.removeCategory(config.getCategory("blocks"));
	        logger.info("Migration complete. Removed legacy category 'blocks'.");
	    }
	    
	    blockOverrides.clear(); // Clear old entries

	    String currentVersion = reference.VERSION;
	    String configVersion = config.get("general", "configVersion", "1.0.0",
	    	    "DO NOT EDIT. Used to track config version.").getString();
	    
	    if (!configVersion.equals(currentVersion)) {
	        logger.warn("Config version mismatch! Found: " + configVersion + ", expected: " + currentVersion);
	        Property versionProp = config.get("general", "configVersion", currentVersion);
	        versionProp.set(currentVersion);
	        versionProp.setComment("DO NOT EDIT. Used to track config version.");
	    }
	    
	    String[] entries = config.get("settings", "blockSettings", new String[0],
	    	    "List of blocks to override.\n" +
	    	    "Format: modid:blockname=hardness,resistance\n" +
	    	    "Example: minecraft:obsidian=5.0,1200.0\n" +
	    	    "Note: Bedrock has a hardness of -1.0 by default, and resistance of 3600000.0\n" +
	    	    "Setting hardness to -1.0 may make blocks unbreakable (like bedrock)."
	    	).getStringList();

	    for (String entry : entries) {
	        try {
	            String[] parts = entry.split("=");
	            if (parts.length != 2) continue;

	            String blockId = parts[0].trim();
	            String[] values = parts[1].split(",");
	            if (values.length != 2) continue;

	            float hardness = Float.parseFloat(values[0].trim());
	            float resistance = Float.parseFloat(values[1].trim());

	            blockOverrides.put(blockId, new float[]{hardness, resistance});
	            
	            logger.info("Reloaded block: " + blockId + " → " + hardness + ", " + resistance);
	            
	        } catch (Exception e) {
	            logger.error("Invalid config line: " + entry, e);
	        }
	    }

	    if (config.hasChanged()) config.save();

	    for (Map.Entry<String, float[]> entry : blockOverrides.entrySet()) {
	        modifyBlock(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
	    }
	    
	    for (Block block : originalValues.keySet()) {
	        ResourceLocation id = Block.REGISTRY.getNameForObject(block);
	        if (!blockOverrides.containsKey(id.toString())) {
	            float[] defaults = originalValues.get(block);
	            block.setHardness(defaults[0]);
	            block.setResistance(defaults[1]);
	            logger.info("Reset block: " + id + " to original values: " + defaults[0] + ", " + defaults[1]);
	        }
	    }
	    
	}
	
	@EventHandler
	public void serverInit(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandBlockOverride());
	}
}
