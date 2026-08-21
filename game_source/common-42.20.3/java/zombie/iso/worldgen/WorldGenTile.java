/*
 * Decompiled with CFR 0.152.
 */
package zombie.iso.worldgen;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import zombie.debug.DebugType;
import zombie.iso.CellLoader;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.sprite.IsoSprite;
import zombie.iso.sprite.IsoSpriteManager;
import zombie.iso.worldgen.ChunksCache;
import zombie.iso.worldgen.WorldGenUtils;
import zombie.iso.worldgen.biomes.Feature;
import zombie.iso.worldgen.biomes.FeatureType;
import zombie.iso.worldgen.biomes.IBiome;
import zombie.iso.worldgen.biomes.TileGroup;
import zombie.iso.worldgen.enums.TileLookup;
import zombie.iso.worldgen.enums.TileLookupRetValue;
import zombie.iso.worldgen.enums.TileReplacement;
import zombie.iso.worldgen.enums.TileReplacementRetValue;
import zombie.iso.worldgen.roads.Road;
import zombie.iso.worldgen.veins.OreVein;

public class WorldGenTile {
    public static final String NO_TREE = "$no_tree";
    public static final String NO_BUSH = "$no_bush";
    public static final String NO_GRASS = "$no_grass";
    public static final String ANY = "$any";
    public static final String SUBBIOME = "$subbiome";

    public TileReplacementRetValue setTiles(IBiome biome, FeatureType type, IsoGridSquare square, ChunksCache chunks, IsoChunk ch, IsoCell cell, int x, int y, int z, int tileX, int tileY, int tileZ, EnumMap<FeatureType, String[]> toBeDone, Random rnd) {
        for (int i = 0; i < 16; ++i) {
            int targetSize = 8 >> i;
            if (targetSize == 0) {
                return new TileReplacementRetValue(TileReplacement.FAILURE, null);
            }
            TileLookupRetValue tileLookupReturn = this.getBiomeTile(biome, type, chunks, ch, x, y, z, tileX, tileY, tileZ, toBeDone, rnd, targetSize);
            TileLookup tileLookup = tileLookupReturn.tileLookup();
            String tile = tileLookupReturn.tile();
            if (tile == null && (tileLookup == TileLookup.FEATURES_LIST_EMPTY || tileLookup == TileLookup.NO_FEATURE)) {
                return new TileReplacementRetValue(TileReplacement.DELETE, null);
            }
            if (tile != null && (tile.equals(ANY) || tile.equals(SUBBIOME) || tile.equals(NO_BUSH) || tile.equals(NO_GRASS) || tile.equals(NO_TREE))) {
                return new TileReplacementRetValue(TileReplacement.PENDING, null);
            }
            if (tile == null) continue;
            return this.applyTile(tile, square, cell, x, y, z, rnd);
        }
        return new TileReplacementRetValue(TileReplacement.FAILURE, null);
    }

    private TileLookupRetValue getBiomeTile(IBiome biome, FeatureType type, ChunksCache chunks, IsoChunk ch, int x, int y, int z, int tileX, int tileY, int tileZ, EnumMap<FeatureType, String[]> toBeDone, Random rnd, int targetSize) {
        return this.getBiomeTile(biome, type, chunks, ch, x, y, z, tileX, tileY, tileZ, toBeDone, rnd, targetSize, 0);
    }

