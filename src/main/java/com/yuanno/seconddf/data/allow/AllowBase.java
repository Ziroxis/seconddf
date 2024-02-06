package com.yuanno.seconddf.data.allow;

import net.minecraft.entity.LivingEntity;

public class AllowBase implements IAllow {

    private LivingEntity owner;
    private boolean hasAllow = false;
    private boolean hasTwoDevilFruits = false;
    private String secondFruit = "";

    @Override
    public IAllow setOwner(LivingEntity owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public void setAllow(boolean allow) {
        this.hasAllow = allow;
    }
    @Override
    public boolean hasAllow() {
        return this.hasAllow;
    }

    @Override
    public void setTwoDevilFruits(boolean twoDevilFruits) {
        this.hasTwoDevilFruits = twoDevilFruits;
    }
    @Override
    public boolean hasTwoDevilFruits() {
        return this.hasTwoDevilFruits;
    }

    @Override
    public void setSecondFruit(String fruit) {
        this.secondFruit = fruit;
    }
    @Override
    public String getSecondFruit() {
        return this.secondFruit;
    }
}
