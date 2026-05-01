package com.troblecodings.tcutility.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.troblecodings.tcutility.TCUtilityMain;
import com.troblecodings.tcutility.blocks.TCBigDoor;
import com.troblecodings.tcutility.blocks.TCCube;
import com.troblecodings.tcutility.blocks.TCCubeRotation;
import com.troblecodings.tcutility.blocks.TCCubeRotationAll;
import com.troblecodings.tcutility.blocks.TCDoor;
import com.troblecodings.tcutility.blocks.TCFence;
import com.troblecodings.tcutility.blocks.TCFenceGate;
import com.troblecodings.tcutility.blocks.TCGarageDoor;
import com.troblecodings.tcutility.blocks.TCGarageGate;
import com.troblecodings.tcutility.blocks.TCHanging;
import com.troblecodings.tcutility.blocks.TCLadder;
import com.troblecodings.tcutility.blocks.TCSlab;
import com.troblecodings.tcutility.blocks.TCStairs;
import com.troblecodings.tcutility.blocks.TCTrapDoor;
import com.troblecodings.tcutility.blocks.TCWall;
import com.troblecodings.tcutility.blocks.TCWindow;
import com.troblecodings.tcutility.enums.BlockTypes;
import com.troblecodings.tcutility.items.TCBigDoorItem;
import com.troblecodings.tcutility.items.TCDoorItem;
import com.troblecodings.tcutility.items.TCSlabItem;
import com.troblecodings.tcutility.utils.BlockCreateInfo;
import com.troblecodings.tcutility.utils.BlockProperties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Sammelt im Mod-Konstruktor alle aus den JSON-Definitionen abgeleiteten
 * Block-Instanzen mit ihren ResourceLocations und registriert sie in
 * 1.19's {@link RegisterEvent}-Pattern (kein {@code setRegistryName} mehr).
 */
