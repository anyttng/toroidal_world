package com.toroidalworld.compat.journeymap.mixin;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.Collection;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.toroidalworld.compat.MapCopies;
import com.toroidalworld.compat.journeymap.JourneyMapFold;
import com.toroidalworld.compat.journeymap.JourneyMapSeamPass;

import journeymap.api.v2.client.util.UIState;
import journeymap.api.v2.common.Context;
import journeymap.client.model.map.MapType;
import journeymap.client.model.region.RegionCoord;
import journeymap.client.model.region.RegionImageCache;
import journeymap.client.model.region.RegionImageSet;
import journeymap.client.render.JmRenderRouter;
import journeymap.client.render.map.RegionTile;
import journeymap.client.render.map.TileGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.ColoredRectangleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

@Mixin(targets = "journeymap.client.render.map.MapRenderer", remap = false)
public abstract class MapRendererMixin implements JourneyMapSeamPass {
    @Unique
    private static final String CENTER = "center(Ljava/io/File;Ljourneymap/client/model/map/MapType;DDI)Z";

    @Unique
    private static final String BLOCK_PIXEL_IN_GRID =
            "getBlockPixelInGrid(Lnet/minecraft/core/BlockPos;)Ljava/awt/geom/Point2D$Double;";

    @Unique
    private static final String BLOCK_COORD_PIXEL_IN_GRID = "getBlockPixelInGrid(DD)Ljava/awt/geom/Point2D$Double;";

    @Unique
    private static final String MOVE = "move(DD)V";

    @Shadow(remap = false)
    protected double centerBlockX;

    @Shadow(remap = false)
    protected double centerBlockZ;

    @Shadow(remap = false)
    protected int zoom;

    @Shadow(remap = false)
    private volatile File worldDir;

    @Shadow(remap = false)
    @Final
    TileGrid<RegionCoord, RegionTile> regions;

    @Shadow(remap = false)
    public abstract void clear();

    @Shadow(remap = false)
    public abstract UIState getUIState();

    @Shadow(remap = false)
    public abstract Point2D.Double getBlockPixelInGrid(BlockPos pos);

    @Unique
    private static final int SEAM_ARGB = 0x59FFFFFF;

    // Render-thread only, like every caller of these methods.
    @Unique
    private static boolean toroidal$anchorPass;

    @Unique
    private ResourceKey<Level> toroidal$lastLevelDimension;

    @Unique
    private File toroidal$lastWorldDir;

    @Inject(method = CENTER, at = @At("HEAD"))
    private void toroidal$dropTilesOnWorldChange(File worldDir, MapType mapType, double blockX, double blockZ,
            int zoom, CallbackInfoReturnable<Boolean> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        String reason = JourneyMapFold.staleGridReason(toroidal$lastLevelDimension, dimension,
                toroidal$lastWorldDir, worldDir);
        if (reason != null) {
            boolean byWorld = JourneyMapFold.WORLD_CHANGED.equals(reason);
            JourneyMapFold.gridDropped(reason,
                    byWorld ? toroidal$lastWorldDir.getName() : toroidal$lastLevelDimension.identifier().toString(),
                    byWorld ? worldDir.getName() : dimension.identifier().toString(),
                    this.regions.size());
            this.clear();
        }

        toroidal$lastLevelDimension = dimension;
        if (worldDir != null) {
            toroidal$lastWorldDir = worldDir;
        }
    }

    @WrapOperation(
            method = "loadInMemoryRegions",
            at = @At(value = "INVOKE",
                    target = "Ljourneymap/client/model/region/RegionImageCache;getRegionImageSets()Ljava/util/Collection;"))
    private Collection<RegionImageSet> toroidal$onlyThisWorldsRegions(RegionImageCache cache,
            Operation<Collection<RegionImageSet>> original) {
        Collection<RegionImageSet> sets = original.call(cache);
        File dir = this.worldDir;
        if (dir == null) {
            return sets;
        }

        return sets.stream().filter(set -> dir.equals(set.getRegionCoord().worldDir)).toList();
    }

    @WrapMethod(method = CENTER)
    private boolean toroidal$keepSingleViewOnTheMap(File worldDir, MapType mapType, double blockX, double blockZ, int zoom,
            Operation<Boolean> original) {
        if (JourneyMapFold.copiesOf(this.getUIState().ui) != MapCopies.SINGLE || !JourneyMapFold.active()) {
            return original.call(worldDir, mapType, blockX, blockZ, zoom);
        }

        Window window = Minecraft.getInstance().getWindow();
        int flooredZoom = Math.max(zoom, JourneyMapFold.fullscreenZoomFloor(window.getWidth(), window.getHeight()));
        return original.call(worldDir, mapType,
                JourneyMapFold.seatSingleCenter(Direction.Axis.X, blockX, flooredZoom, window.getWidth()),
                JourneyMapFold.seatSingleCenter(Direction.Axis.Z, blockZ, flooredZoom, window.getHeight()),
                flooredZoom);
    }

