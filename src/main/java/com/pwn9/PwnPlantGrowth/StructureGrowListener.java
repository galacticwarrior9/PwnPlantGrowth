package com.pwn9.PwnPlantGrowth;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.ArrayList;
import java.util.List;

public class StructureGrowListener implements Listener 
{

	private final PwnPlantGrowth plugin;
	
	public StructureGrowListener(PwnPlantGrowth plugin) 
	{
	    plugin.getServer().getPluginManager().registerEvents(this, plugin);    
	    this.plugin = plugin;
	}
	
	public static Calculate getCalcs(List<List<String>> specialBlocks, String thisBlock, String curBiome, Boolean isDark)
	{
		return new Calculate(specialBlocks, thisBlock, curBiome, isDark);
	}

	// retrieve list of special blocks
	public static List<List<String>> specialBlockList(Location location)
	{
		List<String> fBlocksFound = new ArrayList<String>();
		List<String> wkBlocksFound = new ArrayList<String>();
		List<String> uvBlocksFound = new ArrayList<String>();;

		List<List<String>> result = new ArrayList<List<String>>();
		
		if (PwnPlantGrowth.fenabled) 
		{
			for (int x = -(PwnPlantGrowth.fradius); x <= PwnPlantGrowth.fradius; x++) 
			{
	            for (int y = -(PwnPlantGrowth.fradius); y <= PwnPlantGrowth.fradius; y++) 
	            {
	               for (int z = -(PwnPlantGrowth.fradius); z <= PwnPlantGrowth.fradius; z++) 
	               {
					   if (location.isChunkLoaded()) {
						   fBlocksFound.add(String.valueOf(location.getBlock().getRelative(x, y, z).getType()));
					   }
	               }
	            }
	        }
		}		
		
		if (PwnPlantGrowth.wkenabled)
		{
			for (int x = -(PwnPlantGrowth.wkradius); x <= PwnPlantGrowth.wkradius; x++) 
			{
	            for (int y = -(PwnPlantGrowth.wkradius); y <= PwnPlantGrowth.wkradius; y++) 
	            {
	               for (int z = -(PwnPlantGrowth.wkradius); z <= PwnPlantGrowth.wkradius; z++) 
	               {
					   if (location.isChunkLoaded()) {
						   wkBlocksFound.add(String.valueOf(location.getBlock().getRelative(x, y, z).getType()));
					   }
	               }
	            }
	        }
		}		
		
		// Check for uv blocks
		if (PwnPlantGrowth.uvenabled)
		{
			for (int x = -(PwnPlantGrowth.uvradius); x <= PwnPlantGrowth.uvradius; x++) 
			{
	            for (int y = -(PwnPlantGrowth.uvradius); y <= PwnPlantGrowth.uvradius; y++) 
	            {
	               for (int z = -(PwnPlantGrowth.uvradius); z <= PwnPlantGrowth.uvradius; z++) 
	               {
					   if (location.isChunkLoaded()) {
						   uvBlocksFound.add(String.valueOf(location.getBlock().getRelative(x, y, z).getType()));
					   }
	               }
	            }
	        }
		}	
		
		result.add(fBlocksFound);
		result.add(wkBlocksFound);
		result.add(uvBlocksFound);

		return result;
	}	
	
	// Structure Growth eg: trees
	// Run at HIGHEST: ExoticGarden listens to this event at HIGH
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onStructureGrow(StructureGrowEvent event) {
		structureGrow(event, event.getLocation(), event.getSpecies().toString(),
				event.getLocation().getBlock().getType().toString(), event.isFromBonemeal()
		);
	}

	public static void structureGrow(Cancellable event, Location location, String curBlock, String speciesName, boolean isBoneMeal) {
		FileConfiguration config = PwnPlantGrowth.getInstance().getConfig();

		// Enabled in world?
		World world = location.getWorld();
		if (!PwnPlantGrowth.isEnabledIn(world.getName())) return;

		// Get current block type and make a string for comparison later
		String eventBlock = speciesName;
		
		// Get event coords
		String coords = String.valueOf(location);
		
		// Get current biome and make a string for comparison later
		String curBiome = PwnPlantGrowth.getBiomeName(location.getBlock());
		
		if ((PwnPlantGrowth.logEnabled) && (PwnPlantGrowth.logTreeEnabled) && (PwnPlantGrowth.logVerbose)) 
		{
			PwnPlantGrowth.logToFile("Structure Event for: " + curBlock + " - In biome: " + curBiome, "StructureGrow");
			PwnPlantGrowth.logToFile("Species: " + eventBlock, "StructureGrow");
		}		
		
		//TODO: check for bonemeal usage on structure growth and handle it
		if (isBoneMeal) {
			// bonemeal triggered this event, what should we do with it?
			if (!(PwnPlantGrowth.limitBonemeal)) 
			{
				if ((PwnPlantGrowth.logEnabled) && (PwnPlantGrowth.logTreeEnabled)) 
				{
					PwnPlantGrowth.logToFile("Bonemeal was used on " + curBlock, "TreeGrow");
				}
				return;
			}
		}
		
		// Is anything set for this block in the config, if not, abort
		if (!(config.isSet(curBlock)))
		{
			PwnPlantGrowth.logToFile("No tree configuration set in config for: " + curBlock);
			return;
		}
				
		// Setup boolean to see if event is in defined natural light or not
		Boolean isDark = false;
		
		// Get the current natural light level
		int lightLevel = location.getBlock().getLightFromSky();
		
		// If the light level is lower than configured threshold and the plant is NOT exempt from dark grow, set this transaction to isDark = true
		if ((PwnPlantGrowth.naturalLight > lightLevel) && (!PwnPlantGrowth.canDarkGrow(speciesName)))
		{
			isDark = true;
		}
		
		String toLog = "";
		
		if (PwnPlantGrowth.logCoords) 
		{
			toLog += coords + ": Growing: " + curBlock;
		}
		else {
			toLog += "Growing: " + curBlock;
		}

		Calculate cal = getCalcs(specialBlockList(location), curBlock, curBiome, isDark);
		toLog += cal.doLog;
		event.setCancelled(cal.isCancelled);
		// CCNet - Block#isPassable is a quick hack to ensure we don't replace the block if it has changed (e.g. due to Harvester moving onto the block)
		if (cal.replacement != null && location.getBlock().isPassable()) {
			// CCNet - applying physics can break the dead bushes upon placement
			location.getBlock().setType(cal.replacement, false);
		}
		

		// log it
		if ((PwnPlantGrowth.logEnabled) && (PwnPlantGrowth.logTreeEnabled)) 
		{	
			PwnPlantGrowth.logToFile(toLog, "StructureGrow");
		}
		return;
	}
	
}