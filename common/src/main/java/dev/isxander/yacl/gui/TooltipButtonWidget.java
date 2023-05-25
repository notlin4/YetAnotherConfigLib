package dev.isxander.yacl.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class TooltipButtonWidget extends Button {

    protected final Screen screen;

    public TooltipButtonWidget(Screen screen, int x, int y, int width, int height, Component message, Component tooltip, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.screen = screen;
        if (tooltip != null)
            setTooltip(Tooltip.create(tooltip));
    }

    @Override
    protected @NotNull ClientTooltipPositioner createTooltipPositioner() {
        return new YACLTooltipPositioner(this);
    }
}
