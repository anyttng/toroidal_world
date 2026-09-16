package com.toroidalworld.engine.noise;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.core.WorldFold;

import net.minecraft.world.level.levelgen.synth.GradientNoise;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NoiseStack;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SmearedPerlinNoise;

public final class PeriodicOctaveSampler {
    public static float sample(WorldFold transformer, NoiseFrame frame, double scale, NoiseStack stack,
            double x, double y, double z) {
        return sample(transformer, frame, scale, null, stack, x, y, z);
    }

    @SuppressWarnings("deprecation")
    public static float sample(WorldFold transformer, NoiseFrame frame, double scale, double @Nullable [] layerFactors,
            NoiseStack stack, double x, double y, double z) {
        SlotAxes axes = frame.axes();
        NoiseStack.Layer[] layers = stack.layers;
        float value = 0.0F;
        for (int i = 0; i < layers.length; i++) {
            NoiseStack.Layer layer = layers[i];
            Noise leaf = layer.noise();
            double frequency = layer.frequency();
            double layerScale = layerFactors == null ? scale * frequency : scale * layerFactors[i] * frequency;
            double slotX = slotInput(axes.x(), x, frequency);
            double slotY = slotInput(axes.y(), y, frequency);
            double slotZ = slotInput(axes.z(), z, frequency);
            float sample;
            if (leaf.getClass() == PerlinNoise.class) {
                PerlinNoise perlin = (PerlinNoise) leaf;
                sample = PeriodicNoiseSampler.sample(perlin.perms, perlin.offsetX, perlin.offsetY, perlin.offsetZ,
                        transformer, frame, layerScale, slotX, slotY, slotZ);
            } else if (leaf.getClass() == SmearedPerlinNoise.class) {
                SmearedPerlinNoise smeared = (SmearedPerlinNoise) leaf;
                sample = PeriodicNoiseSampler.sampleSmeared(smeared.perms, smeared.offsetX, smeared.offsetY,
                        smeared.offsetZ, transformer, frame, layerScale, slotX, slotY, slotZ, y * frequency,
                        smeared.fudgeYScale);
            } else {
                throw new IllegalArgumentException("Periodic octaves sample Perlin leaves only, got "
                        + leaf.getClass().getName());
            }

            value += layer.amplitude() * sample;
        }

        return value;
    }

    private static double slotInput(SlotAxis axis, double coord, double frequency) {
        return axis.carriesWorldAxis() ? coord : GradientNoise.wrap(coord * frequency);
    }

    private PeriodicOctaveSampler() {
    }
}
