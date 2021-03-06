package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.server.network.NetworkPackage;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeSetTime implements NetworkPackage {
    private long time;
    private boolean running;

    @Override
    public void decode(ByteBuf buffer) {
        time = buffer.readLong();
        running = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeLong(time);
        buffer.writeBoolean(running);
    }
}
