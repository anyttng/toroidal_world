package com.toroidalworld.compat.biolith;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.engine.noise.GenerationTransformerContext;
import com.toroidalworld.engine.noise.GenerationTransformerContext.Context;
import com.toroidalworld.engine.noise.PeriodicNoiseSampler;

import net.minecraft.core.QuartPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

public final class ReplacementNoiseFold {
    public static final double NOT_FOLDED = Double.NaN;

    private static final double OPEN_SIMPLEX_AMPLITUDE = 1.848;

    private static final double FLAT_Y = 0.0;

    private static final double NO_Y_SCALE = 0.0;

    private static final double NO_FUDGE = 0.0;

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
        Context context = GenerationTransformerContext.context();
        WorldFold fold = context.wrappedTransformer();
        if (fold == null) {
            return NOT_FOLDED;
        }

        Octaves resolved = octavesFor(seed);
        double x = QuartPos.toBlock(quartX);
        double z = QuartPos.toBlock(quartZ);
        double sum = 0.0;
        for (int octave = 0; octave < this.weights.length; octave++) {
            ImprovedNoise noise = resolved.noises()[octave];
            double scale = 1.0 / (scaleQuarts[octave] * BLOCKS_PER_QUART);
            try (Context.ScaleScope _ = context.withScale(scale)) {
                sum += this.weights[octave] * PeriodicNoiseSampler.sample(noise.p, noise.xo, noise.yo, noise.zo,
                        fold, context, x, FLAT_Y, z, NO_Y_SCALE, NO_FUDGE);
            }
        }

        return sum * OPEN_SIMPLEX_AMPLITUDE;
    }

    private Octaves octavesFor(long seed) {
        Octaves held = this.octaves;
        if (held != null && held.seed() == seed) {
            return held;
        }

        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(seed));
        ImprovedNoise[] noises = new ImprovedNoise[this.weights.length];
        for (int octave = 0; octave < noises.length; octave++) {
            noises[octave] = new ImprovedNoise(random);
        }

        Octaves built = new Octaves(seed, noises);
        this.octaves = built;
        return built;
    }

    private record Octaves(long seed, ImprovedNoise[] noises) {
    }
}
