package com.troblecodings.tcutility.fluids;

import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.BucketItem;
import net.minecraftforge.fluids.ForgeFlowingFluid;

/**
 * Bundle der vier Komponenten, aus denen ein Custom-Fluid in 1.14.4 besteht:
 * eine Quell-Variante ({@link ForgeFlowingFluid.Source}), eine Flow-Variante
 * ({@link ForgeFlowingFluid.Flowing}), der zugehoerige Block
 * ({@link FlowingFluidBlock}) und das Bucket-Item ({@link BucketItem}).
 *
 * Wird von {@link com.troblecodings.tcutility.init.TCFluidsInit} pro JSON-
 * Eintrag aus {@code fluiddefinitions/} erzeugt.
 */
public class TCFluids {

    public final String name;
    public final ForgeFlowingFluid.Source source;
    public final ForgeFlowingFluid.Flowing flowing;
    public final FlowingFluidBlock block;
    public final BucketItem bucket;

    public TCFluids(final String name, final ForgeFlowingFluid.Source source,
            final ForgeFlowingFluid.Flowing flowing, final FlowingFluidBlock block,
            final BucketItem bucket) {
        this.name = name;
        this.source = source;
        this.flowing = flowing;
        this.block = block;
        this.bucket = bucket;
    }
}