    @ModifyVariable(
            method = CENTER,
            at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double toroidal$foldCenterX(double blockX) {
        return JourneyMapFold.foldCenterCoord(Direction.Axis.X, blockX);
    }

    @ModifyVariable(
            method = CENTER,
            at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double toroidal$foldCenterZ(double blockZ) {
        return JourneyMapFold.foldCenterCoord(Direction.Axis.Z, blockZ);
    }

    @Inject(method = BLOCK_PIXEL_IN_GRID, at = @At("HEAD"))
    private void toroidal$beginAnchorPass(CallbackInfoReturnable<Point2D.Double> cir) {
        toroidal$anchorPass = true;
    }

    @Inject(method = BLOCK_PIXEL_IN_GRID, at = @At("RETURN"))
    private void toroidal$endAnchorPass(CallbackInfoReturnable<Point2D.Double> cir) {
        toroidal$anchorPass = false;
    }

    @ModifyVariable(method = BLOCK_COORD_PIXEL_IN_GRID, at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double toroidal$foldPixelX(double blockX) {
        return toroidal$anchorPass ? blockX : JourneyMapFold.seatPixelCoord(
                Direction.Axis.X, this.centerBlockX, blockX, JourneyMapFold.copiesOf(this.getUIState().ui));
    }

    @ModifyVariable(method = BLOCK_COORD_PIXEL_IN_GRID, at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double toroidal$foldPixelZ(double blockZ) {
        return toroidal$anchorPass ? blockZ : JourneyMapFold.seatPixelCoord(
                Direction.Axis.Z, this.centerBlockZ, blockZ, JourneyMapFold.copiesOf(this.getUIState().ui));
    }

    @ModifyVariable(method = MOVE, at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double toroidal$stopMoveXAtTheEdge(double deltaBlockX) {
        return JourneyMapFold.copiesOf(this.getUIState().ui) == MapCopies.SINGLE
                ? JourneyMapFold.clampedMove(Direction.Axis.X, this.centerBlockX, deltaBlockX, this.zoom,
                        Minecraft.getInstance().getWindow().getWidth())
                : deltaBlockX;
    }

    @ModifyVariable(method = MOVE, at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double toroidal$stopMoveZAtTheEdge(double deltaBlockZ) {
        return JourneyMapFold.copiesOf(this.getUIState().ui) == MapCopies.SINGLE
                ? JourneyMapFold.clampedMove(Direction.Axis.Z, this.centerBlockZ, deltaBlockZ, this.zoom,
                        Minecraft.getInstance().getWindow().getHeight())
                : deltaBlockZ;
    }

    @ModifyReturnValue(method = "getCalculatedGridSize(I)I", at = @At("RETURN"))
    private int toroidal$floorGridSizeToWorld(int original) {
        return Math.max(original, JourneyMapFold.minGridSize());
    }

    @ModifyVariable(method = "setZoom(D)Z", at = @At("HEAD"), argsOnly = true)
    private double toroidal$floorFullscreenZoom(double zoom) {
        if (!Context.UI.Fullscreen.equals(this.getUIState().ui)) {
            return zoom;
        }

        Window window = Minecraft.getInstance().getWindow();
        return Math.max(zoom, JourneyMapFold.fullscreenZoomFloor(window.getWidth(), window.getHeight()));
    }

    @Override
    public void toroidal$drawSeams(GuiGraphicsExtractor graphics, Matrix3x2fStack pose, double offsetX, double offsetZ) {
        if (!Context.UI.Fullscreen.equals(this.getUIState().ui) || JourneyMapFold.loopedAxes() == 0
                || JourneyMapFold.copiesOf(Context.UI.Fullscreen) == MapCopies.SINGLE) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        int[] spanX = JourneyMapFold.viewSpan(this.centerBlockX, window.getWidth(), this.zoom);
        int[] spanZ = JourneyMapFold.viewSpan(this.centerBlockZ, window.getHeight(), this.zoom);
        int[] seamsX = JourneyMapFold.copies(Direction.Axis.X).seams(spanX[0], spanX[1]);
        int[] seamsZ = JourneyMapFold.copies(Direction.Axis.Z).seams(spanZ[0], spanZ[1]);

        Matrix3x2f poseSnapshot = new Matrix3x2f(pose);
        for (int seam : seamsX) {
            int pixelX = (int) (this.getBlockPixelInGrid(new BlockPos(seam, 0, 0)).x + offsetX);
            toroidal$fillSeam(graphics, poseSnapshot, pixelX, 0, pixelX + 1, window.getHeight());
        }

        for (int seam : seamsZ) {
            int pixelZ = (int) (this.getBlockPixelInGrid(new BlockPos(0, 0, seam)).y + offsetZ);
            toroidal$fillSeam(graphics, poseSnapshot, 0, pixelZ, window.getWidth(), pixelZ + 1);
        }
    }

    @Unique
    private static void toroidal$fillSeam(GuiGraphicsExtractor graphics, Matrix3x2f pose, int x0, int y0, int x1, int y1) {
        JmRenderRouter.addGuiElement(graphics, new ColoredRectangleRenderState(
                RenderPipelines.GUI, TextureSetup.noTexture(), pose, x0, y0, x1, y1, SEAM_ARGB, SEAM_ARGB, null));
    }
}
