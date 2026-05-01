package com.troblecodings.tcutility.blocks;

import com.troblecodings.tcutility.utils.BlockCreateInfo;

import net.minecraft.block.FenceGateBlock;

/**
 * 1.12.2 hatte hier eine custom Garage-Gate-Logik. Stub fuer den
 * 1.14.4-Port.
 */
public class TCGarageGate extends FenceGateBlock {

    public TCGarageGate(final BlockCreateInfo blockInfo) {
        super(blockInfo.toProperties());
    }
}
