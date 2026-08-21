/*
 * Decompiled with CFR 0.152.
 */
package org.joml;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.NumberFormat;
import org.joml.Math;
import org.joml.Matrix2dc;
import org.joml.Matrix2fc;
import org.joml.Matrix3x2fc;
import org.joml.MemUtil;
import org.joml.Options;
import org.joml.Runtime;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import zombie.UsedFromLua;

@UsedFromLua
public class Vector2f
implements Externalizable,
Vector2fc {
    private static final long serialVersionUID = 1L;
    public float x;
    public float y;

    public Vector2f() {
    }

    public Vector2f(float d) {
        this.x = d;
        this.y = d;
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Vector2fc v) {
        this.x = v.x();
        this.y = v.y();
    }

    public Vector2f(Vector2ic v) {
        this.x = v.x();
        this.y = v.y();
    }

    public Vector2f(float[] xy) {
        this.x = xy[0];
        this.y = xy[1];
    }

    public Vector2f(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    public Vector2f(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }

    public Vector2f(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
    }

    public Vector2f(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
    }

    @Override
    public float x() {
        return this.x;
    }

    @Override
    public float y() {
        return this.y;
    }

    public Vector2f set(float d) {
        this.x = d;
        this.y = d;
        return this;
    }

    public Vector2f set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2f set(double d) {
        this.x = (float)d;
        this.y = (float)d;
        return this;
    }

    public Vector2f set(double x, double y) {
        this.x = (float)x;
        this.y = (float)y;
        return this;
    }

    public Vector2f set(Vector2fc v) {
        this.x = v.x();
        this.y = v.y();
        return this;
    }

    public Vector2f set(Vector2ic v) {
        this.x = v.x();
        this.y = v.y();
        return this;
    }

    public Vector2f set(Vector2dc v) {
        this.x = (float)v.x();
        this.y = (float)v.y();
        return this;
    }

    public Vector2f set(float[] xy) {
        this.x = xy[0];
        this.y = xy[1];
        return this;
    }

    public Vector2f set(ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    public Vector2f set(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }

    public Vector2f set(FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, buffer.position(), buffer);
        return this;
    }

    public Vector2f set(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.get(this, index, buffer);
        return this;
    }

    public Vector2f setFromAddress(long address) {
        if (Options.NO_UNSAFE) {
            throw new UnsupportedOperationException("Not supported when using joml.nounsafe");
        }
        MemUtil.MemUtilUnsafe.get(this, address);
        return this;
    }

    @Override
    public float get(int component) throws IllegalArgumentException {
        switch (component) {
            case 0: {
                return this.x;
            }
            case 1: {
                return this.y;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Vector2i get(int mode, Vector2i dest) {
        dest.x = Math.roundUsing(this.x(), mode);
        dest.y = Math.roundUsing(this.y(), mode);
        return dest;
    }

    @Override
    public Vector2f get(Vector2f dest) {
        dest.x = this.x();
        dest.y = this.y();
        return dest;
    }

    @Override
    public Vector2d get(Vector2d dest) {
        dest.x = this.x();
        dest.y = this.y();
        return dest;
    }

    public Vector2f setComponent(int component, float value) throws IllegalArgumentException {
        switch (component) {
            case 0: {
                this.x = value;
                break;
            }
            case 1: {
                this.y = value;
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
        return this;
    }

    @Override
    public ByteBuffer get(ByteBuffer buffer) {
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    @Override
    public ByteBuffer get(int index, ByteBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    @Override
    public FloatBuffer get(FloatBuffer buffer) {
        MemUtil.INSTANCE.put(this, buffer.position(), buffer);
        return buffer;
    }

    @Override
    public FloatBuffer get(int index, FloatBuffer buffer) {
        MemUtil.INSTANCE.put(this, index, buffer);
        return buffer;
    }

    @Override
    public Vector2fc getToAddress(long address) {
        if (Options.NO_UNSAFE) {
            throw new UnsupportedOperationException("Not supported when using joml.nounsafe");
        }
        MemUtil.MemUtilUnsafe.put(this, address);
        return this;
    }

    public Vector2f perpendicular() {
        float xTemp = this.y;
        this.y = this.x * -1.0f;
        this.x = xTemp;
        return this;
    }

    public Vector2f sub(Vector2fc v) {
        this.x -= v.x();
        this.y -= v.y();
        return this;
    }

    @Override
    public Vector2f sub(Vector2fc v, Vector2f dest) {
        dest.x = this.x - v.x();
        dest.y = this.y - v.y();
        return dest;
    }

    public Vector2f sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    @Override
    public Vector2f sub(float x, float y, Vector2f dest) {
        dest.x = this.x - x;
        dest.y = this.y - y;
        return dest;
    }

    @Override
    public float dot(Vector2fc v) {
        return this.x * v.x() + this.y * v.y();
    }

    @Override
    public float angle(Vector2fc v) {
        float dot = this.x * v.x() + this.y * v.y();
        float det = this.x * v.y() - this.y * v.x();
        return Math.atan2(det, dot);
    }

    @Override
    public float lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public static float lengthSquared(float x, float y) {
        return x * x + y * y;
    }

    @Override
    public float length() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public static float length(float x, float y) {
        return Math.sqrt(x * x + y * y);
    }

    @Override
    public float distance(Vector2fc v) {
        float dx = this.x - v.x();
        float dy = this.y - v.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public float distanceSquared(Vector2fc v) {
        float dx = this.x - v.x();
        float dy = this.y - v.y();
        return dx * dx + dy * dy;
    }

    @Override
    public float distance(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public float distanceSquared(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return dx * dx + dy * dy;
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static float distanceSquared(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    public Vector2f normalize() {
        float invLength = Math.invsqrt(this.x * this.x + this.y * this.y);
        this.x *= invLength;
        this.y *= invLength;
        return this;
    }

    @Override
    public Vector2f normalize(Vector2f dest) {
        float invLength = Math.invsqrt(this.x * this.x + this.y * this.y);
        dest.x = this.x * invLength;
        dest.y = this.y * invLength;
        return dest;
    }

    public Vector2f normalize(float length) {
        float invLength = Math.invsqrt(this.x * this.x + this.y * this.y) * length;
        this.x *= invLength;
        this.y *= invLength;
        return this;
    }

    @Override
    public Vector2f normalize(float length, Vector2f dest) {
        float invLength = Math.invsqrt(this.x * this.x + this.y * this.y) * length;
        dest.x = this.x * invLength;
        dest.y = this.y * invLength;
        return dest;
    }

    public Vector2f add(Vector2fc v) {
        this.x += v.x();
        this.y += v.y();
        return this;
    }

    @Override
    public Vector2f add(Vector2fc v, Vector2f dest) {
        dest.x = this.x + v.x();
        dest.y = this.y + v.y();
        return dest;
    }

    public Vector2f add(float x, float y) {
        return this.add(x, y, this);
    }

    @Override
    public Vector2f add(float x, float y, Vector2f dest) {
        dest.x = this.x + x;
        dest.y = this.y + y;
        return dest;
    }

    public Vector2f zero() {
        this.x = 0.0f;
        this.y = 0.0f;
        return this;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(this.x);
        out.writeFloat(this.y);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.x = in.readFloat();
        this.y = in.readFloat();
    }

    public Vector2f negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    @Override
    public Vector2f negate(Vector2f dest) {
        dest.x = -this.x;
        dest.y = -this.y;
        return dest;
    }

    public Vector2f mul(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    @Override
    public Vector2f mul(float scalar, Vector2f dest) {
        dest.x = this.x * scalar;
        dest.y = this.y * scalar;
        return dest;
    }

    public Vector2f mul(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    @Override
    public Vector2f mul(float x, float y, Vector2f dest) {
        dest.x = this.x * x;
        dest.y = this.y * y;
        return dest;
    }

    public Vector2f mul(Vector2fc v) {
        this.x *= v.x();
        this.y *= v.y();
        return this;
    }

    @Override
    public Vector2f mul(Vector2fc v, Vector2f dest) {
        dest.x = this.x * v.x();
        dest.y = this.y * v.y();
        return dest;
    }

    public Vector2f div(Vector2fc v) {
        this.x /= v.x();
        this.y /= v.y();
        return this;
    }

    @Override
    public Vector2f div(Vector2fc v, Vector2f dest) {
        dest.x = this.x / v.x();
        dest.y = this.y / v.y();
        return dest;
    }

    public Vector2f div(float scalar) {
        float inv = 1.0f / scalar;
        this.x *= inv;
        this.y *= inv;
        return this;
    }

    @Override
    public Vector2f div(float scalar, Vector2f dest) {
        float inv = 1.0f / scalar;
        dest.x = this.x * inv;
        dest.y = this.y * inv;
        return dest;
    }

    public Vector2f div(float x, float y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    @Override
    public Vector2f div(float x, float y, Vector2f dest) {
        dest.x = this.x / x;
        dest.y = this.y / y;
        return dest;
    }

    public Vector2f mul(Matrix2fc mat) {
        float rx = mat.m00() * this.x + mat.m10() * this.y;
        float ry = mat.m01() * this.x + mat.m11() * this.y;
        this.x = rx;
        this.y = ry;
        return this;
    }

    @Override
    public Vector2f mul(Matrix2fc mat, Vector2f dest) {
        float rx = mat.m00() * this.x + mat.m10() * this.y;
        float ry = mat.m01() * this.x + mat.m11() * this.y;
        dest.x = rx;
        dest.y = ry;
        return dest;
    }

    public Vector2f mul(Matrix2dc mat) {
        double rx = mat.m00() * (double)this.x + mat.m10() * (double)this.y;
        double ry = mat.m01() * (double)this.x + mat.m11() * (double)this.y;
        this.x = (float)rx;
        this.y = (float)ry;
        return this;
    }

    @Override
    public Vector2f mul(Matrix2dc mat, Vector2f dest) {
        double rx = mat.m00() * (double)this.x + mat.m10() * (double)this.y;
        double ry = mat.m01() * (double)this.x + mat.m11() * (double)this.y;
        dest.x = (float)rx;
        dest.y = (float)ry;
        return dest;
    }

    public Vector2f mulTranspose(Matrix2fc mat) {
        float rx = mat.m00() * this.x + mat.m01() * this.y;
        float ry = mat.m10() * this.x + mat.m11() * this.y;
        this.x = rx;
        this.y = ry;
        return this;
    }

    @Override
    public Vector2f mulTranspose(Matrix2fc mat, Vector2f dest) {
        float rx = mat.m00() * this.x + mat.m01() * this.y;
        float ry = mat.m10() * this.x + mat.m11() * this.y;
        dest.x = rx;
        dest.y = ry;
        return dest;
    }

    public Vector2f mulPosition(Matrix3x2fc mat) {
        this.x = mat.m00() * this.x + mat.m10() * this.y + mat.m20();
        this.y = mat.m01() * this.x + mat.m11() * this.y + mat.m21();
        return this;
    }

    @Override
    public Vector2f mulPosition(Matrix3x2fc mat, Vector2f dest) {
        dest.x = mat.m00() * this.x + mat.m10() * this.y + mat.m20();
        dest.y = mat.m01() * this.x + mat.m11() * this.y + mat.m21();
        return dest;
    }

    public Vector2f mulDirection(Matrix3x2fc mat) {
        this.x = mat.m00() * this.x + mat.m10() * this.y;
        this.y = mat.m01() * this.x + mat.m11() * this.y;
        return this;
    }

    @Override
    public Vector2f mulDirection(Matrix3x2fc mat, Vector2f dest) {
        dest.x = mat.m00() * this.x + mat.m10() * this.y;
        dest.y = mat.m01() * this.x + mat.m11() * this.y;
        return dest;
    }

    public Vector2f lerp(Vector2fc other, float t) {
        this.x += (other.x() - this.x) * t;
        this.y += (other.y() - this.y) * t;
        return this;
    }

    @Override
    public Vector2f lerp(Vector2fc other, float t, Vector2f dest) {
        dest.x = this.x + (other.x() - this.x) * t;
        dest.y = this.y + (other.y() - this.y) * t;
        return dest;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + Float.floatToIntBits(this.x);
        result = 31 * result + Float.floatToIntBits(this.y);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Vector2f other = (Vector2f)obj;
        if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) {
            return false;
        }
        return Float.floatToIntBits(this.y) == Float.floatToIntBits(other.y);
    }

    @Override
    public boolean equals(Vector2fc v, float delta) {
        if (this == v) {
            return true;
        }
        if (v == null) {
            return false;
        }
        if (!(v instanceof Vector2fc)) {
            return false;
        }
        if (!Runtime.equals(this.x, v.x(), delta)) {
            return false;
        }
        return Runtime.equals(this.y, v.y(), delta);
    }

    @Override
    public boolean equals(float x, float y) {
        if (Float.floatToIntBits(this.x) != Float.floatToIntBits(x)) {
            return false;
        }
        return Float.floatToIntBits(this.y) == Float.floatToIntBits(y);
    }

    public String toString() {
        return Runtime.formatNumbers(this.toString(Options.NUMBER_FORMAT));
    }

    public String toString(NumberFormat formatter) {
        return "(" + Runtime.format(this.x, formatter) + " " + Runtime.format(this.y, formatter) + ")";
    }

    public Vector2f fma(Vector2fc a, Vector2fc b) {
        this.x += a.x() * b.x();
        this.y += a.y() * b.y();
        return this;
    }

    public Vector2f fma(float a, Vector2fc b) {
        this.x += a * b.x();
        this.y += a * b.y();
        return this;
    }

    @Override
    public Vector2f fma(Vector2fc a, Vector2fc b, Vector2f dest) {
        dest.x = this.x + a.x() * b.x();
        dest.y = this.y + a.y() * b.y();
        return dest;
    }

    @Override
    public Vector2f fma(float a, Vector2fc b, Vector2f dest) {
        dest.x = this.x + a * b.x();
        dest.y = this.y + a * b.y();
        return dest;
    }

    public Vector2f min(Vector2fc v) {
        this.x = this.x < v.x() ? this.x : v.x();
        this.y = this.y < v.y() ? this.y : v.y();
        return this;
    }

    @Override
    public Vector2f min(Vector2fc v, Vector2f dest) {
        dest.x = this.x < v.x() ? this.x : v.x();
        dest.y = this.y < v.y() ? this.y : v.y();
        return dest;
    }

    public Vector2f max(Vector2fc v) {
        this.x = this.x > v.x() ? this.x : v.x();
        this.y = this.y > v.y() ? this.y : v.y();
        return this;
    }

    @Override
    public Vector2f max(Vector2fc v, Vector2f dest) {
        dest.x = this.x > v.x() ? this.x : v.x();
        dest.y = this.y > v.y() ? this.y : v.y();
        return dest;
    }

    @Override
    public int maxComponent() {
        float absY;
        float absX = Math.abs(this.x);
        if (absX >= (absY = Math.abs(this.y))) {
            return 0;
        }
        return 1;
    }

    @Override
    public int minComponent() {
        float absY;
        float absX = Math.abs(this.x);
        if (absX < (absY = Math.abs(this.y))) {
            return 0;
        }
        return 1;
    }

    public Vector2f floor() {
        this.x = Math.floor(this.x);
        this.y = Math.floor(this.y);
        return this;
    }

    @Override
    public Vector2f floor(Vector2f dest) {
        dest.x = Math.floor(this.x);
        dest.y = Math.floor(this.y);
        return dest;
    }

    public Vector2f ceil() {
        this.x = Math.ceil(this.x);
        this.y = Math.ceil(this.y);
        return this;
    }

    @Override
    public Vector2f ceil(Vector2f dest) {
        dest.x = Math.ceil(this.x);
        dest.y = Math.ceil(this.y);
        return dest;
    }

    public Vector2f round() {
        this.x = Math.ceil(this.x);
        this.y = Math.ceil(this.y);
        return this;
    }

    @Override
    public Vector2f round(Vector2f dest) {
        dest.x = Math.round(this.x);
        dest.y = Math.round(this.y);
        return dest;
    }

    @Override
    public boolean isFinite() {
        return Math.isFinite(this.x) && Math.isFinite(this.y);
    }

    public Vector2f absolute() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        return this;
    }

    @Override
    public Vector2f absolute(Vector2f dest) {
        dest.x = Math.abs(this.x);
        dest.y = Math.abs(this.y);
        return dest;
    }
}

