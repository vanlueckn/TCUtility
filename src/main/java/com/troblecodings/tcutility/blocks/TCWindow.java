package com.troblecodings.tcutility.blocks;

import com.troblecodings.tcutility.utils.BlockCreateInfo;

import net.minecraft.block.GlassBlock;

public class TCWindow extends GlassBlock {

    public TCWindow(final BlockCreateInfo blockInfo) {
        super(blockInfo.toProperties());
    }
}
