package com.troblecodings.tcutility.init;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TCTabs {

    private TCTabs() {
    }

    /**
     * No-op-Aufruf, der das Klassen-Loading von {@link TCTabs} im Mod-
     * Konstruktor erzwingt. Dadurch landen alle Custom-{@link CreativeModeTab}
     * Instanzen via ihrer Static-Initializer in {@code CreativeModeTab.TABS},
     * bevor das Creative-Inventory-UI sie zum ersten Mal scannt.
     */
    public static void touch() {
    }

    public static final CreativeModeTab SPECIAL = new CreativeModeTab("tcspecial") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.GLASS_PANE);
        }
    };

    public static final CreativeModeTab BLOCKS = new CreativeModeTab("tcblocks") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.OAK_PLANKS);
        }
    };

    public static final CreativeModeTab SLABS = new CreativeModeTab("tcslabs") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.OAK_SLAB);
        }
    };

    public static final CreativeModeTab STAIRS = new CreativeModeTab("tcstairs") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.OAK_STAIRS);
        }
    };

    public static final CreativeModeTab FENCE = new CreativeModeTab("tcfence") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.OAK_FENCE);
        }
    };

    public static final CreativeModeTab DOORS = new CreativeModeTab("tcdoors") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.OAK_DOOR);
        }
    };

    public static final CreativeModeTab ITEMS = new CreativeModeTab("tcitems") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.PAPER);
        }
    };
}
