package com.pwn9.PwnPlantGrowth;

import java.util.List;

import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class Calculate {
	public Boolean isCancelled;
	public Material replacement;
	public String doLog;
	public int curDeath;
	public int curGrowth;

	Calculate(List<List<String>> specialBlocks, String thisBlock, String curBiome, Boolean isDark)
	{
		isCancelled = false;
		String toLog = "";
		String frontLog = ", Biome: " + curBiome + ", Dark: " + isDark.toString() + ", ";
		String darkLog = "Dark Settings: {";
		String groupLog = "Settings: {";

		// bool to catch if the biome is never declared in any config, therefor a bad biome and should not grow
		// not true - i'm an idiot, the null biome is ok to have actually and means default growth
		boolean noBiome = true;

		FileConfiguration config = PwnPlantGrowth.getInstance().getConfig();
		List<String> blockBiomeSettings = config.getStringList(thisBlock + ".Biome");

		curGrowth = config.getInt(thisBlock+".Growth");
		frontLog += "Default Growth: " + curGrowth + ", ";
		curDeath = config.getInt(thisBlock+".Death");
		frontLog += "Default Death: " + curDeath + ", ";


		if ((config.isSet(thisBlock+".BiomeGroup")) || (blockBiomeSettings.isEmpty()) || (blockBiomeSettings.contains(curBiome)))
		{
			// check the area to find if any of the special blocks are found
			List<String> fBlocksFound = specialBlocks.get(0);
			List<String> wkBlocksFound = specialBlocks.get(1);
			List<String> uvBlocksFound = specialBlocks.get(2);



			// check the biome group settings if they are set.
			if (config.isSet(thisBlock+".BiomeGroup"))
			{

				// create list from the config setting
				List<?> groupList = config.getList(thisBlock+".BiomeGroup");

				groupLog += "BiomeGroup: " + groupList.toString() + ", ";

				// iterate through list and see if any of that list matches curBiome
				boolean matches = false;
				for (int i = 0; i < groupList.size(); i++)
				{

					// check the biomegroup for this named group
					if ((config.getList("BiomeGroup."+groupList.get(i)) != null) && (config.getList("BiomeGroup."+groupList.get(i)).contains(curBiome)))
					{
						matches = true;
						noBiome = false;
						groupLog += "Matches: " + groupList.get(i) + ", ";

						// reference the configs now to see if the config settings are set!
						if (config.isSet(thisBlock+"."+groupList.get(i)+".Growth"))
						{
							curGrowth = config.getInt(thisBlock+"."+groupList.get(i)+".Growth");
							groupLog += "New Growth: " + curGrowth + ", ";
						}

						if (config.isSet(thisBlock+"."+groupList.get(i)+".Death"))
						{
							curDeath = config.getInt(thisBlock+"."+groupList.get(i)+".Death");
							groupLog += "New Death: " + curDeath + ", ";
						}
					}
				}
				if (!matches) {
					groupLog += "Matches: NULL, ";
				}
			}
			else {
				groupLog += "BiomeGroup: NULL,  ";
			}

			groupLog += "Specific Settings: {";

			// BIOME SETTINGS - if per biome is set, it overrides a biome group
			if (blockBiomeSettings.contains(curBiome)) {
				noBiome = false;
				// override with individual settings
				if (config.isSet(thisBlock+"."+curBiome+".Growth"))
				{
					curGrowth = config.getInt(thisBlock+"."+curBiome+".Growth");
					groupLog += "Growth for " + curBiome + ": " + curGrowth + ", ";
				}

				if (config.isSet(thisBlock+"."+curBiome+".Death"))
				{
					curDeath = config.getInt(thisBlock+"."+curBiome+".Death");
					groupLog += "Death for " + curBiome + ": " + curDeath + ", ";
				}
			}




			// if there is fertilizer, grow this plant at the fertilizer rate - default 100%
			// TODO: should fertilizer override dark settings or not - i think not for now
			if (fBlocksFound.contains(PwnPlantGrowth.fertilizer))
			{
				groupLog += PwnPlantGrowth.fertFound;
				// set the current growth to the fertilizer rate
				curGrowth = PwnPlantGrowth.frate;
			}
			groupLog += "}}, ";




			// See if there are special settings for dark growth
			if (isDark)
			{
				// If uv is enabled and found, isDark remains false.
				if (uvBlocksFound.contains(PwnPlantGrowth.uv))
				{
					darkLog += PwnPlantGrowth.uvFound;
				}
				else
				{
					// ISDARK: default isDark config rates (if exist)
					if (config.isSet(thisBlock+".GrowthDark"))
					{
						curGrowth = config.getInt(thisBlock+".GrowthDark");
						darkLog += "Growth: " + curGrowth + ", ";
					}

					if (config.isSet(thisBlock+".DeathDark"))
					{
						curDeath = config.getInt(thisBlock+".DeathDark");
						darkLog += "Death: " + curDeath + ", ";
					}



					// ISDARK: override default values with biome group values
					if (config.isSet(thisBlock+".BiomeGroup"))
					{

						// create list from the config setting
						List<?> groupList = config.getList(thisBlock+".BiomeGroup");

						darkLog += "BiomeGroup: " + groupList.toString() + ", ";

						// iterate through list and see if any of that list matches curBiome
						boolean matches = false;
						for (int i = 0; i < groupList.size(); i++) {

							// check the biomegroup for this named group
							if  ((config.getList("BiomeGroup."+groupList.get(i)) != null) && (config.getList("BiomeGroup."+groupList.get(i)).contains(curBiome)))
							{

								matches = true;
								noBiome = false;
								darkLog += "Matching: " + groupList.get(i) + ", ";

								// reference the configs now to see if the config settings are set!
								if (config.isSet(thisBlock+"."+groupList.get(i)+".GrowthDark"))
								{
									curGrowth = config.getInt(thisBlock+"."+groupList.get(i)+".GrowthDark");
									darkLog += "New Growth: " + curGrowth + ", ";
								}

								if (config.isSet(thisBlock+"."+groupList.get(i)+".DeathDark"))
								{
									curDeath = config.getInt(thisBlock+"."+groupList.get(i)+".DeathDark");
									darkLog += "New Death: " + curDeath + ", ";
								}
							}
						}
						if (!matches) {
							darkLog += "Matches: NULL, ";
						}
					}
					else {
						darkLog += "BiomeGroup: NULL, ";
					}

					darkLog += "Specific Settings: {";

					// ISDARK: per biome isDark rates (if exist) override biomegroup rates
					if (blockBiomeSettings.contains(curBiome)) {
						noBiome = false;
						if (config.isSet(thisBlock+"."+curBiome+".GrowthDark"))
						{
							curGrowth = config.getInt(thisBlock+"."+curBiome+".GrowthDark");
							darkLog += "Growth for " + curBiome + ": " + curGrowth + ", ";
						}

						if (config.isSet(thisBlock+"."+curBiome+".DeathDark"))
						{
							curDeath = config.getInt(thisBlock+"."+curBiome+".DeathDark");
							darkLog += "Death for " + curBiome + ": " + curDeath + ", ";
						}
					}

					darkLog += "}}, ";
				}
			}

			// if BIOME was left empty, BIOME: [] it was intentional.. and this means use default growth rate.
			if ((blockBiomeSettings.isEmpty())) {
				noBiome = false;
			}


			// check config again - nobiome means the plant had no config available anywhere not even empty/default
			if (noBiome)
			{
				isCancelled = true;
				toLog += "RESULT: {Failed Growth: Bad Biome}";
				// chance of death
				if (PwnPlantGrowth.random(curDeath))
				{
					// TODO: make these configurable
					if (thisBlock == "COCOA") {
						replacement = Material.VINE;
					}
					else if (thisBlock == "KELP") {
						replacement = Material.SEAGRASS;
					}
					else {
						replacement = Material.DEAD_BUSH;
					}
					toLog += " {Plant Died, Rate: " + curDeath + "}";
				}
			}
			// Run the chance for growth here...
			else if (!(PwnPlantGrowth.random(curGrowth)))
			{
				isCancelled = true;
				toLog += "RESULT: {Failed Growth, Rate: " + curGrowth + "} ";

				if (wkBlocksFound.contains(PwnPlantGrowth.weedKiller))
				{
					toLog += PwnPlantGrowth.wkFound;
				}
				else
				{
					// chance of death
					if (PwnPlantGrowth.random(curDeath))
					{
						// TODO: make these configurable
						if (thisBlock == "COCOA") {
							replacement = Material.VINE;
						}
						else if (thisBlock == "KELP") {
							replacement = Material.SEAGRASS;
						}
						else {
							replacement = Material.DEAD_BUSH;
						}
						toLog += " {Plant Died, Rate: " + curDeath + "}";
					}
				}
			}
			else
			{
				toLog += "RESULT: {Plant Grew, Rate: " + curGrowth + "}";

			}
		}
		else
		{
			isCancelled = true;
			toLog += "RESULT: {Failed Growth: Bad Biome}";
			// chance of death
			if (PwnPlantGrowth.random(curDeath))
			{
				// TODO: make these configurable
				if (thisBlock == "COCOA") {
					replacement = Material.VINE;
				}
				else if (thisBlock == "KELP") {
					replacement = Material.SEAGRASS;
				}
				else {
					replacement = Material.DEAD_BUSH;
				}
				toLog += " {Plant Died, Rate: " + curDeath + "}";
			}
		}

		String midLog = "";
		if (isDark) {
			midLog += darkLog;
		}
		else {
			midLog += groupLog;
		}

		doLog = frontLog + midLog + toLog;
	}

	// just report the rate for player listener feature for growth rates
	Calculate(Boolean report, List<List<String>> specialBlocks, String thisBlock, String curBiome, Boolean isDark)
	{
		isCancelled = false;
		String toLog = "";
		String blockName = WordUtils.capitalizeFully(thisBlock.replace("_", " "));
		String biomeName = WordUtils.capitalizeFully(curBiome.replace("_", " "));

		// bool to catch if the biome is never declared in any config, therefor a bad biome and should not grow
		boolean noBiome = true;
		boolean fert = false;
		boolean uv = false;

		// defaults
		FileConfiguration config = PwnPlantGrowth.getInstance().getConfig();
		List<String> blockBiomeSettings = config.getStringList(thisBlock + ".Biome");

		curGrowth = config.getInt(thisBlock+".Growth");
		curDeath = config.getInt(thisBlock+".Death");

		if ((config.isSet(thisBlock+".BiomeGroup")) || (blockBiomeSettings.isEmpty()) || (blockBiomeSettings.contains(curBiome)))
		{
			// check the area to find if any of the special blocks are found
			List<String> fBlocksFound = specialBlocks.get(0);
			//List<String> wkBlocksFound = specialBlocks.get(1);
			List<String> uvBlocksFound = specialBlocks.get(2);

			// check the biome group settings
			if (config.isSet(thisBlock+".BiomeGroup"))
			{

				// create list from the config setting
				List<?> groupList = config.getList(thisBlock+".BiomeGroup");

				// iterate through list and see if any of that list matches curBiome
				for (int i = 0; i < groupList.size(); i++)
				{

					// check the biomegroup for this named group
					if ((config.getList("BiomeGroup."+groupList.get(i)) != null) && (config.getList("BiomeGroup."+groupList.get(i)).contains(curBiome)))
					{
						noBiome = false;

						// reference the configs now to see if the config settings are set!
						if (config.isSet(thisBlock+"."+groupList.get(i)+".Growth"))
						{
							curGrowth = config.getInt(thisBlock+"."+groupList.get(i)+".Growth");
						}

						if (config.isSet(thisBlock+"."+groupList.get(i)+".Death"))
						{
							curDeath = config.getInt(thisBlock+"."+groupList.get(i)+".Death");
						}
					}
				}
			}

			if (blockBiomeSettings.contains(curBiome)) {
				noBiome = false;
				// override with individual settings
				if (config.isSet(thisBlock+"."+curBiome+".Growth"))
				{
					curGrowth = config.getInt(thisBlock+"."+curBiome+".Growth");
				}

				if (config.isSet(thisBlock+"."+curBiome+".Death"))
				{
					curDeath = config.getInt(thisBlock+"."+curBiome+".Death");
				}
			}

			// if there is fertilizer, grow this plant at the fertilizer rate - default 100%
			if (fBlocksFound.contains(PwnPlantGrowth.fertilizer))
			{
				// set the current growth to the fertilizer rate
				curGrowth = PwnPlantGrowth.frate;
				fert = true;
			}

			// See if there are special settings for dark growth
			if (isDark)
			{
				// If no UV isDark remains false.
				if (uvBlocksFound.contains(PwnPlantGrowth.uv))
				{
					uv = true;
				}
				else
				{
					// default isDark config rates (if exist)
					if (config.isSet(thisBlock+".GrowthDark"))
					{
						curGrowth = config.getInt(thisBlock+".GrowthDark");
					}

					if (config.isSet(thisBlock+".DeathDark"))
					{
						curDeath = config.getInt(thisBlock+".DeathDark");
					}

					// override default values with biome group values
					if (config.isSet(thisBlock+".BiomeGroup"))
					{

						// create list from the config setting
						List<?> groupList = config.getList(thisBlock+".BiomeGroup");

						// iterate through list and see if any of that list matches curBiome
						for (int i = 0; i < groupList.size(); i++) {

							// check the biomegroup for this named group
							if  ((config.getList("BiomeGroup."+groupList.get(i)) != null) && (config.getList("BiomeGroup."+groupList.get(i)).contains(curBiome)))
							{

								noBiome = false;

								// reference the configs now to see if the config settings are set!
								if (config.isSet(thisBlock+"."+groupList.get(i)+".GrowthDark"))
								{
									curGrowth = config.getInt(thisBlock+"."+groupList.get(i)+".GrowthDark");
								}

								if (config.isSet(thisBlock+"."+groupList.get(i)+".DeathDark"))
								{
									curDeath = config.getInt(thisBlock+"."+groupList.get(i)+".DeathDark");
								}
							}
						}
					}

					// per biome isDark rates (if exist)
					if (blockBiomeSettings.contains(curBiome)) {
						noBiome = false;
						if (config.isSet(thisBlock+"."+curBiome+".GrowthDark"))
						{
							curGrowth = config.getInt(thisBlock+"."+curBiome+".GrowthDark");
						}

						if (config.isSet(thisBlock+"."+curBiome+".DeathDark"))
						{
							curDeath = config.getInt(thisBlock+"."+curBiome+".DeathDark");
						}
					}
				}
			}
		}

		// if BIOME was left empty, BIOME: [] it was intentional.. and this means use default growth rate.
		if (blockBiomeSettings.isEmpty()) {
			noBiome = false;
		}

		if (noBiome)
		{
			toLog += blockName + " will not grow in biome: " + biomeName;
		}
		else
		{
			toLog += String.format("%s grows at %d%% and dies at %d%% in biome %s", blockName, curGrowth, curDeath, biomeName);
			if (isDark)
			{
				toLog += " in the dark";
				if (uv)
				{
					toLog += " with UV block nearby";
				}
			}
			if (fert)
			{
				toLog += " with fertilizer block nearby";
			}
		}

		doLog = toLog + ". ";
		return;
	}
}
