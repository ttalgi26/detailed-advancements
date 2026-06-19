package com.detailedadvancements.util;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DetailTooltipRenderer {

    private static final int PANEL_WIDTH = 220;
    private static final int PADDING = 6;
    private static final int LINE_HEIGHT = 10;
    // 남은 항목 최대 표시 수 (완료 포함 합산)
    private static final int MAX_REMAINING = 18;
    private static final int MAX_COMPLETED = 8;

    private static final int COLOR_BG = 0xE5050510;
    private static final int COLOR_BORDER_TOP = 0xFF5A3A7E;
    private static final int COLOR_BORDER_BOTTOM = 0xFF2D1C44;
    private static final int COLOR_TITLE = 0xFFFFFFAA;
    private static final int COLOR_PROGRESS_TEXT = 0xFFAAAAAA;
    private static final int COLOR_REMAINING_HEADER = 0xFFFF7070;
    private static final int COLOR_COMPLETED_HEADER = 0xFF70FF70;
    private static final int COLOR_REMAINING = 0xFFDD5555;
    private static final int COLOR_COMPLETED = 0xFF55BB55;
    private static final int COLOR_ELLIPSIS = 0xFF888888;
    private static final int COLOR_DONE_TITLE = 0xFFFFD700;

    public static void render(GuiGraphics graphics, Font font,
                              AdvancementHolder holder, AdvancementProgress progress,
                              int mouseX, int mouseY, int screenWidth, int screenHeight) {
        // 도전과제 표시 이름
        String title;
        if (holder.value().display().isPresent()) {
            title = holder.value().display().get().getTitle().getString();
        } else {
            title = holder.id().getPath();
        }
        // 제목이 너무 길면 자르기
        if (font.width(title) > PANEL_WIDTH - PADDING * 2) {
            while (font.width(title + "...") > PANEL_WIDTH - PADDING * 2 && title.length() > 1) {
                title = title.substring(0, title.length() - 1);
            }
            title = title + "...";
        }

        // 기준(criteria) 완료 여부 수집
        Map<String, Boolean> criteriaStatus = new TreeMap<>();
        for (Map.Entry<String, ?> entry : holder.value().criteria().entrySet()) {
            String name = entry.getKey();
            CriterionProgress cp = progress.getCriterion(name);
            criteriaStatus.put(name, cp != null && cp.isDone());
        }

        int total = criteriaStatus.size();
        // 기준이 1개 이하면 추가 패널 불필요
        if (total <= 1) return;

        List<String> remainingNames = new ArrayList<>();
        List<String> completedNames = new ArrayList<>();
        for (Map.Entry<String, Boolean> e : criteriaStatus.entrySet()) {
            if (e.getValue()) {
                completedNames.add(CriterionFormatter.format(e.getKey()));
            } else {
                remainingNames.add(CriterionFormatter.format(e.getKey()));
            }
        }

        int done = completedNames.size();
        boolean fullyDone = progress.isDone();

        // 렌더링할 줄 목록 구성
        List<Object[]> lines = new ArrayList<>(); // {text, color}

        // 제목
        lines.add(new Object[]{title, fullyDone ? COLOR_DONE_TITLE : COLOR_TITLE});

        // 진행도 표시를 위한 sentinel (null = 프로그레스 바 영역)
        lines.add(null);

        // 남은 항목
        if (!remainingNames.isEmpty()) {
            lines.add(new Object[]{"남은 항목 (" + remainingNames.size() + "):", COLOR_REMAINING_HEADER});
            int shown = 0;
            for (String name : remainingNames) {
                if (shown >= MAX_REMAINING) {
                    lines.add(new Object[]{"  ... 외 " + (remainingNames.size() - shown) + "개", COLOR_ELLIPSIS});
                    break;
                }
                lines.add(new Object[]{"  ✗ " + name, COLOR_REMAINING});
                shown++;
            }
        }

        // 완료 항목 (전부 완료 시 생략)
        if (!completedNames.isEmpty() && !fullyDone) {
            lines.add(new Object[]{"완료 (" + done + "):", COLOR_COMPLETED_HEADER});
            int shown = 0;
            for (String name : completedNames) {
                if (shown >= MAX_COMPLETED) {
                    lines.add(new Object[]{"  ... 외 " + (completedNames.size() - shown) + "개", COLOR_ELLIPSIS});
                    break;
                }
                lines.add(new Object[]{"  ✓ " + name, COLOR_COMPLETED});
                shown++;
            }
        } else if (fullyDone) {
            lines.add(new Object[]{"모든 항목 완료! ★", COLOR_COMPLETED_HEADER});
        }

        // 패널 높이 계산: PADDING*2 + 각 줄 LINE_HEIGHT + 프로그레스바 영역(LINE_HEIGHT + 8)
        int progressBarExtra = LINE_HEIGHT + 8; // 퍼센트 텍스트 + 바
        int panelHeight = PADDING * 2 + lines.size() * LINE_HEIGHT + progressBarExtra;

        // 패널 위치: 커서 오른쪽 우선, 공간 없으면 왼쪽
        int panelX = mouseX + 16;
        if (panelX + PANEL_WIDTH > screenWidth - 4) {
            panelX = mouseX - PANEL_WIDTH - 8;
        }
        int panelY = mouseY - 12;
        if (panelY + panelHeight > screenHeight - 4) {
            panelY = screenHeight - panelHeight - 4;
        }
        panelY = Math.max(panelY, 4);

        // 배경
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, COLOR_BG);

        // 테두리 (위→아래 그라데이션)
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, COLOR_BORDER_TOP);
        graphics.fill(panelX, panelY + panelHeight - 1, panelX + PANEL_WIDTH, panelY + panelHeight, COLOR_BORDER_BOTTOM);
        graphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, COLOR_BORDER_TOP);
        graphics.fill(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, COLOR_BORDER_BOTTOM);

        int curY = panelY + PADDING;
        int textX = panelX + PADDING;

        for (Object[] line : lines) {
            if (line == null) {
                // 프로그레스 바 렌더링
                float pct = total > 0 ? (float) done / total : 0f;
                String pctStr = done + " / " + total + "  (" + (int) (pct * 100) + "%)";
                graphics.drawString(font, pctStr, textX, curY, COLOR_PROGRESS_TEXT, false);
                curY += LINE_HEIGHT;

                int barX = textX;
                int barW = PANEL_WIDTH - PADDING * 2;
                int barH = 5;
                graphics.fill(barX, curY, barX + barW, curY + barH, 0xFF222233);
                int fillW = (int) (barW * pct);
                if (fillW > 0) {
                    int fillColor = fullyDone ? 0xFF44DDAA : 0xFF3366BB;
                    graphics.fill(barX, curY, barX + fillW, curY + barH, fillColor);
                }
                // 바 테두리
                graphics.fill(barX, curY, barX + barW, curY + 1, 0xFF445566);
                graphics.fill(barX, curY + barH - 1, barX + barW, curY + barH, 0xFF445566);
                curY += barH + 4;
            } else {
                String text = (String) line[0];
                int color = (int) line[1];
                graphics.drawString(font, text, textX, curY, color, false);
                curY += LINE_HEIGHT;
            }
        }
    }
}
