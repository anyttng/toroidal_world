package com.toroidalworld.shape.torus;

import java.util.Collections;

import com.toroidalworld.accessors.ClimateCompressionCache;
import com.toroidalworld.core.WorldFold;
import com.toroidalworld.engine.noise.ClimateScaleCompression;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.synth.NoiseStack;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class ClimateCompression {
    private static final double UNMODIFIED_AMPLITUDE = 1.0;

    private static final double LN_2 = Math.log(2.0);

    public record Resolved(WorldFold fold, double baseScale, double verticalShare, double factor) {
        boolean covers(WorldFold fold, double baseScale, double verticalShare) {
            return this.fold == fold && this.baseScale == baseScale && this.verticalShare == verticalShare;
        }
    }

    public static double resolve(ClimateCompressionCache cache, WorldFold fold, boolean climateField,
            DoubleList amplitudes, double lowestFreqInputFactor, double baseScale, double verticalShare) {
        Resolved resolved = cache.toroidal$climateCompression();
        if (resolved == null || !resolved.covers(fold, baseScale, verticalShare)) {
            resolved = new Resolved(fold, baseScale, verticalShare,
                    factor(fold, climateField, amplitudes, lowestFreqInputFactor, baseScale, verticalShare));
            cache.toroidal$climateCompression(resolved);
        }

        return resolved.factor();
    }

    public static double warpDivisor(Holder<NormalNoise> noise, WorldFold fold, double xzScale,
            double verticalShare) {
        return xzScale * factor(fold, noise, xzScale, verticalShare);
    }

    public static double[] layerFactors(WorldFold fold, Holder<NormalNoise> noise, NoiseStack stack,
            double baseScale, double verticalShare) {
        NoiseStack.Layer[] layers = stack.layers;
        double[] factors = new double[layers.length];
        for (int i = 0; i < layers.length; i++) {
            factors[i] = factor(fold, noise, baseScale * detuneOf(layers[i].frequency()), verticalShare);
        }

        return factors;
    }

    public static double factor(WorldFold fold, Holder<NormalNoise> noise, double baseScale, double verticalShare) {
        NormalNoise.Parameters parameters = noise.value().parameters;
        boolean climateField = noise.unwrapKey().filter(ClimateFields::isClimate).isPresent();
        return factor(fold, climateField, octaveAmplitudes(parameters), Math.pow(2.0, parameters.baseOctave()),
                baseScale, verticalShare);
    }

    public static double factor(WorldFold fold, boolean climateField, DoubleList amplitudes,
            double lowestFreqInputFactor, double baseScale, double verticalShare) {
        ClimateScale scale = fold.generationOptions().get(CompactBiomes.OPTION);
        if (scale.mode() == ClimateScale.Mode.OFF
                || !ClimateScaleCompression.compressible(fold, verticalShare)) {
            return ClimateScaleCompression.NO_COMPRESSION;
        }

        if (scale.isFixed() && !climateField) {
            return ClimateScaleCompression.NO_COMPRESSION;
        }

        if (scale.mode() == ClimateScale.Mode.CUSTOM) {
            return scale.factor();
        }

        double fitted = ClimateScaleCompression.fitted(fold, amplitudes, lowestFreqInputFactor, baseScale);
        return scale.mode() == ClimateScale.Mode.STRONG
                ? Math.max(fitted, ClimateScale.STRONG_FACTOR)
                : fitted;
    }

    private static DoubleList octaveAmplitudes(NormalNoise.Parameters parameters) {
        return parameters.amplitudeModifiers().isEmpty()
                ? new DoubleArrayList(Collections.nCopies(parameters.octaveCount(), UNMODIFIED_AMPLITUDE))
                : parameters.amplitudeModifiers();
    }

    private static double detuneOf(double frequency) {
        return frequency / Math.pow(2.0, Math.round(Math.log(frequency) / LN_2));
    }

    private ClimateCompression() {
    }
}
