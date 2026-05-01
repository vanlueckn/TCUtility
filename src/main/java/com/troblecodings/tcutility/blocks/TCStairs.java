package com.troblecodings.tcutility.blocks;

import com.troblecodings.tcutility.utils.BlockCreateInfo;

import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;

public class TCStairs extends StairsBlock {

    public TCStairs(final BlockCreateInfo blockInfo) {
        super(() -> Blocks.OAK_PLANKS.getDefaultState(), blockInfo.toProperties());
    }
}
