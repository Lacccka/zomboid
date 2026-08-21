/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.EnumValue;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.EnumValueOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Option;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.OptionOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.SourceContext;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.SourceContextOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Syntax;
import java.util.List;

public interface EnumOrBuilder
extends MessageOrBuilder {
    public String getName();

    public ByteString getNameBytes();

    public List<EnumValue> getEnumvalueList();

    public EnumValue getEnumvalue(int var1);

    public int getEnumvalueCount();

    public List<? extends EnumValueOrBuilder> getEnumvalueOrBuilderList();

    public EnumValueOrBuilder getEnumvalueOrBuilder(int var1);

    public List<Option> getOptionsList();

    public Option getOptions(int var1);

    public int getOptionsCount();

    public List<? extends OptionOrBuilder> getOptionsOrBuilderList();

    public OptionOrBuilder getOptionsOrBuilder(int var1);

    public boolean hasSourceContext();

    public SourceContext getSourceContext();

    public SourceContextOrBuilder getSourceContextOrBuilder();

    public int getSyntaxValue();

    public Syntax getSyntax();

    public String getEdition();

    public ByteString getEditionBytes();
}

