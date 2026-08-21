/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.signers;

import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.DSAKCalculator;
import org.bouncycastle.crypto.signers.RandomDSAKCalculator;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECMultiplier;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

public class SM2Signer
implements Signer,
ECConstants {
    private final DSAKCalculator kCalculator = new RandomDSAKCalculator();
    private final SM3Digest digest = new SM3Digest();
    private ECDomainParameters ecParams;
    private ECPoint pubPoint;
    private ECKeyParameters ecKey;
    private byte[] z;

    public void init(boolean bl, CipherParameters cipherParameters) {
        byte[] byArray;
        CipherParameters cipherParameters2;
        if (cipherParameters instanceof ParametersWithID) {
            cipherParameters2 = ((ParametersWithID)cipherParameters).getParameters();
            byArray = ((ParametersWithID)cipherParameters).getID();
        } else {
            cipherParameters2 = cipherParameters;
            byArray = Hex.decode("31323334353637383132333435363738");
        }
        if (bl) {
            if (cipherParameters2 instanceof ParametersWithRandom) {
                ParametersWithRandom parametersWithRandom = (ParametersWithRandom)cipherParameters2;
                this.ecKey = (ECKeyParameters)parametersWithRandom.getParameters();
                this.ecParams = this.ecKey.getParameters();
                this.kCalculator.init(this.ecParams.getN(), parametersWithRandom.getRandom());
            } else {
                this.ecKey = (ECKeyParameters)cipherParameters2;
                this.ecParams = this.ecKey.getParameters();
                this.kCalculator.init(this.ecParams.getN(), CryptoServicesRegistrar.getSecureRandom());
            }
            this.pubPoint = this.createBasePointMultiplier().multiply(this.ecParams.getG(), ((ECPrivateKeyParameters)this.ecKey).getD()).normalize();
        } else {
            this.ecKey = (ECKeyParameters)cipherParameters2;
            this.ecParams = this.ecKey.getParameters();
            this.pubPoint = ((ECPublicKeyParameters)this.ecKey).getQ();
        }
        this.z = this.getZ(byArray);
        this.digest.update(this.z, 0, this.z.length);
    }

    public void update(byte by) {
        this.digest.update(by);
    }

    public void update(byte[] byArray, int n, int n2) {
        this.digest.update(byArray, n, n2);
    }

    public boolean verifySignature(byte[] byArray) {
        try {
            BigInteger[] bigIntegerArray = this.derDecode(byArray);
            if (bigIntegerArray != null) {
                return this.verifySignature(bigIntegerArray[0], bigIntegerArray[1]);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return false;
    }

    public void reset() {
        this.digest.reset();
        if (this.z != null) {
            this.digest.update(this.z, 0, this.z.length);
        }
    }

    public byte[] generateSignature() throws CryptoException {
        BigInteger bigInteger;
        BigInteger bigInteger2;
        byte[] byArray = this.digestDoFinal();
        BigInteger bigInteger3 = this.ecParams.getN();
        BigInteger bigInteger4 = this.calculateE(byArray);
        BigInteger bigInteger5 = ((ECPrivateKeyParameters)this.ecKey).getD();
        ECMultiplier eCMultiplier = this.createBasePointMultiplier();
        while (true) {
            BigInteger bigInteger6 = this.kCalculator.nextK();
            Object object = eCMultiplier.multiply(this.ecParams.getG(), bigInteger6).normalize();
            bigInteger2 = bigInteger4.add(((ECPoint)object).getAffineXCoord().toBigInteger()).mod(bigInteger3);
            if (bigInteger2.equals(ZERO) || bigInteger2.add(bigInteger6).equals(bigInteger3)) continue;
            object = bigInteger5.add(ONE).modInverse(bigInteger3);
            bigInteger = bigInteger6.subtract(bigInteger2.multiply(bigInteger5)).mod(bigInteger3);
            if (!(bigInteger = ((BigInteger)object).multiply(bigInteger).mod(bigInteger3)).equals(ZERO)) break;
        }
        try {
            return this.derEncode(bigInteger2, bigInteger);
        }
        catch (IOException iOException) {
            throw new CryptoException("unable to encode signature: " + iOException.getMessage(), iOException);
        }
    }

    private boolean verifySignature(BigInteger bigInteger, BigInteger bigInteger2) {
        BigInteger bigInteger3 = this.ecParams.getN();
        if (bigInteger.compareTo(ONE) < 0 || bigInteger.compareTo(bigInteger3) >= 0) {
            return false;
        }
        if (bigInteger2.compareTo(ONE) < 0 || bigInteger2.compareTo(bigInteger3) >= 0) {
            return false;
        }
        byte[] byArray = this.digestDoFinal();
        BigInteger bigInteger4 = this.calculateE(byArray);
        BigInteger bigInteger5 = bigInteger.add(bigInteger2).mod(bigInteger3);
        if (bigInteger5.equals(ZERO)) {
            return false;
        }
        ECPoint eCPoint = ((ECPublicKeyParameters)this.ecKey).getQ();
        ECPoint eCPoint2 = ECAlgorithms.sumOfTwoMultiplies(this.ecParams.getG(), bigInteger2, eCPoint, bigInteger5).normalize();
        if (eCPoint2.isInfinity()) {
            return false;
        }
        BigInteger bigInteger6 = bigInteger4.add(eCPoint2.getAffineXCoord().toBigInteger()).mod(bigInteger3);
        return bigInteger6.equals(bigInteger);
    }

    private byte[] digestDoFinal() {
        byte[] byArray = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(byArray, 0);
        this.reset();
        return byArray;
    }

    private byte[] getZ(byte[] byArray) {
        this.digest.reset();
        this.addUserID(this.digest, byArray);
        this.addFieldElement(this.digest, this.ecParams.getCurve().getA());
        this.addFieldElement(this.digest, this.ecParams.getCurve().getB());
        this.addFieldElement(this.digest, this.ecParams.getG().getAffineXCoord());
        this.addFieldElement(this.digest, this.ecParams.getG().getAffineYCoord());
        this.addFieldElement(this.digest, this.pubPoint.getAffineXCoord());
        this.addFieldElement(this.digest, this.pubPoint.getAffineYCoord());
        byte[] byArray2 = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(byArray2, 0);
        return byArray2;
    }

    private void addUserID(Digest digest, byte[] byArray) {
        int n = byArray.length * 8;
        digest.update((byte)(n >> 8 & 0xFF));
        digest.update((byte)(n & 0xFF));
        digest.update(byArray, 0, byArray.length);
    }

    private void addFieldElement(Digest digest, ECFieldElement eCFieldElement) {
        byte[] byArray = eCFieldElement.getEncoded();
        digest.update(byArray, 0, byArray.length);
    }

    protected ECMultiplier createBasePointMultiplier() {
        return new FixedPointCombMultiplier();
    }

    protected BigInteger calculateE(byte[] byArray) {
        return new BigInteger(1, byArray);
    }

    protected BigInteger[] derDecode(byte[] byArray) throws IOException {
        BigInteger bigInteger;
        ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(ASN1Primitive.fromByteArray(byArray));
        if (aSN1Sequence.size() != 2) {
            return null;
        }
        BigInteger bigInteger2 = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0)).getValue();
        byte[] byArray2 = this.derEncode(bigInteger2, bigInteger = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(1)).getValue());
        if (!Arrays.constantTimeAreEqual(byArray2, byArray)) {
            return null;
        }
        return new BigInteger[]{bigInteger2, bigInteger};
    }

    protected byte[] derEncode(BigInteger bigInteger, BigInteger bigInteger2) throws IOException {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(new ASN1Integer(bigInteger));
        aSN1EncodableVector.add(new ASN1Integer(bigInteger2));
        return new DERSequence(aSN1EncodableVector).getEncoded("DER");
    }
}

