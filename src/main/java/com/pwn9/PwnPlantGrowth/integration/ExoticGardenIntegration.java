package com.pwn9.PwnPlantGrowth.integration;

import com.pwn9.PwnPlantGrowth.PwnPlantGrowth;
import com.pwn9.PwnPlantGrowth.StructureGrowListener;
import io.github.thebusybiscuit.exoticgarden.events.ExoticGardenGrowEvent;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExoticGardenIntegration implements Listener {
    public ExoticGardenIntegration(PwnPlantGrowth plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onExoticGardenGrow(ExoticGardenGrowEvent event) {
        StructureGrowListener.structureGrow(event, event.getLocation(), event.getItemId(),
                event.getItemId(), event.getStructureGrowEvent().isFromBonemeal());
    }

    public static boolean isSlimefunBlock(@NotNull Block block) {
        return BlockStorage.hasBlockInfo(block);
    }

    @Nullable
    public static String getSlimefunBlock(@NotNull Block block) {
        return BlockStorage.checkID(block);
    }
}
