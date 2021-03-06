package com.voxelwind.api.game.entities;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.api.game.level.Chunk;
import com.voxelwind.api.game.level.Level;
import com.voxelwind.api.game.level.block.BlockTypes;
import com.voxelwind.api.util.Rotation;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public interface Entity {
    long getEntityId();

    @Nonnull
    Level getLevel();

    @Nonnull
    Vector3f getPosition();

    @Nonnull
    Vector3f getGamePosition();

    @Nonnull
    Rotation getRotation();

    void setRotation(Rotation rotation);

    Vector3f getMotion();

    void setMotion(Vector3f motion);

    boolean isSprinting();

    void setSprinting(boolean sprinting);

    boolean isSneaking();

    void setSneaking(boolean sneaking);

    boolean isInvisible();

    void setInvisible(boolean invisible);

    boolean onTick();

    default boolean isOnGround() {
        Vector3i blockPosition = getPosition().sub(0f, 0.1f, 0f).toInt();

        if (blockPosition.getY() < 0) {
            return false;
        }

        int chunkX = blockPosition.getX() >> 4;
        int chunkZ = blockPosition.getZ() >> 4;
        int chunkInX = blockPosition.getX() % 16;
        int chunkInZ = blockPosition.getZ() % 16;

        Optional<Chunk> chunkOptional = getLevel().getChunkIfLoaded(chunkX, chunkZ);
        return chunkOptional.isPresent() && chunkOptional.get().getBlock(chunkInX, blockPosition.getY(), chunkInZ).getBlockState().getBlockType() != BlockTypes.AIR;
    }

    void teleport(Vector3f position);

    void teleport(Level level, Vector3f position);

    void teleport(Level level, Vector3f position, Rotation rotation);

    void remove();

    boolean isRemoved();
}
