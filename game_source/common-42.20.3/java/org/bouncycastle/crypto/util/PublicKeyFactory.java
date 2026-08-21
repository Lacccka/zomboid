/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
import org.bouncycastle.asn1.oiw.ElGamalParameter;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.ua.DSTU4145BinaryField;
import org.bouncycastle.asn1.ua.DSTU4145ECBinary;
import org.bouncycastle.asn1.ua.DSTU4145NamedCurves;
import org.bouncycastle.asn1.ua.DSTU4145Params;
import org.bouncycastle.asn1.ua.DSTU4145PointEncoder;
import org.bouncycastle.asn1.ua.UAObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x9.DHPublicKey;
import org.bouncycastle.asn1.x9.DomainParameters;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.ValidationParams;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.crypto.params.DHValidationParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.math.ec.ECCurve;

public class PublicKeyFactory {
    private static Map converters = new HashMap();

    public static AsymmetricKeyParameter createKey(byte[] byArray) throws IOException {
        return PublicKeyFactory.createKey(SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(byArray)));
    }

    public static AsymmetricKeyParameter createKey(InputStream inputStream2) throws IOException {
        return PublicKeyFactory.createKey(SubjectPublicKeyInfo.getInstance(new ASN1InputStream(inputStream2).readObject()));
    }

    public static AsymmetricKeyParameter createKey(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
        return PublicKeyFactory.createKey(subjectPublicKeyInfo, null);
    }

    public static AsymmetricKeyParameter createKey(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
        AlgorithmIdentifier algorithmIdentifier = subjectPublicKeyInfo.getAlgorithm();
        SubjectPublicKeyInfoConverter subjectPublicKeyInfoConverter = (SubjectPublicKeyInfoConverter)converters.get(algorithmIdentifier.getAlgorithm());
        if (subjectPublicKeyInfoConverter != null) {
            return subjectPublicKeyInfoConverter.getPublicKeyParameters(subjectPublicKeyInfo, object);
        }
        throw new IOException("algorithm identifier in key not recognised: " + algorithmIdentifier.getAlgorithm());
    }

    static {
        converters.put(PKCSObjectIdentifiers.rsaEncryption, new RSAConverter());
        converters.put(X509ObjectIdentifiers.id_ea_rsa, new RSAConverter());
        converters.put(X9ObjectIdentifiers.dhpublicnumber, new DHPublicNumberConverter());
        converters.put(PKCSObjectIdentifiers.dhKeyAgreement, new DHAgreementConverter());
        converters.put(X9ObjectIdentifiers.id_dsa, new DSAConverter());
        converters.put(OIWObjectIdentifiers.dsaWithSHA1, new DSAConverter());
        converters.put(OIWObjectIdentifiers.elGamalAlgorithm, new ElGamalConverter());
        converters.put(X9ObjectIdentifiers.id_ecPublicKey, new ECConverter());
        converters.put(CryptoProObjectIdentifiers.gostR3410_2001, new GOST3410_2001Converter());
        converters.put(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256, new GOST3410_2012Converter());
        converters.put(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512, new GOST3410_2012Converter());
        converters.put(UAObjectIdentifiers.dstu4145be, new DSTUConverter());
        converters.put(UAObjectIdentifiers.dstu4145le, new DSTUConverter());
    }

    private static class DHAgreementConverter
    extends SubjectPublicKeyInfoConverter {
        private DHAgreementConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            DHParameter dHParameter = DHParameter.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            ASN1Integer aSN1Integer = (ASN1Integer)subjectPublicKeyInfo.parsePublicKey();
            BigInteger bigInteger = dHParameter.getL();
            int n = bigInteger == null ? 0 : bigInteger.intValue();
            DHParameters dHParameters = new DHParameters(dHParameter.getP(), dHParameter.getG(), null, n);
            return new DHPublicKeyParameters(aSN1Integer.getValue(), dHParameters);
        }
    }

    private static class DHPublicNumberConverter
    extends SubjectPublicKeyInfoConverter {
        private DHPublicNumberConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            DHPublicKey dHPublicKey = DHPublicKey.getInstance(subjectPublicKeyInfo.parsePublicKey());
            BigInteger bigInteger = dHPublicKey.getY();
            DomainParameters domainParameters = DomainParameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            BigInteger bigInteger2 = domainParameters.getP();
            BigInteger bigInteger3 = domainParameters.getG();
            BigInteger bigInteger4 = domainParameters.getQ();
            BigInteger bigInteger5 = null;
            if (domainParameters.getJ() != null) {
                bigInteger5 = domainParameters.getJ();
            }
            DHValidationParameters dHValidationParameters = null;
            ValidationParams validationParams = domainParameters.getValidationParams();
            if (validationParams != null) {
                byte[] byArray = validationParams.getSeed();
                BigInteger bigInteger6 = validationParams.getPgenCounter();
                dHValidationParameters = new DHValidationParameters(byArray, bigInteger6.intValue());
            }
            return new DHPublicKeyParameters(bigInteger, new DHParameters(bigInteger2, bigInteger3, bigInteger4, bigInteger5, dHValidationParameters));
        }
    }

    private static class DSAConverter
    extends SubjectPublicKeyInfoConverter {
        private DSAConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            ASN1Integer aSN1Integer = (ASN1Integer)subjectPublicKeyInfo.parsePublicKey();
            ASN1Encodable aSN1Encodable = subjectPublicKeyInfo.getAlgorithm().getParameters();
            DSAParameters dSAParameters = null;
            if (aSN1Encodable != null) {
                DSAParameter dSAParameter = DSAParameter.getInstance(aSN1Encodable.toASN1Primitive());
                dSAParameters = new DSAParameters(dSAParameter.getP(), dSAParameter.getQ(), dSAParameter.getG());
            }
            return new DSAPublicKeyParameters(aSN1Integer.getValue(), dSAParameters);
        }
    }

    private static class DSTUConverter
    extends SubjectPublicKeyInfoConverter {
        private DSTUConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            ECDomainParameters eCDomainParameters;
            DSTU4145Params dSTU4145Params;
            ASN1OctetString aSN1OctetString;
            DERBitString dERBitString = subjectPublicKeyInfo.getPublicKeyData();
            try {
                aSN1OctetString = (ASN1OctetString)ASN1Primitive.fromByteArray(dERBitString.getBytes());
            }
            catch (IOException iOException) {
                throw new IllegalArgumentException("error recovering public key");
            }
            byte[] byArray = aSN1OctetString.getOctets();
            if (subjectPublicKeyInfo.getAlgorithm().getAlgorithm().equals(UAObjectIdentifiers.dstu4145le)) {
                this.reverseBytes(byArray);
            }
            if ((dSTU4145Params = DSTU4145Params.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters())).isNamedCurve()) {
                ASN1ObjectIdentifier aSN1ObjectIdentifier = dSTU4145Params.getNamedCurve();
                eCDomainParameters = DSTU4145NamedCurves.getByOID(aSN1ObjectIdentifier);
            } else {
                DSTU4145ECBinary dSTU4145ECBinary = dSTU4145Params.getECBinary();
                byte[] byArray2 = dSTU4145ECBinary.getB();
                if (subjectPublicKeyInfo.getAlgorithm().getAlgorithm().equals(UAObjectIdentifiers.dstu4145le)) {
                    this.reverseBytes(byArray2);
                }
                DSTU4145BinaryField dSTU4145BinaryField = dSTU4145ECBinary.getField();
                ECCurve.F2m f2m = new ECCurve.F2m(dSTU4145BinaryField.getM(), dSTU4145BinaryField.getK1(), dSTU4145BinaryField.getK2(), dSTU4145BinaryField.getK3(), dSTU4145ECBinary.getA(), new BigInteger(1, byArray2));
                byte[] byArray3 = dSTU4145ECBinary.getG();
                if (subjectPublicKeyInfo.getAlgorithm().getAlgorithm().equals(UAObjectIdentifiers.dstu4145le)) {
                    this.reverseBytes(byArray3);
                }
                eCDomainParameters = new ECDomainParameters(f2m, DSTU4145PointEncoder.decodePoint(f2m, byArray3), dSTU4145ECBinary.getN());
            }
            return new ECPublicKeyParameters(DSTU4145PointEncoder.decodePoint(eCDomainParameters.getCurve(), byArray), eCDomainParameters);
        }

        private void reverseBytes(byte[] byArray) {
            for (int i = 0; i < byArray.length / 2; ++i) {
                byte by = byArray[i];
                byArray[i] = byArray[byArray.length - 1 - i];
                byArray[byArray.length - 1 - i] = by;
            }
        }
    }

    private static class ECConverter
    extends SubjectPublicKeyInfoConverter {
        private ECConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
            int n;
            ECDomainParameters eCDomainParameters;
            Object object2;
            ASN1Object aSN1Object;
            X962Parameters x962Parameters = X962Parameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            if (x962Parameters.isNamedCurve()) {
                aSN1Object = (ASN1ObjectIdentifier)x962Parameters.getParameters();
                object2 = CustomNamedCurves.getByOID((ASN1ObjectIdentifier)aSN1Object);
                if (object2 == null) {
                    object2 = ECNamedCurveTable.getByOID((ASN1ObjectIdentifier)aSN1Object);
                }
                eCDomainParameters = new ECNamedDomainParameters((ASN1ObjectIdentifier)aSN1Object, ((X9ECParameters)object2).getCurve(), ((X9ECParameters)object2).getG(), ((X9ECParameters)object2).getN(), ((X9ECParameters)object2).getH(), ((X9ECParameters)object2).getSeed());
            } else if (x962Parameters.isImplicitlyCA()) {
                eCDomainParameters = (ECDomainParameters)object;
            } else {
                aSN1Object = X9ECParameters.getInstance(x962Parameters.getParameters());
                eCDomainParameters = new ECDomainParameters(((X9ECParameters)aSN1Object).getCurve(), ((X9ECParameters)aSN1Object).getG(), ((X9ECParameters)aSN1Object).getN(), ((X9ECParameters)aSN1Object).getH(), ((X9ECParameters)aSN1Object).getSeed());
            }
            aSN1Object = subjectPublicKeyInfo.getPublicKeyData();
            object2 = ((ASN1BitString)aSN1Object).getBytes();
            ASN1OctetString aSN1OctetString = new DEROctetString((byte[])object2);
            if (object2[0] == 4 && object2[1] == ((Object)object2).length - 2 && (object2[2] == 2 || object2[2] == 3) && (n = new X9IntegerConverter().getByteLength(eCDomainParameters.getCurve())) >= ((Object)object2).length - 3) {
                try {
                    aSN1OctetString = (ASN1OctetString)ASN1Primitive.fromByteArray((byte[])object2);
                }
                catch (IOException iOException) {
                    throw new IllegalArgumentException("error recovering public key");
                }
            }
            X9ECPoint x9ECPoint = new X9ECPoint(eCDomainParameters.getCurve(), aSN1OctetString);
            return new ECPublicKeyParameters(x9ECPoint.getPoint(), eCDomainParameters);
        }
    }

    private static class ElGamalConverter
    extends SubjectPublicKeyInfoConverter {
        private ElGamalConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            ElGamalParameter elGamalParameter = ElGamalParameter.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            ASN1Integer aSN1Integer = (ASN1Integer)subjectPublicKeyInfo.parsePublicKey();
            return new ElGamalPublicKeyParameters(aSN1Integer.getValue(), new ElGamalParameters(elGamalParameter.getP(), elGamalParameter.getG()));
        }
    }

    private static class GOST3410_2001Converter
    extends SubjectPublicKeyInfoConverter {
        private GOST3410_2001Converter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
            Object object2;
            ASN1ObjectIdentifier aSN1ObjectIdentifier;
            ASN1OctetString aSN1OctetString;
            DERBitString dERBitString = subjectPublicKeyInfo.getPublicKeyData();
            try {
                aSN1OctetString = (ASN1OctetString)ASN1Primitive.fromByteArray(dERBitString.getBytes());
            }
            catch (IOException iOException) {
                throw new IllegalArgumentException("error recovering public key");
            }
            byte[] byArray = aSN1OctetString.getOctets();
            byte[] byArray2 = new byte[65];
            byArray2[0] = 4;
            for (int i = 1; i <= 32; ++i) {
                byArray2[i] = byArray[32 - i];
                byArray2[i + 32] = byArray[64 - i];
            }
            if (subjectPublicKeyInfo.getAlgorithm().getParameters() instanceof ASN1ObjectIdentifier) {
                aSN1ObjectIdentifier = ASN1ObjectIdentifier.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            } else {
                object2 = GOST3410PublicKeyAlgParameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
                aSN1ObjectIdentifier = ((GOST3410PublicKeyAlgParameters)object2).getPublicKeyParamSet();
            }
            object2 = ECGOST3410NamedCurves.getByOID(aSN1ObjectIdentifier);
            return new ECPublicKeyParameters(((ECDomainParameters)object2).getCurve().decodePoint(byArray2), (ECDomainParameters)object2);
        }
    }

    private static class GOST3410_2012Converter
    extends SubjectPublicKeyInfoConverter {
        private GOST3410_2012Converter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) {
            ASN1OctetString aSN1OctetString;
            ASN1ObjectIdentifier aSN1ObjectIdentifier = subjectPublicKeyInfo.getAlgorithm().getAlgorithm();
            DERBitString dERBitString = subjectPublicKeyInfo.getPublicKeyData();
            try {
                aSN1OctetString = (ASN1OctetString)ASN1Primitive.fromByteArray(dERBitString.getBytes());
            }
            catch (IOException iOException) {
                throw new IllegalArgumentException("error recovering public key");
            }
            byte[] byArray = aSN1OctetString.getOctets();
            int n = 32;
            if (aSN1ObjectIdentifier.equals(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512)) {
                n = 64;
            }
            int n2 = 2 * n;
            byte[] byArray2 = new byte[1 + n2];
            byArray2[0] = 4;
            for (int i = 1; i <= n; ++i) {
                byArray2[i] = byArray[n - i];
                byArray2[i + n] = byArray[n2 - i];
            }
            GOST3410PublicKeyAlgParameters gOST3410PublicKeyAlgParameters = GOST3410PublicKeyAlgParameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            ECDomainParameters eCDomainParameters = ECGOST3410NamedCurves.getByOID(gOST3410PublicKeyAlgParameters.getPublicKeyParamSet());
            return new ECPublicKeyParameters(eCDomainParameters.getCurve().decodePoint(byArray2), eCDomainParameters);
        }
    }

    private static class RSAConverter
    extends SubjectPublicKeyInfoConverter {
        private RSAConverter() {
        }

        AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object object) throws IOException {
            RSAPublicKey rSAPublicKey = RSAPublicKey.getInstance(subjectPublicKeyInfo.parsePublicKey());
            return new RSAKeyParameters(false, rSAPublicKey.getModulus(), rSAPublicKey.getPublicExponent());
        }
    }

    private static abstract class SubjectPublicKeyInfoConverter {
        private SubjectPublicKeyInfoConverter() {
        }

        abstract AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo var1, Object var2) throws IOException;
    }
}

