/*
 * Decompiled with CFR 0.152.
 */
package org.joml;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import org.joml.AxisAngle4d;
import org.joml.AxisAngle4f;
import org.joml.Matrix3d;
import org.joml.Matrix3fc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface Matrix3dc {
    public double m00();

    public double m01();

    public double m02();

    public double m10();

    public double m11();

    public double m12();

    public double m20();

    public double m21();

    public double m22();

    public Matrix3d mul(Matrix3dc var1, Matrix3d var2);

    public Matrix3d mulLocal(Matrix3dc var1, Matrix3d var2);

    public Matrix3d mul(Matrix3fc var1, Matrix3d var2);

    public double determinant();

    public Matrix3d invert(Matrix3d var1);

    public Matrix3d transpose(Matrix3d var1);

    public Matrix3d get(Matrix3d var1);

    public AxisAngle4f getRotation(AxisAngle4f var1);

    public Quaternionf getUnnormalizedRotation(Quaternionf var1);

    public Quaternionf getNormalizedRotation(Quaternionf var1);

    public Quaterniond getUnnormalizedRotation(Quaterniond var1);

    public Quaterniond getNormalizedRotation(Quaterniond var1);

    public DoubleBuffer get(DoubleBuffer var1);

    public DoubleBuffer get(int var1, DoubleBuffer var2);

    public FloatBuffer get(FloatBuffer var1);

    public FloatBuffer get(int var1, FloatBuffer var2);

    public ByteBuffer get(ByteBuffer var1);

    public ByteBuffer get(int var1, ByteBuffer var2);

    public ByteBuffer getFloats(ByteBuffer var1);

    public ByteBuffer getFloats(int var1, ByteBuffer var2);

    public Matrix3dc getToAddress(long var1);

    public double[] get(double[] var1, int var2);

    public double[] get(double[] var1);

    public float[] get(float[] var1, int var2);

    public float[] get(float[] var1);

    public Matrix3d scale(Vector3dc var1, Matrix3d var2);

    public Matrix3d scale(double var1, double var3, double var5, Matrix3d var7);

    public Matrix3d scale(double var1, Matrix3d var3);

    public Matrix3d scaleLocal(double var1, double var3, double var5, Matrix3d var7);

    public Vector3d transform(Vector3d var1);

    public Vector3d transform(Vector3dc var1, Vector3d var2);

    public Vector3f transform(Vector3f var1);

    public Vector3f transform(Vector3fc var1, Vector3f var2);

    public Vector3d transform(double var1, double var3, double var5, Vector3d var7);

    public Vector3d transformTranspose(Vector3d var1);

    public Vector3d transformTranspose(Vector3dc var1, Vector3d var2);

    public Vector3d transformTranspose(double var1, double var3, double var5, Vector3d var7);

    public Matrix3d rotateX(double var1, Matrix3d var3);

    public Matrix3d rotateY(double var1, Matrix3d var3);

    public Matrix3d rotateZ(double var1, Matrix3d var3);

    public Matrix3d rotateXYZ(double var1, double var3, double var5, Matrix3d var7);

    public Matrix3d rotateZYX(double var1, double var3, double var5, Matrix3d var7);

    public Matrix3d rotateYXZ(double var1, double var3, double var5, Matrix3d var7);

    public Matrix3d rotate(double var1, double var3, double var5, double var7, Matrix3d var9);

    public Matrix3d rotateLocal(double var1, double var3, double var5, double var7, Matrix3d var9);

    public Matrix3d rotateLocalX(double var1, Matrix3d var3);

    public Matrix3d rotateLocalY(double var1, Matrix3d var3);

    public Matrix3d rotateLocalZ(double var1, Matrix3d var3);

    public Matrix3d rotateLocal(Quaterniondc var1, Matrix3d var2);

    public Matrix3d rotateLocal(Quaternionfc var1, Matrix3d var2);

    public Matrix3d rotate(Quaterniondc var1, Matrix3d var2);

    public Matrix3d rotate(Quaternionfc var1, Matrix3d var2);

    public Matrix3d rotate(AxisAngle4f var1, Matrix3d var2);

    public Matrix3d rotate(AxisAngle4d var1, Matrix3d var2);

    public Matrix3d rotate(double var1, Vector3dc var3, Matrix3d var4);

    public Matrix3d rotate(double var1, Vector3fc var3, Matrix3d var4);

    public Vector3d getRow(int var1, Vector3d var2) throws IndexOutOfBoundsException;

    public Vector3d getColumn(int var1, Vector3d var2) throws IndexOutOfBoundsException;

    public double get(int var1, int var2);

    public double getRowColumn(int var1, int var2);

    public Matrix3d normal(Matrix3d var1);

    public Matrix3d cofactor(Matrix3d var1);

    public Matrix3d lookAlong(Vector3dc var1, Vector3dc var2, Matrix3d var3);

    public Matrix3d lookAlong(double var1, double var3, double var5, double var7, double var9, double var11, Matrix3d var13);

    public Vector3d getScale(Vector3d var1);

    public Vector3d positiveZ(Vector3d var1);

    public Vector3d normalizedPositiveZ(Vector3d var1);

    public Vector3d positiveX(Vector3d var1);

    public Vector3d normalizedPositiveX(Vector3d var1);

    public Vector3d positiveY(Vector3d var1);

    public Vector3d normalizedPositiveY(Vector3d var1);

    public Matrix3d add(Matrix3dc var1, Matrix3d var2);

    public Matrix3d sub(Matrix3dc var1, Matrix3d var2);

    public Matrix3d mulComponentWise(Matrix3dc var1, Matrix3d var2);

    public Matrix3d lerp(Matrix3dc var1, double var2, Matrix3d var4);

    public Matrix3d rotateTowards(Vector3dc var1, Vector3dc var2, Matrix3d var3);

    public Matrix3d rotateTowards(double var1, double var3, double var5, double var7, double var9, double var11, Matrix3d var13);

    public Vector3d getEulerAnglesZYX(Vector3d var1);

    public Matrix3d obliqueZ(double var1, double var3, Matrix3d var5);

    public boolean equals(Matrix3dc var1, double var2);

    public Matrix3d reflect(double var1, double var3, double var5, Matrix3d var7);

    public Matrix3d reflect(Quaterniondc var1, Matrix3d var2);

    public Matrix3d reflect(Vector3dc var1, Matrix3d var2);

    public boolean isFinite();

    public double quadraticFormProduct(double var1, double var3, double var5);

    public double quadraticFormProduct(Vector3dc var1);

    public double quadraticFormProduct(Vector3fc var1);
}

