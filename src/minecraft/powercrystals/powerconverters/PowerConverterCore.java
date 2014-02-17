package powercrystals.powerconverters;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import powercrystals.powerconverters.common.BlockPowerConverterCommon;
import powercrystals.powerconverters.common.ItemBlockPowerConverterCommon;
import powercrystals.powerconverters.common.TileEntityCharger;
import powercrystals.powerconverters.common.TileEntityEnergyBridge;
import powercrystals.powerconverters.gui.PCGUIHandler;
import powercrystals.powerconverters.mods.BuildCraft;
import powercrystals.powerconverters.mods.IndustrialCraft;
import powercrystals.powerconverters.mods.ThermalExpansion;
import powercrystals.powerconverters.mods.base.LoaderBase;
import powercrystals.powerconverters.power.PowerSystem;
import powercrystals.powerconverters.power.steam.BlockPowerConverterSteam;
import powercrystals.powerconverters.power.steam.ItemBlockPowerConverterSteam;
import powercrystals.powerconverters.power.steam.TileEntitySteamConsumer;
import powercrystals.powerconverters.power.steam.TileEntitySteamProducer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
//import powercrystals.powerconverters.power.steam.BlockPowerConverterSteam;
//import powercrystals.powerconverters.power.steam.ItemBlockPowerConverterSteam;
//import powercrystals.powerconverters.power.steam.TileEntitySteamConsumer;
//import powercrystals.powerconverters.power.steam.TileEntitySteamProducer;

