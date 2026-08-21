/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.jcajce.provider.keystore.bcfks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.bc.EncryptedObjectStoreData;
import org.bouncycastle.asn1.bc.EncryptedPrivateKeyData;
import org.bouncycastle.asn1.bc.EncryptedSecretKeyData;
import org.bouncycastle.asn1.bc.ObjectData;
import org.bouncycastle.asn1.bc.ObjectDataSequence;
import org.bouncycastle.asn1.bc.ObjectStore;
import org.bouncycastle.asn1.bc.ObjectStoreData;
import org.bouncycastle.asn1.bc.ObjectStoreIntegrityCheck;
import org.bouncycastle.asn1.bc.PbkdMacIntegrityCheck;
import org.bouncycastle.asn1.bc.SecretKeyData;
import org.bouncycastle.asn1.cms.CCMParameters;
import org.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.ScryptParams;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.nsri.NSRIObjectIdentifiers;
import org.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.EncryptionScheme;
import org.bouncycastle.asn1.pkcs.KeyDerivationFunc;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.PBKDF2Config;
import org.bouncycastle.crypto.util.PBKDFConfig;
import org.bouncycastle.crypto.util.ScryptConfig;
import org.bouncycastle.jcajce.BCFKSStoreParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class BcFKSKeyStoreSpi
extends KeyStoreSpi {
    private static final Map<String, ASN1ObjectIdentifier> oidMap = new HashMap<String, ASN1ObjectIdentifier>();
    private static final Map<ASN1ObjectIdentifier, String> publicAlgMap = new HashMap<ASN1ObjectIdentifier, String>();
    private static final BigInteger CERTIFICATE;
    private static final BigInteger PRIVATE_KEY;
    private static final BigInteger SECRET_KEY;
    private static final BigInteger PROTECTED_PRIVATE_KEY;
    private static final BigInteger PROTECTED_SECRET_KEY;
    private final BouncyCastleProvider provider;
    private final Map<String, ObjectData> entries = new HashMap<String, ObjectData>();
    private final Map<String, PrivateKey> privateKeyCache = new HashMap<String, PrivateKey>();
    private AlgorithmIdentifier hmacAlgorithm;
    private KeyDerivationFunc hmacPkbdAlgorithm;
    private Date creationDate;
    private Date lastModifiedDate;

    private static String getPublicKeyAlg(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        String string = publicAlgMap.get(aSN1ObjectIdentifier);
        if (string != null) {
            return string;
        }
        return aSN1ObjectIdentifier.getId();
    }

    BcFKSKeyStoreSpi(BouncyCastleProvider bouncyCastleProvider) {
        this.provider = bouncyCastleProvider;
    }

    @Override
    public Key engineGetKey(String string, char[] cArray) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            if (objectData.getType().equals(PRIVATE_KEY) || objectData.getType().equals(PROTECTED_PRIVATE_KEY)) {
                PrivateKey privateKey = this.privateKeyCache.get(string);
                if (privateKey != null) {
                    return privateKey;
                }
                EncryptedPrivateKeyData encryptedPrivateKeyData = EncryptedPrivateKeyData.getInstance(objectData.getData());
                EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = EncryptedPrivateKeyInfo.getInstance(encryptedPrivateKeyData.getEncryptedPrivateKeyInfo());
                try {
                    PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(this.decryptData("PRIVATE_KEY_ENCRYPTION", encryptedPrivateKeyInfo.getEncryptionAlgorithm(), cArray, encryptedPrivateKeyInfo.getEncryptedData()));
                    KeyFactory keyFactory = this.provider != null ? KeyFactory.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm().getId(), this.provider) : KeyFactory.getInstance(BcFKSKeyStoreSpi.getPublicKeyAlg(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm()));
                    PrivateKey privateKey2 = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded()));
                    this.privateKeyCache.put(string, privateKey2);
                    return privateKey2;
                }
                catch (Exception exception) {
                    throw new UnrecoverableKeyException("BCFKS KeyStore unable to recover private key (" + string + "): " + exception.getMessage());
                }
            }
            if (objectData.getType().equals(SECRET_KEY) || objectData.getType().equals(PROTECTED_SECRET_KEY)) {
                EncryptedSecretKeyData encryptedSecretKeyData = EncryptedSecretKeyData.getInstance(objectData.getData());
                try {
                    SecretKeyData secretKeyData = SecretKeyData.getInstance(this.decryptData("SECRET_KEY_ENCRYPTION", encryptedSecretKeyData.getKeyEncryptionAlgorithm(), cArray, encryptedSecretKeyData.getEncryptedKeyData()));
                    SecretKeyFactory secretKeyFactory = this.provider != null ? SecretKeyFactory.getInstance(secretKeyData.getKeyAlgorithm().getId(), this.provider) : SecretKeyFactory.getInstance(secretKeyData.getKeyAlgorithm().getId());
                    return secretKeyFactory.generateSecret(new SecretKeySpec(secretKeyData.getKeyBytes(), secretKeyData.getKeyAlgorithm().getId()));
                }
                catch (Exception exception) {
                    throw new UnrecoverableKeyException("BCFKS KeyStore unable to recover secret key (" + string + "): " + exception.getMessage());
                }
            }
            throw new UnrecoverableKeyException("BCFKS KeyStore unable to recover secret key (" + string + "): type not recognized");
        }
        return null;
    }

    @Override
    public java.security.cert.Certificate[] engineGetCertificateChain(String string) {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null && (objectData.getType().equals(PRIVATE_KEY) || objectData.getType().equals(PROTECTED_PRIVATE_KEY))) {
            EncryptedPrivateKeyData encryptedPrivateKeyData = EncryptedPrivateKeyData.getInstance(objectData.getData());
            Certificate[] certificateArray = encryptedPrivateKeyData.getCertificateChain();
            java.security.cert.Certificate[] certificateArray2 = new X509Certificate[certificateArray.length];
            for (int i = 0; i != certificateArray2.length; ++i) {
                certificateArray2[i] = this.decodeCertificate(certificateArray[i]);
            }
            return certificateArray2;
        }
        return null;
    }

    @Override
    public java.security.cert.Certificate engineGetCertificate(String string) {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            if (objectData.getType().equals(PRIVATE_KEY) || objectData.getType().equals(PROTECTED_PRIVATE_KEY)) {
                EncryptedPrivateKeyData encryptedPrivateKeyData = EncryptedPrivateKeyData.getInstance(objectData.getData());
                Certificate[] certificateArray = encryptedPrivateKeyData.getCertificateChain();
                return this.decodeCertificate(certificateArray[0]);
            }
            if (objectData.getType().equals(CERTIFICATE)) {
                return this.decodeCertificate(objectData.getData());
            }
        }
        return null;
    }

    private java.security.cert.Certificate decodeCertificate(Object object) {
        if (this.provider != null) {
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", this.provider);
                return certificateFactory.generateCertificate(new ByteArrayInputStream(Certificate.getInstance(object).getEncoded()));
            }
            catch (Exception exception) {
                return null;
            }
        }
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return certificateFactory.generateCertificate(new ByteArrayInputStream(Certificate.getInstance(object).getEncoded()));
        }
        catch (Exception exception) {
            return null;
        }
    }

    @Override
    public Date engineGetCreationDate(String string) {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            try {
                return objectData.getLastModifiedDate().getDate();
            }
            catch (ParseException parseException) {
                return new Date();
            }
        }
        return null;
    }

    @Override
    public void engineSetKeyEntry(String string, Key key, char[] cArray, java.security.cert.Certificate[] certificateArray) throws KeyStoreException {
        Date date;
        Date date2 = date = new Date();
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            date = this.extractCreationDate(objectData, date);
        }
        this.privateKeyCache.remove(string);
        if (key instanceof PrivateKey) {
            if (certificateArray == null) {
                throw new KeyStoreException("BCFKS KeyStore requires a certificate chain for private key storage.");
            }
            try {
                byte[] byArray = key.getEncoded();
                KeyDerivationFunc keyDerivationFunc = this.generatePkbdAlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, 32);
                byte[] byArray2 = this.generateKey(keyDerivationFunc, "PRIVATE_KEY_ENCRYPTION", cArray != null ? cArray : new char[]{}, 32);
                Cipher cipher = this.createCipher("AES/CCM/NoPadding", byArray2);
                byte[] byArray3 = cipher.doFinal(byArray);
                AlgorithmParameters algorithmParameters = cipher.getParameters();
                PBES2Parameters pBES2Parameters = algorithmParameters != null ? new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_CCM, CCMParameters.getInstance(algorithmParameters.getEncoded()))) : new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_wrap_pad, null));
                EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, pBES2Parameters), byArray3);
                EncryptedPrivateKeyData encryptedPrivateKeyData = this.createPrivateKeySequence(encryptedPrivateKeyInfo, certificateArray);
                this.entries.put(string, new ObjectData(PRIVATE_KEY, string, date, date2, encryptedPrivateKeyData.getEncoded(), null));
            }
            catch (Exception exception) {
                throw new ExtKeyStoreException("BCFKS KeyStore exception storing private key: " + exception.toString(), exception);
            }
        } else if (key instanceof SecretKey) {
            if (certificateArray != null) {
                throw new KeyStoreException("BCFKS KeyStore cannot store certificate chain with secret key.");
            }
            try {
                Object object;
                SecretKeyData secretKeyData;
                byte[] byArray = key.getEncoded();
                KeyDerivationFunc keyDerivationFunc = this.generatePkbdAlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, 32);
                byte[] byArray4 = this.generateKey(keyDerivationFunc, "SECRET_KEY_ENCRYPTION", cArray != null ? cArray : new char[]{}, 32);
                Cipher cipher = this.createCipher("AES/CCM/NoPadding", byArray4);
                String string2 = Strings.toUpperCase(key.getAlgorithm());
                if (string2.indexOf("AES") > -1) {
                    secretKeyData = new SecretKeyData(NISTObjectIdentifiers.aes, byArray);
                } else {
                    object = oidMap.get(string2);
                    if (object != null) {
                        secretKeyData = new SecretKeyData((ASN1ObjectIdentifier)object, byArray);
                    } else {
                        object = oidMap.get(string2 + "." + byArray.length * 8);
                        if (object != null) {
                            secretKeyData = new SecretKeyData((ASN1ObjectIdentifier)object, byArray);
                        } else {
                            throw new KeyStoreException("BCFKS KeyStore cannot recognize secret key (" + string2 + ") for storage.");
                        }
                    }
                }
                object = cipher.doFinal(secretKeyData.getEncoded());
                AlgorithmParameters algorithmParameters = cipher.getParameters();
                PBES2Parameters pBES2Parameters = algorithmParameters != null ? new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_CCM, CCMParameters.getInstance(algorithmParameters.getEncoded()))) : new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_wrap_pad, null));
                EncryptedSecretKeyData encryptedSecretKeyData = new EncryptedSecretKeyData(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, pBES2Parameters), (byte[])object);
                this.entries.put(string, new ObjectData(SECRET_KEY, string, date, date2, encryptedSecretKeyData.getEncoded(), null));
            }
            catch (Exception exception) {
                throw new ExtKeyStoreException("BCFKS KeyStore exception storing private key: " + exception.toString(), exception);
            }
        } else {
            throw new KeyStoreException("BCFKS KeyStore unable to recognize key.");
        }
        this.lastModifiedDate = date2;
    }

    private Cipher createCipher(String string, byte[] byArray) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = this.provider == null ? Cipher.getInstance(string) : Cipher.getInstance(string, this.provider);
        cipher.init(1, new SecretKeySpec(byArray, "AES"));
        return cipher;
    }

    private SecureRandom getDefaultSecureRandom() {
        return CryptoServicesRegistrar.getSecureRandom();
    }

    private EncryptedPrivateKeyData createPrivateKeySequence(EncryptedPrivateKeyInfo encryptedPrivateKeyInfo, java.security.cert.Certificate[] certificateArray) throws CertificateEncodingException {
        Certificate[] certificateArray2 = new Certificate[certificateArray.length];
        for (int i = 0; i != certificateArray.length; ++i) {
            certificateArray2[i] = Certificate.getInstance(certificateArray[i].getEncoded());
        }
        return new EncryptedPrivateKeyData(encryptedPrivateKeyInfo, certificateArray2);
    }

    @Override
    public void engineSetKeyEntry(String string, byte[] byArray, java.security.cert.Certificate[] certificateArray) throws KeyStoreException {
        Date date;
        Date date2 = date = new Date();
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            date = this.extractCreationDate(objectData, date);
        }
        if (certificateArray != null) {
            EncryptedPrivateKeyInfo encryptedPrivateKeyInfo;
            try {
                encryptedPrivateKeyInfo = EncryptedPrivateKeyInfo.getInstance(byArray);
            }
            catch (Exception exception) {
                throw new ExtKeyStoreException("BCFKS KeyStore private key encoding must be an EncryptedPrivateKeyInfo.", exception);
            }
            try {
                this.privateKeyCache.remove(string);
                this.entries.put(string, new ObjectData(PROTECTED_PRIVATE_KEY, string, date, date2, this.createPrivateKeySequence(encryptedPrivateKeyInfo, certificateArray).getEncoded(), null));
            }
            catch (Exception exception) {
                throw new ExtKeyStoreException("BCFKS KeyStore exception storing protected private key: " + exception.toString(), exception);
            }
        }
        try {
            this.entries.put(string, new ObjectData(PROTECTED_SECRET_KEY, string, date, date2, byArray, null));
        }
        catch (Exception exception) {
            throw new ExtKeyStoreException("BCFKS KeyStore exception storing protected private key: " + exception.toString(), exception);
        }
        this.lastModifiedDate = date2;
    }

    @Override
    public void engineSetCertificateEntry(String string, java.security.cert.Certificate certificate) throws KeyStoreException {
        Date date;
        ObjectData objectData = this.entries.get(string);
        Date date2 = date = new Date();
        if (objectData != null) {
            if (!objectData.getType().equals(CERTIFICATE)) {
                throw new KeyStoreException("BCFKS KeyStore already has a key entry with alias " + string);
            }
            date = this.extractCreationDate(objectData, date);
        }
        try {
            this.entries.put(string, new ObjectData(CERTIFICATE, string, date, date2, certificate.getEncoded(), null));
        }
        catch (CertificateEncodingException certificateEncodingException) {
            throw new ExtKeyStoreException("BCFKS KeyStore unable to handle certificate: " + certificateEncodingException.getMessage(), certificateEncodingException);
        }
        this.lastModifiedDate = date2;
    }

    private Date extractCreationDate(ObjectData objectData, Date date) {
        try {
            date = objectData.getCreationDate().getDate();
        }
        catch (ParseException parseException) {
            // empty catch block
        }
        return date;
    }

    @Override
    public void engineDeleteEntry(String string) throws KeyStoreException {
        ObjectData objectData = this.entries.get(string);
        if (objectData == null) {
            return;
        }
        this.privateKeyCache.remove(string);
        this.entries.remove(string);
        this.lastModifiedDate = new Date();
    }

    @Override
    public Enumeration<String> engineAliases() {
        final Iterator<String> iterator2 = new HashSet<String>(this.entries.keySet()).iterator();
        return new Enumeration(){

            public boolean hasMoreElements() {
                return iterator2.hasNext();
            }

            public Object nextElement() {
                return iterator2.next();
            }
        };
    }

    @Override
    public boolean engineContainsAlias(String string) {
        if (string == null) {
            throw new NullPointerException("alias value is null");
        }
        return this.entries.containsKey(string);
    }

    @Override
    public int engineSize() {
        return this.entries.size();
    }

    @Override
    public boolean engineIsKeyEntry(String string) {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            BigInteger bigInteger = objectData.getType();
            return bigInteger.equals(PRIVATE_KEY) || bigInteger.equals(SECRET_KEY) || bigInteger.equals(PROTECTED_PRIVATE_KEY) || bigInteger.equals(PROTECTED_SECRET_KEY);
        }
        return false;
    }

    @Override
    public boolean engineIsCertificateEntry(String string) {
        ObjectData objectData = this.entries.get(string);
        if (objectData != null) {
            return objectData.getType().equals(CERTIFICATE);
        }
        return false;
    }

    @Override
    public String engineGetCertificateAlias(java.security.cert.Certificate certificate) {
        byte[] byArray;
        if (certificate == null) {
            return null;
        }
        try {
            byArray = certificate.getEncoded();
        }
        catch (CertificateEncodingException certificateEncodingException) {
            return null;
        }
        for (String string : this.entries.keySet()) {
            ObjectData objectData = this.entries.get(string);
            if (objectData.getType().equals(CERTIFICATE)) {
                if (!Arrays.areEqual(objectData.getData(), byArray)) continue;
                return string;
            }
            if (!objectData.getType().equals(PRIVATE_KEY) && !objectData.getType().equals(PROTECTED_PRIVATE_KEY)) continue;
            try {
                EncryptedPrivateKeyData encryptedPrivateKeyData = EncryptedPrivateKeyData.getInstance(objectData.getData());
                if (!Arrays.areEqual(encryptedPrivateKeyData.getCertificateChain()[0].toASN1Primitive().getEncoded(), byArray)) continue;
                return string;
            }
            catch (IOException iOException) {
            }
        }
        return null;
    }

    private byte[] generateKey(KeyDerivationFunc keyDerivationFunc, String string, char[] cArray, int n) throws IOException {
        byte[] byArray = PBEParametersGenerator.PKCS12PasswordToBytes(cArray);
        byte[] byArray2 = PBEParametersGenerator.PKCS12PasswordToBytes(string.toCharArray());
        int n2 = n;
        if (MiscObjectIdentifiers.id_scrypt.equals(keyDerivationFunc.getAlgorithm())) {
            ScryptParams scryptParams = ScryptParams.getInstance(keyDerivationFunc.getParameters());
            if (scryptParams.getKeyLength() != null) {
                n2 = scryptParams.getKeyLength().intValue();
            } else if (n2 == -1) {
                throw new IOException("no keyLength found in ScryptParams");
            }
            return SCrypt.generate(Arrays.concatenate(byArray, byArray2), scryptParams.getSalt(), scryptParams.getCostParameter().intValue(), scryptParams.getBlockSize().intValue(), scryptParams.getBlockSize().intValue(), n2);
        }
        if (keyDerivationFunc.getAlgorithm().equals(PKCSObjectIdentifiers.id_PBKDF2)) {
            PBKDF2Params pBKDF2Params = PBKDF2Params.getInstance(keyDerivationFunc.getParameters());
            if (pBKDF2Params.getKeyLength() != null) {
                n2 = pBKDF2Params.getKeyLength().intValue();
            } else if (n2 == -1) {
                throw new IOException("no keyLength found in PBKDF2Params");
            }
            if (pBKDF2Params.getPrf().getAlgorithm().equals(PKCSObjectIdentifiers.id_hmacWithSHA512)) {
                PKCS5S2ParametersGenerator pKCS5S2ParametersGenerator = new PKCS5S2ParametersGenerator(new SHA512Digest());
                pKCS5S2ParametersGenerator.init(Arrays.concatenate(byArray, byArray2), pBKDF2Params.getSalt(), pBKDF2Params.getIterationCount().intValue());
                return ((KeyParameter)pKCS5S2ParametersGenerator.generateDerivedParameters(n2 * 8)).getKey();
            }
            if (pBKDF2Params.getPrf().getAlgorithm().equals(NISTObjectIdentifiers.id_hmacWithSHA3_512)) {
                PKCS5S2ParametersGenerator pKCS5S2ParametersGenerator = new PKCS5S2ParametersGenerator(new SHA3Digest(512));
                pKCS5S2ParametersGenerator.init(Arrays.concatenate(byArray, byArray2), pBKDF2Params.getSalt(), pBKDF2Params.getIterationCount().intValue());
                return ((KeyParameter)pKCS5S2ParametersGenerator.generateDerivedParameters(n2 * 8)).getKey();
            }
            throw new IOException("BCFKS KeyStore: unrecognized MAC PBKD PRF: " + pBKDF2Params.getPrf().getAlgorithm());
        }
        throw new IOException("BCFKS KeyStore: unrecognized MAC PBKD.");
    }

    private void verifyMac(byte[] byArray, PbkdMacIntegrityCheck pbkdMacIntegrityCheck, char[] cArray) throws NoSuchAlgorithmException, IOException {
        byte[] byArray2 = this.calculateMac(byArray, pbkdMacIntegrityCheck.getMacAlgorithm(), pbkdMacIntegrityCheck.getPbkdAlgorithm(), cArray);
        if (!Arrays.constantTimeAreEqual(byArray2, pbkdMacIntegrityCheck.getMac())) {
            throw new IOException("BCFKS KeyStore corrupted: MAC calculation failed.");
        }
    }

    private byte[] calculateMac(byte[] byArray, AlgorithmIdentifier algorithmIdentifier, KeyDerivationFunc keyDerivationFunc, char[] cArray) throws NoSuchAlgorithmException, IOException {
        String string = algorithmIdentifier.getAlgorithm().getId();
        Mac mac = this.provider != null ? Mac.getInstance(string, this.provider) : Mac.getInstance(string);
        try {
            mac.init(new SecretKeySpec(this.generateKey(keyDerivationFunc, "INTEGRITY_CHECK", cArray != null ? cArray : new char[]{}, -1), string));
        }
        catch (InvalidKeyException invalidKeyException) {
            throw new IOException("Cannot set up MAC calculation: " + invalidKeyException.getMessage());
        }
        return mac.doFinal(byArray);
    }

    @Override
    public void engineStore(KeyStore.LoadStoreParameter loadStoreParameter) throws CertificateException, NoSuchAlgorithmException, IOException {
        char[] cArray;
        if (loadStoreParameter == null) {
            throw new IllegalArgumentException("'parameter' arg cannot be null");
        }
        if (!(loadStoreParameter instanceof BCFKSStoreParameter)) {
            throw new IllegalArgumentException("no support for 'parameter' of type " + loadStoreParameter.getClass().getName());
        }
        BCFKSStoreParameter bCFKSStoreParameter = (BCFKSStoreParameter)loadStoreParameter;
        KeyStore.ProtectionParameter protectionParameter = bCFKSStoreParameter.getProtectionParameter();
        if (protectionParameter == null) {
            cArray = null;
        } else if (protectionParameter instanceof KeyStore.PasswordProtection) {
            cArray = ((KeyStore.PasswordProtection)protectionParameter).getPassword();
        } else if (protectionParameter instanceof KeyStore.CallbackHandlerProtection) {
            CallbackHandler callbackHandler = ((KeyStore.CallbackHandlerProtection)protectionParameter).getCallbackHandler();
            PasswordCallback passwordCallback = new PasswordCallback("password: ", false);
            try {
                callbackHandler.handle(new Callback[]{passwordCallback});
                cArray = passwordCallback.getPassword();
            }
            catch (UnsupportedCallbackException unsupportedCallbackException) {
                throw new IllegalArgumentException("PasswordCallback not recognised: " + unsupportedCallbackException.getMessage(), unsupportedCallbackException);
            }
        } else {
            throw new IllegalArgumentException("no support for protection parameter of type " + protectionParameter.getClass().getName());
        }
        this.hmacPkbdAlgorithm = bCFKSStoreParameter.getStorePBKDFConfig().getAlgorithm().equals(MiscObjectIdentifiers.id_scrypt) ? this.generatePkbdAlgorithmIdentifier(bCFKSStoreParameter.getStorePBKDFConfig(), 64) : this.generatePkbdAlgorithmIdentifier(bCFKSStoreParameter.getStorePBKDFConfig(), 64);
        this.engineStore(bCFKSStoreParameter.getOutputStream(), cArray);
    }

    @Override
    public void engineStore(OutputStream outputStream2, char[] cArray) throws IOException, NoSuchAlgorithmException, CertificateException {
        EncryptedObjectStoreData encryptedObjectStoreData;
        Object object;
        Object object2;
        ObjectData[] objectDataArray = this.entries.values().toArray(new ObjectData[this.entries.size()]);
        KeyDerivationFunc keyDerivationFunc = this.generatePkbdAlgorithmIdentifier(this.hmacPkbdAlgorithm, 32);
        byte[] byArray = this.generateKey(keyDerivationFunc, "STORE_ENCRYPTION", cArray != null ? cArray : new char[]{}, 32);
        ObjectStoreData objectStoreData = new ObjectStoreData(this.hmacAlgorithm, this.creationDate, this.lastModifiedDate, new ObjectDataSequence(objectDataArray), null);
        try {
            object2 = this.provider == null ? Cipher.getInstance("AES/CCM/NoPadding") : Cipher.getInstance("AES/CCM/NoPadding", this.provider);
            ((Cipher)object2).init(1, new SecretKeySpec(byArray, "AES"));
            object = ((Cipher)object2).doFinal(objectStoreData.getEncoded());
            AlgorithmParameters algorithmParameters = ((Cipher)object2).getParameters();
            PBES2Parameters pBES2Parameters = new PBES2Parameters(keyDerivationFunc, new EncryptionScheme(NISTObjectIdentifiers.id_aes256_CCM, CCMParameters.getInstance(algorithmParameters.getEncoded())));
            encryptedObjectStoreData = new EncryptedObjectStoreData(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, pBES2Parameters), (byte[])object);
        }
        catch (NoSuchPaddingException noSuchPaddingException) {
            throw new NoSuchAlgorithmException(noSuchPaddingException.toString());
        }
        catch (BadPaddingException badPaddingException) {
            throw new IOException(badPaddingException.toString());
        }
        catch (IllegalBlockSizeException illegalBlockSizeException) {
            throw new IOException(illegalBlockSizeException.toString());
        }
        catch (InvalidKeyException invalidKeyException) {
            throw new IOException(invalidKeyException.toString());
        }
        if (MiscObjectIdentifiers.id_scrypt.equals(this.hmacPkbdAlgorithm.getAlgorithm())) {
            object2 = ScryptParams.getInstance(this.hmacPkbdAlgorithm.getParameters());
            this.hmacPkbdAlgorithm = this.generatePkbdAlgorithmIdentifier(this.hmacPkbdAlgorithm, ((ScryptParams)object2).getKeyLength().intValue());
        } else {
            object2 = PBKDF2Params.getInstance(this.hmacPkbdAlgorithm.getParameters());
            this.hmacPkbdAlgorithm = this.generatePkbdAlgorithmIdentifier(this.hmacPkbdAlgorithm, ((PBKDF2Params)object2).getKeyLength().intValue());
        }
        object2 = this.calculateMac(encryptedObjectStoreData.getEncoded(), this.hmacAlgorithm, this.hmacPkbdAlgorithm, cArray);
        object = new ObjectStore(encryptedObjectStoreData, new ObjectStoreIntegrityCheck(new PbkdMacIntegrityCheck(this.hmacAlgorithm, this.hmacPkbdAlgorithm, (byte[])object2)));
        outputStream2.write(((ASN1Object)object).getEncoded());
        outputStream2.flush();
    }

    @Override
    public void engineLoad(InputStream inputStream2, char[] cArray) throws IOException, NoSuchAlgorithmException, CertificateException {
        ObjectStoreData objectStoreData;
        ASN1Object aSN1Object;
        Object object;
        ObjectStore objectStore;
        this.entries.clear();
        this.privateKeyCache.clear();
        this.creationDate = null;
        this.lastModifiedDate = null;
        this.hmacAlgorithm = null;
        if (inputStream2 == null) {
            this.lastModifiedDate = this.creationDate = new Date();
            this.hmacAlgorithm = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE);
            this.hmacPkbdAlgorithm = this.generatePkbdAlgorithmIdentifier(PKCSObjectIdentifiers.id_PBKDF2, 64);
            return;
        }
        ASN1InputStream aSN1InputStream = new ASN1InputStream(inputStream2);
        try {
            objectStore = ObjectStore.getInstance(aSN1InputStream.readObject());
        }
        catch (Exception exception) {
            throw new IOException(exception.getMessage());
        }
        ObjectStoreIntegrityCheck objectStoreIntegrityCheck = objectStore.getIntegrityCheck();
        if (objectStoreIntegrityCheck.getType() != 0) {
            throw new IOException("BCFKS KeyStore unable to recognize integrity check.");
        }
        ASN1Encodable aSN1Encodable = PbkdMacIntegrityCheck.getInstance(objectStoreIntegrityCheck.getIntegrityCheck());
        this.hmacAlgorithm = aSN1Encodable.getMacAlgorithm();
        this.hmacPkbdAlgorithm = aSN1Encodable.getPbkdAlgorithm();
        this.verifyMac(objectStore.getStoreData().toASN1Primitive().getEncoded(), (PbkdMacIntegrityCheck)aSN1Encodable, cArray);
        aSN1Encodable = objectStore.getStoreData();
        if (aSN1Encodable instanceof EncryptedObjectStoreData) {
            object = (EncryptedObjectStoreData)aSN1Encodable;
            aSN1Object = ((EncryptedObjectStoreData)object).getEncryptionAlgorithm();
            objectStoreData = ObjectStoreData.getInstance(this.decryptData("STORE_ENCRYPTION", (AlgorithmIdentifier)aSN1Object, cArray, ((EncryptedObjectStoreData)object).getEncryptedContent().getOctets()));
        } else {
            objectStoreData = ObjectStoreData.getInstance(aSN1Encodable);
        }
        try {
            this.creationDate = objectStoreData.getCreationDate().getDate();
            this.lastModifiedDate = objectStoreData.getLastModifiedDate().getDate();
        }
        catch (ParseException parseException) {
            throw new IOException("BCFKS KeyStore unable to parse store data information.");
        }
        if (!objectStoreData.getIntegrityAlgorithm().equals(this.hmacAlgorithm)) {
            throw new IOException("BCFKS KeyStore storeData integrity algorithm does not match store integrity algorithm.");
        }
        object = objectStoreData.getObjectDataSequence().iterator();
        while (object.hasNext()) {
            aSN1Object = ObjectData.getInstance(object.next());
            this.entries.put(((ObjectData)aSN1Object).getIdentifier(), (ObjectData)aSN1Object);
        }
    }

    private byte[] decryptData(String string, AlgorithmIdentifier algorithmIdentifier, char[] cArray, byte[] byArray) throws IOException {
        if (!algorithmIdentifier.getAlgorithm().equals(PKCSObjectIdentifiers.id_PBES2)) {
            throw new IOException("BCFKS KeyStore cannot recognize protection algorithm.");
        }
        PBES2Parameters pBES2Parameters = PBES2Parameters.getInstance(algorithmIdentifier.getParameters());
        EncryptionScheme encryptionScheme = pBES2Parameters.getEncryptionScheme();
        try {
            Object object;
            AlgorithmParameters algorithmParameters;
            Cipher cipher;
            if (encryptionScheme.getAlgorithm().equals(NISTObjectIdentifiers.id_aes256_CCM)) {
                if (this.provider == null) {
                    cipher = Cipher.getInstance("AES/CCM/NoPadding");
                    algorithmParameters = AlgorithmParameters.getInstance("CCM");
                } else {
                    cipher = Cipher.getInstance("AES/CCM/NoPadding", this.provider);
                    algorithmParameters = AlgorithmParameters.getInstance("CCM", this.provider);
                }
                object = CCMParameters.getInstance(encryptionScheme.getParameters());
                algorithmParameters.init(((ASN1Object)object).getEncoded());
            } else if (encryptionScheme.getAlgorithm().equals(NISTObjectIdentifiers.id_aes256_wrap_pad)) {
                if (this.provider == null) {
                    cipher = Cipher.getInstance("AESKWP");
                    algorithmParameters = null;
                } else {
                    cipher = Cipher.getInstance("AESKWP", this.provider);
                    algorithmParameters = null;
                }
            } else {
                throw new IOException("BCFKS KeyStore cannot recognize protection encryption algorithm.");
            }
            object = this.generateKey(pBES2Parameters.getKeyDerivationFunc(), string, cArray != null ? cArray : new char[]{}, 32);
            cipher.init(2, (Key)new SecretKeySpec((byte[])object, "AES"), algorithmParameters);
            byte[] byArray2 = cipher.doFinal(byArray);
            return byArray2;
        }
        catch (IOException iOException) {
            throw iOException;
        }
        catch (Exception exception) {
            throw new IOException(exception.toString());
        }
    }

    private KeyDerivationFunc generatePkbdAlgorithmIdentifier(PBKDFConfig pBKDFConfig, int n) {
        if (MiscObjectIdentifiers.id_scrypt.equals(pBKDFConfig.getAlgorithm())) {
            ScryptConfig scryptConfig = (ScryptConfig)pBKDFConfig;
            byte[] byArray = new byte[scryptConfig.getSaltLength()];
            this.getDefaultSecureRandom().nextBytes(byArray);
            ScryptParams scryptParams = new ScryptParams(byArray, scryptConfig.getCostParameter(), scryptConfig.getBlockSize(), scryptConfig.getParallelizationParameter(), n);
            return new KeyDerivationFunc(MiscObjectIdentifiers.id_scrypt, scryptParams);
        }
        PBKDF2Config pBKDF2Config = (PBKDF2Config)pBKDFConfig;
        byte[] byArray = new byte[pBKDF2Config.getSaltLength()];
        this.getDefaultSecureRandom().nextBytes(byArray);
        return new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(byArray, pBKDF2Config.getIterationCount(), n, pBKDF2Config.getPRF()));
    }

    private KeyDerivationFunc generatePkbdAlgorithmIdentifier(KeyDerivationFunc keyDerivationFunc, int n) {
        if (MiscObjectIdentifiers.id_scrypt.equals(keyDerivationFunc.getAlgorithm())) {
            ScryptParams scryptParams = ScryptParams.getInstance(keyDerivationFunc.getParameters());
            byte[] byArray = new byte[scryptParams.getSalt().length];
            this.getDefaultSecureRandom().nextBytes(byArray);
            ScryptParams scryptParams2 = new ScryptParams(byArray, scryptParams.getCostParameter(), scryptParams.getBlockSize(), scryptParams.getParallelizationParameter(), BigInteger.valueOf(n));
            return new KeyDerivationFunc(MiscObjectIdentifiers.id_scrypt, scryptParams2);
        }
        PBKDF2Params pBKDF2Params = PBKDF2Params.getInstance(keyDerivationFunc.getParameters());
        byte[] byArray = new byte[pBKDF2Params.getSalt().length];
        this.getDefaultSecureRandom().nextBytes(byArray);
        PBKDF2Params pBKDF2Params2 = new PBKDF2Params(byArray, pBKDF2Params.getIterationCount().intValue(), n, pBKDF2Params.getPrf());
        return new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, pBKDF2Params2);
    }

    private KeyDerivationFunc generatePkbdAlgorithmIdentifier(ASN1ObjectIdentifier aSN1ObjectIdentifier, int n) {
        byte[] byArray = new byte[64];
        this.getDefaultSecureRandom().nextBytes(byArray);
        if (PKCSObjectIdentifiers.id_PBKDF2.equals(aSN1ObjectIdentifier)) {
            return new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(byArray, 51200, n, new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA512, DERNull.INSTANCE)));
        }
        throw new IllegalStateException("unknown derivation algorithm: " + aSN1ObjectIdentifier);
    }

    static {
        oidMap.put("DESEDE", OIWObjectIdentifiers.desEDE);
        oidMap.put("TRIPLEDES", OIWObjectIdentifiers.desEDE);
        oidMap.put("TDEA", OIWObjectIdentifiers.desEDE);
        oidMap.put("HMACSHA1", PKCSObjectIdentifiers.id_hmacWithSHA1);
        oidMap.put("HMACSHA224", PKCSObjectIdentifiers.id_hmacWithSHA224);
        oidMap.put("HMACSHA256", PKCSObjectIdentifiers.id_hmacWithSHA256);
        oidMap.put("HMACSHA384", PKCSObjectIdentifiers.id_hmacWithSHA384);
        oidMap.put("HMACSHA512", PKCSObjectIdentifiers.id_hmacWithSHA512);
        oidMap.put("SEED", KISAObjectIdentifiers.id_seedCBC);
        oidMap.put("CAMELLIA.128", NTTObjectIdentifiers.id_camellia128_cbc);
        oidMap.put("CAMELLIA.192", NTTObjectIdentifiers.id_camellia192_cbc);
        oidMap.put("CAMELLIA.256", NTTObjectIdentifiers.id_camellia256_cbc);
        oidMap.put("ARIA.128", NSRIObjectIdentifiers.id_aria128_cbc);
        oidMap.put("ARIA.192", NSRIObjectIdentifiers.id_aria192_cbc);
        oidMap.put("ARIA.256", NSRIObjectIdentifiers.id_aria256_cbc);
        publicAlgMap.put(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        publicAlgMap.put(X9ObjectIdentifiers.id_ecPublicKey, "EC");
        publicAlgMap.put(OIWObjectIdentifiers.elGamalAlgorithm, "DH");
        publicAlgMap.put(PKCSObjectIdentifiers.dhKeyAgreement, "DH");
        publicAlgMap.put(X9ObjectIdentifiers.id_dsa, "DSA");
        CERTIFICATE = BigInteger.valueOf(0L);
        PRIVATE_KEY = BigInteger.valueOf(1L);
        SECRET_KEY = BigInteger.valueOf(2L);
        PROTECTED_PRIVATE_KEY = BigInteger.valueOf(3L);
        PROTECTED_SECRET_KEY = BigInteger.valueOf(4L);
    }

    public static class Def
    extends BcFKSKeyStoreSpi {
        public Def() {
            super(null);
        }
    }

    public static class DefShared
    extends SharedKeyStoreSpi {
        public DefShared() {
            super(null);
        }
    }

    private static class ExtKeyStoreException
    extends KeyStoreException {
        private final Throwable cause;

        ExtKeyStoreException(String string, Throwable throwable) {
            super(string);
            this.cause = throwable;
        }

        public Throwable getCause() {
            return this.cause;
        }
    }

    private static class SharedKeyStoreSpi
    extends BcFKSKeyStoreSpi
    implements PKCSObjectIdentifiers,
    X509ObjectIdentifiers {
        private final Map<String, byte[]> cache;
        private final byte[] seedKey;

        public SharedKeyStoreSpi(BouncyCastleProvider bouncyCastleProvider) {
            super(bouncyCastleProvider);
            try {
                this.seedKey = new byte[32];
                if (bouncyCastleProvider != null) {
                    SecureRandom.getInstance("DEFAULT", bouncyCastleProvider).nextBytes(this.seedKey);
                } else {
                    SecureRandom.getInstance("DEFAULT").nextBytes(this.seedKey);
                }
            }
            catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                throw new IllegalArgumentException("can't create cert factory - " + noSuchAlgorithmException.toString());
            }
            this.cache = new HashMap<String, byte[]>();
        }

        public void engineDeleteEntry(String string) throws KeyStoreException {
            this.cache.remove(string);
            super.engineDeleteEntry(string);
        }

        public Key engineGetKey(String string, char[] cArray) throws NoSuchAlgorithmException, UnrecoverableKeyException {
            Object object;
            byte[] byArray;
            try {
                byArray = this.calculateMac(string, cArray);
            }
            catch (InvalidKeyException invalidKeyException) {
                throw new UnrecoverableKeyException("unable to recover key (" + string + "): " + invalidKeyException.getMessage());
            }
            if (this.cache.containsKey(string) && !Arrays.constantTimeAreEqual((byte[])(object = (Object)this.cache.get(string)), byArray)) {
                throw new UnrecoverableKeyException("unable to recover key (" + string + ")");
            }
            object = super.engineGetKey(string, cArray);
            if (object != null && !this.cache.containsKey(string)) {
                this.cache.put(string, byArray);
            }
            return object;
        }

        private byte[] calculateMac(String string, char[] cArray) throws NoSuchAlgorithmException, InvalidKeyException {
            byte[] byArray = cArray != null ? Arrays.concatenate(Strings.toUTF8ByteArray(cArray), Strings.toUTF8ByteArray(string)) : Arrays.concatenate(this.seedKey, Strings.toUTF8ByteArray(string));
            return SCrypt.generate(byArray, this.seedKey, 16384, 8, 1, 32);
        }
    }

    public static class Std
    extends BcFKSKeyStoreSpi {
        public Std() {
            super(new BouncyCastleProvider());
        }
    }

    public static class StdShared
    extends SharedKeyStoreSpi {
        public StdShared() {
            super(new BouncyCastleProvider());
        }
    }
}

