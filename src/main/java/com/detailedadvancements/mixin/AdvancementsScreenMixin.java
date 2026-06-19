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

    // leftPos/topPos: 화면 내 도전과제 창 위치
    @Shadow
    private int leftPos;

    @Shadow
    private int topPos;

    // Screen 클래스에서 상속된 화면 크기
    @Shadow
    public int width;

    @Shadow
    public int height;

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
        DetailTooltipRenderer.render(
                extractor,
                advancementNode.holder(),
                progress,
                mouseX, mouseY,
                this.width, this.height
        );
    }

    private AdvancementWidget findHoveredWidget(AdvancementTabAccessor tabAccessor, int mouseX, int mouseY) {
        // 내용 영역 시작: leftPos + WINDOW_INSIDE_X(9), topPos + WINDOW_INSIDE_Y(18)
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
