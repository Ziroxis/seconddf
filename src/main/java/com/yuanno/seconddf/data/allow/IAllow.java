package com.yuanno.seconddf.data.allow;

import net.minecraft.entity.LivingEntity;

public interface IAllow {

    IAllow setOwner(LivingEntity owner);
    void setAllow(boolean allow);
    boolean hasAllow();

    void setTwoDevilFruits(boolean twoDevilFruits);
    boolean hasTwoDevilFruits();

    void setSecondFruit(String fruit);
    String getSecondFruit();
}
