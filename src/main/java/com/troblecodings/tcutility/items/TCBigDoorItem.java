package com.troblecodings.tcutility.items;

import com.troblecodings.tcutility.blocks.TCBigDoor;
import com.troblecodings.tcutility.init.TCTabs;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.TallBlockItem;

public class TCBigDoorItem extends TallBlockItem {

    public TCBigDoorItem(final Block block) {
        super(block, new Item.Properties().group(TCTabs.DOORS));
        if (block instanceof TCBigDoor) {
            ((TCBigDoor) block).setItem(this);
        }
    }
}
