/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

public class RandomDotOrgSeedGenerator
implements SeedGenerator {
    private static final String BASE_URL = "https://www.random.org";
    private static final String RANDOM_URL = "https://www.random.org/integers/?num={0,number,0}&min=0&max=255&col=1&base=16&format=plain&rnd=new";
    private static final String USER_AGENT = RandomDotOrgSeedGenerator.class.getName();
    private static final int MAX_REQUEST_SIZE = 10000;
    private static final Lock cacheLock = new ReentrantLock();
    private static byte[] cache = new byte[1024];
    private static int cacheOffset = cache.length;

    public byte[] generateSeed(int length) throws SeedException {
        byte[] seedData = new byte[length];
        try {
            cacheLock.lock();
            int count = 0;
            while (count < length) {
                if (cacheOffset < cache.length) {
                    int numberOfBytes = Math.min(length - count, cache.length - cacheOffset);
                    System.arraycopy(cache, cacheOffset, seedData, count, numberOfBytes);
                    count += numberOfBytes;
                    cacheOffset += numberOfBytes;
                    continue;
                }
                this.refreshCache(length - count);
            }
        }
        catch (IOException ex) {
            throw new SeedException("Failed downloading bytes from https://www.random.org", ex);
        }
        catch (SecurityException ex) {
            throw new SeedException("SecurityManager prevented access to https://www.random.org", ex);
        }
        finally {
            cacheLock.unlock();
        }
        return seedData;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void refreshCache(int requiredBytes) throws IOException {
        int numberOfBytes = Math.max(requiredBytes, cache.length);
        if ((numberOfBytes = Math.min(numberOfBytes, 10000)) != cache.length) {
            cache = new byte[numberOfBytes];
            cacheOffset = numberOfBytes;
        }
        URL url = new URL(MessageFormat.format(RANDOM_URL, numberOfBytes));
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        try {
            int index = -1;
            String line = reader.readLine();
            while (line != null) {
                RandomDotOrgSeedGenerator.cache[++index] = (byte)Integer.parseInt(line, 16);
                line = reader.readLine();
            }
            if (index < cache.length - 1) {
                throw new IOException("Insufficient data received.");
            }
            cacheOffset = 0;
        }
        finally {
            reader.close();
        }
    }

    public String toString() {
        return BASE_URL;
    }
}