    private TileLookupRetValue getBiomeTile(IBiome biome, FeatureType type, ChunksCache chunks, IsoChunk ch, int x, int y, int z, int tileX, int tileY, int tileZ, EnumMap<FeatureType, String[]> toBeDone, Random rnd, int targetSize, int depth) {
        String tile = toBeDone.get((Object)type)[tileX + tileY * 16];
        if (tile == null || tile.isEmpty() || tile.equals(ANY) || tile.equals(SUBBIOME)) {
            Map<FeatureType, List<Feature>> features = biome.getFeatures();
            if (features == null) {
                return new TileLookupRetValue(TileLookup.NO_FEATURES, null);
            }
            List<Feature> featuresList = features.get((Object)type);
            if (featuresList == null) {
                return new TileLookupRetValue(TileLookup.NO_SPECIFIC_TYPE, null);
            }
            if (featuresList.isEmpty()) {
                return new TileLookupRetValue(TileLookup.FEATURES_LIST_EMPTY, null);
            }
            ArrayList<Feature> featuresFiltered = new ArrayList<Feature>();
            for (Feature feature : featuresList) {
                if (feature.minSize() > targetSize) continue;
                featuresFiltered.add(feature);
            }
            float prefilterProba = 0.0f;
            float postfilterProba = 0.0f;
            for (Feature feature : featuresList) {
                prefilterProba += feature.probability().getValue();
            }
            for (Feature feature : featuresFiltered) {
                postfilterProba += feature.probability().getValue();
            }
            Feature feature = this.findFeature(featuresFiltered, prefilterProba, postfilterProba, rnd);
            if (feature == null) {
                return new TileLookupRetValue(TileLookup.NO_FEATURE, null);
            }
            ArrayList<TileGroup> tileGroups = new ArrayList<TileGroup>();
            for (TileGroup tileGroup : feature.tileGroups()) {
                if (tileGroup.sx() > targetSize || tileGroup.sy() > targetSize) continue;
                tileGroups.add(tileGroup);
            }
            if (tileGroups.isEmpty()) {
                return new TileLookupRetValue(TileLookup.TILE_GROUPS_EMPTY, null);
            }
            TileGroup tileGroup = (TileGroup)tileGroups.get(rnd.nextInt(tileGroups.size()));
            if (tileX + tileGroup.sx() - 1 >= 16 || tileY + tileGroup.sy() - 1 >= 16) {
                return new TileLookupRetValue(TileLookup.TOO_BIG, null);
            }
            int cx = x - ch.wx * 8;
            int cy = y - ch.wy * 8;
            if (biome.placements() != null && !this.checkFutureSquares(tileGroup, chunks, ch, cx, cy, z, biome.placements().get((Object)type))) {
                return new TileLookupRetValue(TileLookup.CANT_BE_PLACED, null);
            }
            for (int ix = 0; ix < tileGroup.sx(); ++ix) {
                for (int iy = 0; iy < tileGroup.sy(); ++iy) {
                    toBeDone.get((Object)((Object)type))[ix + tileX + (iy + tileY) * 16] = tileGroup.tiles().get(ix + iy * tileGroup.sx());
                }
            }
            tile = toBeDone.get((Object)type)[tileX + tileY * 16];
            if (tile.equals(ANY)) {
                toBeDone.get((Object)((Object)type))[tileX + tileY * 16] = null;
                if (depth < 1) {
                    return this.getBiomeTile(biome, type, chunks, ch, x, y, z, tileX, tileY, tileZ, toBeDone, rnd, targetSize, depth + 1);
                }
            }
        }
        return new TileLookupRetValue(TileLookup.SUCCESS, tile);
    }

    public Feature findFeature(List<Feature> features, float prefilterProba, float postfilterProba, Random rnd) {
        if (features == null || features.isEmpty()) {
            return null;
        }
        float rndValue = rnd.nextFloat();
        float probability = 0.0f;
        Feature feature = null;
        for (Feature tmpFeature : features) {
            if (rndValue >= (probability += tmpFeature.probability().getValue() / postfilterProba * prefilterProba)) continue;
            feature = tmpFeature;
            break;
        }
        return feature;
    }

    private boolean checkFutureSquares(TileGroup tg, ChunksCache chunks, IsoChunk ch, int tileX, int tileY, int tileZ, List<String> placement) {
        if (tg.sx() == 1 && tg.sy() == 1) {
            return true;
        }
        for (int ix = 0; ix < tg.sx(); ++ix) {
            for (int iy = 0; iy < tg.sy(); ++iy) {
                if (ix == 0 && iy == 0) continue;
                IsoGridSquare square = chunks.getGridSquare(tileX + ix, tileY + iy, tileZ);
                if (square == null) {
                    DebugType.WorldGen.warn("Square not found | {c: [%d %d] t:[%d %d] i:[%d %d]} => {%d %d}", ch.wx, ch.wy, tileX, tileY, ix, iy, ch.wx * 8 + tileX + ix, ch.wy * 8 + tileY + iy);
                    return false;
                }
                IsoObject floor = square.getFloor();
                if (floor == null) {
                    DebugType.WorldGen.warn("Floor not found | {c: [%d %d] t:[%d %d] i:[%d %d]} => {%d %d}", ch.wx, ch.wy, tileX, tileY, ix, iy, ch.wx * 8 + tileX + ix, ch.wy * 8 + tileY + iy);
                    return false;
                }
                if (this.isHumanStructure(square)) {
                    return false;
                }
                if (WorldGenUtils.INSTANCE.canPlace(placement, floor.getSprite().getName())) continue;
                return false;
            }
        }
        return true;
    }

