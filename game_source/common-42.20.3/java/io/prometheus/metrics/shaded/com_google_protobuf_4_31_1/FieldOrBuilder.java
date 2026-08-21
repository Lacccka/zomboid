/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Field;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Option;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.OptionOrBuilder;
import java.util.List;

public interface FieldOrBuilder
extends MessageOrBuilder {
    public int getKindValue();

    public Field.Kind getKind();

    public int getCardinalityValue();

    public Field.Cardinality getCardinality();

    public int getNumber();

    public String getName();

    public ByteString getNameBytes();

    public String getTypeUrl();

    public ByteString getTypeUrlBytes();

    public int getOneofIndex();

    public boolean getPacked();

    public List<Option> getOptionsList();

    public Option getOptions(int var1);

    public int getOptionsCount();

    public List<? extends OptionOrBuilder> getOptionsOrBuilderList();

    public OptionOrBuilder getOptionsOrBuilder(int var1);

    public String getJsonName();

    public ByteString getJsonNameBytes();

    public String getDefaultValue();

    public ByteString getDefaultValueBytes();
}

