package com.toroidalworld.compat.biolith;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.engine.noise.GenerationTransformerContext;
import com.toroidalworld.engine.noise.LapFloor;
import com.toroidalworld.engine.noise.NoiseFrame;
import com.toroidalworld.engine.noise.PeriodicNoiseSampler;

import net.minecraft.core.QuartPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public final class ReplacementNoiseFold {
    public static final double NOT_FOLDED = Double.NaN;

    private static final NoiseFrame UNDAMPED_FRAME = NoiseFrame.UNDECLARED;

    private static final double OPEN_SIMPLEX_AMPLITUDE = 1.848;

    private static final double FLAT_Y = 0.0;

    private static final double BLOCKS_PER_QUART = 4.0;

    private final double[] weights;

    private volatile @Nullable Octaves octaves;

    public ReplacementNoiseFold(double... weights) {
        this.weights = weights.clone();
    }

    public static boolean folded(double sum) {
        return !Double.isNaN(sum);
    }

    public double sum(long seed, double[] scaleQuarts, int quartX, int quartZ) {
        WorldFold fold = GenerationTransformerContext.context().wrappedTransformer();
        if (fold == null) {
            return NOT_FOLDED;
        }

        Octaves resolved = octavesFor(seed);
        double x = QuartPos.toBlock(quartX);
        double z = QuartPos.toBlock(quartZ);
        double sum = 0.0;
        for (int octave = 0; octave < this.weights.length; octave++) {
            PerlinNoise noise = resolved.noises()[octave];
            double scale = 1.0 / (scaleQuarts[octave] * BLOCKS_PER_QUART);
            sum += this.weights[octave] * PeriodicNoiseSampler.sample(noise.perms, noise.offsetX, noise.offsetY,
                    noise.offsetZ, fold, UNDAMPED_FRAME, scale, x, FLAT_Y, z, LapFloor.of(fold));
        }

        return sum * OPEN_SIMPLEX_AMPLITUDE;
    }

    private Octaves octavesFor(long seed) {
        Octaves held = this.octaves;
        if (held != null && held.seed() == seed) {
            return held;
        }

        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(seed));
        PerlinNoise[] noises = new PerlinNoise[this.weights.length];
        for (int octave = 0; octave < noises.length; octave++) {
            noises[octave] = new PerlinNoise(random);
        }

        Octaves built = new Octaves(seed, noises);
        this.octaves = built;
        return built;
    }

    private record Octaves(long seed, PerlinNoise[] noises) {
    }
}
