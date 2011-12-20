/**
 * This software is provided under the terms of the Minecraft Forge Public 
 * License v1.0.
 */

package net.minecraft.src.forge;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

import java.util.*;

public class MinecraftForge {

    private static LinkedList<IBucketHandler> bucketHandlers = new LinkedList<IBucketHandler>();
    /**
     * Register a new custom bucket handler.
     */
    public static void registerCustomBucketHandler(IBucketHandler handler) {
        bucketHandlers.add(handler);
    }

    /**
     * Registers a new sleeping handler.
     */
    public static void registerSleepHandler(ISleepHandler handler) {
	ForgeHooks.sleepHandlers.add(handler);
    }

    /**
     * Registers a new bonemeal handler.
     */
    public static void registerBonemealHandler(IBonemealHandler handler) {
	ForgeHooks.bonemealHandlers.add(handler);
    }

    /**
     * Registers a new hoe handler.
     */
    public static void registerHoeHandler(IHoeHandler handler) {
	ForgeHooks.hoeHandlers.add(handler);
    }

    /**
     * Registers a new destroy tool handler.
     */
    public static void registerDestroyToolHandler(IDestroyToolHandler handler) {
	ForgeHooks.destroyToolHandlers.add(handler);
    }

    /**
     * Registers a new crafting handler.
     */
    public static void registerCraftingHandler(ICraftingHandler handler) {
        ForgeHooks.craftingHandlers.add(handler);
    }

    /**
     * This is not supposed to be called outside of Minecraft internals.
     */
    public static ItemStack fillCustomBucket(World w, int i, int j, int k) {
        for (IBucketHandler handler : bucketHandlers) {
            ItemStack stack = handler.fillCustomBucket(w, i, j, k);

            if (stack != null) {
                return stack;
            }
        }

        return null;
    }

    // Ore Dictionary
    // ------------------------------------------------------------
    private static LinkedList<IOreHandler> oreHandlers = new LinkedList<IOreHandler>();
    private static TreeMap<String,List<ItemStack>> oreDict=
	    new TreeMap<String,List<ItemStack>>();

    /** Register a new ore handler.  This will automatically call the handler
     * with all current ores during registration, and every time a new ore is
     * added later.
     */
    public static void registerOreHandler(IOreHandler handler) {
	    oreHandlers.add(handler);

	    for(String key : oreDict.keySet()) {
		    List<ItemStack> val=oreDict.get(key);
		    for(ItemStack ist : val) {
			    handler.registerOre(key,ist);
		    }
	    }
    }

    /** Register a new item with the ore dictionary.
     * @param oreClass The string class of the ore.
     * @param ore The ItemStack for the ore.
     */
    public static void registerOre(String oreClass, ItemStack ore) {
	    List<ItemStack> orelist=oreDict.get(oreClass);
	    if(orelist==null) {
		    orelist=new ArrayList<ItemStack>();
		    oreDict.put(oreClass,orelist);
	    }
	    orelist.add(ore);
	    for(IOreHandler ioh : oreHandlers) {
		    ioh.registerOre(oreClass,ore);
	    }
    }

    /** Get the list of ores in a given class.
     */
    public static List<ItemStack> getOreClass(String oreClass) {
	    return oreDict.get(oreClass);
    }

    public static class OreQuery implements Iterable<Object[]> {
	    Object[] proto;

	    public class OreQueryIterator implements Iterator<Object[]> {
		    LinkedList itering;
		    LinkedList output;

		    private OreQueryIterator() {
			    itering=new LinkedList();
			    output=new LinkedList();

			    for(int i=0; i<proto.length; i++) {
				    if(proto[i] instanceof Collection) {
					    Iterator it=((Collection)proto[i])
						    .iterator();
					    if(!it.hasNext()) {
						    output=null; break;
					    }
					    itering.addLast(it);
					    output.addLast(it.next());
				    } else {
					    itering.addLast(proto[i]);
					    output.addLast(proto[i]);
				    }
			    }
		    }

		    public boolean hasNext() {
			    return output!=null;
		    }

