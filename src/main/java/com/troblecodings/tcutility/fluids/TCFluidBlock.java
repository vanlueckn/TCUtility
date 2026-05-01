package com.troblecodings.tcutility.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * 1.12.2 hatte hier BlockFluidClassic mit Quanta/Flow-Logik. Fuer den
 * 1.14.4-Port als statischer Block-Stub realisiert; richtige
 * FlowingFluidBlock-Mechanik muss separat reimplementiert werden, sobald
 * das Fluid-Pipeline auf ForgeFlowingFluid umgestellt wird.
 */
public class TCFluidBlock extends Block {

    public final TCFluids fluid;

    public TCFluidBlock(final TCFluids fluid) {
        super(Block.Properties.create(Material.WATER)
                .doesNotBlockMovement()
                .hardnessAndResistance(100.0F)
                .noDrops()
                .sound(SoundType.SLIME)
                .lightValue(fluid.luminosity));
        this.fluid = fluid;
    }
}
