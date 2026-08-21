/*
 * Decompiled with CFR 0.152.
 */
package zombie.iso;

import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix3f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import zombie.core.math.PZMath;
import zombie.debug.DebugLog;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.WaterFlowLookup;
import zombie.iso.WaterFlowObject;
import zombie.util.FastTrig360;

public final class IsoWaterFlow {
    private static final float MIN_DISTANCE = 0.1f;
    private static final float IGNORE_DISTANCE_FACTOR = 10.0f;
    private static final List<WaterFlowObject> POINTS = new ArrayList<WaterFlowObject>();
    private static final List<Matrix3f> ZONES = new ArrayList<Matrix3f>();
    private static final WaterFlowLookup LOOKUP = new WaterFlowLookup();
    private static final WaterFlowObject TEMP_POINT = new WaterFlowObject();
    private static final List<WaterFlowObject> NEAREST_MULTIPLE = new ArrayList<WaterFlowObject>();
    private static final Vector3f TEMP_VEC3F = new Vector3f();

    public static void addFlow(float x, float y, float flow, float speed) {
        int degrees = (360 - (int)flow - 45) % 360;
        if (degrees < 0) {
            degrees += 360;
        }
        flow = (float)Math.toRadians(degrees);
        WaterFlowObject point = new WaterFlowObject((int)x, (int)y, flow, speed);
        POINTS.add(point);
        LOOKUP.insert(point);
    }

    public static void addZone(float x1, float y1, float x2, float y2, float shore, float waterGround) {
        if (x1 > x2 || y1 > y2 || (double)shore > 1.0) {
            DebugLog.log("ERROR IsoWaterFlow: Invalid waterzone (" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")");
        }
        ZONES.add(new Matrix3f(x1, y1, x2, y2, shore, waterGround, 0.0f, 0.0f, 0.0f));
    }

    public static int getShore(int x, int y) {
        for (int i = 0; i < ZONES.size(); ++i) {
            Matrix3f zone = ZONES.get(i);
            if (!(zone.m00 <= (float)x) || !(zone.m02 >= (float)x) || !(zone.m01 <= (float)y) || !(zone.m10 >= (float)y)) continue;
            return (int)zone.m11;
        }
        return 1;
    }

    private static float trigonometricCircleMethod(float angle1, float weight1, float angle2, float weight2) {
        float x = weight1 * FastTrig360.cos(angle1) + weight2 * FastTrig360.cos(angle2);
        float y = weight1 * FastTrig360.sin(angle1) + weight2 * FastTrig360.sin(angle2);
        return PZMath.wrap(FastTrig360.approximateAtan2(y, x), 0.0f, (float)Math.PI * 2);
    }

    private static void blendDistFlowSpeed(float distA, float radiansA, float speedA, float distB, float radiansB, float speedB, Vector3f out) {
        float distA2 = PZMath.max(distA, 0.1f);
        float distB2 = PZMath.max(distB, 0.1f);
        float dist = distA2 + distB2;
        out.x = IsoWaterFlow.trigonometricCircleMethod(radiansA, 1.0f - distA2 / dist, radiansB, 1.0f - distB2 / dist);
        out.y = speedA * (1.0f - distA2 / dist) + speedB * (1.0f - distB2 / dist);
        out.z = distA2 * (1.0f - distA2 / dist) + distB2 * (1.0f - distB2 / dist);
    }

    public static Vector2f getFlow(IsoGridSquare square, int ax, int ay, Vector2f out) {
        WaterFlowObject objectB;
        float distB;
        if (POINTS.isEmpty()) {
            return out.set(0.0f, 0.0f);
        }
        WaterFlowObject object = TEMP_POINT;
        object.x = square.getX() + ax;
        object.y = square.getY() + ay;
        LOOKUP.nearestMultiple(object, 3, NEAREST_MULTIPLE);
        WaterFlowObject objectA = NEAREST_MULTIPLE.get(0);
        float distA = PZMath.sqrt(objectA.distSq(object));
        float flow = objectA.radians;
        float speed = objectA.speed;
        if (NEAREST_MULTIPLE.size() > 1 && distA * 10.0f >= (distB = PZMath.sqrt((objectB = NEAREST_MULTIPLE.get(1)).distSq(object)))) {
            if (NEAREST_MULTIPLE.size() > 2) {
                WaterFlowObject objectC = NEAREST_MULTIPLE.get(2);
                float distC = PZMath.sqrt(objectC.distSq(object));
                if (distB * 10.0f >= distC) {
                    IsoWaterFlow.blendDistFlowSpeed(distB, objectB.radians, objectB.speed, distC, objectC.radians, objectC.speed, TEMP_VEC3F);
                    IsoWaterFlow.blendDistFlowSpeed(distA, objectA.radians, objectA.speed, IsoWaterFlow.TEMP_VEC3F.z, IsoWaterFlow.TEMP_VEC3F.x, IsoWaterFlow.TEMP_VEC3F.y, TEMP_VEC3F);
                    flow = IsoWaterFlow.TEMP_VEC3F.x;
                    speed = IsoWaterFlow.TEMP_VEC3F.y;
                } else {
                    IsoWaterFlow.blendDistFlowSpeed(distA, objectA.radians, objectA.speed, distB, objectB.radians, objectB.speed, TEMP_VEC3F);
                    flow = IsoWaterFlow.TEMP_VEC3F.x;
                    speed = IsoWaterFlow.TEMP_VEC3F.y;
                }
            } else {
                IsoWaterFlow.blendDistFlowSpeed(distA, objectA.radians, objectA.speed, distB, objectB.radians, objectB.speed, TEMP_VEC3F);
                flow = IsoWaterFlow.TEMP_VEC3F.x;
                speed = IsoWaterFlow.TEMP_VEC3F.y;
            }
        }
        float s = 1.0f;
        IsoCell cell = square.getCell();
        for (int dx = -5; dx < 5; ++dx) {
            for (int dy = -5; dy < 5; ++dy) {
                IsoGridSquare square1 = cell.getGridSquare(square.x + ax + dx, square.y + ay + dy, 0);
                if (square1 != null && square1.getProperties().has(IsoFlagType.water)) continue;
                s = (float)Math.min((double)s, Math.max(0.0, Math.sqrt(dx * dx + dy * dy)) / 4.0);
            }
        }
        return out.set(flow, speed *= s);
    }

    public static List<WaterFlowObject> getPoints() {
        return POINTS;
    }

    public static WaterFlowObject getNearest(int x, int y) {
        IsoWaterFlow.TEMP_POINT.x = x;
        IsoWaterFlow.TEMP_POINT.y = y;
        return LOOKUP.nearest(TEMP_POINT);
    }

    public static List<WaterFlowObject> getNearestMultiple(int x, int y, int max) {
        IsoWaterFlow.TEMP_POINT.x = x;
        IsoWaterFlow.TEMP_POINT.y = y;
        return LOOKUP.nearestMultiple(TEMP_POINT, max, NEAREST_MULTIPLE);
    }

    public static void Reset() {
        POINTS.clear();
        ZONES.clear();
        LOOKUP.clear();
    }
}