		    public Object[] next() {
			    Object[] tr=output.toArray();

			    Object to;
			    while(true) {
				    if(itering.size()==0) {
					    output=null;
					    return tr;
				    }
				    to=itering.getLast();
				    output.removeLast();
				    if(to instanceof Iterator) {
					    Iterator it=(Iterator)to;
					    if(it.hasNext()) {
						    output.addLast(it.next());
						    break;
					    }
				    }
				    itering.removeLast();
			    }
			    for(int i=itering.size(); i<proto.length; i++) {
				    if(proto[i] instanceof Collection) {
					    Iterator it=((Collection)proto[i])
						    .iterator();
					    if(!it.hasNext()) {
						    output=null; break;
					    }
					    itering.addLast(it);
					    output.addLast(it.next());
				    } else {
					    itering.addLast(proto[i]);
					    output.addLast(proto[i]);
				    }
			    }
			    return tr;
		    }

		    public void remove() {}
	    }

	    private OreQuery(Object[] pattern) {
		    proto=pattern;
	    }

	    public Iterator<Object[]> iterator() {
		    return new OreQueryIterator();
	    }
    }

    /** Generate all valid legal recipe combinations.  Any Lists in pattern
     * will be fully expanded to all valid combinations.
     */
    public static OreQuery generateRecipes(Object... pattern) {
	    return new OreQuery(pattern);
    }

    // ------------------------------------------------------------

    /** Register a new plant to be planted when bonemeal is used on grass.
     * @param bid The block ID to plant.
     * @param md The metadata to plant.
     * @param prop The relative probability of the plant, where red flowers are
     * 10 and yellow flowers are 20.
     */
    public static void addGrassPlant(int item, int md, int prop) {
	    ForgeHooks.addPlantGrass(item,md,prop);
    }

    /** Register a new seed to be dropped when breaking tall grass.
     * @param bid The item ID of the seeds.
     * @param md The metadata of the seeds.
     * @param qty The quantity of seeds to drop.
     * @param prop The relative probability of the seeds, where wheat seeds are
     * 10.
     */
    public static void addGrassSeed(int item, int md, int qty, int prop) {
	    ForgeHooks.addGrassSeed(item,md,qty,prop);
    }

    /** Register a tool as a tool class with a given harvest level.
     *
     * @param tool The custom tool to register.
     * @param tclass The tool class to register as.  The predefined tool
     * clases are "pickaxe", "shovel", "axe".  You can add others for custom
     * tools.
     * @param hlevel The harvest level of the tool.
     */
    public static void setToolClass(Item tool, String tclass, int hlevel) {
	    ForgeHooks.initTools();
	    ForgeHooks.toolClasses.put(tool.shiftedIndex,
		Arrays.asList(tclass,hlevel));
    }

    /** Register a block to be harvested by a tool class.  This is the metadata
     * sensitive version, use it if your blocks are using metadata variants.
     * By default, this sets the block class as effective against that type.
     *
     * @param bl The block to register.
     * @param md The metadata for the block subtype.
     * @param tclass The tool class to register as able to remove this block.
     * You may register the same block multiple times with different tool
     * classes, if multiple tool types can be used to harvest this block.
     * @param hlevel The minimum tool harvest level required to successfully
     * harvest the block.
     * @see setToolClass for details on tool classes.
     */
    public static void setBlockHarvestLevel(Block bl, int md, String tclass, 
		int hlevel) {
	    ForgeHooks.initTools();
	    List key=Arrays.asList(bl.blockID,md,tclass);
	    ForgeHooks.toolHarvestLevels.put(key,hlevel);
	    ForgeHooks.toolEffectiveness.add(key);
    }

    /** Remove a block effectiveness mapping.  Since setBlockHarvestLevel
     * makes the tool class effective against the block by default, this can be
     * used to remove that mapping.  This will force a block to be harvested at
     * the same speed regardless of tool quality, while still requiring a given
     * harvesting level.
     * @param bl The block to remove effectiveness from.
     * @param md The metadata for the block subtype.
     * @param tclass The tool class to remove the effectiveness mapping from.
     * @see setToolClass for details on tool classes.
     */
    public static void removeBlockEffectiveness(Block bl, int md,
		    String tclass) {
	    ForgeHooks.initTools();
	    List key=Arrays.asList(bl.blockID,md,tclass);
	    ForgeHooks.toolEffectiveness.remove(key);
    }

