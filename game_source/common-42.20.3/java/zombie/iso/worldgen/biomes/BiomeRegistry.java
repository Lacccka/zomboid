/*
 * Decompiled with CFR 0.152.
 */
package zombie.iso.worldgen.biomes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import zombie.iso.worldgen.biomes.Biome;
import zombie.iso.worldgen.biomes.BiomeNoise;
import zombie.iso.worldgen.biomes.BiomeType;
import zombie.iso.worldgen.biomes.IBiome;

public class BiomeRegistry {
    public static BiomeRegistry instance = new BiomeRegistry();
    private final Map<BiomeGetter, List<IBiome>> biomeCache = new HashMap<BiomeGetter, List<IBiome>>();
    private final Map<BiomeGetterFiltered, List<IBiome>> biomeCacheFiltered = new HashMap<BiomeGetterFiltered, List<IBiome>>();

    private BiomeRegistry() {
    }

    public void reset() {
        this.biomeCache.clear();
        this.biomeCacheFiltered.clear();
    }

    public IBiome get(Map<String, Biome> biomesIn, BiomeNoise noises, double selector, Map<BiomeType.Landscape, List<Double>> landscapeProb, Map<BiomeType.Plant, List<Double>> plantProb, Map<BiomeType.Bush, List<Double>> bushProb, Map<BiomeType.Temperature, List<Double>> temperatureProb, Map<BiomeType.Hygrometry, List<Double>> hygrometryProb, Map<BiomeType.OreLevel, List<Double>> oreLevelProb) {
        BiomeType.OreLevel oreLevel;
        BiomeType.Hygrometry hygrometry;
        BiomeType.Temperature temperature;
        BiomeType.Bush bush;
        BiomeType.Plant plant;
        BiomeType.Landscape landscape = this.getLandscape(noises.landscape(), landscapeProb);
        BiomeGetter biomeGetter = new BiomeGetter(landscape, plant = this.getPlant(noises.plant(), plantProb), bush = this.getBush(noises.bush(), bushProb), temperature = this.getTemperature(noises.temperature(), temperatureProb), hygrometry = this.getHygrometry(noises.hygrometry(), hygrometryProb), oreLevel = this.getOre(noises.ore(), oreLevelProb));
        List biomes = this.biomeCache.computeIfAbsent(biomeGetter, k -> biomesIn.values().stream().filter(b -> b.landscape().contains((Object)landscape)).filter(b -> b.plant().contains((Object)plant)).filter(b -> b.bush().contains((Object)bush)).filter(b -> b.temperature().contains((Object)temperature)).filter(b -> b.hygrometry().contains((Object)hygrometry)).filter(b -> b.oreLevel().contains((Object)oreLevel)).collect(Collectors.toList()));
        if (biomes.isEmpty()) {
            return Biome.DEFAULT_BIOME;
        }
        return (IBiome)biomes.get((int)(selector * (double)biomes.size()));
    }

    public IBiome get(Map<String, Biome> biomesIn, String filter, BiomeNoise noises, double selector, Map<BiomeType.Bush, List<Double>> bushProb, Map<BiomeType.OreLevel, List<Double>> oreLevelProb) {
        BiomeType.OreLevel oreLevel;
        if (filter == null) {
            return null;
        }
        BiomeType.Bush bush = this.getBush(noises.bush(), bushProb);
        BiomeGetterFiltered biomeGetter = new BiomeGetterFiltered(filter, bush, oreLevel = this.getOre(noises.ore(), oreLevelProb));
        List biomes = this.biomeCacheFiltered.computeIfAbsent(biomeGetter, k -> biomesIn.values().stream().filter(b -> b.name().startsWith(filter)).filter(b -> b.bush().contains((Object)bush)).filter(b -> b.oreLevel().contains((Object)oreLevel)).collect(Collectors.toList()));
        if (biomes.isEmpty()) {
            return Biome.DEFAULT_BIOME;
        }
        return (IBiome)biomes.get((int)(selector * (double)biomes.size()));
    }

    private BiomeType.Landscape getLandscape(double noise, Map<BiomeType.Landscape, List<Double>> landscapeProb) {
        BiomeType.Landscape landscape = noise >= landscapeProb.get((Object)BiomeType.Landscape.FOREST).get(0) && noise < landscapeProb.get((Object)BiomeType.Landscape.FOREST).get(1) ? BiomeType.Landscape.FOREST : (noise >= landscapeProb.get((Object)BiomeType.Landscape.LIGHT_FOREST).get(0) && noise < landscapeProb.get((Object)BiomeType.Landscape.LIGHT_FOREST).get(1) ? BiomeType.Landscape.LIGHT_FOREST : (noise >= landscapeProb.get((Object)BiomeType.Landscape.PLAIN).get(0) && noise < landscapeProb.get((Object)BiomeType.Landscape.PLAIN).get(1) ? BiomeType.Landscape.PLAIN : BiomeType.Landscape.NONE));
        return landscape;
    }

