package com.yuanno.seconddf.network;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import xyz.pixelatedw.mineminenomi.api.helpers.AttributeHelper;

import java.util.function.Supplier;

public class SSyncAllowPacket
{
	private int entityId;
	private INBT data;

	public SSyncAllowPacket()
	{
	}

	public SSyncAllowPacket(int entityId, IAllow props)
	{
		this.data = new CompoundNBT();
		this.data = AllowCapability.INSTANCE.getStorage().writeNBT(AllowCapability.INSTANCE, props, null);
		this.entityId = entityId;
	}

	public void encode(PacketBuffer buffer)
	{
		buffer.writeInt(this.entityId);
		buffer.writeNbt((CompoundNBT) this.data);
	}

	public static SSyncAllowPacket decode(PacketBuffer buffer)
	{
		SSyncAllowPacket msg = new SSyncAllowPacket();
		msg.entityId = buffer.readInt();
		msg.data = buffer.readNbt();
		return msg;
	}

	public static void handle(SSyncAllowPacket message, final Supplier<NetworkEvent.Context> ctx)
	{
		if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
		{
			ctx.get().enqueueWork(() ->
			{
				ClientHandler.handle(message);
			});
		}
		ctx.get().setPacketHandled(true);
	}
	
	public static class ClientHandler
	{
		@OnlyIn(Dist.CLIENT)
		public static void handle(SSyncAllowPacket message)
		{
			Entity target = Minecraft.getInstance().level.getEntity(message.entityId);
			if (target == null || !(target instanceof LivingEntity))
				return;

			IAllow props = AllowCapability.get((LivingEntity) target);
			AllowCapability.INSTANCE.getStorage().readNBT(AllowCapability.INSTANCE, props, null, message.data);	
			if(target instanceof PlayerEntity)
				AttributeHelper.updateHPAttribute((PlayerEntity) target);
		}
	}

}
