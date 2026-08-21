/*
 * Decompiled with CFR 0.152.
 */
package zombie.iso.worldgen;

import java.util.Random;
import zombie.iso.weather.SimplexNoise;
import zombie.iso.worldgen.biomes.BiomeNoise;

public class WorldGenSimplexGenerator {
    private final WGSimplexNoise noises;
    private final WGSimplex selector;

    public WorldGenSimplexGenerator(long seed) {
        Random rnd = new Random(seed + 100L);
        WGSimplex tree = new WGSimplex(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), 128.0, 0.0);
        rnd = new Random(seed + 200L);
        WGSimplex plant = new WGSimplex(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), 128.0, 0.0);
        rnd = new Random(seed + 300L);
        WGSimplex bush = new WGSimplex(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), 128.0, 0.0);
        rnd = new Random(seed + 400L);
        WGSimplex temperature = new WGSimplex(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), 128.0, 0.0);
        rnd = new Random(seed + 500L);
        WGSimplex hygrometry = new WGSimplex(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), 128.0, 0.0);
        rnd = new Random(seed + 600L);
        WGSimplex ore = new WGSimplex(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), 128.0, 0.0);
        this.noises = new WGSimplexNoise(tree, plant, bush, temperature, hygrometry, ore);
        rnd = new Random(seed + 700L);
        this.selector = new WGSimplex(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), 16.0, 0.0);
    }

    public BiomeNoise noise(double x, double y) {
        return new BiomeNoise(this.noises.tree().noise(x, y), this.noises.plant().noise(x, y), this.noises.bush().noise(x, y), this.noises.temperature().noise(x, y), this.noises.hygrometry().noise(x, y), this.noises.ore().noise(x, y));
    }

    public double selector(double x, double y) {
        return (this.selector.noise(x, y) + 1.0) / 2.0;
    }

    private record WGSimplex(double offsetX, double offsetY, double depth, double scale, double offsetNoise) {
        public double noise(double x, double y) {
            return SimplexNoise.noise((x + this.offsetX) / this.scale, (y + this.offsetY) / this.scale, this.depth);
        }
    }

    private record WGSimplexNoise(WGSimplex tree, WGSimplex plant, WGSimplex bush, WGSimplex temperature, WGSimplex hygrometry, WGSimplex ore) {
    }
}

