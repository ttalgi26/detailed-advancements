package com.detailedadvancements.mixin;

import com.detailedadvancements.util.DetailTooltipRenderer;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementsScreen.class)
public class AdvancementsScreenMixin {

    @Shadow
    @Nullable
    private AdvancementTab selectedTab;

    // AdvancementsScreen에 직접 선언된 필드 (Screen 상속 필드 아님)
    @Shadow
    private int leftPos;

    @Shadow
    private int topPos;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (selectedTab == null) return;

        AdvancementTabAccessor tabAccessor = (AdvancementTabAccessor) selectedTab;

        // AdvancementTab이 이미 hovered 필드를 관리함
        AdvancementWidget hoveredWidget = tabAccessor.getHovered();

        // hovered가 null이면 위치 기반으로 직접 탐색
        if (hoveredWidget == null) {
            hoveredWidget = findHoveredWidget(tabAccessor, mouseX, mouseY);
        }

        if (hoveredWidget == null) return;

        AdvancementWidgetAccessor widgetAccessor = (AdvancementWidgetAccessor) hoveredWidget;
        AdvancementNode advancementNode = widgetAccessor.getAdvancementNode();
        AdvancementProgress progress = widgetAccessor.getProgress();

        // display 없는 숨겨진 도전과제는 표시 안 함
        if (advancementNode.advancement().display().isEmpty()) return;
        // progress가 아직 설정되지 않은 경우
        if (progress == null) return;

        extractor.nextStratum();
        // Screen.width/height 대신 extractor.guiWidth/Height() 사용 (Lunar Client 호환)
        DetailTooltipRenderer.render(
                extractor,
                advancementNode.holder(),
                progress,
                mouseX, mouseY,
                extractor.guiWidth(), extractor.guiHeight()
        );
    }

    private AdvancementWidget findHoveredWidget(AdvancementTabAccessor tabAccessor, int mouseX, int mouseY) {
        // WINDOW_INSIDE_X = 9, WINDOW_INSIDE_Y = 18 (도전과제 창 내부 콘텐츠 영역 오프셋)
        int contentX = leftPos + 9;
        int contentY = topPos + 18;
        int scrollX = (int) tabAccessor.getScrollX();
        int scrollY = (int) tabAccessor.getScrollY();

        for (AdvancementWidget widget : tabAccessor.getWidgets().values()) {
            AdvancementWidgetAccessor wa = (AdvancementWidgetAccessor) widget;
            int wx = contentX + scrollX + wa.getX();
            int wy = contentY + scrollY + wa.getY();
            if (mouseX >= wx && mouseX < wx + 26 && mouseY >= wy && mouseY < wy + 26) {
                return widget;
            }
        }
        return null;
    }
}
