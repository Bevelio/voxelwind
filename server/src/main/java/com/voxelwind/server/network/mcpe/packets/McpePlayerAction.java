package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.NetworkPackage;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpePlayerAction implements NetworkPackage {
    private long entityId;
    private Action action;
    private Vector3i position;
    private int face;

    @Override
    public void decode(ByteBuf buffer) {
        entityId = buffer.readLong();
        action = Action.values()[buffer.readInt()];
        position = McpeUtil.readVector3i(buffer);
        face = buffer.readInt();
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeLong(entityId);
        buffer.writeInt(action.ordinal());
        McpeUtil.writeVector3i(buffer, position);
        buffer.writeInt(face);
    }

    public enum Action {
        ACTION_START_BREAK,
        ACTION_ABORT_BREAK,
        ACTION_STOP_BREAK,
        ACTION_RELEASE_ITEM,
        ACTION_STOP_SLEEPING,
        ACTION_SPAWN_SAME_DIMENSION,
        ACTION_JUMP,
        ACTION_START_SPRINT,
        ACTION_STOP_SPRINT,
        ACTION_START_SNEAK,
        ACTION_STOP_SNEAK,
        ACTION_SPAWN_OVERWORLD,
        ACTION_SPAWN_NETHER
    }
}
