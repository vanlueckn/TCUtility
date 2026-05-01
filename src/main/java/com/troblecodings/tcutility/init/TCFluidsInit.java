package com.troblecodings.tcutility.init;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.troblecodings.tcutility.TCUtilityMain;
import com.troblecodings.tcutility.fluids.TCFluidBlock;
import com.troblecodings.tcutility.fluids.TCFluids;
import com.troblecodings.tcutility.utils.FluidCreateInfo;
import com.troblecodings.tcutility.utils.FluidProperties;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 1.14.4-Port: FluidRegistry und BlockFluidClassic gibt es nicht mehr; eine
 * vollstaendige Umstellung auf {@link net.minecraftforge.fluids.ForgeFlowingFluid}
 * mit Source / Flowing / FlowingFluidBlock / Bucket-Item ist noch offen.
 * Aktuell werden die in JSON definierten Fluids als statische Bloecke registriert,
 * damit das JSON-Schema 1:1 erhalten bleibt und Content Packs weiterhin geladen
 * werden koennen.
 */
@Mod.EventBusSubscriber(modid = TCUtilityMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TCFluidsInit {

    private TCFluidsInit() {
    }

    public static final ArrayList<Block> blocksToRegister = new ArrayList<>();

    @SubscribeEvent
    public static void registerBlock(final RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        blocksToRegister.forEach(registry::register);
    }

    @SubscribeEvent
    public static void registerItem(final RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        for (final Block block : blocksToRegister) {
            final ResourceLocation rl = block.getRegistryName();
            if (rl == null) {
                continue;
            }
            final BlockItem item = new BlockItem(block,
                    new Item.Properties().group(TCTabs.SPECIAL));
            item.setRegistryName(rl);
            registry.register(item);
        }
    }

    public static void initJsonFiles() {
        final Map<String, FluidProperties> fluids = getFromJson("fluiddefinitions");

        for (final Entry<String, FluidProperties> fluidsEntry : fluids.entrySet()) {
            final String objectname = fluidsEntry.getKey();
            final FluidProperties property = fluidsEntry.getValue();
            final FluidCreateInfo fluidInfo = property.getFluidInfo();

            final TCFluids fluid = new TCFluids(objectname,
                    new ResourceLocation(TCUtilityMain.MODID, "blocks/" + objectname + "_still"),
                    new ResourceLocation(TCUtilityMain.MODID, "blocks/" + objectname + "_flow"),
                    fluidInfo);

            final TCFluidBlock block = new TCFluidBlock(fluid);
            block.setRegistryName(new ResourceLocation(TCUtilityMain.MODID, objectname));
            blocksToRegister.add(block);
        }
    }

    private static Map<String, FluidProperties> getFromJson(final String directory) {
        final Gson gson = new Gson();
        final List<Entry<String, String>> entrySet = TCUtilityMain.fileHandler.getFiles(directory);
        final Map<String, FluidProperties> properties = new HashMap<>();
        final Type typeOfHashMap = new TypeToken<Map<String, FluidProperties>>() {
        }.getType();
        if (entrySet != null) {
            entrySet.forEach(entry -> {
                final Map<String, FluidProperties> json =
                        gson.fromJson(entry.getValue(), typeOfHashMap);
                if (json != null) {
                    properties.putAll(json);
                }
            });
        }
        return properties;
    }
}
