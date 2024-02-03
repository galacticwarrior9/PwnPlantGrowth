package com.pwn9.PwnPlantGrowth;

import java.util.ArrayList;
import java.util.List;

import com.pwn9.PwnPlantGrowth.integration.ExoticGardenIntegration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener
{

	public PlayerListener(PwnPlantGrowth plugin) 
	{
	    plugin.getServer().getPluginManager().registerEvents(this, plugin);    
	}	
	
	
	static Calculate getCalcs(Boolean report, List<List<String>> specialBlocks, String thisBlock, String curBiome, Boolean isDark)
	{
		return new Calculate(report, specialBlocks, thisBlock, curBiome, isDark);
	}
	
	// retrieve list of special blocks
	public List<List<String>> specialBlockList(Location origin)
	{
		List<String> fBlocksFound = new ArrayList<String>();
		List<String> wkBlocksFound = new ArrayList<String>();
		List<String> uvBlocksFound = new ArrayList<String>();;

		List<List<String>> result = new ArrayList<List<String>>();
		
		// Check for fertilizer blocks
		if (PwnPlantGrowth.fenabled) 
		{
			for (int fx = -(PwnPlantGrowth.fradius); fx <= PwnPlantGrowth.fradius; fx++) 
			{
	            for (int fy = -(PwnPlantGrowth.fradius); fy <= PwnPlantGrowth.fradius; fy++) 
	            {
	               for (int fz = -(PwnPlantGrowth.fradius); fz <= PwnPlantGrowth.fradius; fz++) 
	               {
	            	   fBlocksFound.add(String.valueOf(origin.getBlock().getRelative(fx, fy, fz).getType()));
	               }
	            }
	        }
		}		
		
		// Check for weed killer blocks
		if (PwnPlantGrowth.wkenabled)
		{
			for (int wx = -(PwnPlantGrowth.wkradius); wx <= PwnPlantGrowth.wkradius; wx++) 
			{
	            for (int wy = -(PwnPlantGrowth.wkradius); wy <= PwnPlantGrowth.wkradius; wy++) 
	            {
	               for (int wz = -(PwnPlantGrowth.wkradius); wz <= PwnPlantGrowth.wkradius; wz++) 
	               {
	            	   wkBlocksFound.add(String.valueOf(origin.getBlock().getRelative(wx, wy, wz).getType()));
	               }
	            }
	        }
		}
		
		// Check for uv blocks
		if (PwnPlantGrowth.uvenabled)
		{
			for (int ux = -(PwnPlantGrowth.uvradius); ux <= PwnPlantGrowth.uvradius; ux++) 
			{
	            for (int uy = -(PwnPlantGrowth.uvradius); uy <= PwnPlantGrowth.uvradius; uy++) 
	            {
	               for (int uz = -(PwnPlantGrowth.uvradius); uz <= PwnPlantGrowth.uvradius; uz++) 
	               {
	            	   uvBlocksFound.add(String.valueOf(origin.getBlock().getRelative(ux, uy, uz).getType()));
	               }
	            }
	        }
		}		
		
		result.add(fBlocksFound);
		result.add(wkBlocksFound);
		result.add(uvBlocksFound);

		return result;
	}	
	//TODO: BlockFertilizeEvent for bonemeal 
	
	// https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/player/PlayerInteractEvent.html
	// This will check for block clicks with growable items and report if they can grow well here or not - feature...
	@EventHandler(ignoreCancelled = true)
	public void checkBlockClick(PlayerInteractEvent e) 
	{
		
		World world = e.getPlayer().getLocation().getWorld();
		
		// if the plugin is enabled and the setting enabled, otherwise bail
		if (!PwnPlantGrowth.isEnabledIn(world.getName())) return;
		if (!PwnPlantGrowth.reportGrowth) return;
		
		// bail if no item in hand
		if (!e.hasItem()) return;
		
		// get action
		Action action = e.getAction();
		
		if (action == Action.LEFT_CLICK_BLOCK) 
		{
			
			Player p = e.getPlayer();

			Block block = e.getClickedBlock();

			if (block.getType() == Material.FARMLAND || block.getType() == Material.DIRT || block.getType() == Material.GRASS_BLOCK || block.getType() == Material.JUNGLE_LOG || block.getType() == Material.SAND || block.getType() == Material.SOUL_SAND) 
			{
				Material m = e.getMaterial();
				String blockName;
				String sfItemId = null;
				if (PwnPlantGrowth.getInstance().isExoticGardenEnabled()) {
					sfItemId = ExoticGardenIntegration.getSfItemID(e.getItem());
					blockName = (sfItemId == null) ? e.getMaterial().name() : sfItemId;
				} else {
					blockName = m.name();
				}

				String a = "";
				boolean isDark = false;
				// need to get biome data

				String curBiome = PwnPlantGrowth.getBiomeName(e);

				// Get the current natural light level
				int lightLevel = block.getLightFromSky();

				if(PwnPlantGrowth.plantTypes.contains(blockName) || sfItemId != null) {
					
					
					// If the light level is lower than configured threshold and the plant is NOT exempt from dark grow, set this transaction to isDark = true
					if ((PwnPlantGrowth.naturalLight > lightLevel) && (!PwnPlantGrowth.canDarkGrow(blockName)))
					{
						isDark = true;
					}					
					
					//do a calculate here
					Calculate cal = getCalcs(true, specialBlockList(block.getLocation()), blockName, curBiome, isDark);
					a += cal.doLog;

					String msg = PwnPlantGrowth.msgFormat + a;
					p.sendRichMessage(msg);
					
					// annoying unable to test in create without breaking block so cancel even in creative only
					if (p.getGameMode() == GameMode.CREATIVE) {
						e.setCancelled(true);
					}
					
				}
				else if (PwnPlantGrowth.seedTypes.contains(blockName)) {
					Location blockLoc = block.getLocation();
					if (m == Material.BEETROOT_SEEDS || m == Material.BEETROOT) {
						Calculate cal = getCalcs(true, specialBlockList(blockLoc), "BEETROOTS", curBiome, isDark);
						a += cal.doLog;
					}
					else if (m == Material.CARROT) {
						Calculate cal = getCalcs(true, specialBlockList(blockLoc), "CARROTS", curBiome, isDark);
						a += cal.doLog;						
					}						
					else if (m == Material.COCOA_BEANS) {
						Calculate cal = getCalcs(true, specialBlockList(blockLoc), "COCOA", curBiome, isDark);
						a += cal.doLog;						
					}	
					else if (m == Material.MELON_SEEDS) {
						Calculate cal = getCalcs(true, specialBlockList(blockLoc), "MELON_STEM", curBiome, isDark);
						a += cal.doLog;		
						Calculate cal2 = getCalcs(true, specialBlockList(blockLoc), "MELON", curBiome, isDark);
						a += " " + cal2.doLog;							
					}
					else if (m == Material.POTATO) {
						Calculate cal = getCalcs(true, specialBlockList(blockLoc), "POTATOES", curBiome, isDark);
						a += cal.doLog;						
					}					
					else if (m == Material.PUMPKIN_SEEDS) {
						Calculate cal = getCalcs(true, specialBlockList(blockLoc), "PUMPKIN_STEM", curBiome, isDark);
						a += cal.doLog;		
						Calculate cal2 = getCalcs(true, specialBlockList(blockLoc), "PUMPKIN", curBiome, isDark);
						a += " " + cal2.doLog;						
					}
					else if (m == Material.SWEET_BERRIES) {		
						Calculate cal = getCalcs(true, specialBlockList(blockLoc), "SWEET_BERRY_BUSH", curBiome, isDark);
						a += cal.doLog;						
					}
					else if (m == Material.WHEAT_SEEDS) {	
						Calculate cal = getCalcs(true, specialBlockList(blockLoc), "WHEAT", curBiome, isDark);
						a += cal.doLog;						
					}
					
					String msg = PwnPlantGrowth.msgFormat + a;
					p.sendRichMessage(msg);
					
					// annoying unable to test in creative without breaking block so cancel event in creative only
					if (p.getGameMode() == GameMode.CREATIVE) {
						e.setCancelled(true);
					}					
				}
			}
		}
	}	
}