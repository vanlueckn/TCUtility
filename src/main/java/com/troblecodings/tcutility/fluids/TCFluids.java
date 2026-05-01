package com.troblecodings.tcutility.fluids;

import com.troblecodings.tcutility.utils.FluidCreateInfo;

import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;

/**
 * Fluid-Datenholder fuer den 1.14.4-Port. In 1.14 wurde FluidRegistry
 * abgeschafft; eine vollstaendige Implementierung wuerde ueber
 * ForgeFlowingFluid.Source / .Flowing und FlowingFluidBlock laufen. Hier
 * wird das JSON-Schema 1:1 erhalten, die tatsaechliche Fluid-Registrierung
 * muss separat reimplementiert werden.
 */
public class TCFluids {

    public final String fluidName;
    public final ResourceLocation still;
    public final ResourceLocation flowing;
    public final int density;
    public final int temperature;
    public final int viscosity;
    public final int luminosity;
    public final int flowLength;
    public final boolean canCreateSource;
    public final Effect effect;
    public final int effectDuration;
    public final int effectAmplifier;

    public TCFluids(final String fluidName, final ResourceLocation still,
            final ResourceLocation flowing, final FluidCreateInfo fluidInfo) {
        this.fluidName = fluidName;
        this.still = still;
        this.flowing = flowing;
        this.density = fluidInfo.density;
        this.temperature = fluidInfo.temperature;
        this.viscosity = fluidInfo.viscosity;
        this.luminosity = fluidInfo.luminosity;
        this.flowLength = fluidInfo.flowLength;
        this.canCreateSource = fluidInfo.canCreateSource;
        this.effect = fluidInfo.effect;
        this.effectDuration = fluidInfo.effectDuration;
        this.effectAmplifier = fluidInfo.effectAmplifier;
    }

    public String getName() {
        return fluidName;
    }
}