    /** Register a block to be harvested by a tool class.
     * By default, this sets the block class as effective against that type.
     *
     * @param bl The block to register.
     * @param tclass The tool class to register as able to remove this block.
     * You may register the same block multiple times with different tool
     * classes, if multiple tool types can be used to harvest this block.
     * @param hlevel The minimum tool harvest level required to successfully
     * harvest the block.
     * @see setToolClass for details on tool classes.
     */
    public static void setBlockHarvestLevel(Block bl, String tclass, 
		int hlevel) {
	    ForgeHooks.initTools();
	    for(int md=0; md<16; md++) {
		    List key=Arrays.asList(bl.blockID,md,tclass);
		    ForgeHooks.toolHarvestLevels.put(key,hlevel);
		    ForgeHooks.toolEffectiveness.add(key);
	    }
    }

    /** Remove a block effectiveness mapping.  Since setBlockHarvestLevel
     * makes the tool class effective against the block by default, this can be
     * used to remove that mapping.  This will force a block to be harvested at
     * the same speed regardless of tool quality, while still requiring a given
     * harvesting level.
     * @param bl The block to remove effectiveness from.
     * @param tclass The tool class to remove the effectiveness mapping from.
     * @see setToolClass for details on tool classes.
     */
    public static void removeBlockEffectiveness(Block bl, String tclass) {
	    ForgeHooks.initTools();
	    for(int md=0; md<16; md++) {
		    List key=Arrays.asList(bl.blockID,md,tclass);
		    ForgeHooks.toolEffectiveness.remove(key);
	    }
    }

    /**
     * Kill minecraft with an error message.
     */
    public static void killMinecraft(String modname, String msg) {
	    throw new RuntimeException(modname+": "+msg);
    }

    /**
     * Version checking.  Ensures that a sufficiently recent version of Forge
     * is installed.  Will result in a fatal error if the major versions
     * mismatch or if the version is too old.  Will print a warning message if
     * the minor versions don't match.
     */
    public static void versionDetect(String modname,
		    int major, int minor, int revision) {
	    if(major!=ForgeHooks.majorVersion) {
		    killMinecraft(modname,"MinecraftForge Major Version Mismatch, expecting "+major+".x.x");
	    } else if(minor!=ForgeHooks.minorVersion) {
		    if(minor>ForgeHooks.minorVersion) {
			    killMinecraft(modname,"MinecraftForge Too Old, need at least "+major+"."+minor+"."+revision);
		    } else {
			    System.out.println(modname + ": MinecraftForge minor version mismatch, expecting "+major+"."+minor+".x, may lead to unexpected behavior");
		    }
	    } else if(revision>ForgeHooks.revisionVersion) {
		    killMinecraft(modname,"MinecraftForge Too Old, need at least "+major+"."+minor+"."+revision);
	    }
    }

    /**
     * Strict version checking.  Ensures that a sufficiently recent version of
     * Forge is installed.  Will result in a fatal error if the major or minor 
     * versions mismatch or if the version is too old.  Use this function for
     * mods that use recent, new, or unstable APIs to prevent
     * incompatibilities.
     */
    public static void versionDetectStrict(String modname,
		    int major, int minor, int revision) {
	    if(major!=ForgeHooks.majorVersion) {
		    killMinecraft(modname,"MinecraftForge Major Version Mismatch, expecting "+major+".x.x");
	    } else if(minor!=ForgeHooks.minorVersion) {
		    if(minor>ForgeHooks.minorVersion) {
			    killMinecraft(modname,"MinecraftForge Too Old, need at least "+major+"."+minor+"."+revision);
		    } else {
			    killMinecraft(modname,"MinecraftForge minor version mismatch, expecting "+major+"."+minor+".x");
		    }
	    } else if(revision>ForgeHooks.revisionVersion) {
		    killMinecraft(modname,"MinecraftForge Too Old, need at least "+major+"."+minor+"."+revision);
	    }
    }

