package com.troblecodings.tcutility.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.troblecodings.tcutility.TCUtilityMain;
import com.troblecodings.tcutility.enums.ArmorTypes;
import com.troblecodings.tcutility.utils.ArmorCreateInfo;
import com.troblecodings.tcutility.utils.ArmorProperties;
import com.troblecodings.tcutility.utils.ItemProperties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = TCUtilityMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TCItems {

    private TCItems() {
    }

    /** (ResourceLocation, Item)-Tupel, die im RegisterEvent eingespielt werden. */
    public static final List<Entry<ResourceLocation, Item>> itemEntries = new ArrayList<>();

    public static void addItem(final Item item, final ResourceLocation rl) {
        itemEntries.add(new AbstractMap.SimpleImmutableEntry<>(rl, item));
    }

    public static void init() {
        for (final Field field : TCItems.class.getFields()) {
            final int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && Modifier.isPublic(modifiers)) {
                final String name = field.getName().toLowerCase();
                try {
                    final Object value = field.get(null);
                    if (value instanceof Item) {
                        addItem((Item) value, new ResourceLocation(TCUtilityMain.MODID, name));
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegister(final RegisterEvent event) {
        if (!event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS)) {
            return;
        }
        for (final Entry<ResourceLocation, Item> entry : itemEntries) {
            event.register(ForgeRegistries.Keys.ITEMS, entry.getKey(), entry::getValue);
        }
    }

    public static void initJsonFiles() {
        final Map<String, ArmorProperties> armor = getArmorFromJson("armordefinitions");

        for (final Entry<String, ArmorProperties> armorEntry : armor.entrySet()) {
            final String armorName = armorEntry.getKey();
            final ArmorProperties property = armorEntry.getValue();
            final ArmorCreateInfo armorInfo = property.getArmorInfo();
            final ArmorMaterial material = makeArmorMaterial(armorName, armorInfo);
            final List<String> slots = property.getSlots();

            for (final String slot : slots) {
                final ArmorTypes type = Enum.valueOf(ArmorTypes.class, slot.toUpperCase());
                final String registryName = type.getRegistryName(armorName);
                final EquipmentSlot equipSlot = mapSlot(type);
                final ArmorItem armorItem = new ArmorItem(material, equipSlot,
                        new Item.Properties().tab(CreativeModeTab.TAB_COMBAT));
                addItem(armorItem, new ResourceLocation(TCUtilityMain.MODID, registryName));
            }
        }

        final Map<String, ItemProperties> items = getItemFromJson("itemdefinitions");

        for (final Entry<String, ItemProperties> itemEntry : items.entrySet()) {
            final String itemName = itemEntry.getKey();
            final Item item = new Item(new Item.Properties().tab(TCTabs.ITEMS));
            addItem(item, new ResourceLocation(TCUtilityMain.MODID, itemName));
        }
    }

    private static EquipmentSlot mapSlot(final ArmorTypes type) {
        switch (type) {
            case HEAD:
                return EquipmentSlot.HEAD;
            case CHEST:
                return EquipmentSlot.CHEST;
            case LEGS:
                return EquipmentSlot.LEGS;
            case FEET:
                return EquipmentSlot.FEET;
            default:
                throw new IllegalStateException("Unknown armor slot " + type);
        }
    }

    /**
     * 1.19.2 hat keinen EnumHelper.addArmorMaterial; stattdessen wird ein
     * {@link ArmorMaterial} pro Material instanziert.
     */
    private static ArmorMaterial makeArmorMaterial(final String name,
            final ArmorCreateInfo info) {
        final String texturePrefix = TCUtilityMain.MODID + ":" + name;
        return new ArmorMaterial() {
            @Override
            public int getDurabilityForSlot(final EquipmentSlot slot) {
                return info.durability;
            }

            @Override
            public int getDefenseForSlot(final EquipmentSlot slot) {
                return 1;
            }

            @Override
            public int getEnchantmentValue() {
                return info.enchantability;
            }

            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_GENERIC;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.EMPTY;
            }

            @Override
            public String getName() {
                return texturePrefix;
            }

            @Override
            public float getToughness() {
                return info.toughness;
            }

            @Override
            public float getKnockbackResistance() {
                return 0.0F;
            }
        };
    }

    private static Map<String, ArmorProperties> getArmorFromJson(final String directory) {
        return parseJson(directory, new TypeToken<Map<String, ArmorProperties>>() {
        }.getType());
    }

    private static Map<String, ItemProperties> getItemFromJson(final String directory) {
        return parseJson(directory, new TypeToken<Map<String, ItemProperties>>() {
        }.getType());
    }

    private static <V> Map<String, V> parseJson(final String directory, final Type typeOfHashMap) {
        final Gson gson = new Gson();
        final List<Entry<String, String>> entrySet = TCUtilityMain.fileHandler.getFiles(directory);
        final Map<String, V> properties = new HashMap<>();
        if (entrySet != null) {
            entrySet.forEach(entry -> {
                final Map<String, V> json = gson.fromJson(entry.getValue(), typeOfHashMap);
                if (json != null) {
                    properties.putAll(json);
                }
            });
        }
        return properties;
    }
}