@Mod.EventBusSubscriber(modid = TCUtilityMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TCBlocks {

    private TCBlocks() {
    }

    /** Liste aller (Block + Name)-Tupel, die im RegisterEvent eingespielt werden. */
    public static final List<Entry<ResourceLocation, Block>> blockEntries = new ArrayList<>();
    /** Reine Block-Liste fuer Render-Layer-Setup und Compatibility-Lookups. */
    public static final List<Block> blocksToRegister = new ArrayList<>();

    public static void init() {
        // Reflection ueber static final Felder; falls in der Mod manuelle
        // Block-Definitionen ueber Felder kommen, werden sie hier eingesammelt.
        for (final Field field : TCBlocks.class.getFields()) {
            final int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && Modifier.isPublic(modifiers)) {
                final String name = field.getName().toLowerCase();
                try {
                    final Object value = field.get(null);
                    if (value instanceof Block) {
                        final Block block = (Block) value;
                        register(block, new ResourceLocation(TCUtilityMain.MODID, name));
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegister(final RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS)) {
            for (final Entry<ResourceLocation, Block> entry : blockEntries) {
                event.register(ForgeRegistries.Keys.BLOCKS, entry.getKey(), entry::getValue);
            }
            return;
        }
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS)) {
            for (final Entry<ResourceLocation, Block> entry : blockEntries) {
                final Block block = entry.getValue();
                // TCDoor und TCBigDoor bekommen ein separates DoubleHighBlockItem
                // (TCDoorItem / TCBigDoorItem), das in initJsonFiles erzeugt und
                // ueber TCItems registriert wird -- hier ueberspringen.
                if (block instanceof TCDoor || block instanceof TCBigDoor) {
                    continue;
                }
                final Item.Properties props = new Item.Properties().tab(groupFor(block));
                final BlockItem blockItem = (block instanceof TCSlab) ? new TCSlabItem(block)
                        : new BlockItem(block, props);
                event.register(ForgeRegistries.Keys.ITEMS, entry.getKey(), () -> blockItem);
            }
        }
    }

    private static CreativeModeTab groupFor(final Block block) {
        if (block instanceof TCSlab) {
            return TCTabs.SLABS;
        }
        if (block instanceof TCStairs) {
            return TCTabs.STAIRS;
        }
        if (block instanceof TCFence || block instanceof TCFenceGate) {
            return TCTabs.FENCE;
        }
        if (block instanceof TCGarageDoor || block instanceof TCGarageGate) {
            return TCTabs.DOORS;
        }
        if (block instanceof TCWindow) {
            return TCTabs.SPECIAL;
        }
        return TCTabs.BLOCKS;
    }

    public static void initJsonFiles() {
        final Map<String, BlockProperties> blocks = getFromJson("blockdefinitions");

        for (final Entry<String, BlockProperties> blocksEntry : blocks.entrySet()) {
            final String objectname = blocksEntry.getKey();
            final BlockProperties property = blocksEntry.getValue();
            final BlockCreateInfo blockInfo = property.getBlockInfo();
            final List<String> states = property.getStates();

            for (final String state : states) {
                final BlockTypes type = Enum.valueOf(BlockTypes.class, state.toUpperCase());
                final String registryName = type.getRegistryName(objectname);
                final ResourceLocation rl = new ResourceLocation(TCUtilityMain.MODID, registryName);

                switch (type) {
                    case CUBE:
                        register(new TCCube(blockInfo), rl);
                        break;
                    case CUBE_ROT:
                        register(new TCCubeRotation(blockInfo), rl);
                        break;
                    case STAIR:
                        register(new TCStairs(blockInfo), rl);
                        break;
                    case SLAB:
                        register(new TCSlab(blockInfo), rl);
                        break;
                    case FENCE:
                        register(new TCFence(blockInfo), rl);
                        break;
                    case FENCE_GATE:
                        register(new TCFenceGate(blockInfo), rl);
                        break;
                    case WALL:
                        register(new TCWall(blockInfo), rl);
                        break;
                    case TRAPDOOR:
                        register(new TCTrapDoor(blockInfo), rl);
                        break;
                    case WINDOW:
                        register(new TCWindow(blockInfo), rl);
                        break;
                    case LADDER:
                        register(new TCLadder(blockInfo), rl);
                        break;
                    case DOOR: {
                        final TCDoor door = new TCDoor(blockInfo);
                        register(door, rl);
                        final TCDoorItem dooritem = new TCDoorItem(door);
                        TCItems.addItem(dooritem,
                                new ResourceLocation(TCUtilityMain.MODID, "door_" + objectname));
                        break;
                    }
                    case BIGDOOR: {
                        final TCBigDoor bigdoor = new TCBigDoor(blockInfo);
                        register(bigdoor, rl);
                        final TCBigDoorItem bigdooritem = new TCBigDoorItem(bigdoor);
                        TCItems.addItem(bigdooritem,
                                new ResourceLocation(TCUtilityMain.MODID, "bigdoor_" + objectname));
                        break;
                    }
                    case HANGING:
                        register(new TCHanging(blockInfo), rl);
                        break;
                    case CUBE_ROT_ALL:
                        register(new TCCubeRotationAll(blockInfo), rl);
                        break;
                    case GARAGE: {
                        register(new TCGarageDoor(blockInfo), rl);
                        register(new TCGarageGate(blockInfo),
                                new ResourceLocation(TCUtilityMain.MODID, registryName + "_gate"));
                        break;
                    }
                    default:
                        throw new IllegalStateException(
                                "The given state " + state + " is not valid.");
                }
            }
        }
    }

    private static void register(final Block block, final ResourceLocation rl) {
        blockEntries.add(Map.entry(rl, block));
        blocksToRegister.add(block);
    }

    private static Map<String, BlockProperties> getFromJson(final String directory) {
        final Gson gson = new Gson();
        final List<Entry<String, String>> entrySet = TCUtilityMain.fileHandler.getFiles(directory);
        final Map<String, BlockProperties> properties = new HashMap<>();
        final Type typeOfHashMap = new TypeToken<Map<String, BlockProperties>>() {
        }.getType();
        if (entrySet != null) {
            entrySet.forEach(entry -> {
                final Map<String, BlockProperties> json =
                        gson.fromJson(entry.getValue(), typeOfHashMap);
                if (json != null) {
                    properties.putAll(json);
                }
            });
        }
        return properties;
    }
}
