package com.detailedadvancements.mixin;

import com.detailedadvancements.util.DetailTooltipRenderer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AdvancementsScreen.class)
public class AdvancementsScreenMixin {

    @Shadow
    @Nullable
    private AdvancementTab selectedTab;

    // Screen에서 상속된 너비/높이
    @Shadow
    public int width;

    @Shadow
    public int height;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (selectedTab == null) return;

        AdvancementTabAccessor tabAccessor = (AdvancementTabAccessor) selectedTab;
        Map<AdvancementHolder, AdvancementWidget> widgets = tabAccessor.getWidgets();

        // 도전과제 화면 내부 콘텐츠 영역 시작 위치
        // 배경 창: 252×140, 화면 중앙 배치
        // 콘텐츠 영역: 좌측 +9, 상단 +18 오프셋 (탭 버튼 등 제외)
        int windowX = (this.width - 252) / 2;
        int windowY = (this.height - 140) / 2;
        int contentX = windowX + 9;
        int contentY = windowY + 18;

        int scrollX = (int) tabAccessor.getScrollX();
        int scrollY = (int) tabAccessor.getScrollY();

        AdvancementHolder hovered = null;
        for (Map.Entry<AdvancementHolder, AdvancementWidget> entry : widgets.entrySet()) {
            AdvancementWidgetAccessor widgetAccessor = (AdvancementWidgetAccessor) entry.getValue();
            int wx = contentX + scrollX + widgetAccessor.getX();
            int wy = contentY + scrollY + widgetAccessor.getY();
            // 도전과제 위젯: 26×26 픽셀
            if (mouseX >= wx && mouseX < wx + 26 && mouseY >= wy && mouseY < wy + 26) {
                hovered = entry.getKey();
                break;
            }
        }

        if (hovered == null) return;

        // display 없는 숨겨진 도전과제는 건너뜀
        if (hovered.value().display().isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.connection == null) return;

        ClientAdvancements clientAdvancements = mc.player.connection.getAdvancements();
        AdvancementProgress advancementProgress = clientAdvancements.getOrStartProgress(hovered);

        DetailTooltipRenderer.render(
                graphics,
                mc.font,
                hovered,
                advancementProgress,
                mouseX, mouseY,
                this.width, this.height
        );
    }
}
