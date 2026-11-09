package com.tontoque28.rpgcore.item;

public final class ModItems {
    public static final SpiritualVesselItem SPIRITUAL_VESSEL = new SpiritualVesselItem();
    public static final LifeHeartItem LIFE_HEART = new LifeHeartItem();
    public static final GuideBookItem GUIDE_BOOK = new GuideBookItem();

    static {
        SPIRITUAL_VESSEL.setRegistryName("rpgcore", "spiritual_vessel").setUnlocalizedName("rpgcore.spiritual_vessel");
        LIFE_HEART.setRegistryName("rpgcore", "life_heart").setUnlocalizedName("rpgcore.life_heart");
        GUIDE_BOOK.setRegistryName("rpgcore", "guide_book").setUnlocalizedName("rpgcore.guide_book");
    }

    private ModItems() {
    }
}
