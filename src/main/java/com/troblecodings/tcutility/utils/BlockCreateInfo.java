package com.troblecodings.tcutility.utils;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockCreateInfo {

    public final Material material;
    public final float hardness;
    public final SoundType soundtype;
    public final int opacity;
    public final int lightValue;
    public final List<Integer> box;
    public final boolean fullblock;

    public BlockCreateInfo(final Material material, final float hardness, final SoundType soundtype,
            final int opacity, final int lightValue, final List<Integer> box,
            final boolean fullblock) {
        this.material = material;
        this.hardness = hardness;
        this.soundtype = soundtype;
        this.opacity = opacity;
        this.lightValue = lightValue;
        this.box = box;
        this.fullblock = fullblock;
    }

    public Block.Properties toProperties() {
        // 1.14.4-28.2.28: notSolid() / nonOpaque() ist nicht verfuegbar; das
        // fullblock-Flag wird hier nur in den Properties weggetragen, der Block
        // bleibt vorerst opaque. Das laesst sich pro Block-Subklasse via
        // Block.Properties#noOcclusion (sobald verfuegbar) oder eigene Override
        // nachschaerfen.
        return Block.Properties.create(material)
                .hardnessAndResistance(hardness)
                .sound(soundtype)
                .lightValue(lightValue);
    }
}
