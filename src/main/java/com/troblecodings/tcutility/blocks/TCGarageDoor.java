package com.troblecodings.tcutility.blocks;

import com.troblecodings.tcutility.utils.BlockCreateInfo;

import net.minecraft.block.DoorBlock;
import net.minecraft.item.Item;

/**
 * 1.12.2 hatte hier eine custom Garage-Door-Logik. Stub fuer den
 * 1.14.4-Port.
 */
public class TCGarageDoor extends DoorBlock {

    protected Item item;

    public TCGarageDoor(final BlockCreateInfo blockInfo) {
        super(blockInfo.toProperties());
    }

    public void setItem(final Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
