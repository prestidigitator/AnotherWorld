/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.anotherWorld;

import com.google.common.base.Function;

import org.terasology.anotherWorld.generation.BiomeProvider;
import org.terasology.anotherWorld.generation.HillynessProvider;
import org.terasology.anotherWorld.generation.HumidityProvider;
import org.terasology.anotherWorld.generation.PerlinSurfaceHeightProvider;
import org.terasology.anotherWorld.generation.TemperatureProvider;
import org.terasology.anotherWorld.generation.TerrainVariationProvider;
import org.terasology.anotherWorld.util.alpha.IdentityAlphaFunction;
import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.climateConditions.ConditionsBaseField;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.core.world.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.engine.SimpleUri;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.World;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.WorldGenerator;

import java.util.LinkedList;
import java.util.List;

public abstract class PluggableWorldGenerator implements WorldGenerator {
    private World world;
    private List<ChunkDecorator> chunkDecorators = new LinkedList<>();
    private List<FeatureGenerator> featureGenerators = new LinkedList<>();
    private List<FacetProvider> facetProviders = new LinkedList<>();

    private int seaLevel = 32;
    private int maxLevel = 220;
    private float biomeDiversity = 0.5f;

    private SimpleUri uri;
    private String worldSeed;

    private Function<Float, Float> temperatureFunction = IdentityAlphaFunction.singleton();
    private Function<Float, Float> humidityFunction = IdentityAlphaFunction.singleton();

    private PerlinSurfaceHeightProvider surfaceHeightProvider;

    public PluggableWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    public void addChunkDecorator(ChunkDecorator chunkGenerator) {
        chunkDecorators.add(chunkGenerator);
    }

    public void addFeatureGenerator(FeatureGenerator featureGenerator) {
        featureGenerators.add(featureGenerator);
    }

    public void addFacetProvider(FacetProvider facetProvider) {
        facetProviders.add(facetProvider);
    }

    public void setSeaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    /**
     * 0=changing slowly, 1=changing frequently
     *
     * @param biomeDiversity
     */
    public void setBiomeDiversity(float biomeDiversity) {
        this.biomeDiversity = biomeDiversity;
    }

    public void setTemperatureFunction(Function<Float, Float> temperatureFunction) {
        this.temperatureFunction = temperatureFunction;
    }

    public void setHumidityFunction(Function<Float, Float> humidityFunction) {
        this.humidityFunction = humidityFunction;
    }

    public void setLandscapeOptions(float seaFrequency, float terrainDiversity, Function<Float, Float> generalTerrainFunction,
                                    Function<Float, Float> heightBelowSeaLevelFunction,
                                    Function<Float, Float> heightAboveSeaLevelFunction,
                                    float hillinessDiversity, Function<Float, Float> hillynessFunction) {
        surfaceHeightProvider = new PerlinSurfaceHeightProvider(seaFrequency, terrainDiversity, generalTerrainFunction,
                heightBelowSeaLevelFunction,
                heightAboveSeaLevelFunction,
                hillinessDiversity, hillynessFunction, seaLevel, maxLevel);
    }

    @Override
    public void initialize() {
        setupGenerator();

        ClimateConditionsSystem environmentSystem = CoreRegistry.get(ClimateConditionsSystem.class);
        environmentSystem.configureHumidity(seaLevel, maxLevel, biomeDiversity, humidityFunction, 0, 1);
        environmentSystem.configureTemperature(seaLevel, maxLevel, biomeDiversity, temperatureFunction, -20, 40);

        ConditionsBaseField temperatureBaseField = environmentSystem.getTemperatureBaseField();
        ConditionsBaseField humidityBaseField = environmentSystem.getHumidityBaseField();

        WorldBuilder worldBuilder = new WorldBuilder()
                .addProvider(new BiomeProvider())
                .addProvider(new HillynessProvider())
                .addProvider(surfaceHeightProvider)
                .addProvider(new SurfaceToDensityProvider())
                .addProvider(new HumidityProvider(humidityBaseField))
                .addProvider(new TemperatureProvider(temperatureBaseField))
                .addProvider(new TerrainVariationProvider())
                .addProvider(new SeaLevelProvider(seaLevel));
        worldBuilder.setSeed(getSeed());

        for (FacetProvider facetProvider : facetProviders) {
            worldBuilder.addProvider(facetProvider);
        }

        for (ChunkDecorator chunkDecorator : chunkDecorators) {
            worldBuilder.addRasterizer(chunkDecorator);
        }
        for (FeatureGenerator featureGenerator : featureGenerators) {
            worldBuilder.addRasterizer(featureGenerator);
        }

        world = worldBuilder.build();
        world.initialize();
    }

    @Override
    public void setWorldSeed(String seedString) {
        worldSeed = seedString;
    }

    @Override
    public String getWorldSeed() {
        return worldSeed;
    }

    public long getSeed() {
        return worldSeed.hashCode();
    }

    protected abstract void setupGenerator();


    @Override
    public void createChunk(CoreChunk chunk) {
        world.rasterizeChunk(chunk);
    }

    @Override
    public SimpleUri getUri() {
        return uri;
    }

    @Override
    public World getWorld() {
        return world;
    }
}
