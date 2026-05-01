package com.troblecodings.tcutility.init;

import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.troblecodings.tcutility.TCUtilityMain;
import com.troblecodings.tcutility.fluids.TCFluidBlock;
import com.troblecodings.tcutility.fluids.TCFluids;
import com.troblecodings.tcutility.utils.FluidCreateInfo;
import com.troblecodings.tcutility.utils.FluidProperties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * 1.19.2-Fluid-Pipeline. Pro JSON-Eintrag werden ein {@link FluidType} (neu
 * in 1.18+, ersetzt {@code FluidAttributes}), eine
 * {@link ForgeFlowingFluid.Source}, eine {@link ForgeFlowingFluid.Flowing},
 * ein {@link TCFluidBlock} und ein {@link BucketItem} angelegt. Die
 * zirkulaeren Suppliers werden weiter ueber {@link AtomicReference}-Holder
 * verbunden; FluidType-, Fluid-, Block- und Item-Registrierung passieren
 * im neuen {@link RegisterEvent}-Pattern.
 */
@Mod.EventBusSubscriber(modid = TCUtilityMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TCFluidsInit {

    private TCFluidsInit() {
    }

    public static final List<TCFluids> fluidSets = new ArrayList<>();
    public static final List<Entry<ResourceLocation, FluidType>> fluidTypeEntries =
            new ArrayList<>();
    public static final List<Entry<ResourceLocation, Fluid>> fluidEntries = new ArrayList<>();
    public static final List<Entry<ResourceLocation, Block>> blockEntries = new ArrayList<>();
    public static final List<Entry<ResourceLocation, Item>> itemEntries = new ArrayList<>();
    /** Reine Block-Liste fuer das client-seitige Render-Layer-Setup. */
    public static final List<Block> blocksToRegister = new ArrayList<>();

    @SubscribeEvent
    public static void onRegister(final RegisterEvent event) {
        event.register(ForgeRegistries.Keys.FLUID_TYPES, helper -> {
            for (final Entry<ResourceLocation, FluidType> e : fluidTypeEntries) {
                helper.register(e.getKey(), e.getValue());
            }
        });
        event.register(ForgeRegistries.Keys.FLUIDS, helper -> {
            for (final Entry<ResourceLocation, Fluid> e : fluidEntries) {
                helper.register(e.getKey(), e.getValue());
            }
        });
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            for (final Entry<ResourceLocation, Block> e : blockEntries) {
                helper.register(e.getKey(), e.getValue());
            }
        });
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            for (final Entry<ResourceLocation, Item> e : itemEntries) {
                helper.register(e.getKey(), e.getValue());
            }
        });
    }

    public static void initJsonFiles() {
        final Map<String, FluidProperties> fluids = getFromJson("fluiddefinitions");
        for (final Entry<String, FluidProperties> entry : fluids.entrySet()) {
            buildFluid(entry.getKey(), entry.getValue());
        }
    }

    private static void buildFluid(final String name, final FluidProperties properties) {
        final FluidCreateInfo info = properties.getFluidInfo();
        final ResourceLocation rlType = new ResourceLocation(TCUtilityMain.MODID, name + "_type");
        final ResourceLocation rlSource = new ResourceLocation(TCUtilityMain.MODID, name);
        final ResourceLocation rlFlowing = new ResourceLocation(TCUtilityMain.MODID,
                name + "_flowing");
        final ResourceLocation rlBucket = new ResourceLocation(TCUtilityMain.MODID,
                name + "_bucket");

        final FluidType.Properties typeProps = FluidType.Properties.create()
                .density(info.density)
                .viscosity(info.viscosity)
                .lightLevel(info.luminosity)
                .temperature(info.temperature);
        final FluidType fluidType = new FluidType(typeProps);

        // Holder pro Komponente, weil ForgeFlowingFluid.Properties die
        // anderen Komponenten als Supplier ueber gegenseitige Referenzen
        // einbindet (Source <-> Flowing <-> Block <-> Bucket).
        final AtomicReference<FlowingFluid> sourceRef = new AtomicReference<>();
        final AtomicReference<FlowingFluid> flowingRef = new AtomicReference<>();
        final AtomicReference<LiquidBlock> blockRef = new AtomicReference<>();
        final AtomicReference<Item> bucketRef = new AtomicReference<>();

        final ForgeFlowingFluid.Properties fluidProps = new ForgeFlowingFluid.Properties(
                () -> fluidType, sourceRef::get, flowingRef::get)
                        .block(blockRef::get)
                        .bucket(bucketRef::get)
                        .slopeFindDistance(Math.max(1, info.flowLength))
                        .tickRate(20)
                        .explosionResistance(100f);

        final ForgeFlowingFluid.Source source = new ForgeFlowingFluid.Source(fluidProps);
        sourceRef.set(source);

        final ForgeFlowingFluid.Flowing flowing = new ForgeFlowingFluid.Flowing(fluidProps);
        flowingRef.set(flowing);

        final TCFluidBlock block = new TCFluidBlock(sourceRef::get,
                BlockBehaviour.Properties.of(Material.WATER)
                        .noCollission()
                        .strength(100f)
                        .noLootTable()
                        .lightLevel(state -> info.luminosity),
                info.effect, info.effectDuration, info.effectAmplifier);
        blockRef.set(block);

        final BucketItem bucket = new BucketItem(sourceRef::get,
                new Item.Properties().tab(TCTabs.SPECIAL).stacksTo(1)
                        .craftRemainder(Items.BUCKET));
        bucketRef.set(bucket);

        fluidSets.add(new TCFluids(name, source, flowing, block, bucket));
        fluidTypeEntries.add(new AbstractMap.SimpleImmutableEntry<>(rlType, fluidType));
        fluidEntries.add(new AbstractMap.SimpleImmutableEntry<>(rlSource, source));
        fluidEntries.add(new AbstractMap.SimpleImmutableEntry<>(rlFlowing, flowing));
        blockEntries.add(new AbstractMap.SimpleImmutableEntry<>(rlSource, block));
        blocksToRegister.add(block);
        itemEntries.add(new AbstractMap.SimpleImmutableEntry<>(rlBucket, bucket));
    }

    private static Map<String, FluidProperties> getFromJson(final String directory) {
        final Gson gson = new Gson();
        final List<Entry<String, String>> entrySet = TCUtilityMain.fileHandler.getFiles(directory);
        final Map<String, FluidProperties> result = new HashMap<>();
        final Type typeOfHashMap = new TypeToken<Map<String, FluidProperties>>() {
        }.getType();
        if (entrySet != null) {
            entrySet.forEach(entry -> {
                final Map<String, FluidProperties> json = gson.fromJson(entry.getValue(),
                        typeOfHashMap);
                if (json != null) {
                    result.putAll(json);
                }
            });
        }
        return result;
    }
}