    private BiomeType.Plant getPlant(double noise, Map<BiomeType.Plant, List<Double>> plantProb) {
        BiomeType.Plant plant = noise >= plantProb.get((Object)BiomeType.Plant.FLOWER).get(0) && noise < plantProb.get((Object)BiomeType.Plant.FLOWER).get(1) ? BiomeType.Plant.FLOWER : (noise >= plantProb.get((Object)BiomeType.Plant.GRASS).get(0) && noise < plantProb.get((Object)BiomeType.Plant.GRASS).get(1) ? BiomeType.Plant.GRASS : BiomeType.Plant.NONE);
        return plant;
    }

    private BiomeType.Bush getBush(double noise, Map<BiomeType.Bush, List<Double>> bushProb) {
        BiomeType.Bush bush = noise >= bushProb.get((Object)BiomeType.Bush.DRY).get(0) && noise < bushProb.get((Object)BiomeType.Bush.DRY).get(1) ? BiomeType.Bush.DRY : (noise >= bushProb.get((Object)BiomeType.Bush.REGULAR).get(0) && noise < bushProb.get((Object)BiomeType.Bush.REGULAR).get(1) ? BiomeType.Bush.REGULAR : (noise >= bushProb.get((Object)BiomeType.Bush.FAT).get(0) && noise < bushProb.get((Object)BiomeType.Bush.FAT).get(1) ? BiomeType.Bush.FAT : BiomeType.Bush.NONE));
        return bush;
    }

    private BiomeType.Temperature getTemperature(double noise, Map<BiomeType.Temperature, List<Double>> temperatureProb) {
        BiomeType.Temperature temperature = noise >= temperatureProb.get((Object)BiomeType.Temperature.HOT).get(0) && noise < temperatureProb.get((Object)BiomeType.Temperature.HOT).get(1) ? BiomeType.Temperature.HOT : (noise >= temperatureProb.get((Object)BiomeType.Temperature.MEDIUM).get(0) && noise < temperatureProb.get((Object)BiomeType.Temperature.MEDIUM).get(1) ? BiomeType.Temperature.MEDIUM : (noise >= temperatureProb.get((Object)BiomeType.Temperature.COLD).get(0) && noise < temperatureProb.get((Object)BiomeType.Temperature.COLD).get(1) ? BiomeType.Temperature.COLD : BiomeType.Temperature.NONE));
        return temperature;
    }

    private BiomeType.Hygrometry getHygrometry(double noise, Map<BiomeType.Hygrometry, List<Double>> hygrometryProb) {
        BiomeType.Hygrometry hygrometry = noise >= hygrometryProb.get((Object)BiomeType.Hygrometry.FLOODING).get(0) && noise < hygrometryProb.get((Object)BiomeType.Hygrometry.FLOODING).get(1) ? BiomeType.Hygrometry.FLOODING : (noise >= hygrometryProb.get((Object)BiomeType.Hygrometry.RAIN).get(0) && noise < hygrometryProb.get((Object)BiomeType.Hygrometry.RAIN).get(1) ? BiomeType.Hygrometry.RAIN : (noise >= hygrometryProb.get((Object)BiomeType.Hygrometry.DRY).get(0) && noise < hygrometryProb.get((Object)BiomeType.Hygrometry.DRY).get(1) ? BiomeType.Hygrometry.DRY : BiomeType.Hygrometry.NONE));
        return hygrometry;
    }

    private BiomeType.OreLevel getOre(double noise, Map<BiomeType.OreLevel, List<Double>> oreLevelProb) {
        BiomeType.OreLevel oreLevel = noise >= oreLevelProb.get((Object)BiomeType.OreLevel.VERY_HIGH).get(0) && noise < oreLevelProb.get((Object)BiomeType.OreLevel.VERY_HIGH).get(1) ? BiomeType.OreLevel.VERY_HIGH : (noise >= oreLevelProb.get((Object)BiomeType.OreLevel.HIGH).get(0) && noise < oreLevelProb.get((Object)BiomeType.OreLevel.HIGH).get(1) ? BiomeType.OreLevel.HIGH : (noise >= oreLevelProb.get((Object)BiomeType.OreLevel.MEDIUM).get(0) && noise < oreLevelProb.get((Object)BiomeType.OreLevel.MEDIUM).get(1) ? BiomeType.OreLevel.MEDIUM : (noise >= oreLevelProb.get((Object)BiomeType.OreLevel.LOW).get(0) && noise < oreLevelProb.get((Object)BiomeType.OreLevel.LOW).get(1) ? BiomeType.OreLevel.LOW : (noise >= oreLevelProb.get((Object)BiomeType.OreLevel.VERY_LOW).get(0) && noise < oreLevelProb.get((Object)BiomeType.OreLevel.VERY_LOW).get(1) ? BiomeType.OreLevel.VERY_LOW : BiomeType.OreLevel.NONE))));
        return oreLevel;
    }

    private record BiomeGetter(BiomeType.Landscape landscape, BiomeType.Plant plant, BiomeType.Bush bush, BiomeType.Temperature temperature, BiomeType.Hygrometry hygrometry, BiomeType.OreLevel oreLevel) {
    }

    private record BiomeGetterFiltered(String filter, BiomeType.Bush bush, BiomeType.OreLevel oreLevel) {
    }
}

