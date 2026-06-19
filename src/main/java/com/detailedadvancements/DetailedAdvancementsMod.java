package com.detailedadvancements;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetailedAdvancementsMod implements ClientModInitializer {
    public static final String MOD_ID = "detailedadvancements";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Detailed Advancements 모드 로드 완료");
    }
}
