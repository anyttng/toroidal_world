package com.toroidalworld.compat.distanthorizons.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.toroidalworld.compat.distanthorizons.DhGenerators;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.seibel.distanthorizons.api.interfaces.override.worldGenerator.IDhApiWorldGenerator;
import com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper;
import com.seibel.distanthorizons.coreapi.DependencyInjection.WorldGeneratorInjector;

@Mixin(WorldGeneratorInjector.class)
public class WorldGeneratorInjectorMixin {
    @WrapMethod(method = "bind")
    private void toroidal$keepDhGeneratorOnShapedLevels(IDhApiLevelWrapper level, IDhApiWorldGenerator generator,
            Operation<Void> original) {
        DhGenerators.requireOwn(level, generator);
        original.call(level, generator);
    }
}
