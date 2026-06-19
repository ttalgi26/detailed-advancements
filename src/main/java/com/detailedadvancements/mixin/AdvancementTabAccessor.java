package com.detailedadvancements.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AdvancementTab.class)
public interface AdvancementTabAccessor {
    @Accessor("widgets")
    Map<AdvancementHolder, AdvancementWidget> getWidgets();

    @Accessor("scrollX")
    double getScrollX();

    @Accessor("scrollY")
    double getScrollY();

    @Accessor("hovered")
    @Nullable
    AdvancementWidget getHovered();
}
