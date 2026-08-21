/*
 * Decompiled with CFR 0.152.
 */
package org.joml;

public class RayAabIntersection {
    private float originX;
    private float originY;
    private float originZ;
    private float dirX;
    private float dirY;
    private float dirZ;
    private float cXy;
    private float cYx;
    private float cZy;
    private float cYz;
    private float cXz;
    private float cZx;
    private float sXy;
    private float sYx;
    private float sZy;
    private float sYz;
    private float sXz;
    private float sZx;
    private byte classification;

    public RayAabIntersection() {
    }

    public RayAabIntersection(float originX, float originY, float originZ, float dirX, float dirY, float dirZ) {
        this.set(originX, originY, originZ, dirX, dirY, dirZ);
    }

    public void set(float originX, float originY, float originZ, float dirX, float dirY, float dirZ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.dirX = dirX;
        this.dirY = dirY;
        this.dirZ = dirZ;
        this.precomputeSlope();
    }

    private static int signum(float f) {
        return f == 0.0f || Float.isNaN(f) ? 0 : (1 - Float.floatToIntBits(f) >>> 31 << 1) - 1;
    }

    private void precomputeSlope() {
        float invDirX = 1.0f / this.dirX;
        float invDirY = 1.0f / this.dirY;
        float invDirZ = 1.0f / this.dirZ;
        this.sYx = this.dirX * invDirY;
        this.sXy = this.dirY * invDirX;
        this.sZy = this.dirY * invDirZ;
        this.sYz = this.dirZ * invDirY;
        this.sXz = this.dirZ * invDirX;
        this.sZx = this.dirX * invDirZ;
        this.cXy = this.originY - this.sXy * this.originX;
        this.cYx = this.originX - this.sYx * this.originY;
        this.cZy = this.originY - this.sZy * this.originZ;
        this.cYz = this.originZ - this.sYz * this.originY;
        this.cXz = this.originZ - this.sXz * this.originX;
        this.cZx = this.originX - this.sZx * this.originZ;
        int sgnX = RayAabIntersection.signum(this.dirX);
        int sgnY = RayAabIntersection.signum(this.dirY);
        int sgnZ = RayAabIntersection.signum(this.dirZ);
        this.classification = (byte)(sgnZ + 1 << 4 | sgnY + 1 << 2 | sgnX + 1);
    }

