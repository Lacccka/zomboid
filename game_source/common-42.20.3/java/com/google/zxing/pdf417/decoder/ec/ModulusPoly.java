/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.pdf417.decoder.ec;

import com.google.zxing.pdf417.decoder.ec.ModulusGF;

final class ModulusPoly {
    private final ModulusGF field;
    private final int[] coefficients;

    ModulusPoly(ModulusGF field, int[] coefficients) {
        if (coefficients.length == 0) {
            throw new IllegalArgumentException();
        }
        this.field = field;
        int coefficientsLength = coefficients.length;
        if (coefficientsLength > 1 && coefficients[0] == 0) {
            int firstNonZero;
            for (firstNonZero = 1; firstNonZero < coefficientsLength && coefficients[firstNonZero] == 0; ++firstNonZero) {
            }
            if (firstNonZero == coefficientsLength) {
                this.coefficients = new int[]{0};
            } else {
                this.coefficients = new int[coefficientsLength - firstNonZero];
                System.arraycopy(coefficients, firstNonZero, this.coefficients, 0, this.coefficients.length);
            }
        } else {
            this.coefficients = coefficients;
        }
    }

    int[] getCoefficients() {
        return this.coefficients;
    }

    int getDegree() {
        return this.coefficients.length - 1;
    }

    boolean isZero() {
        return this.coefficients[0] == 0;
    }

    int getCoefficient(int degree) {
        return this.coefficients[this.coefficients.length - 1 - degree];
    }

    int evaluateAt(int a) {
        if (a == 0) {
            return this.getCoefficient(0);
        }
        int size = this.coefficients.length;
        if (a == 1) {
            int result = 0;
            for (int coefficient : this.coefficients) {
                result = this.field.add(result, coefficient);
            }
            return result;
        }
        int result = this.coefficients[0];
        for (int i = 1; i < size; ++i) {
            result = this.field.add(this.field.multiply(a, result), this.coefficients[i]);
        }
        return result;
    }

    ModulusPoly add(ModulusPoly other) {
        if (!this.field.equals(other.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        }
        if (this.isZero()) {
            return other;
        }
        if (other.isZero()) {
            return this;
        }
        int[] smallerCoefficients = this.coefficients;
        int[] largerCoefficients = other.coefficients;
        if (smallerCoefficients.length > largerCoefficients.length) {
            int[] temp = smallerCoefficients;
            smallerCoefficients = largerCoefficients;
            largerCoefficients = temp;
        }
        int[] sumDiff = new int[largerCoefficients.length];
        int lengthDiff = largerCoefficients.length - smallerCoefficients.length;
        System.arraycopy(largerCoefficients, 0, sumDiff, 0, lengthDiff);
        for (int i = lengthDiff; i < largerCoefficients.length; ++i) {
            sumDiff[i] = this.field.add(smallerCoefficients[i - lengthDiff], largerCoefficients[i]);
        }
        return new ModulusPoly(this.field, sumDiff);
    }

    ModulusPoly subtract(ModulusPoly other) {
        if (!this.field.equals(other.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        }
        if (other.isZero()) {
            return this;
        }
        return this.add(other.negative());
    }

    ModulusPoly multiply(ModulusPoly other) {
        if (!this.field.equals(other.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        }
        if (this.isZero() || other.isZero()) {
            return this.field.getZero();
        }
        int[] aCoefficients = this.coefficients;
        int aLength = aCoefficients.length;
        int[] bCoefficients = other.coefficients;
        int bLength = bCoefficients.length;
        int[] product = new int[aLength + bLength - 1];
        for (int i = 0; i < aLength; ++i) {
            int aCoeff = aCoefficients[i];
            for (int j = 0; j < bLength; ++j) {
                product[i + j] = this.field.add(product[i + j], this.field.multiply(aCoeff, bCoefficients[j]));
            }
        }
        return new ModulusPoly(this.field, product);
    }

    ModulusPoly negative() {
        int size = this.coefficients.length;
        int[] negativeCoefficients = new int[size];
        for (int i = 0; i < size; ++i) {
            negativeCoefficients[i] = this.field.subtract(0, this.coefficients[i]);
        }
        return new ModulusPoly(this.field, negativeCoefficients);
    }

    ModulusPoly multiply(int scalar) {
        if (scalar == 0) {
            return this.field.getZero();
        }
        if (scalar == 1) {
            return this;
        }
        int size = this.coefficients.length;
        int[] product = new int[size];
        for (int i = 0; i < size; ++i) {
            product[i] = this.field.multiply(this.coefficients[i], scalar);
        }
        return new ModulusPoly(this.field, product);
    }

    ModulusPoly multiplyByMonomial(int degree, int coefficient) {
        if (degree < 0) {
            throw new IllegalArgumentException();
        }
        if (coefficient == 0) {
            return this.field.getZero();
        }
        int size = this.coefficients.length;
        int[] product = new int[size + degree];
        for (int i = 0; i < size; ++i) {
            product[i] = this.field.multiply(this.coefficients[i], coefficient);
        }
        return new ModulusPoly(this.field, product);
    }

    ModulusPoly[] divide(ModulusPoly other) {
        if (!this.field.equals(other.field)) {
            throw new IllegalArgumentException("ModulusPolys do not have same ModulusGF field");
        }
        if (other.isZero()) {
            throw new IllegalArgumentException("Divide by 0");
        }
        ModulusPoly quotient = this.field.getZero();
        ModulusPoly remainder = this;
        int denominatorLeadingTerm = other.getCoefficient(other.getDegree());
        int inverseDenominatorLeadingTerm = this.field.inverse(denominatorLeadingTerm);
        while (remainder.getDegree() >= other.getDegree() && !remainder.isZero()) {
            int degreeDifference = remainder.getDegree() - other.getDegree();
            int scale = this.field.multiply(remainder.getCoefficient(remainder.getDegree()), inverseDenominatorLeadingTerm);
            ModulusPoly term = other.multiplyByMonomial(degreeDifference, scale);
            ModulusPoly iterationQuotient = this.field.buildMonomial(degreeDifference, scale);
            quotient = quotient.add(iterationQuotient);
            remainder = remainder.subtract(term);
        }
        return new ModulusPoly[]{quotient, remainder};
    }

    public String toString() {
        StringBuilder result = new StringBuilder(8 * this.getDegree());
        for (int degree = this.getDegree(); degree >= 0; --degree) {
            int coefficient = this.getCoefficient(degree);
            if (coefficient == 0) continue;
            if (coefficient < 0) {
                result.append(" - ");
                coefficient = -coefficient;
            } else if (result.length() > 0) {
                result.append(" + ");
            }
            if (degree == 0 || coefficient != 1) {
                result.append(coefficient);
            }
            if (degree == 0) continue;
            if (degree == 1) {
                result.append('x');
                continue;
            }
            result.append("x^");
            result.append(degree);
        }
        return result.toString();
    }
}

