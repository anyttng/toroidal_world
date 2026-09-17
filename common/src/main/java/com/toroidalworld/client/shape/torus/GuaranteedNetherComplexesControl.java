package com.toroidalworld.client.shape.torus;

import com.toroidalworld.api.v1.client.WorldOptionContext;
import com.toroidalworld.api.v1.client.WorldOptionControl;
import com.toroidalworld.api.v1.option.GenerationOptions;
import com.toroidalworld.client.shape.LoopSizeControls;
import com.toroidalworld.shape.torus.GuaranteedNetherComplexes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

public final class GuaranteedNetherComplexesControl implements WorldOptionControl {
    private static final Component LABEL =
            Component.translatable("gui.toroidal_world.toroidal_settings.guaranteed_nether_complexes");
    private static final Component HINT =
            Component.translatable("gui.toroidal_world.toroidal_settings.guaranteed_nether_complexes_hint");

    private boolean guaranteed;

    public GuaranteedNetherComplexesControl(WorldOptionContext context) {
        this.guaranteed = context.options().get(GuaranteedNetherComplexes.OPTION);
    }

    @Override
    public void addWidgets(Font font, LinearLayout contents) {
        contents.addChild(CycleButton.onOffBuilder(this.guaranteed)
                .withTooltip(chosen -> Tooltip.create(HINT))
                .create(0, 0, LoopSizeControls.FIELD_WIDTH, LoopSizeControls.FIELD_HEIGHT, LABEL,
                        (button, chosen) -> this.guaranteed = chosen));
    }

    @Override
    public GenerationOptions commit(GenerationOptions options) {
        return options.with(GuaranteedNetherComplexes.OPTION, this.guaranteed);
    }
}