	private static int dungeonLootAttempts = 8;
	private static ArrayList<ObjectPair<Float, String>> dungeonMobs = new ArrayList<ObjectPair<Float, String>>();
	private static ArrayList<ObjectPair<Float, DungeonLoot>> dungeonLoot = new ArrayList<ObjectPair<Float, DungeonLoot>>();
    /**
     * Set the number of item stacks that will be attempted to be added to each Dungeon chest.
     * Note: Due to random number generation, you will not always get this amount per chest.
     * @param number The maximum number of item stacks to add to a chest.
     */
    public static void setDungeonLootTries(int number)
    {
    	dungeonLootAttempts = number;
    }
    
    /**
     * @return The max number of item stacks found in each dungeon chest.
     */
    public static int getDungeonLootTries()
    {
    	return dungeonLootAttempts;
    }
    
    /**
     * Adds a mob to the possible list of creatures the spawner will create.
     * If the mob is already in the spawn list, the rarity will be added to the existing one,
     * causing the mob to be more common.
     *  
     * @param name The name of the monster, use the same name used when registering the entity.
     * @param rarity The rarity of selecting this mob over others. Must be greater then 0.
     *        Vanilla Minecraft has the following mobs:
     *        Spider   1
     *        Skeleton 1
     *        Zombie   2
     *        Meaning, Zombies are twice as common as spiders or skeletons.
     * @return The new rarity of the monster,         
     */
    public static float addDungeonMob(String name, float rarity)
    {
    	if (rarity <= 0)
    	{
    		throw new IllegalArgumentException("Rarity must be greater then zero");
    	}
    	
    	for(ObjectPair<Float, String> mob : dungeonMobs)
    	{
    		if (name.equals(mob.getValue2()))
    		{
    			mob.setValue1(mob.getValue1() + rarity);
    			return mob.getValue1();
    		}
    	}

    	dungeonMobs.add(new ObjectPair<Float, String>(rarity, name));
    	return rarity;
    }
    
    /**
     * Will completely remove a Mob from the dungeon spawn list.
     * 
     * @param name The name of the mob to remove
     * @return The rarity of the removed mob, prior to being removed.
     */
    public static float removeDungeonMob(String name)
    {
    	for (ObjectPair<Float, String> mob : dungeonMobs)
    	{
    		if (name.equals(name))
    		{
    			dungeonMobs.remove(mob);
    			return mob.getValue1();
    		}
    	}
    	return 0;
    }
    
    /**
     * Gets a random mob name from the list.
     * @param rand World generation random number generator
     * @return The mob name
     */
    public static String getRandomDungeonMob(Random rand)
    {
    	float maxRarity = 0f;
    	for (ObjectPair<Float, String> mob : dungeonMobs)
    	{
    		maxRarity += mob.getValue1();
    	}
    	
    	float targetRarity = rand.nextFloat() * maxRarity;
    	for (ObjectPair<Float, String> mob : dungeonMobs)
    	{
    		if (targetRarity < mob.getValue1()) 
    		{
    			return mob.getValue2();
    		}
    		targetRarity -= mob.getValue1();
    	}
    	
    	return "";
    }
    
    /**
     * Adds a item stack to the dungeon loot list with a stack size
     * of 1.
     * 
     * @param item The ItemStack to be added to the loot list
     * @param rarity The relative chance that this item will spawn, Vanilla has 
     * 			most of its items set to 1. Like the saddle, bread, silk, wheat, etc..
     * 			Rarer items are set to lower values, EXA: Golden Apple 0.01
     */
    public static void addDungeonLoot(ItemStack item, float rarity)
    {
    	addDungeonLoot(item, rarity, 1, 1);
    }
    
    /**
     * Adds a item stack, with a range of sizes, to the dungeon loot list.
     * If a stack matching the same item, and size range, is already in the list
     * the rarities will be added together making the item more common.
     * 
     * @param item The ItemStack to be added to the loot list
     * @param rarity The relative chance that this item will spawn, Vanilla has 
     * 			most of its items set to 1. Like the saddle, bread, silk, wheat, etc..
     * 			Rarer items are set to lower values, EXA: Golden Apple 0.01
     * @param minCount When this item does generate, the minimum number that is in the stack
     * @param maxCount When this item does generate, the maximum number that can bein the stack
     * @return The new rarity of the loot. 
     */
    public static float addDungeonLoot(ItemStack item, float rarity, int minCount, int maxCount)
    {
    	for (ObjectPair<Float, DungeonLoot> loot : dungeonLoot)
    	{
    		if (loot.getValue2().equals(item, minCount, maxCount))
    		{
    			loot.setValue1(loot.getValue1() + rarity);
    			return loot.getValue1();
    		}
    	}
    	
    	dungeonLoot.add(new ObjectPair<Float, DungeonLoot>(rarity, new DungeonLoot(item, minCount, maxCount)));
    	return rarity;    	
    }
    /**
     * Removes a item stack from the dungeon loot list, this will remove all items
     * as long as the item stack matches, it will not care about matching the stack
     * size ranges perfectly.
     * 
     * @param item The item stack to remove
     * @return The total rarity of all items removed
     */
    public static float removeDungeonLoot(ItemStack item)
    {
    	return removeDungeonLoot(item, -1, 0);
    }
    
