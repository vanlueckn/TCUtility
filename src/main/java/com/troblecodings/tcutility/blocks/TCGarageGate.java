package com.troblecodings.tcutility.blocks;

import com.troblecodings.tcutility.utils.BlockCreateInfo;

/**
 * Garage-Gate ist im 1.12.2-Original eigentlich ein rotierter Cube
 * (Hitbox dreht mit FACING), kein Vanilla-FenceGate. Daher von
 * {@link TCCubeRotation} ableiten -- damit kommen FACING-State,
 * direction-spezifische Hitbox und rotate/mirror "for free" mit.
 */
public class TCGarageGate extends TCCubeRotation {

    public TCGarageGate(final BlockCreateInfo blockInfo) {
        super(blockInfo);
    }
}