    private boolean isHumanStructure(IsoGridSquare square) {
        int numObjects = square.getObjects().size();
        int numGrasses = square.getGrassLike().size();
        int numBushes = square.getBushes().size();
        int numOres = square.getOres().size();
        boolean hasTree = square.getTree() != null;
        return numObjects - numGrasses - numBushes - numOres > 1 && !hasTree;
    }

    public void setTile(OreVein vein, IsoGridSquare square, IsoCell cell, int x, int y, int z, int tileX, int tileY, int tileZ, EnumMap<FeatureType, String[]> toBeDone, Random rnd) {
        List<TileGroup> tileGroups = vein.getSingleFeatures();
        TileGroup tileGroup = tileGroups.get(rnd.nextInt(tileGroups.size()));
        this.applyTile(tileGroup.tiles().get(0), square, cell, x, y, z, rnd);
    }

    public void setTile(Road road, IsoGridSquare square, IsoCell cell, int x, int y, int z, int tileX, int tileY, int tileZ, EnumMap<FeatureType, String[]> toBeDone, Random rnd) {
        List<TileGroup> tileGroups = road.getSingleFeatures();
        TileGroup tileGroup = tileGroups.get(rnd.nextInt(tileGroups.size()));
        this.applyTile(tileGroup.tiles().get(0), square, cell, x, y, z, rnd);
    }

    public TileReplacementRetValue applyTile(String tile, IsoGridSquare square, IsoCell cell, int x, int y, int z, Random rnd) {
        TileReplacement retval = TileReplacement.SUCCESS;
        IsoSprite spr = this.getSprite(IsoChunk.Fix2x(tile));
        if (spr == null) {
            spr = IsoSprite.getSprite(IsoSpriteManager.instance, "carpentry_02_58", 0);
            retval = TileReplacement.FAILURE;
        }
        CellLoader.DoTileObjectCreation(spr, spr.getTileType(), square, cell, x, y, z, tile);
        return new TileReplacementRetValue(retval, spr);
    }

    public IsoSprite getSprite(String tile) {
        IsoSprite spr = IsoSpriteManager.instance.namedMap.get(tile);
        if (spr == null) {
            DebugType.WorldGen.error("Missing tile definition: " + tile);
        }
        return spr;
    }

    public TileGroup getGround(IBiome biome, Random rnd) {
        List<Feature> features = biome.getFeatures().get((Object)FeatureType.GROUND);
        Feature feature = features.get(rnd.nextInt(features.size()));
        List<TileGroup> tileGroups = feature.tileGroups();
        return tileGroups.get(rnd.nextInt(features.size()));
    }

    public TileGroup getPlant(IBiome biome, Random rnd) {
        List<Feature> features = biome.getFeatures().get((Object)FeatureType.PLANT);
        if (features == null || features.isEmpty()) {
            return null;
        }
        Feature feature = features.get(rnd.nextInt(features.size()));
        if (rnd.nextFloat() > feature.probability().getValue()) {
            return null;
        }
        List<TileGroup> tileGroups = feature.tileGroups();
        return tileGroups.get(rnd.nextInt(tileGroups.size()));
    }

    public void setGround(IsoSprite spr, IsoGridSquare sq) {
        spr.solidfloor = true;
        IsoObject floor = sq.getFloor();
        if (floor != null) {
            floor.clearAttachedAnimSprite();
            floor.setSprite(spr);
        }
    }

    public void deleteTiles(IsoGridSquare sq) {
        ArrayList<IsoObject> toDelete = new ArrayList<IsoObject>();
        for (IsoObject element : sq.getObjects().getElements()) {
            if (element == null || element.isFloor()) continue;
            toDelete.add(element);
        }
        for (IsoObject element : toDelete) {
            sq.DeleteTileObject(element);
        }
    }

    public void deleteTiles(IsoGridSquare sq, List<String> toRemove) {
        ArrayList<IsoObject> toDelete = new ArrayList<IsoObject>();
        for (IsoObject element : sq.getObjects().getElements()) {
            String tmpTile;
            if (element == null || !toRemove.contains(tmpTile = element.getSprite().name)) continue;
            toDelete.add(element);
        }
        for (IsoObject element : toDelete) {
            sq.DeleteTileObject(element);
        }
    }
}

