package com.pwn9.PwnPlantGrowth.integration;

import com.pwn9.PwnPlantGrowth.PwnPlantGrowth;
import com.pwn9.PwnPlantGrowth.StructureGrowListener;
import io.github.thebusybiscuit.exoticgarden.events.ExoticGardenPlantGrowEvent;
import io.github.thebusybiscuit.exoticgarden.events.ExoticGardenStructureGrowEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExoticGardenIntegration implements Listener {
    public ExoticGardenIntegration(PwnPlantGrowth plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onExoticGardenGrow(ExoticGardenStructureGrowEvent event) {
        StructureGrowListener.structureGrow(event, event.getLocation(), event.getItemId(),
                event.getItemId(), event.getStructureGrowEvent().isFromBonemeal());
    }

    @EventHandler(ignoreCancelled = true)
    public void onExoticGardenPlantGrow(ExoticGardenPlantGrowEvent event) {
        StructureGrowListener.structureGrow(event, event.getLocation(), event.getItemId(), event.getItemId(), false);
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
        if (!Tag.SAPLINGS.isTagged(item.getType()))
            return null;
        SlimefunItem sfItem = SlimefunItem.getByItem(item);
        return (sfItem == null) ? null : sfItem.getId();
    }
}
