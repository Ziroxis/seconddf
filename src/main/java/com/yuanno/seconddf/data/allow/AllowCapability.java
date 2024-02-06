package com.yuanno.seconddf.data.allow;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class AllowCapability {

    @CapabilityInject(IAllow.class)
    public static final Capability<IAllow> INSTANCE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IAllow.class, new Capability.IStorage<IAllow>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IAllow> capability, IAllow instance, Direction side) {
                CompoundNBT props = new CompoundNBT();

                props.putBoolean("hasAllow", instance.hasAllow());
                props.putBoolean("hasTwoDevilFruits", instance.hasTwoDevilFruits());
                props.putString("secondFruit", instance.getSecondFruit());

                return props;
            }

            @Override
            public void readNBT(Capability<IAllow> capability, IAllow instance, Direction side, INBT nbt) {
                CompoundNBT props = (CompoundNBT) nbt;

                instance.setAllow(props.getBoolean("hasAllow"));
                instance.setTwoDevilFruits(props.getBoolean("hasTwoDevilFruits"));
                instance.setSecondFruit(props.getString("secondFruit"));
            }
        }, () -> new AllowBase());
    }

    public static IAllow get(final LivingEntity entity) {
        IAllow props = entity.getCapability(INSTANCE).orElse(null);
        props.setOwner(entity);
        return props;
    }
}
