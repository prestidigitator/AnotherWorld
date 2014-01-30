package org.terasology.anotherWorld.decorator;

import org.terasology.anotherWorld.ChunkDecorator;
import org.terasology.anotherWorld.GenerationParameters;
import org.terasology.anotherWorld.util.Filter;
import org.terasology.math.Vector2i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BeachDecorator implements ChunkDecorator {
    private Filter<Block> blockFilter;
    private Block beachBlock;
    private int aboveSeaLevel;
    private int belowSeaLevel;

    public BeachDecorator(Filter<Block> blockFilter, Block beachBlock, int aboveSeaLevel, int belowSeaLevel) {
        this.blockFilter = blockFilter;
        this.beachBlock = beachBlock;
        this.aboveSeaLevel = aboveSeaLevel;
        this.belowSeaLevel = belowSeaLevel;
    }

    @Override
    public void initializeWithSeed(String seed) {
    }

    @Override
    public void generateInChunk(Chunk chunk, GenerationParameters generationParameters) {
        int chunkStartX = chunk.getChunkWorldPosX();
        int chunkStartZ = chunk.getChunkWorldPosZ();
        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                int groundLevel = generationParameters.getLandscapeProvider().getHeight(new Vector2i(chunkStartX + x, chunkStartZ + z));
                int seaLevel = generationParameters.getSeaLevel();
                if (groundLevel <= seaLevel + aboveSeaLevel && groundLevel >= seaLevel - belowSeaLevel) {
                    for (int y = seaLevel - belowSeaLevel; y < seaLevel + aboveSeaLevel; y++) {
                        if (blockFilter.accepts(chunk.getBlock(x, y, z))) {
                            chunk.setBlock(x, y, z, beachBlock);
                        }
                    }
                }
            }
        }
    }
}
