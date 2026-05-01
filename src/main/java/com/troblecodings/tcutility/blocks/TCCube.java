package com.troblecodings.tcutility.blocks;

import com.troblecodings.tcutility.utils.BlockCreateInfo;

import net.minecraft.block.Block;

public class TCCube extends Block {

    public TCCube(final BlockCreateInfo blockInfo) {
        super(blockInfo.toProperties());
    }
}
