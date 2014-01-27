/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.anotherWorld;

import org.terasology.anotherWorld.util.AlphaFunction;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.utilities.procedural.BrownianNoise2D;
import org.terasology.utilities.procedural.SimplexNoise;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PerlinLandscapeGenerator implements LandscapeProvider {
    public static final int CACHE_SIZE = 10000;
    private LinkedHashMap<Vector2i, Integer> heightCache = new LinkedHashMap<>();

    private BrownianNoise2D noise;
    private double noiseScale;

    private float seaFrequency;
    private AlphaFunction heightAboveSeaLevelFunction;
    private float terrainDiversity;
    private AlphaFunction terrainFunction;
    private TerrainDeformation terrainDeformation;
    private int seaLevel;
    private int maxLevel;

    public PerlinLandscapeGenerator(float seaFrequency, AlphaFunction heightAboveSeaLevelFunction,
                                    float terrainDiversity, AlphaFunction terrainFunction) {
        this.seaFrequency = seaFrequency;
        this.heightAboveSeaLevelFunction = heightAboveSeaLevelFunction;
        this.terrainDiversity = terrainDiversity;
        this.terrainFunction = terrainFunction;
    }

    @Override
    public void initialize(String seed, int seaLevel, int maxLevel) {
        this.seaLevel = seaLevel;
        this.maxLevel = maxLevel;
        noise = new BrownianNoise2D(new SimplexNoise(seed.hashCode()), 6);
        noiseScale = noise.getScale();
        terrainDeformation = new TerrainDeformation(seed, terrainDiversity, terrainFunction);
    }

    @Override
    public int getHeight(Vector2i position) {
        Integer height = heightCache.get(position);
        if (height != null) {
            return height;
        }

        float hillyness = terrainDeformation.getHillyness(position.x, position.y);
        float noise = getNoiseInWorld(hillyness, position.x, position.y);
        if (noise < seaFrequency) {
            height = (int) (seaLevel * noise / seaFrequency);
        } else {
            // Number in range 0<=alpha<1
            float alphaAboveSeaLevel = (noise - seaFrequency) / (1 - seaFrequency);
            float resultAlpha = heightAboveSeaLevelFunction.execute(alphaAboveSeaLevel);
            height = (int) (seaLevel + resultAlpha * (maxLevel - seaLevel));
        }

        synchronized (heightCache) {
            heightCache.put(position, height);
            if (heightCache.size() > CACHE_SIZE) {
                Iterator<Vector2i> iterator = heightCache.keySet().iterator();
                iterator.next();
                iterator.remove();
            }
        }

        return height;
    }

    private float getNoiseInWorld(float hillyness, int worldX, int worldZ) {
        double noise = 0;
        int scanArea = (int) ((1 - hillyness) * 50);
        int divider = 0;
        // Scan and average heights in the circle of blocks with diameter of "scanArea" (based on hillyness)
        for (int x = worldX - scanArea; x <= worldX + scanArea; x++) {
            int zScan = (int) Math.sqrt(scanArea * scanArea - (x - worldX) * (x - worldX));
            for (int z = worldZ - zScan; z <= worldZ + zScan; z++) {
                noise += this.noise.noise(0.004 * x, 0.004 * z) / noiseScale;
                divider++;
            }
        }
        noise /= divider;
        return (float) TeraMath.clamp((noise + 1.0) / 2);
    }
}
