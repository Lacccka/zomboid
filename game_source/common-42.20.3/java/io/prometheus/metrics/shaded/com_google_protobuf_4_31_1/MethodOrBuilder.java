/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Option;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.OptionOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Syntax;
import java.util.List;

public interface MethodOrBuilder
extends MessageOrBuilder {
    public String getName();

    public ByteString getNameBytes();

    public String getRequestTypeUrl();

    public ByteString getRequestTypeUrlBytes();

    public boolean getRequestStreaming();

    public String getResponseTypeUrl();

    public ByteString getResponseTypeUrlBytes();

    public boolean getResponseStreaming();

    public List<Option> getOptionsList();

    public Option getOptions(int var1);

    public int getOptionsCount();

    public List<? extends OptionOrBuilder> getOptionsOrBuilderList();

    public OptionOrBuilder getOptionsOrBuilder(int var1);

    public int getSyntaxValue();

    public Syntax getSyntax();
}

