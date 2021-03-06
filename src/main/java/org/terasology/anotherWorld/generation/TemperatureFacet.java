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
package org.terasology.anotherWorld.generation;

import org.terasology.climateConditions.ConditionsBaseField;
import org.terasology.math.Region3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseFacet3D;

/**
 * Created by Marcin on 2014-10-20.
 */
public class TemperatureFacet extends BaseFacet3D {
    private ConditionsBaseField temperatureBaseField;

    public TemperatureFacet(Region3i targetRegion, Border3D border, ConditionsBaseField temperatureBaseField) {
        super(targetRegion, border);
        this.temperatureBaseField = temperatureBaseField;
    }

    public float get(int x, int y, int z) {
        return temperatureBaseField.get(x, y, z);
    }
}
