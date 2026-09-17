package com.toroidalworld.compat.scalablelux.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WorldFolds;
import com.toroidalworld.core.WorldLoopAttachments;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

@Mixin(targets = "ca.spottedleaf.starlight.common.light.StarLightInterface", remap = false)
public class StarLightInterfaceMixin {
    @Shadow
    @Final
    protected @Nullable Level world;

    @Unique
    private @Nullable WorldFold toroidal$transformer;

    @ModifyVariable(method = "getAnyChunkNow", at = @At("HEAD"), argsOnly = true, index = 1)
    private int toroidal$wrapChunkX(int chunkX) {
        return toroidal$transformer().chunkDomain(Direction.Axis.X).wrap(chunkX);
    }

    @ModifyVariable(method = "getAnyChunkNow", at = @At("HEAD"), argsOnly = true, index = 2)
    private int toroidal$wrapChunkZ(int chunkZ) {
        return toroidal$transformer().chunkDomain(Direction.Axis.Z).wrap(chunkZ);
    }

    @Unique
    private WorldFold toroidal$transformer() {
        if (this.toroidal$transformer == null) {
            this.toroidal$transformer = this.world == null
                    ? WorldFolds.NOOP
                    : WorldLoopAttachments.transformerOf(this.world);
        }

        return this.toroidal$transformer;
    }
}
