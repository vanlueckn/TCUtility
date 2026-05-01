package com.troblecodings.tcutility.blocks;

import com.troblecodings.tcutility.utils.BlockCreateInfo;

import net.minecraft.block.DoorBlock;
import net.minecraft.item.Item;

/**
 * 1.12.2 hatte hier eine custom 2x2-Block-Door-Implementierung.
 * Fuer den 1.14.4-Port als minimaler DoorBlock-Stub realisiert; die
 * grossen-Tore-Logik muss separat reimplementiert werden.
 */
public class TCBigDoor extends DoorBlock {

    protected Item item;

    public TCBigDoor(final BlockCreateInfo blockInfo) {
        super(blockInfo.toProperties());
    }

    public void setItem(final Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
