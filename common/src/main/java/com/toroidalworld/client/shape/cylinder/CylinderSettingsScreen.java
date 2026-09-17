package com.toroidalworld.client.shape.cylinder;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.toroidalworld.api.v1.client.WorldOptionContext;
import com.toroidalworld.api.v1.client.WorldOptionControl;
import com.toroidalworld.api.v1.client.WorldOptionControls;
import com.toroidalworld.api.v1.option.GenerationOptions;
import com.toroidalworld.client.shape.LoopSettingsScreen;
import com.toroidalworld.client.shape.LoopSizeControls;
import com.toroidalworld.api.v1.shape.LoopSpans;
import com.toroidalworld.shape.cylinder.CylinderSettings;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class CylinderSettingsScreen extends LoopSettingsScreen<CylinderSettings> {
    private static final Component TITLE = Component.translatable("gui.toroidal_world.cylinder_settings.title");
    private static final Component AXIS_LABEL = Component.translatable("gui.toroidal_world.cylinder_settings.axis");
    private static final Component AXIS_HINT = Component.translatable("gui.toroidal_world.cylinder_settings.axis_hint");
    private static final Component ADDITIONAL_SECTION =
            Component.translatable("gui.toroidal_world.toroidal_settings.section.additional")
                    .withStyle(ChatFormatting.BOLD);

    private final GenerationOptions generationOptions;
    private final List<WorldOptionControl> optionControls;

    private Direction.Axis axis;

    public CylinderSettingsScreen(Screen parent, CylinderSettings current, Consumer<CylinderSettings> onDone) {
        super(TITLE, parent, onChange -> LoopSizeControls.single(current.chunkWidth(), current.netherScale(),
                current.endChunkWidth(), onChange), onDone);
        this.axis = current.axis();
        this.generationOptions = current.generationOptions();
        this.optionControls = WorldOptionControls.createAll(new ScreenContext(), CylinderSettings.OFFERED_OPTIONS);
    }

    @Override
    protected void addBeforeFields(Font font, LinearLayout contents) {
        contents.addChild(CycleButton.builder(CylinderSettingsScreen::axisName, this.axis)
                .withValues(Direction.Axis.X, Direction.Axis.Z)
                .withTooltip(chosen -> Tooltip.create(AXIS_HINT))
                .create(0, 0, LoopSizeControls.FIELD_WIDTH, LoopSizeControls.FIELD_HEIGHT, AXIS_LABEL,
                        (button, chosen) -> this.chooseAxis(chosen)));
    }

    @Override
    protected void addAfterFields(Font font, LinearLayout contents) {
        contents.addChild(new StringWidget(ADDITIONAL_SECTION, font), LayoutSettings::alignHorizontallyCenter);

        for (WorldOptionControl control : this.optionControls) {
            control.addWidgets(font, contents);
        }
    }

    @Override
    protected boolean isComplete() {
        return super.isComplete() && this.optionControls.stream().allMatch(WorldOptionControl::isComplete);
    }

    @Override
    protected void onControlsChanged() {
        super.onControlsChanged();

        for (WorldOptionControl control : this.optionControls) {
            control.onSharedStateChanged();
        }
    }

    @Override
    protected CylinderSettings build() {
        GenerationOptions chosen = this.generationOptions;
        for (WorldOptionControl control : this.optionControls) {
            chosen = control.commit(chosen);
        }

        return new CylinderSettings(
                LoopSpans.ofWidth(this.axis, this.controls.effectiveSize(this.axis)),
                this.controls.netherScale(),
                LoopSpans.ofWidth(this.axis, this.controls.effectiveEndSize()),
                chosen);
    }

    private void chooseAxis(Direction.Axis chosen) {
        this.axis = chosen;
        this.onControlsChanged();
    }

    private static Component axisName(Direction.Axis axis) {
        return Component.literal(axis.getName().toUpperCase(Locale.ROOT));
    }

    private final class ScreenContext implements WorldOptionContext {
        @Override
        public Screen parent() {
            return CylinderSettingsScreen.this.parent();
        }

        @Override
        public @Nullable Integer loopChunkWidth() {
            return CylinderSettingsScreen.this.controls.effectiveSize(CylinderSettingsScreen.this.axis);
        }

        @Override
        public @Nullable LoopSpans loopSpans() {
            Integer chunkWidth = this.loopChunkWidth();
            return chunkWidth == null ? null : LoopSpans.ofWidth(CylinderSettingsScreen.this.axis, chunkWidth);
        }

        @Override
        public GenerationOptions options() {
            return CylinderSettingsScreen.this.generationOptions;
        }

        @Override
        public void onChanged() {
            CylinderSettingsScreen.this.refreshDoneButton();
        }

        @Override
        public void rebuild() {
            CylinderSettingsScreen.this.rebuildWidgets();
        }
    }
}
