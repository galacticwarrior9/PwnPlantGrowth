package com.pwn9.PwnPlantGrowth.integration;

import com.pwn9.PwnPlantGrowth.Calculate;
import com.pwn9.PwnPlantGrowth.PwnPlantGrowth;
import com.pwn9.PwnPlantGrowth.StructureGrowListener;
import io.github.thebusybiscuit.exoticgarden.events.ExoticGardenCalculateGrowthEvent;
import io.github.thebusybiscuit.exoticgarden.events.ExoticGardenPlantGrowEvent;
import io.github.thebusybiscuit.exoticgarden.events.ExoticGardenStructureGrowEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.pwn9.PwnPlantGrowth.StructureGrowListener.getCalcs;
import static com.pwn9.PwnPlantGrowth.StructureGrowListener.specialBlockList;

public class ExoticGardenIntegration implements Listener {
    public ExoticGardenIntegration(PwnPlantGrowth plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onExoticGardenGrow(ExoticGardenStructureGrowEvent event) {
        event.setCancelled(true);
        //StructureGrowListener.structureGrow(event, event.getLocation(), event.getItemId(), event.getItemId(), event.getStructureGrowEvent().isFromBonemeal());
    }

    @EventHandler(ignoreCancelled = true)
    public void onExoticGardenPlantGrow(ExoticGardenPlantGrowEvent event) {
        StructureGrowListener.structureGrow(event, event.getLocation(), event.getItemId(), event.getItemId(), false);
    }

    @EventHandler
    public void onExoticGardenCalculateGrowth(ExoticGardenCalculateGrowthEvent event) {
        Block block = event.getLocation().getBlock();
        boolean isDark = PwnPlantGrowth.naturalLight > block.getLightFromSky() && !PwnPlantGrowth.canDarkGrow(event.getPlantId());
        Calculate cal = getCalcs(specialBlockList(event.getLocation()), event.getPlantId(), block.getBiome().name(), isDark);
        event.setGrowthChance(cal.curGrowth);
        event.setDeathChance(cal.curDeath);
    }

    public static boolean isSlimefunBlock(@NotNull Block block) {
        return BlockStorage.hasBlockInfo(block);
    }

    @Nullable
    public static String getSlimefunBlock(@NotNull Block block) {
        return BlockStorage.checkID(block);
    }

    @Nullable
    public static String getSfItemID(@NotNull ItemStack item) {
        // Restrict to ExoticGarden items
        if (item.getType() != Material.PAPER && item.getType() != Material.OAK_SAPLING)
            return null;
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        return (sfItem == null) ? null : sfItem.getId();
    }
}
