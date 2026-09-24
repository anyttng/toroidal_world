package com.toroidalworld.compat.reterraforged.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.toroidalworld.compat.reterraforged.PoissonLattice;
import com.toroidalworld.compat.reterraforged.RtfLap;

import raccoonman.reterraforged.world.worldgen.feature.placement.poisson.FastPoisson;
import raccoonman.reterraforged.world.worldgen.feature.placement.poisson.FastPoissonContext;

@Mixin(value = FastPoisson.class, remap = false)
public abstract class FastPoissonMixin {
    @WrapMethod(method = "getPoint")
    private static long toroidal$closeOnLap(int seed, float x, float z, FastPoissonContext context,
            Operation<Long> original) {
        RtfLap.Frame frame = RtfLap.boundFrame();
        if (frame == null) {
            return original.call(seed, x, z, context);
        }

        return PoissonLattice.point(frame, seed, x, z, context);
    }
}
