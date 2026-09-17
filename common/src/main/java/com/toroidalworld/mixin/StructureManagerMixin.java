package com.toroidalworld.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.toroidalworld.accessors.FramedStructureStart;
import com.toroidalworld.accessors.LevelHolder;
import com.toroidalworld.core.WorldFold;
import com.toroidalworld.core.WorldLoopAttachments;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

@Mixin(StructureManager.class)
public class StructureManagerMixin {
    @Shadow
    @Final
    private LevelAccessor level;

    @WrapMethod(
            method = "startsForStructure(IILjava/util/function/Predicate;)Ljava/util/List;")
    private List<StructureStart> toroidal$startsInTheAskingChunksFrame(int chunkX, int chunkZ, Predicate<Structure> matcher,
            Operation<List<StructureStart>> original) {
        List<StructureStart> starts = original.call(chunkX, chunkZ, matcher);
        if (!(this.level instanceof WorldGenRegion region)) {
            return starts;
        }

        WorldFold transformer = WorldLoopAttachments.wrappedTransformerOf(((LevelHolder) region).toroidal$level());
        if (transformer != null && !starts.isEmpty()) {
            ChunkPos pos = new ChunkPos(chunkX, chunkZ);
            List<StructureStart> framed = new ArrayList<>(starts.size());
            for (StructureStart start : starts) {
                StructureStart inFrame = ((FramedStructureStart) (Object) start)
                        .toroidal$framedToward(region, transformer, pos);
                if (inFrame != null) {
                    framed.add(inFrame);
                }
            }

            starts = framed;
        }

        return starts;
    }
}
