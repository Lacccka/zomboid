/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.client.result;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import java.util.Map;

public final class ExpandedProductParsedResult
extends ParsedResult {
    public static final String KILOGRAM = "KG";
    public static final String POUND = "LB";
    private final String rawText;
    private final String productID;
    private final String sscc;
    private final String lotNumber;
    private final String productionDate;
    private final String packagingDate;
    private final String bestBeforeDate;
    private final String expirationDate;
    private final String weight;
    private final String weightType;
    private final String weightIncrement;
    private final String price;
    private final String priceIncrement;
    private final String priceCurrency;
    private final Map<String, String> uncommonAIs;

    public ExpandedProductParsedResult(String rawText, String productID, String sscc, String lotNumber, String productionDate, String packagingDate, String bestBeforeDate, String expirationDate, String weight, String weightType, String weightIncrement, String price, String priceIncrement, String priceCurrency, Map<String, String> uncommonAIs) {
        super(ParsedResultType.PRODUCT);
        this.rawText = rawText;
        this.productID = productID;
        this.sscc = sscc;
        this.lotNumber = lotNumber;
        this.productionDate = productionDate;
        this.packagingDate = packagingDate;
        this.bestBeforeDate = bestBeforeDate;
        this.expirationDate = expirationDate;
        this.weight = weight;
        this.weightType = weightType;
        this.weightIncrement = weightIncrement;
        this.price = price;
        this.priceIncrement = priceIncrement;
        this.priceCurrency = priceCurrency;
        this.uncommonAIs = uncommonAIs;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ExpandedProductParsedResult)) {
            return false;
        }
        ExpandedProductParsedResult other = (ExpandedProductParsedResult)o;
        return ExpandedProductParsedResult.equalsOrNull(this.productID, other.productID) && ExpandedProductParsedResult.equalsOrNull(this.sscc, other.sscc) && ExpandedProductParsedResult.equalsOrNull(this.lotNumber, other.lotNumber) && ExpandedProductParsedResult.equalsOrNull(this.productionDate, other.productionDate) && ExpandedProductParsedResult.equalsOrNull(this.bestBeforeDate, other.bestBeforeDate) && ExpandedProductParsedResult.equalsOrNull(this.expirationDate, other.expirationDate) && ExpandedProductParsedResult.equalsOrNull(this.weight, other.weight) && ExpandedProductParsedResult.equalsOrNull(this.weightType, other.weightType) && ExpandedProductParsedResult.equalsOrNull(this.weightIncrement, other.weightIncrement) && ExpandedProductParsedResult.equalsOrNull(this.price, other.price) && ExpandedProductParsedResult.equalsOrNull(this.priceIncrement, other.priceIncrement) && ExpandedProductParsedResult.equalsOrNull(this.priceCurrency, other.priceCurrency) && ExpandedProductParsedResult.equalsOrNull(this.uncommonAIs, other.uncommonAIs);
    }

    private static boolean equalsOrNull(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public int hashCode() {
        int hash = 0;
        hash ^= ExpandedProductParsedResult.hashNotNull(this.productID);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.sscc);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.lotNumber);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.productionDate);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.bestBeforeDate);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.expirationDate);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.weight);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.weightType);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.weightIncrement);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.price);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.priceIncrement);
        hash ^= ExpandedProductParsedResult.hashNotNull(this.priceCurrency);
        return hash ^= ExpandedProductParsedResult.hashNotNull(this.uncommonAIs);
    }

    private static int hashNotNull(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    public String getRawText() {
        return this.rawText;
    }

    public String getProductID() {
        return this.productID;
    }

    public String getSscc() {
        return this.sscc;
    }

    public String getLotNumber() {
        return this.lotNumber;
    }

    public String getProductionDate() {
        return this.productionDate;
    }

    public String getPackagingDate() {
        return this.packagingDate;
    }

    public String getBestBeforeDate() {
        return this.bestBeforeDate;
    }

    public String getExpirationDate() {
        return this.expirationDate;
    }

    public String getWeight() {
        return this.weight;
    }

    public String getWeightType() {
        return this.weightType;
    }

    public String getWeightIncrement() {
        return this.weightIncrement;
    }

    public String getPrice() {
        return this.price;
    }

    public String getPriceIncrement() {
        return this.priceIncrement;
    }

    public String getPriceCurrency() {
        return this.priceCurrency;
    }

    public Map<String, String> getUncommonAIs() {
        return this.uncommonAIs;
    }

    @Override
    public String getDisplayResult() {
        return String.valueOf(this.rawText);
    }
}

