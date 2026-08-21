/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Method;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MethodOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Mixin;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MixinOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Option;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.OptionOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.SourceContext;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.SourceContextOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Syntax;
import java.util.List;

public interface ApiOrBuilder
extends MessageOrBuilder {
    public String getName();

    public ByteString getNameBytes();

    public List<Method> getMethodsList();

    public Method getMethods(int var1);

    public int getMethodsCount();

    public List<? extends MethodOrBuilder> getMethodsOrBuilderList();

    public MethodOrBuilder getMethodsOrBuilder(int var1);

    public List<Option> getOptionsList();

    public Option getOptions(int var1);

    public int getOptionsCount();

    public List<? extends OptionOrBuilder> getOptionsOrBuilderList();

    public OptionOrBuilder getOptionsOrBuilder(int var1);

    public String getVersion();

    public ByteString getVersionBytes();

    public boolean hasSourceContext();

    public SourceContext getSourceContext();

    public SourceContextOrBuilder getSourceContextOrBuilder();

    public List<Mixin> getMixinsList();

    public Mixin getMixins(int var1);

    public int getMixinsCount();

    public List<? extends MixinOrBuilder> getMixinsOrBuilderList();

    public MixinOrBuilder getMixinsOrBuilder(int var1);

    public int getSyntaxValue();

    public Syntax getSyntax();
}

