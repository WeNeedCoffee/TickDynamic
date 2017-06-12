package com.wildex999.tickdynamic;

import com.wildex999.tickdynamic.listinject.EntityGroup;
import com.wildex999.tickdynamic.listinject.EntityType;
import com.wildex999.tickdynamic.listinject.ListManager;
import com.wildex999.tickdynamic.timemanager.ITimed;
import com.wildex999.tickdynamic.timemanager.TimeManager;
import com.wildex999.tickdynamic.timemanager.TimedEntities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class TickDynamicConfig {
	public static Configuration config;
	public static final String CATEGORY_GENERAL = "general";
	public static final String CATEGORY_WORLDS = "worlds";
	public static final String CATEGORY_DIM0_ENTITY = "worlds.dim0.entity";
	public static final String CATEGORY_DIM0_TILEENTITY = "worlds.dim0.tileentity";
	public static final String CATEGORY_GROUPS = "groups";

	private static Property ENABLED_PROP;
	public static boolean isModEnabled = true;
	private static final String isModEnabled_name = "enabled";
	private static Property DEBUG_PROP;
	public static boolean debugEnabled = false;
	private static final String debugEnabled_name = "debug";
	private static Property DEBUGGROUPS_PROP;
	public static boolean debugGroupsEnabled = false;
	private static final String debugGroupsEnabled_name = "debugGroups";
	private static Property DEBUGTIMER_PROP;
	public static boolean debugTimerEnabled = false;
	private static final String debugTimerEnabled_name = "debugTimer";
	private static Property DEFAULTWORLDSLICESMAX_PROP;
	public static int defaultWorldSlicesMax = 100;
	private static final String defaultWorldSlicesMax_name = "defaultWorldSlicesMax";
	private static Property DEFAULTAVERAGETICKS_PROP;
	public static int defaultAverageTicks = 20;
	private static final String defaultAverageTicks_name = "averageTicks";
	private static Property DEFAULTTICKTIME_PROP;
	public static int defaultTickTime = 50;
	private static final String defaultTickTime_name = "tickTime";

	public static final int defaultEntitySlicesMax = 100;
	public static final int defaultEntityMinimumObjects = 100;
	public static final float defaultEntityMinimumTPS = 0;
	public static final float defaultEntityMinimumTime = 0;

	public static void init(File configFile){
		config = new Configuration(configFile);
		config.load();
	}

	public static void syncConfig(){
		isModEnabled = ENABLED_PROP.getBoolean();
		debugEnabled = DEBUG_PROP.getBoolean();
		debugGroupsEnabled = DEBUGGROUPS_PROP.getBoolean();
		debugTimerEnabled = DEBUGTIMER_PROP.getBoolean();
		defaultWorldSlicesMax = DEFAULTWORLDSLICESMAX_PROP.getInt();
		defaultAverageTicks = DEFAULTAVERAGETICKS_PROP.getInt();
		defaultTickTime = DEFAULTTICKTIME_PROP.getInt();
		config.save();
	}

	public static void loadConfig(boolean groups) {
		config = new Configuration(config.getConfigFile());

		//--GENERAL CONFIG--
		config.getCategory(CATEGORY_GENERAL);
		config.setCategoryComment(CATEGORY_GENERAL, getComment(CATEGORY_GENERAL));

		ENABLED_PROP = config.get(CATEGORY_GENERAL, isModEnabled_name, isModEnabled);

		DEBUG_PROP = config.get(CATEGORY_GENERAL, debugEnabled_name, debugEnabled, getComment(debugEnabled_name));

		DEBUGGROUPS_PROP = config.get(CATEGORY_GENERAL, debugGroupsEnabled_name, debugGroupsEnabled, getComment(debugGroupsEnabled_name));

		DEBUGTIMER_PROP = config.get(CATEGORY_GENERAL, debugTimerEnabled_name, debugTimerEnabled, getComment(debugTimerEnabled_name));

		DEFAULTWORLDSLICESMAX_PROP = config.get(CATEGORY_GENERAL, defaultWorldSlicesMax_name, defaultWorldSlicesMax, getComment(defaultWorldSlicesMax_name));

		DEFAULTAVERAGETICKS_PROP = config.get(CATEGORY_GENERAL, defaultAverageTicks_name, defaultAverageTicks, getComment(defaultAverageTicks_name));

		//-- WORLDS CONFIG --
		DEFAULTTICKTIME_PROP = config.get(CATEGORY_WORLDS, defaultTickTime_name, defaultTickTime, getComment(defaultTickTime_name));

		//-- GROUPS CONFIG --
		/*
		mod.config.setCategoryComment(mod.configCategoryDefaultEntities, "The default values for Entities in a world, which does not belong to any other group.");
		mod.defaultEntitySlicesMax = mod.config.get(mod.configCategoryDefaultEntities, TimedGroup.configKeySlicesMax, mod.defaultEntitySlicesMax, 
    			"The number of time slices given to the group.").getInt();
		mod.defaultEntityMinimumObjects = mod.config.get(mod.configCategoryDefaultEntities, TimedGroup.configKeyMinimumObjects, mod.defaultEntityMinimumObjects, 
    			"The minimum number of Entities to update per tick, independent of time given.").getInt();
    	
		mod.config.setCategoryComment(mod.configCategoryDefaultTileEntities, "The default values for TileEntities in a world, which does not belong to any other group.");
		mod.defaultTileEntitySlicesMax = mod.config.get(mod.configCategoryDefaultTileEntities, TimedGroup.configKeySlicesMax, mod.defaultTileEntitySlicesMax, 
    			"The number of time slices given to the group.").getInt();
		mod.defaultTileEntityMinimumObjects = mod.config.get(mod.configCategoryDefaultTileEntities, TimedGroup.configKeyMinimumObjects, mod.defaultTileEntityMinimumObjects, 
    			"The minimum number of TileEntities to update per tick, independent of time given.").getInt();*/

		//Load New, Reload and Remove old groups
		if (groups) {
			loadGlobalGroups();

			//Default example for Entities and TileEntities in dim0(Overworld)
			if (!config.hasCategory(CATEGORY_DIM0_ENTITY)) {
				config.get(CATEGORY_DIM0_ENTITY, ITimed.configKeySlicesMax, defaultEntitySlicesMax);
				config.get(CATEGORY_DIM0_ENTITY, EntityGroup.config_groupType, EntityType.Entity.toString());
			}
			if (!config.hasCategory(CATEGORY_DIM0_TILEENTITY)) {
				config.get(CATEGORY_DIM0_TILEENTITY, ITimed.configKeySlicesMax, defaultEntitySlicesMax);
				config.get(CATEGORY_DIM0_TILEENTITY, EntityGroup.config_groupType, EntityType.TileEntity.toString());
			}

			//Reload local groups
			WorldServer[] worlds = DimensionManager.getWorlds();
			for (WorldServer world : worlds) {
				TickDynamicMod.logDebug("Reloading " + world.provider.getDimensionType().getName());

				if (world.loadedEntityList instanceof ListManager) {
					ListManager entityList = (ListManager) world.loadedEntityList;
					TickDynamicMod.logDebug("Reloading " + entityList.size() + " Entities...");
					entityList.reloadGroups();
				}
				if (world.loadedTileEntityList instanceof ListManager) {
					ListManager tileList = (ListManager) world.loadedTileEntityList;
					TickDynamicMod.logDebug("Reloading " + tileList.size() + " TileEntities...");
					tileList.reloadGroups();
				}
			}

			TickDynamicMod.logDebug("Done reloading worlds");

			//Reload Timed
			for (ITimed timed : TickDynamicMod.instance.timedObjects.values()) {
				if (timed instanceof TimedEntities) {
					TimedEntities timedGroup = (TimedEntities) timed;
					if (!timedGroup.getEntityGroup().valid) {
						TickDynamicMod.instance.timedObjects.remove(timedGroup);
						continue;
					}
				}
				timed.loadConfig(false);
			}

			if (TickDynamicMod.instance.root != null)
				TickDynamicMod.instance.root.setTimeMax(defaultTickTime * TimeManager.timeMilisecond);
		}

		//Save any new defaults
		syncConfig();
	}

	//Load the config of Global Groups
	public static void loadGlobalGroups() {
		config.setCategoryComment(CATEGORY_GROUPS, getComment(CATEGORY_GROUPS));

		//Load/Create default entity and tileentity groups
		loadDefaultGlobalGroups();

		//Load Global groups
		loadGroups(CATEGORY_GROUPS);
	}

	//Load all groups under the given category
	public static void loadGroups(String category) {
		ConfigCategory groupsCat = config.getCategory(category);
		Set<ConfigCategory> groups = groupsCat.getChildren();

		//Remove every group which is no longer in groups set
		ArrayList<String> toRemove = new ArrayList<>();
		for (String groupPath : TickDynamicMod.instance.entityGroups.keySet()) {
			if (!groupPath.startsWith(category))
				continue; //We only care about groups in the same category

			int nameIndex = groupPath.lastIndexOf(".");
			String groupName;
			if (nameIndex == -1)
				groupName = groupPath;
			else
				groupName = groupPath.substring(nameIndex + 1);

			boolean remove = true;
			if (config.hasCategory(groupPath))
				remove = false;

			//Check if local copy of a global group
			if (remove) {
				EntityGroup entityGroup = TickDynamicMod.instance.entityGroups.get(groupPath);
				if (entityGroup != null && entityGroup.base != null) {
					//Check if the global group still exists
					if (config.hasCategory("groups." + groupName))
						remove = false;
				}
			}

			//Mark for removal after loop
			if (remove) {
				TickDynamicMod.logDebug("Remove Group: " + groupPath);
				toRemove.add(groupPath);
			}
		}

		//Remove after due to ConcurrentException
		for (String groupPath : toRemove) {
			EntityGroup groupRemoved = TickDynamicMod.instance.entityGroups.remove(groupPath);
			if (groupRemoved != null)
				groupRemoved.valid = false;
		}

		//Load new groups
		ArrayList<EntityGroup> updateGroups = new ArrayList<>();
		for (ConfigCategory group : groups) {
			//Check if group already exists
			String groupPath = category + "." + group.getName();
			EntityGroup entityGroup = TickDynamicMod.instance.getEntityGroup(groupPath);
			if (entityGroup == null) {
				TickDynamicMod.logDebug("Loading group: " + groupPath);

				TimedEntities timedEntities = (TimedEntities) TickDynamicMod.instance.getTimedGroup(groupPath);
				if (timedEntities == null) {
					timedEntities = new TimedEntities(null, group.getName(), groupPath, null);
					timedEntities.init();
				}

				entityGroup = new EntityGroup(null, timedEntities, group.getName(), groupPath, EntityType.Entity, null);
				TickDynamicMod.instance.entityGroups.put(groupPath, entityGroup);
				TickDynamicMod.logDebug("New Group: " + groupPath);
			} else {
				//Add to list of groups to update
				updateGroups.add(entityGroup);
				TickDynamicMod.logDebug("Update Group: " + groupPath);
			}
		}

		//Update old
		for (EntityGroup entityGroup : updateGroups)
			entityGroup.readConfig(false);
	}

	public static void loadDefaultGlobalGroups() {
		EntityGroup group;
		TimedEntities timedGroup;
		String groupPath;

		groupPath = "groups.entity";
		group = TickDynamicMod.instance.getEntityGroup(groupPath);
		if (group == null) {
			timedGroup = new TimedEntities(null, "entity", groupPath, null);
			timedGroup.init();
			group = new EntityGroup(null, timedGroup, "entity", groupPath, EntityType.Entity, null);
			TickDynamicMod.instance.entityGroups.put(groupPath, group);
		}

		//Player group accounts the time used by players(Usually not limited, just used for accounting)
		groupPath = "groups.players";
		group = TickDynamicMod.instance.getEntityGroup(groupPath);
		if (group == null) {
			//Write new defaults before creating group
			config.get(groupPath, TimedEntities.configKeySlicesMax, 0); //No limit by default
			String[] entityClasses = {EntityPlayer.class.getName(), EntityPlayerMP.class.getName()};
			config.get(groupPath, EntityGroup.config_classNames, entityClasses);

			timedGroup = new TimedEntities(null, "players", groupPath, null);
			timedGroup.init();

			group = new EntityGroup(null, timedGroup, "players", groupPath, EntityType.Entity, null);
			TickDynamicMod.instance.entityGroups.put(groupPath, group);
		}

		groupPath = "groups.tileentity";
		group = TickDynamicMod.instance.getEntityGroup(groupPath);
		if (group == null) {
			timedGroup = new TimedEntities(null, "tileentity", groupPath, null);
			timedGroup.init();
			group = new EntityGroup(null, timedGroup, "tileentity", groupPath, EntityType.TileEntity, null);
			TickDynamicMod.instance.entityGroups.put(groupPath, group);
		}
	}

	private static String getComment(String key){
		return TickDynamicMod.proxy.translate(key+".tooltip", "\n");
	}
}
