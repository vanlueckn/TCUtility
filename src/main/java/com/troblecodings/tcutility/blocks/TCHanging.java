package com.troblecodings.tcutility.blocks;

import com.troblecodings.tcutility.utils.BlockCreateInfo;

import net.minecraft.block.Block;

/**
 * Original 1.12.2 hatte custom hanging-block-Logik. Hier als minimaler
 * Block-Stub fuer den 1.14.4-Port; spezifische Aufhaengungs-Mechanik
 * muss separat reimplementiert werden.
 */
public class TCHanging extends Block {

    public TCHanging(final BlockCreateInfo blockInfo) {
        super(blockInfo.toProperties());
    }
}