@Mod(modid = PowerConverterCore.modId, name = PowerConverterCore.modName, version = PowerConverterCore.version, dependencies = "after:BuildCraft|Energy;after:factorization;after:IC2;after:Railcraft;after:ThermalExpansion")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public final class PowerConverterCore
{
    public static final String modId = "PowerConverters";
    public static final String modName = "Power Converters";
    public static final String version = "1.6.4R2.4.0pre3";

    public static final String texturesFolder = modId + ":";
    public static final String guiFolder = modId + ":" + "textures/gui/";

    public static Block converterBlockCommon;
    public static Block converterBlockSteam;

    @Mod.Instance(modId)
    public static PowerConverterCore instance;

    public static int blockIdThermalExpansion;
    public static int blockIdIndustrialCraft;
    public static int blockIdFactorization;
    public static int blockIdBuildCraft;

    public static int bridgeBufferSize;
    public static int throttleSteamConsumer;
    public static int throttleSteamProducer;
    public static PowerSystem powerSystemSteam;
    public static boolean powerSystemSteamEnabled;

    private static int blockIdCommon;
    private static int blockIdSteam;
    private LoaderBase[] bases = new LoaderBase[] { BuildCraft.INSTANCE, /*Factorization.INSTANCE,*/IndustrialCraft.INSTANCE, ThermalExpansion.INSTANCE };

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt)
    {
        File dir = evt.getModConfigurationDirectory();
        loadConfig(dir);

        converterBlockCommon = new BlockPowerConverterCommon(blockIdCommon);
        GameRegistry.registerBlock(converterBlockCommon, ItemBlockPowerConverterCommon.class, converterBlockCommon.getUnlocalizedName());
        GameRegistry.registerTileEntity(TileEntityEnergyBridge.class, "powerConverterEnergyBridge");
        GameRegistry.registerTileEntity(TileEntityCharger.class, "powerConverterUniversalCharger");


        powerSystemSteam = new PowerSystem("Steam", "STEAM", 500, 500,/*875, 875,*/null, null, "mB/t");
        PowerSystem.registerPowerSystem(powerSystemSteam);
        for (LoaderBase base : bases)
            base.load(LoaderBase.Stage.PREINIT);
        }

    @EventHandler
    public void init(FMLInitializationEvent evt) throws Exception
    {
	if (powerSystemSteamEnabled)
	    loadSteamConverters();
	for (LoaderBase base : bases)
	    base.load(LoaderBase.Stage.INIT);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt) throws Exception
    {
	for (LoaderBase base : bases)
	    base.load(LoaderBase.Stage.POSTINIT);

	System.out.println("+++++++++++++++++++++++++[PowerConverters][NOTICE]+++++++++++++++++++++++++");
	System.out.println("Default power ratios are based on FTB standards and not all mods follow these");
	System.out.println("If you find conflicting ratios in your pack, please adjust the config acoordingly or remove PC");
	System.out.println("These conflicts are not the fault of any mod author. It is the nature on minecraft");
	System.out.println("-------------------------[PowerConverters][STEAM]-------------------------");
	System.out.println("Default steam ratios, while based on standards, will create infinite energy loops");
	System.out.println("To prevent over powered infinite energy, use a steam throttle values of less than 5");
	System.out.println("+++++++++++++++++++++++++[PowerConverters][NOTICE]+++++++++++++++++++++++++");

	//original recipes
	//    GameRegistry.addRecipe(new ItemStack(converterBlockCommon, 1, 0), "GRG", "LDL", "GRG", 'G', Item.ingotGold, 'R', Item.redstone, 'L', Block.glass, 'D', Item.diamond);
	//    GameRegistry.addRecipe(new ItemStack(converterBlockCommon, 1, 2), "GRG", "ICI", "GRG", 'G', Item.ingotGold, 'R', Item.redstone, 'I', Item.ingotIron, 'C', Block.chest);

	NetworkRegistry.instance().registerGuiHandler(instance, new PCGUIHandler());
	MinecraftForge.EVENT_BUS.register(instance);

	// Cleanup
	bases = null;
    }

    private void loadSteamConverters() throws Exception
    {
	{
	    converterBlockSteam = new BlockPowerConverterSteam(blockIdSteam);
	    GameRegistry.registerBlock(converterBlockSteam, ItemBlockPowerConverterSteam.class, converterBlockSteam.getUnlocalizedName());
	    GameRegistry.registerTileEntity(TileEntitySteamConsumer.class, "powerConverterSteamConsumer");
	    GameRegistry.registerTileEntity(TileEntitySteamProducer.class, "powerConverterSteamProducer");

	    if (Loader.isModLoaded("Railcraft"))
	    {
		//GameRegistry.addRecipe(new ItemStack(converterBlockSteam, 1, 0), "G G", " E ", "G G", 'G', Item.ingotGold, 'E', new ItemStack((Block) (Class.forName("mods.railcraft.common.blocks.RailcraftBlocks").getMethod("getBlockMachineBeta").invoke(null)), 1, 8));
	    } else
	    {
		//Object fzRegistry = Class.forName("factorization.common.Core").getField("registry").get(null);
		//GameRegistry.addRecipe(new ItemStack(converterBlockSteam, 1, 0), "G G", " E ", "G G", 'G', Item.ingotGold, 'E', (Class.forName("factorization.common.Registry").getField("steamturbine_item").get(fzRegistry)));
	    }

	    GameRegistry.addShapelessRecipe(new ItemStack(converterBlockSteam, 1, 1), new ItemStack(converterBlockSteam, 1, 0));
	    GameRegistry.addShapelessRecipe(new ItemStack(converterBlockSteam, 1, 0), new ItemStack(converterBlockSteam, 1, 1));
	}
    }

    private static void loadConfig(File dir)
    {
	dir = new File(new File(dir, modId.toLowerCase()), "common.cfg");
	Configuration c = new Configuration(dir);

	blockIdCommon = c.getBlock("ID.BlockCommon", 2850).getInt();
	blockIdBuildCraft = c.getBlock("ID.BlockBuildcraft", 2851).getInt();
	blockIdIndustrialCraft = c.getBlock("ID.BlockIndustrialCraft", 2852).getInt();
	blockIdSteam = c.getBlock("ID.BlockSteam", 2853).getInt();
	blockIdFactorization = c.getBlock("ID.BlockFactorization", 2854).getInt();
	blockIdThermalExpansion = c.getBlock("ID.BlockThermalExpansion", 2855).getInt();

	bridgeBufferSize = c.get(Configuration.CATEGORY_GENERAL, "BridgeBufferSize", 160000000).getInt();
	throttleSteamConsumer = c.get("Throttles", "Steam.Consumer", 1000, "mB/t").getInt();
	throttleSteamProducer = c.get("Throttles", "Steam.Producer", 1000, "mB/t (Sugested value for mod exploit handling = 1; this does not diminish steam return)").getInt();

	PowerSystem.loadConfig(c);
	c.save();
    }
}