    /**
     * Removes a item stack from the dungeon loot list. If 'minCount' parameter 
     * is greater then 0, it will only remove loot items that have the same exact 
     * stack size range as passed in by parameters.
     * 
     * @param item The item stack to remove
     * @param minCount The minimum count for the match check, if less then 0, 
     * 			the size check is skipped
     * @param maxCount The max count used in match check when 'minCount' is >= 0
     * @return The total rarity of all items removed
     */
    public static float removeDungeonLoot(ItemStack item, int minCount, int maxCount)
    {
    	float rarity = 0;
    	ArrayList<ObjectPair<Float, DungeonLoot>> lootTmp = (ArrayList<ObjectPair<Float, DungeonLoot>>)dungeonLoot.clone();
    	if (minCount < 0)
    	{
	    	for (ObjectPair<Float, DungeonLoot> loot : lootTmp)
	    	{
	    		if (loot.getValue2().equals(item))
	    		{
	    			dungeonLoot.remove(loot);
	    			rarity += loot.getValue1();
	    		}
	    	}
    	}
    	else
    	{
	    	for (ObjectPair<Float, DungeonLoot> loot : lootTmp)
	    	{
	    		if (loot.getValue2().equals(item, minCount, maxCount))
	    		{
	    			dungeonLoot.remove(loot);
	    			rarity += loot.getValue1();
	    		}
	    	}
    	}
    	
    	return rarity;  
    }
    

    /**
     * Gets a random item stack to place in a dungeon chest during world generation
     * @param rand World generation random number generator
     * @return The item stack
     */
    public static ItemStack getRandomDungeonLoot(Random rand)
    {
    	float maxRarity = 0f;
    	for (ObjectPair<Float, DungeonLoot> loot : dungeonLoot)
    	{
    		maxRarity += loot.getValue1();
    	}
    	
    	float targetRarity = rand.nextFloat() * maxRarity;
    	for (ObjectPair<Float, DungeonLoot> loot : dungeonLoot)
    	{
    		if (targetRarity < loot.getValue1()) 
    		{
    			return loot.getValue2().generateStack(rand);
    		}
    		targetRarity -= loot.getValue1();
    	}
    	
    	return null;
    }
        
    static 
    {
    	addDungeonMob("Skeleton", 1.0f);
    	addDungeonMob("Zombie",   2.0f);
    	addDungeonMob("Spider",   1.0f);
    	
    	addDungeonLoot(new ItemStack(Item.saddle),          1.00f      );
    	addDungeonLoot(new ItemStack(Item.ingotIron),       1.00f, 1, 4);
    	addDungeonLoot(new ItemStack(Item.bread),           1.00f      );
    	addDungeonLoot(new ItemStack(Item.wheat),           1.00f, 1, 4);
    	addDungeonLoot(new ItemStack(Item.gunpowder),       1.00f, 1, 4);
    	addDungeonLoot(new ItemStack(Item.silk),            1.00f, 1, 4);
    	addDungeonLoot(new ItemStack(Item.bucketEmpty),     1.00f      );
    	addDungeonLoot(new ItemStack(Item.appleGold),       0.01f      );
    	addDungeonLoot(new ItemStack(Item.redstone),        0.50f, 1, 4);
    	addDungeonLoot(new ItemStack(Item.record13),        0.05f      );
    	addDungeonLoot(new ItemStack(Item.recordCat),       0.05f      );
    	addDungeonLoot(new ItemStack(Item.dyePowder, 1, 3), 1.00f      );
    }

}
