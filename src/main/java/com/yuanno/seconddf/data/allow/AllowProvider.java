package com.yuanno.seconddf.data.allow;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class AllowProvider implements ICapabilitySerializable<CompoundNBT>
{
    private IAllow instance = AllowCapability.INSTANCE.getDefaultInstance();

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
    {
        return AllowCapability.INSTANCE.orEmpty(cap, LazyOptional.of(() -> instance));
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        return (CompoundNBT) AllowCapability.INSTANCE.getStorage().writeNBT(AllowCapability.INSTANCE, instance, null);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        AllowCapability.INSTANCE.getStorage().readNBT(AllowCapability.INSTANCE, instance, null, nbt);
    }

}