    public boolean test(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        switch (this.classification) {
            case 0: {
                return this.MMM(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 1: {
                return this.OMM(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 2: {
                return this.PMM(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 3: {
                return false;
            }
            case 4: {
                return this.MOM(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 5: {
                return this.OOM(minX, minY, minZ, maxX, maxY);
            }
            case 6: {
                return this.POM(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 7: {
                return false;
            }
            case 8: {
                return this.MPM(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 9: {
                return this.OPM(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 10: {
                return this.PPM(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 11: 
            case 12: 
            case 13: 
            case 14: 
            case 15: {
                return false;
            }
            case 16: {
                return this.MMO(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 17: {
                return this.OMO(minX, minY, minZ, maxX, maxZ);
            }
            case 18: {
                return this.PMO(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 19: {
                return false;
            }
            case 20: {
                return this.MOO(minX, minY, minZ, maxY, maxZ);
            }
            case 21: {
                return false;
            }
            case 22: {
                return this.POO(minY, minZ, maxX, maxY, maxZ);
            }
            case 23: {
                return false;
            }
            case 24: {
                return this.MPO(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 25: {
                return this.OPO(minX, minZ, maxX, maxY, maxZ);
            }
            case 26: {
                return this.PPO(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 27: 
            case 28: 
            case 29: 
            case 30: 
            case 31: {
                return false;
            }
            case 32: {
                return this.MMP(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 33: {
                return this.OMP(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 34: {
                return this.PMP(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 35: {
                return false;
            }
            case 36: {
                return this.MOP(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 37: {
                return this.OOP(minX, minY, maxX, maxY, maxZ);
            }
            case 38: {
                return this.POP(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 39: {
                return false;
            }
            case 40: {
                return this.MPP(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 41: {
                return this.OPP(minX, minY, minZ, maxX, maxY, maxZ);
            }
            case 42: {
                return this.PPP(minX, minY, minZ, maxX, maxY, maxZ);
            }
        }
        return false;
    }

    private boolean MMM(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX >= minX && this.originY >= minY && this.originZ >= minZ && this.sXy * minX - maxY + this.cXy <= 0.0f && this.sYx * minY - maxX + this.cYx <= 0.0f && this.sZy * minZ - maxY + this.cZy <= 0.0f && this.sYz * minY - maxZ + this.cYz <= 0.0f && this.sXz * minX - maxZ + this.cXz <= 0.0f && this.sZx * minZ - maxX + this.cZx <= 0.0f;
    }

    private boolean OMM(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX >= minX && this.originX <= maxX && this.originY >= minY && this.originZ >= minZ && this.sZy * minZ - maxY + this.cZy <= 0.0f && this.sYz * minY - maxZ + this.cYz <= 0.0f;
    }

    private boolean PMM(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX <= maxX && this.originY >= minY && this.originZ >= minZ && this.sXy * maxX - maxY + this.cXy <= 0.0f && this.sYx * minY - minX + this.cYx >= 0.0f && this.sZy * minZ - maxY + this.cZy <= 0.0f && this.sYz * minY - maxZ + this.cYz <= 0.0f && this.sXz * maxX - maxZ + this.cXz <= 0.0f && this.sZx * minZ - minX + this.cZx >= 0.0f;
    }

    private boolean MOM(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originY >= minY && this.originY <= maxY && this.originX >= minX && this.originZ >= minZ && this.sXz * minX - maxZ + this.cXz <= 0.0f && this.sZx * minZ - maxX + this.cZx <= 0.0f;
    }

    private boolean OOM(float minX, float minY, float minZ, float maxX, float maxY) {
        return this.originZ >= minZ && this.originX >= minX && this.originX <= maxX && this.originY >= minY && this.originY <= maxY;
    }

    private boolean POM(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originY >= minY && this.originY <= maxY && this.originX <= maxX && this.originZ >= minZ && this.sXz * maxX - maxZ + this.cXz <= 0.0f && this.sZx * minZ - minX + this.cZx >= 0.0f;
    }

    private boolean MPM(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX >= minX && this.originY <= maxY && this.originZ >= minZ && this.sXy * minX - minY + this.cXy >= 0.0f && this.sYx * maxY - maxX + this.cYx <= 0.0f && this.sZy * minZ - minY + this.cZy >= 0.0f && this.sYz * maxY - maxZ + this.cYz <= 0.0f && this.sXz * minX - maxZ + this.cXz <= 0.0f && this.sZx * minZ - maxX + this.cZx <= 0.0f;
    }

    private boolean OPM(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX >= minX && this.originX <= maxX && this.originY <= maxY && this.originZ >= minZ && this.sZy * minZ - minY + this.cZy >= 0.0f && this.sYz * maxY - maxZ + this.cYz <= 0.0f;
    }

    private boolean PPM(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX <= maxX && this.originY <= maxY && this.originZ >= minZ && this.sXy * maxX - minY + this.cXy >= 0.0f && this.sYx * maxY - minX + this.cYx >= 0.0f && this.sZy * minZ - minY + this.cZy >= 0.0f && this.sYz * maxY - maxZ + this.cYz <= 0.0f && this.sXz * maxX - maxZ + this.cXz <= 0.0f && this.sZx * minZ - minX + this.cZx >= 0.0f;
    }

    private boolean MMO(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originZ >= minZ && this.originZ <= maxZ && this.originX >= minX && this.originY >= minY && this.sXy * minX - maxY + this.cXy <= 0.0f && this.sYx * minY - maxX + this.cYx <= 0.0f;
    }

    private boolean OMO(float minX, float minY, float minZ, float maxX, float maxZ) {
        return this.originY >= minY && this.originX >= minX && this.originX <= maxX && this.originZ >= minZ && this.originZ <= maxZ;
    }

    private boolean PMO(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originZ >= minZ && this.originZ <= maxZ && this.originX <= maxX && this.originY >= minY && this.sXy * maxX - maxY + this.cXy <= 0.0f && this.sYx * minY - minX + this.cYx >= 0.0f;
    }

    private boolean MOO(float minX, float minY, float minZ, float maxY, float maxZ) {
        return this.originX >= minX && this.originY >= minY && this.originY <= maxY && this.originZ >= minZ && this.originZ <= maxZ;
    }

    private boolean POO(float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX <= maxX && this.originY >= minY && this.originY <= maxY && this.originZ >= minZ && this.originZ <= maxZ;
    }

    private boolean MPO(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originZ >= minZ && this.originZ <= maxZ && this.originX >= minX && this.originY <= maxY && this.sXy * minX - minY + this.cXy >= 0.0f && this.sYx * maxY - maxX + this.cYx <= 0.0f;
    }

    private boolean OPO(float minX, float minZ, float maxX, float maxY, float maxZ) {
        return this.originY <= maxY && this.originX >= minX && this.originX <= maxX && this.originZ >= minZ && this.originZ <= maxZ;
    }

    private boolean PPO(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originZ >= minZ && this.originZ <= maxZ && this.originX <= maxX && this.originY <= maxY && this.sXy * maxX - minY + this.cXy >= 0.0f && this.sYx * maxY - minX + this.cYx >= 0.0f;
    }

    private boolean MMP(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX >= minX && this.originY >= minY && this.originZ <= maxZ && this.sXy * minX - maxY + this.cXy <= 0.0f && this.sYx * minY - maxX + this.cYx <= 0.0f && this.sZy * maxZ - maxY + this.cZy <= 0.0f && this.sYz * minY - minZ + this.cYz >= 0.0f && this.sXz * minX - minZ + this.cXz >= 0.0f && this.sZx * maxZ - maxX + this.cZx <= 0.0f;
    }

    private boolean OMP(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX >= minX && this.originX <= maxX && this.originY >= minY && this.originZ <= maxZ && this.sZy * maxZ - maxY + this.cZy <= 0.0f && this.sYz * minY - minZ + this.cYz >= 0.0f;
    }

    private boolean PMP(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX <= maxX && this.originY >= minY && this.originZ <= maxZ && this.sXy * maxX - maxY + this.cXy <= 0.0f && this.sYx * minY - minX + this.cYx >= 0.0f && this.sZy * maxZ - maxY + this.cZy <= 0.0f && this.sYz * minY - minZ + this.cYz >= 0.0f && this.sXz * maxX - minZ + this.cXz >= 0.0f && this.sZx * maxZ - minX + this.cZx >= 0.0f;
    }

    private boolean MOP(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originY >= minY && this.originY <= maxY && this.originX >= minX && this.originZ <= maxZ && this.sXz * minX - minZ + this.cXz >= 0.0f && this.sZx * maxZ - maxX + this.cZx <= 0.0f;
    }

    private boolean OOP(float minX, float minY, float maxX, float maxY, float maxZ) {
        return this.originZ <= maxZ && this.originX >= minX && this.originX <= maxX && this.originY >= minY && this.originY <= maxY;
    }

    private boolean POP(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originY >= minY && this.originY <= maxY && this.originX <= maxX && this.originZ <= maxZ && this.sXz * maxX - minZ + this.cXz >= 0.0f && this.sZx * maxZ - minX + this.cZx <= 0.0f;
    }

    private boolean MPP(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX >= minX && this.originY <= maxY && this.originZ <= maxZ && this.sXy * minX - minY + this.cXy >= 0.0f && this.sYx * maxY - maxX + this.cYx <= 0.0f && this.sZy * maxZ - minY + this.cZy >= 0.0f && this.sYz * maxY - minZ + this.cYz >= 0.0f && this.sXz * minX - minZ + this.cXz >= 0.0f && this.sZx * maxZ - maxX + this.cZx <= 0.0f;
    }

    private boolean OPP(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX >= minX && this.originX <= maxX && this.originY <= maxY && this.originZ <= maxZ && this.sZy * maxZ - minY + this.cZy <= 0.0f && this.sYz * maxY - minZ + this.cYz <= 0.0f;
    }

    private boolean PPP(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return this.originX <= maxX && this.originY <= maxY && this.originZ <= maxZ && this.sXy * maxX - minY + this.cXy >= 0.0f && this.sYx * maxY - minX + this.cYx >= 0.0f && this.sZy * maxZ - minY + this.cZy >= 0.0f && this.sYz * maxY - minZ + this.cYz >= 0.0f && this.sXz * maxX - minZ + this.cXz >= 0.0f && this.sZx * maxZ - minX + this.cZx >= 0.0f;
    }
}

