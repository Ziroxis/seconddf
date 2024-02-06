package com.yuanno.seconddf.init;

import com.yuanno.seconddf.network.SSyncAllowPacket;
import xyz.pixelatedw.mineminenomi.packets.server.ability.SViewProtectionPacket;
import xyz.pixelatedw.mineminenomi.wypi.WyNetwork;

public class ModNetwork {

    public static void init()
    {
        WyNetwork.registerPacket(SSyncAllowPacket.class, SSyncAllowPacket::encode, SSyncAllowPacket::decode, SSyncAllowPacket::handle);

    }
}
