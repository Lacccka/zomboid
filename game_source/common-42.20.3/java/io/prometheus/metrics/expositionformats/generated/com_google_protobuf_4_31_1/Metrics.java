/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_31_1;

import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.AbstractMessage;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.AbstractMessageLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.AbstractParser;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ByteString;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CodedInputStream;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.CodedOutputStream;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Descriptors;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ExtensionRegistry;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ExtensionRegistryLite;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.GeneratedMessage;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Internal;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.InvalidProtocolBufferException;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Message;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.MessageOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Parser;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.ProtocolMessageEnum;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.RepeatedFieldBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.RuntimeVersion;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.SingleFieldBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.Timestamp;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.TimestampOrBuilder;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.TimestampProto;
import io.prometheus.metrics.shaded.com_google_protobuf_4_31_1.UninitializedMessageException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Metrics {
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_LabelPair_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_LabelPair_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_Gauge_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_Gauge_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_Counter_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_Counter_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_Quantile_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_Quantile_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_Summary_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_Summary_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_Untyped_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_Untyped_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_Histogram_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_Histogram_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_Bucket_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_Bucket_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_BucketSpan_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_BucketSpan_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_Exemplar_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_Exemplar_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_Metric_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_Metric_fieldAccessorTable;
    private static final Descriptors.Descriptor internal_static_io_prometheus_client_MetricFamily_descriptor;
    private static final GeneratedMessage.FieldAccessorTable internal_static_io_prometheus_client_MetricFamily_fieldAccessorTable;
    private static Descriptors.FileDescriptor descriptor;

    private Metrics() {
    }

    public static void registerAllExtensions(ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(ExtensionRegistry registry) {
        Metrics.registerAllExtensions((ExtensionRegistryLite)registry);
    }

    public static Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

    static {
        RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Metrics.class.getName());
        String[] descriptorData = new String[]{"\n\u001fsrc/main/protobuf/metrics.proto\u0012\u0014io.prometheus.client\u001a\u001fgoogle/protobuf/timestamp.proto\"(\n\tLabelPair\u0012\f\n\u0004name\u0018\u0001 \u0001(\t\u0012\r\n\u0005value\u0018\u0002 \u0001(\t\"\u0016\n\u0005Gauge\u0012\r\n\u0005value\u0018\u0001 \u0001(\u0001\"\u0081\u0001\n\u0007Counter\u0012\r\n\u0005value\u0018\u0001 \u0001(\u0001\u00120\n\bexemplar\u0018\u0002 \u0001(\u000b2\u001e.io.prometheus.client.Exemplar\u00125\n\u0011created_timestamp\u0018\u0003 \u0001(\u000b2\u001a.google.protobuf.Timestamp\"+\n\bQuantile\u0012\u0010\n\bquantile\u0018\u0001 \u0001(\u0001\u0012\r\n\u0005value\u0018\u0002 \u0001(\u0001\"\u009c\u0001\n\u0007Summary\u0012\u0014\n\fsample_count\u0018\u0001 \u0001(\u0004\u0012\u0012\n\nsample_sum\u0018\u0002 \u0001(\u0001\u00120\n\bquantile\u0018\u0003 \u0003(\u000b2\u001e.io.prometheus.client.Quantile\u00125\n\u0011created_timestamp\u0018\u0004 \u0001(\u000b2\u001a.google.protobuf.Timestamp\"\u0018\n\u0007Untyped\u0012\r\n\u0005value\u0018\u0001 \u0001(\u0001\"\u0091\u0004\n\tHistogram\u0012\u0014\n\fsample_count\u0018\u0001 \u0001(\u0004\u0012\u001a\n\u0012sample_count_float\u0018\u0004 \u0001(\u0001\u0012\u0012\n\nsample_sum\u0018\u0002 \u0001(\u0001\u0012,\n\u0006bucket\u0018\u0003 \u0003(\u000b2\u001c.io.prometheus.client.Bucket\u00125\n\u0011created_timestamp\u0018\u000f \u0001(\u000b2\u001a.google.protobuf.Timestamp\u0012\u000e\n\u0006schema\u0018\u0005 \u0001(\u0011\u0012\u0016\n\u000ezero_threshold\u0018\u0006 \u0001(\u0001\u0012\u0012\n\nzero_count\u0018\u0007 \u0001(\u0004\u0012\u0018\n\u0010zero_count_float\u0018\b \u0001(\u0001\u00127\n\rnegative_span\u0018\t \u0003(\u000b2 .io.prometheus.client.BucketSpan\u0012\u0016\n\u000enegative_delta\u0018\n \u0003(\u0012\u0012\u0016\n\u000enegative_count\u0018\u000b \u0003(\u0001\u00127\n\rpositive_span\u0018\f \u0003(\u000b2 .io.prometheus.client.BucketSpan\u0012\u0016\n\u000epositive_delta\u0018\r \u0003(\u0012\u0012\u0016\n\u000epositive_count\u0018\u000e \u0003(\u0001\u00121\n\texemplars\u0018\u0010 \u0003(\u000b2\u001e.io.prometheus.client.Exemplar\"\u0089\u0001\n\u0006Bucket\u0012\u0018\n\u0010cumulative_count\u0018\u0001 \u0001(\u0004\u0012\u001e\n\u0016cumulative_count_float\u0018\u0004 \u0001(\u0001\u0012\u0013\n\u000bupper_bound\u0018\u0002 \u0001(\u0001\u00120\n\bexemplar\u0018\u0003 \u0001(\u000b2\u001e.io.prometheus.client.Exemplar\",\n\nBucketSpan\u0012\u000e\n\u0006offset\u0018\u0001 \u0001(\u0011\u0012\u000e\n\u0006length\u0018\u0002 \u0001(\r\"x\n\bExemplar\u0012.\n\u0005label\u0018\u0001 \u0003(\u000b2\u001f.io.prometheus.client.LabelPair\u0012\r\n\u0005value\u0018\u0002 \u0001(\u0001\u0012-\n\ttimestamp\u0018\u0003 \u0001(\u000b2\u001a.google.protobuf.Timestamp\"\u00be\u0002\n\u0006Metric\u0012.\n\u0005label\u0018\u0001 \u0003(\u000b2\u001f.io.prometheus.client.LabelPair\u0012*\n\u0005gauge\u0018\u0002 \u0001(\u000b2\u001b.io.prometheus.client.Gauge\u0012.\n\u0007counter\u0018\u0003 \u0001(\u000b2\u001d.io.prometheus.client.Counter\u0012.\n\u0007summary\u0018\u0004 \u0001(\u000b2\u001d.io.prometheus.client.Summary\u0012.\n\u0007untyped\u0018\u0005 \u0001(\u000b2\u001d.io.prometheus.client.Untyped\u00122\n\thistogram\u0018\u0007 \u0001(\u000b2\u001f.io.prometheus.client.Histogram\u0012\u0014\n\ftimestamp_ms\u0018\u0006 \u0001(\u0003\"\u0096\u0001\n\fMetricFamily\u0012\f\n\u0004name\u0018\u0001 \u0001(\t\u0012\f\n\u0004help\u0018\u0002 \u0001(\t\u0012.\n\u0004type\u0018\u0003 \u0001(\u000e2 .io.prometheus.client.MetricType\u0012,\n\u0006metric\u0018\u0004 \u0003(\u000b2\u001c.io.prometheus.client.Metric\u0012\f\n\u0004unit\u0018\u0005 \u0001(\t*b\n\nMetricType\u0012\u000b\n\u0007COUNTER\u0010\u0000\u0012\t\n\u0005GAUGE\u0010\u0001\u0012\u000b\n\u0007SUMMARY\u0010\u0002\u0012\u000b\n\u0007UNTYPED\u0010\u0003\u0012\r\n\tHISTOGRAM\u0010\u0004\u0012\u0013\n\u000fGAUGE_HISTOGRAM\u0010\u0005B\u008a\u0001\nLio.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_31_1Z:github.com/prometheus/client_model/go;io_prometheus_client"};
        descriptor = Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new Descriptors.FileDescriptor[]{TimestampProto.getDescriptor()});
        internal_static_io_prometheus_client_LabelPair_descriptor = Metrics.getDescriptor().getMessageTypes().get(0);
        internal_static_io_prometheus_client_LabelPair_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_LabelPair_descriptor, new String[]{"Name", "Value"});
        internal_static_io_prometheus_client_Gauge_descriptor = Metrics.getDescriptor().getMessageTypes().get(1);
        internal_static_io_prometheus_client_Gauge_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_Gauge_descriptor, new String[]{"Value"});
        internal_static_io_prometheus_client_Counter_descriptor = Metrics.getDescriptor().getMessageTypes().get(2);
        internal_static_io_prometheus_client_Counter_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_Counter_descriptor, new String[]{"Value", "Exemplar", "CreatedTimestamp"});
        internal_static_io_prometheus_client_Quantile_descriptor = Metrics.getDescriptor().getMessageTypes().get(3);
        internal_static_io_prometheus_client_Quantile_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_Quantile_descriptor, new String[]{"Quantile", "Value"});
        internal_static_io_prometheus_client_Summary_descriptor = Metrics.getDescriptor().getMessageTypes().get(4);
        internal_static_io_prometheus_client_Summary_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_Summary_descriptor, new String[]{"SampleCount", "SampleSum", "Quantile", "CreatedTimestamp"});
        internal_static_io_prometheus_client_Untyped_descriptor = Metrics.getDescriptor().getMessageTypes().get(5);
        internal_static_io_prometheus_client_Untyped_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_Untyped_descriptor, new String[]{"Value"});
        internal_static_io_prometheus_client_Histogram_descriptor = Metrics.getDescriptor().getMessageTypes().get(6);
        internal_static_io_prometheus_client_Histogram_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_Histogram_descriptor, new String[]{"SampleCount", "SampleCountFloat", "SampleSum", "Bucket", "CreatedTimestamp", "Schema", "ZeroThreshold", "ZeroCount", "ZeroCountFloat", "NegativeSpan", "NegativeDelta", "NegativeCount", "PositiveSpan", "PositiveDelta", "PositiveCount", "Exemplars"});
        internal_static_io_prometheus_client_Bucket_descriptor = Metrics.getDescriptor().getMessageTypes().get(7);
        internal_static_io_prometheus_client_Bucket_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_Bucket_descriptor, new String[]{"CumulativeCount", "CumulativeCountFloat", "UpperBound", "Exemplar"});
        internal_static_io_prometheus_client_BucketSpan_descriptor = Metrics.getDescriptor().getMessageTypes().get(8);
        internal_static_io_prometheus_client_BucketSpan_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_BucketSpan_descriptor, new String[]{"Offset", "Length"});
        internal_static_io_prometheus_client_Exemplar_descriptor = Metrics.getDescriptor().getMessageTypes().get(9);
        internal_static_io_prometheus_client_Exemplar_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_Exemplar_descriptor, new String[]{"Label", "Value", "Timestamp"});
        internal_static_io_prometheus_client_Metric_descriptor = Metrics.getDescriptor().getMessageTypes().get(10);
        internal_static_io_prometheus_client_Metric_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_Metric_descriptor, new String[]{"Label", "Gauge", "Counter", "Summary", "Untyped", "Histogram", "TimestampMs"});
        internal_static_io_prometheus_client_MetricFamily_descriptor = Metrics.getDescriptor().getMessageTypes().get(11);
        internal_static_io_prometheus_client_MetricFamily_fieldAccessorTable = new GeneratedMessage.FieldAccessorTable(internal_static_io_prometheus_client_MetricFamily_descriptor, new String[]{"Name", "Help", "Type", "Metric", "Unit"});
        descriptor.resolveAllFeaturesImmutable();
        TimestampProto.getDescriptor();
    }

    public static final class MetricFamily
    extends GeneratedMessage
    implements MetricFamilyOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int NAME_FIELD_NUMBER = 1;
        private volatile Object name_ = "";
        public static final int HELP_FIELD_NUMBER = 2;
        private volatile Object help_ = "";
        public static final int TYPE_FIELD_NUMBER = 3;
        private int type_ = 0;
        public static final int METRIC_FIELD_NUMBER = 4;
        private List<Metric> metric_;
        public static final int UNIT_FIELD_NUMBER = 5;
        private volatile Object unit_ = "";
        private byte memoizedIsInitialized = (byte)-1;
        private static final MetricFamily DEFAULT_INSTANCE;
        private static final Parser<MetricFamily> PARSER;

        private MetricFamily(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private MetricFamily() {
            this.name_ = "";
            this.help_ = "";
            this.type_ = 0;
            this.metric_ = Collections.emptyList();
            this.unit_ = "";
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_MetricFamily_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_MetricFamily_fieldAccessorTable.ensureFieldAccessorsInitialized(MetricFamily.class, Builder.class);
        }

        @Override
        public boolean hasName() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public String getName() {
            Object ref = this.name_;
            if (ref instanceof String) {
                return (String)ref;
            }
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
                this.name_ = s;
            }
            return s;
        }

        @Override
        public ByteString getNameBytes() {
            Object ref = this.name_;
            if (ref instanceof String) {
                ByteString b = ByteString.copyFromUtf8((String)ref);
                this.name_ = b;
                return b;
            }
            return (ByteString)ref;
        }

        @Override
        public boolean hasHelp() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public String getHelp() {
            Object ref = this.help_;
            if (ref instanceof String) {
                return (String)ref;
            }
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
                this.help_ = s;
            }
            return s;
        }

        @Override
        public ByteString getHelpBytes() {
            Object ref = this.help_;
            if (ref instanceof String) {
                ByteString b = ByteString.copyFromUtf8((String)ref);
                this.help_ = b;
                return b;
            }
            return (ByteString)ref;
        }

        @Override
        public boolean hasType() {
            return (this.bitField0_ & 4) != 0;
        }

        @Override
        public MetricType getType() {
            MetricType result = MetricType.forNumber(this.type_);
            return result == null ? MetricType.COUNTER : result;
        }

        @Override
        public List<Metric> getMetricList() {
            return this.metric_;
        }

        @Override
        public List<? extends MetricOrBuilder> getMetricOrBuilderList() {
            return this.metric_;
        }

        @Override
        public int getMetricCount() {
            return this.metric_.size();
        }

        @Override
        public Metric getMetric(int index) {
            return this.metric_.get(index);
        }

        @Override
        public MetricOrBuilder getMetricOrBuilder(int index) {
            return this.metric_.get(index);
        }

        @Override
        public boolean hasUnit() {
            return (this.bitField0_ & 8) != 0;
        }

        @Override
        public String getUnit() {
            Object ref = this.unit_;
            if (ref instanceof String) {
                return (String)ref;
            }
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
                this.unit_ = s;
            }
            return s;
        }

        @Override
        public ByteString getUnitBytes() {
            Object ref = this.unit_;
            if (ref instanceof String) {
                ByteString b = ByteString.copyFromUtf8((String)ref);
                this.unit_ = b;
                return b;
            }
            return (ByteString)ref;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                GeneratedMessage.writeString(output, 1, this.name_);
            }
            if ((this.bitField0_ & 2) != 0) {
                GeneratedMessage.writeString(output, 2, this.help_);
            }
            if ((this.bitField0_ & 4) != 0) {
                output.writeEnum(3, this.type_);
            }
            for (int i = 0; i < this.metric_.size(); ++i) {
                output.writeMessage(4, this.metric_.get(i));
            }
            if ((this.bitField0_ & 8) != 0) {
                GeneratedMessage.writeString(output, 5, this.unit_);
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
                size += GeneratedMessage.computeStringSize(1, this.name_);
            }
            if ((this.bitField0_ & 2) != 0) {
                size += GeneratedMessage.computeStringSize(2, this.help_);
            }
            if ((this.bitField0_ & 4) != 0) {
                size += CodedOutputStream.computeEnumSize(3, this.type_);
            }
            for (int i = 0; i < this.metric_.size(); ++i) {
                size += CodedOutputStream.computeMessageSize(4, this.metric_.get(i));
            }
            if ((this.bitField0_ & 8) != 0) {
                size += GeneratedMessage.computeStringSize(5, this.unit_);
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof MetricFamily)) {
                return super.equals(obj);
            }
            MetricFamily other = (MetricFamily)obj;
            if (this.hasName() != other.hasName()) {
                return false;
            }
            if (this.hasName() && !this.getName().equals(other.getName())) {
                return false;
            }
            if (this.hasHelp() != other.hasHelp()) {
                return false;
            }
            if (this.hasHelp() && !this.getHelp().equals(other.getHelp())) {
                return false;
            }
            if (this.hasType() != other.hasType()) {
                return false;
            }
            if (this.hasType() && this.type_ != other.type_) {
                return false;
            }
            if (!this.getMetricList().equals(other.getMetricList())) {
                return false;
            }
            if (this.hasUnit() != other.hasUnit()) {
                return false;
            }
            if (this.hasUnit() && !this.getUnit().equals(other.getUnit())) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + MetricFamily.getDescriptor().hashCode();
            if (this.hasName()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + this.getName().hashCode();
            }
            if (this.hasHelp()) {
                hash = 37 * hash + 2;
                hash = 53 * hash + this.getHelp().hashCode();
            }
            if (this.hasType()) {
                hash = 37 * hash + 3;
                hash = 53 * hash + this.type_;
            }
            if (this.getMetricCount() > 0) {
                hash = 37 * hash + 4;
                hash = 53 * hash + this.getMetricList().hashCode();
            }
            if (this.hasUnit()) {
                hash = 37 * hash + 5;
                hash = 53 * hash + this.getUnit().hashCode();
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static MetricFamily parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static MetricFamily parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static MetricFamily parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static MetricFamily parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static MetricFamily parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static MetricFamily parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static MetricFamily parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static MetricFamily parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static MetricFamily parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static MetricFamily parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static MetricFamily parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static MetricFamily parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return MetricFamily.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(MetricFamily prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static MetricFamily getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<MetricFamily> parser() {
            return PARSER;
        }

        public Parser<MetricFamily> getParserForType() {
            return PARSER;
        }

        @Override
        public MetricFamily getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", MetricFamily.class.getName());
            DEFAULT_INSTANCE = new MetricFamily();
            PARSER = new AbstractParser<MetricFamily>(){

                @Override
                public MetricFamily parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = MetricFamily.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements MetricFamilyOrBuilder {
            private int bitField0_;
            private Object name_ = "";
            private Object help_ = "";
            private int type_ = 0;
            private List<Metric> metric_ = Collections.emptyList();
            private RepeatedFieldBuilder<Metric, Metric.Builder, MetricOrBuilder> metricBuilder_;
            private Object unit_ = "";

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_MetricFamily_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_MetricFamily_fieldAccessorTable.ensureFieldAccessorsInitialized(MetricFamily.class, Builder.class);
            }

            private Builder() {
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.name_ = "";
                this.help_ = "";
                this.type_ = 0;
                if (this.metricBuilder_ == null) {
                    this.metric_ = Collections.emptyList();
                } else {
                    this.metric_ = null;
                    this.metricBuilder_.clear();
                }
                this.bitField0_ &= 0xFFFFFFF7;
                this.unit_ = "";
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_MetricFamily_descriptor;
            }

            @Override
            public MetricFamily getDefaultInstanceForType() {
                return MetricFamily.getDefaultInstance();
            }

            @Override
            public MetricFamily build() {
                MetricFamily result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public MetricFamily buildPartial() {
                MetricFamily result = new MetricFamily(this);
                this.buildPartialRepeatedFields(result);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartialRepeatedFields(MetricFamily result) {
                if (this.metricBuilder_ == null) {
                    if ((this.bitField0_ & 8) != 0) {
                        this.metric_ = Collections.unmodifiableList(this.metric_);
                        this.bitField0_ &= 0xFFFFFFF7;
                    }
                    result.metric_ = this.metric_;
                } else {
                    result.metric_ = this.metricBuilder_.build();
                }
            }

            private void buildPartial0(MetricFamily result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.name_ = this.name_;
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 2) != 0) {
                    result.help_ = this.help_;
                    to_bitField0_ |= 2;
                }
                if ((from_bitField0_ & 4) != 0) {
                    result.type_ = this.type_;
                    to_bitField0_ |= 4;
                }
                if ((from_bitField0_ & 0x10) != 0) {
                    result.unit_ = this.unit_;
                    to_bitField0_ |= 8;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof MetricFamily) {
                    return this.mergeFrom((MetricFamily)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(MetricFamily other) {
                if (other == MetricFamily.getDefaultInstance()) {
                    return this;
                }
                if (other.hasName()) {
                    this.name_ = other.name_;
                    this.bitField0_ |= 1;
                    this.onChanged();
                }
                if (other.hasHelp()) {
                    this.help_ = other.help_;
                    this.bitField0_ |= 2;
                    this.onChanged();
                }
                if (other.hasType()) {
                    this.setType(other.getType());
                }
                if (this.metricBuilder_ == null) {
                    if (!other.metric_.isEmpty()) {
                        if (this.metric_.isEmpty()) {
                            this.metric_ = other.metric_;
                            this.bitField0_ &= 0xFFFFFFF7;
                        } else {
                            this.ensureMetricIsMutable();
                            this.metric_.addAll(other.metric_);
                        }
                        this.onChanged();
                    }
                } else if (!other.metric_.isEmpty()) {
                    if (this.metricBuilder_.isEmpty()) {
                        this.metricBuilder_.dispose();
                        this.metricBuilder_ = null;
                        this.metric_ = other.metric_;
                        this.bitField0_ &= 0xFFFFFFF7;
                        this.metricBuilder_ = alwaysUseFieldBuilders ? this.internalGetMetricFieldBuilder() : null;
                    } else {
                        this.metricBuilder_.addAllMessages(other.metric_);
                    }
                }
                if (other.hasUnit()) {
                    this.unit_ = other.unit_;
                    this.bitField0_ |= 0x10;
                    this.onChanged();
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block13: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block13;
                            }
                            case 10: {
                                this.name_ = input.readBytes();
                                this.bitField0_ |= 1;
                                continue block13;
                            }
                            case 18: {
                                this.help_ = input.readBytes();
                                this.bitField0_ |= 2;
                                continue block13;
                            }
                            case 24: {
                                int tmpRaw = input.readEnum();
                                MetricType tmpValue = MetricType.forNumber(tmpRaw);
                                if (tmpValue == null) {
                                    this.mergeUnknownVarintField(3, tmpRaw);
                                    continue block13;
                                }
                                this.type_ = tmpRaw;
                                this.bitField0_ |= 4;
                                continue block13;
                            }
                            case 34: {
                                Metric m = input.readMessage(Metric.parser(), extensionRegistry);
                                if (this.metricBuilder_ == null) {
                                    this.ensureMetricIsMutable();
                                    this.metric_.add(m);
                                    continue block13;
                                }
                                this.metricBuilder_.addMessage(m);
                                continue block13;
                            }
                            case 42: {
                                this.unit_ = input.readBytes();
                                this.bitField0_ |= 0x10;
                                continue block13;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasName() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public String getName() {
                Object ref = this.name_;
                if (!(ref instanceof String)) {
                    ByteString bs = (ByteString)ref;
                    String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        this.name_ = s;
                    }
                    return s;
                }
                return (String)ref;
            }

            @Override
            public ByteString getNameBytes() {
                Object ref = this.name_;
                if (ref instanceof String) {
                    ByteString b = ByteString.copyFromUtf8((String)ref);
                    this.name_ = b;
                    return b;
                }
                return (ByteString)ref;
            }

            public Builder setName(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.name_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearName() {
                this.name_ = MetricFamily.getDefaultInstance().getName();
                this.bitField0_ &= 0xFFFFFFFE;
                this.onChanged();
                return this;
            }

            public Builder setNameBytes(ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.name_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasHelp() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public String getHelp() {
                Object ref = this.help_;
                if (!(ref instanceof String)) {
                    ByteString bs = (ByteString)ref;
                    String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        this.help_ = s;
                    }
                    return s;
                }
                return (String)ref;
            }

            @Override
            public ByteString getHelpBytes() {
                Object ref = this.help_;
                if (ref instanceof String) {
                    ByteString b = ByteString.copyFromUtf8((String)ref);
                    this.help_ = b;
                    return b;
                }
                return (ByteString)ref;
            }

            public Builder setHelp(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.help_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder clearHelp() {
                this.help_ = MetricFamily.getDefaultInstance().getHelp();
                this.bitField0_ &= 0xFFFFFFFD;
                this.onChanged();
                return this;
            }

            public Builder setHelpBytes(ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.help_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasType() {
                return (this.bitField0_ & 4) != 0;
            }

            @Override
            public MetricType getType() {
                MetricType result = MetricType.forNumber(this.type_);
                return result == null ? MetricType.COUNTER : result;
            }

            public Builder setType(MetricType value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.bitField0_ |= 4;
                this.type_ = value.getNumber();
                this.onChanged();
                return this;
            }

            public Builder clearType() {
                this.bitField0_ &= 0xFFFFFFFB;
                this.type_ = 0;
                this.onChanged();
                return this;
            }

            private void ensureMetricIsMutable() {
                if ((this.bitField0_ & 8) == 0) {
                    this.metric_ = new ArrayList<Metric>(this.metric_);
                    this.bitField0_ |= 8;
                }
            }

            @Override
            public List<Metric> getMetricList() {
                if (this.metricBuilder_ == null) {
                    return Collections.unmodifiableList(this.metric_);
                }
                return this.metricBuilder_.getMessageList();
            }

            @Override
            public int getMetricCount() {
                if (this.metricBuilder_ == null) {
                    return this.metric_.size();
                }
                return this.metricBuilder_.getCount();
            }

            @Override
            public Metric getMetric(int index) {
                if (this.metricBuilder_ == null) {
                    return this.metric_.get(index);
                }
                return this.metricBuilder_.getMessage(index);
            }

            public Builder setMetric(int index, Metric value) {
                if (this.metricBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureMetricIsMutable();
                    this.metric_.set(index, value);
                    this.onChanged();
                } else {
                    this.metricBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setMetric(int index, Metric.Builder builderForValue) {
                if (this.metricBuilder_ == null) {
                    this.ensureMetricIsMutable();
                    this.metric_.set(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.metricBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addMetric(Metric value) {
                if (this.metricBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureMetricIsMutable();
                    this.metric_.add(value);
                    this.onChanged();
                } else {
                    this.metricBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addMetric(int index, Metric value) {
                if (this.metricBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureMetricIsMutable();
                    this.metric_.add(index, value);
                    this.onChanged();
                } else {
                    this.metricBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addMetric(Metric.Builder builderForValue) {
                if (this.metricBuilder_ == null) {
                    this.ensureMetricIsMutable();
                    this.metric_.add(builderForValue.build());
                    this.onChanged();
                } else {
                    this.metricBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addMetric(int index, Metric.Builder builderForValue) {
                if (this.metricBuilder_ == null) {
                    this.ensureMetricIsMutable();
                    this.metric_.add(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.metricBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllMetric(Iterable<? extends Metric> values2) {
                if (this.metricBuilder_ == null) {
                    this.ensureMetricIsMutable();
                    AbstractMessageLite.Builder.addAll(values2, this.metric_);
                    this.onChanged();
                } else {
                    this.metricBuilder_.addAllMessages(values2);
                }
                return this;
            }

            public Builder clearMetric() {
                if (this.metricBuilder_ == null) {
                    this.metric_ = Collections.emptyList();
                    this.bitField0_ &= 0xFFFFFFF7;
                    this.onChanged();
                } else {
                    this.metricBuilder_.clear();
                }
                return this;
            }

            public Builder removeMetric(int index) {
                if (this.metricBuilder_ == null) {
                    this.ensureMetricIsMutable();
                    this.metric_.remove(index);
                    this.onChanged();
                } else {
                    this.metricBuilder_.remove(index);
                }
                return this;
            }

            public Metric.Builder getMetricBuilder(int index) {
                return this.internalGetMetricFieldBuilder().getBuilder(index);
            }

            @Override
            public MetricOrBuilder getMetricOrBuilder(int index) {
                if (this.metricBuilder_ == null) {
                    return this.metric_.get(index);
                }
                return this.metricBuilder_.getMessageOrBuilder(index);
            }

            @Override
            public List<? extends MetricOrBuilder> getMetricOrBuilderList() {
                if (this.metricBuilder_ != null) {
                    return this.metricBuilder_.getMessageOrBuilderList();
                }
                return Collections.unmodifiableList(this.metric_);
            }

            public Metric.Builder addMetricBuilder() {
                return this.internalGetMetricFieldBuilder().addBuilder(Metric.getDefaultInstance());
            }

            public Metric.Builder addMetricBuilder(int index) {
                return this.internalGetMetricFieldBuilder().addBuilder(index, Metric.getDefaultInstance());
            }

            public List<Metric.Builder> getMetricBuilderList() {
                return this.internalGetMetricFieldBuilder().getBuilderList();
            }

            private RepeatedFieldBuilder<Metric, Metric.Builder, MetricOrBuilder> internalGetMetricFieldBuilder() {
                if (this.metricBuilder_ == null) {
                    this.metricBuilder_ = new RepeatedFieldBuilder(this.metric_, (this.bitField0_ & 8) != 0, this.getParentForChildren(), this.isClean());
                    this.metric_ = null;
                }
                return this.metricBuilder_;
            }

            @Override
            public boolean hasUnit() {
                return (this.bitField0_ & 0x10) != 0;
            }

            @Override
            public String getUnit() {
                Object ref = this.unit_;
                if (!(ref instanceof String)) {
                    ByteString bs = (ByteString)ref;
                    String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        this.unit_ = s;
                    }
                    return s;
                }
                return (String)ref;
            }

            @Override
            public ByteString getUnitBytes() {
                Object ref = this.unit_;
                if (ref instanceof String) {
                    ByteString b = ByteString.copyFromUtf8((String)ref);
                    this.unit_ = b;
                    return b;
                }
                return (ByteString)ref;
            }

            public Builder setUnit(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.unit_ = value;
                this.bitField0_ |= 0x10;
                this.onChanged();
                return this;
            }

            public Builder clearUnit() {
                this.unit_ = MetricFamily.getDefaultInstance().getUnit();
                this.bitField0_ &= 0xFFFFFFEF;
                this.onChanged();
                return this;
            }

            public Builder setUnitBytes(ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.unit_ = value;
                this.bitField0_ |= 0x10;
                this.onChanged();
                return this;
            }
        }
    }

    public static interface MetricFamilyOrBuilder
    extends MessageOrBuilder {
        public boolean hasName();

        public String getName();

        public ByteString getNameBytes();

        public boolean hasHelp();

        public String getHelp();

        public ByteString getHelpBytes();

        public boolean hasType();

        public MetricType getType();

        public List<Metric> getMetricList();

        public Metric getMetric(int var1);

        public int getMetricCount();

        public List<? extends MetricOrBuilder> getMetricOrBuilderList();

        public MetricOrBuilder getMetricOrBuilder(int var1);

        public boolean hasUnit();

        public String getUnit();

        public ByteString getUnitBytes();
    }

    public static final class Metric
    extends GeneratedMessage
    implements MetricOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int LABEL_FIELD_NUMBER = 1;
        private List<LabelPair> label_;
        public static final int GAUGE_FIELD_NUMBER = 2;
        private Gauge gauge_;
        public static final int COUNTER_FIELD_NUMBER = 3;
        private Counter counter_;
        public static final int SUMMARY_FIELD_NUMBER = 4;
        private Summary summary_;
        public static final int UNTYPED_FIELD_NUMBER = 5;
        private Untyped untyped_;
        public static final int HISTOGRAM_FIELD_NUMBER = 7;
        private Histogram histogram_;
        public static final int TIMESTAMP_MS_FIELD_NUMBER = 6;
        private long timestampMs_ = 0L;
        private byte memoizedIsInitialized = (byte)-1;
        private static final Metric DEFAULT_INSTANCE;
        private static final Parser<Metric> PARSER;

        private Metric(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private Metric() {
            this.label_ = Collections.emptyList();
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_Metric_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_Metric_fieldAccessorTable.ensureFieldAccessorsInitialized(Metric.class, Builder.class);
        }

        @Override
        public List<LabelPair> getLabelList() {
            return this.label_;
        }

        @Override
        public List<? extends LabelPairOrBuilder> getLabelOrBuilderList() {
            return this.label_;
        }

        @Override
        public int getLabelCount() {
            return this.label_.size();
        }

        @Override
        public LabelPair getLabel(int index) {
            return this.label_.get(index);
        }

        @Override
        public LabelPairOrBuilder getLabelOrBuilder(int index) {
            return this.label_.get(index);
        }

        @Override
        public boolean hasGauge() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public Gauge getGauge() {
            return this.gauge_ == null ? Gauge.getDefaultInstance() : this.gauge_;
        }

        @Override
        public GaugeOrBuilder getGaugeOrBuilder() {
            return this.gauge_ == null ? Gauge.getDefaultInstance() : this.gauge_;
        }

        @Override
        public boolean hasCounter() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public Counter getCounter() {
            return this.counter_ == null ? Counter.getDefaultInstance() : this.counter_;
        }

        @Override
        public CounterOrBuilder getCounterOrBuilder() {
            return this.counter_ == null ? Counter.getDefaultInstance() : this.counter_;
        }

        @Override
        public boolean hasSummary() {
            return (this.bitField0_ & 4) != 0;
        }

        @Override
        public Summary getSummary() {
            return this.summary_ == null ? Summary.getDefaultInstance() : this.summary_;
        }

        @Override
        public SummaryOrBuilder getSummaryOrBuilder() {
            return this.summary_ == null ? Summary.getDefaultInstance() : this.summary_;
        }

        @Override
        public boolean hasUntyped() {
            return (this.bitField0_ & 8) != 0;
        }

        @Override
        public Untyped getUntyped() {
            return this.untyped_ == null ? Untyped.getDefaultInstance() : this.untyped_;
        }

        @Override
        public UntypedOrBuilder getUntypedOrBuilder() {
            return this.untyped_ == null ? Untyped.getDefaultInstance() : this.untyped_;
        }

        @Override
        public boolean hasHistogram() {
            return (this.bitField0_ & 0x10) != 0;
        }

        @Override
        public Histogram getHistogram() {
            return this.histogram_ == null ? Histogram.getDefaultInstance() : this.histogram_;
        }

        @Override
        public HistogramOrBuilder getHistogramOrBuilder() {
            return this.histogram_ == null ? Histogram.getDefaultInstance() : this.histogram_;
        }

        @Override
        public boolean hasTimestampMs() {
            return (this.bitField0_ & 0x20) != 0;
        }

        @Override
        public long getTimestampMs() {
            return this.timestampMs_;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            for (int i = 0; i < this.label_.size(); ++i) {
                output.writeMessage(1, this.label_.get(i));
            }
            if ((this.bitField0_ & 1) != 0) {
                output.writeMessage(2, this.getGauge());
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeMessage(3, this.getCounter());
            }
            if ((this.bitField0_ & 4) != 0) {
                output.writeMessage(4, this.getSummary());
            }
            if ((this.bitField0_ & 8) != 0) {
                output.writeMessage(5, this.getUntyped());
            }
            if ((this.bitField0_ & 0x20) != 0) {
                output.writeInt64(6, this.timestampMs_);
            }
            if ((this.bitField0_ & 0x10) != 0) {
                output.writeMessage(7, this.getHistogram());
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            for (int i = 0; i < this.label_.size(); ++i) {
                size += CodedOutputStream.computeMessageSize(1, this.label_.get(i));
            }
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputStream.computeMessageSize(2, this.getGauge());
            }
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputStream.computeMessageSize(3, this.getCounter());
            }
            if ((this.bitField0_ & 4) != 0) {
                size += CodedOutputStream.computeMessageSize(4, this.getSummary());
            }
            if ((this.bitField0_ & 8) != 0) {
                size += CodedOutputStream.computeMessageSize(5, this.getUntyped());
            }
            if ((this.bitField0_ & 0x20) != 0) {
                size += CodedOutputStream.computeInt64Size(6, this.timestampMs_);
            }
            if ((this.bitField0_ & 0x10) != 0) {
                size += CodedOutputStream.computeMessageSize(7, this.getHistogram());
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Metric)) {
                return super.equals(obj);
            }
            Metric other = (Metric)obj;
            if (!this.getLabelList().equals(other.getLabelList())) {
                return false;
            }
            if (this.hasGauge() != other.hasGauge()) {
                return false;
            }
            if (this.hasGauge() && !this.getGauge().equals(other.getGauge())) {
                return false;
            }
            if (this.hasCounter() != other.hasCounter()) {
                return false;
            }
            if (this.hasCounter() && !this.getCounter().equals(other.getCounter())) {
                return false;
            }
            if (this.hasSummary() != other.hasSummary()) {
                return false;
            }
            if (this.hasSummary() && !this.getSummary().equals(other.getSummary())) {
                return false;
            }
            if (this.hasUntyped() != other.hasUntyped()) {
                return false;
            }
            if (this.hasUntyped() && !this.getUntyped().equals(other.getUntyped())) {
                return false;
            }
            if (this.hasHistogram() != other.hasHistogram()) {
                return false;
            }
            if (this.hasHistogram() && !this.getHistogram().equals(other.getHistogram())) {
                return false;
            }
            if (this.hasTimestampMs() != other.hasTimestampMs()) {
                return false;
            }
            if (this.hasTimestampMs() && this.getTimestampMs() != other.getTimestampMs()) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + Metric.getDescriptor().hashCode();
            if (this.getLabelCount() > 0) {
                hash = 37 * hash + 1;
                hash = 53 * hash + this.getLabelList().hashCode();
            }
            if (this.hasGauge()) {
                hash = 37 * hash + 2;
                hash = 53 * hash + this.getGauge().hashCode();
            }
            if (this.hasCounter()) {
                hash = 37 * hash + 3;
                hash = 53 * hash + this.getCounter().hashCode();
            }
            if (this.hasSummary()) {
                hash = 37 * hash + 4;
                hash = 53 * hash + this.getSummary().hashCode();
            }
            if (this.hasUntyped()) {
                hash = 37 * hash + 5;
                hash = 53 * hash + this.getUntyped().hashCode();
            }
            if (this.hasHistogram()) {
                hash = 37 * hash + 7;
                hash = 53 * hash + this.getHistogram().hashCode();
            }
            if (this.hasTimestampMs()) {
                hash = 37 * hash + 6;
                hash = 53 * hash + Internal.hashLong(this.getTimestampMs());
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static Metric parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Metric parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Metric parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Metric parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Metric parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Metric parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Metric parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Metric parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Metric parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static Metric parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static Metric parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Metric parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return Metric.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Metric prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static Metric getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Metric> parser() {
            return PARSER;
        }

        public Parser<Metric> getParserForType() {
            return PARSER;
        }

        @Override
        public Metric getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Metric.class.getName());
            DEFAULT_INSTANCE = new Metric();
            PARSER = new AbstractParser<Metric>(){

                @Override
                public Metric parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = Metric.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements MetricOrBuilder {
            private int bitField0_;
            private List<LabelPair> label_ = Collections.emptyList();
            private RepeatedFieldBuilder<LabelPair, LabelPair.Builder, LabelPairOrBuilder> labelBuilder_;
            private Gauge gauge_;
            private SingleFieldBuilder<Gauge, Gauge.Builder, GaugeOrBuilder> gaugeBuilder_;
            private Counter counter_;
            private SingleFieldBuilder<Counter, Counter.Builder, CounterOrBuilder> counterBuilder_;
            private Summary summary_;
            private SingleFieldBuilder<Summary, Summary.Builder, SummaryOrBuilder> summaryBuilder_;
            private Untyped untyped_;
            private SingleFieldBuilder<Untyped, Untyped.Builder, UntypedOrBuilder> untypedBuilder_;
            private Histogram histogram_;
            private SingleFieldBuilder<Histogram, Histogram.Builder, HistogramOrBuilder> histogramBuilder_;
            private long timestampMs_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_Metric_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_Metric_fieldAccessorTable.ensureFieldAccessorsInitialized(Metric.class, Builder.class);
            }

            private Builder() {
                this.maybeForceBuilderInitialization();
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
                this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (alwaysUseFieldBuilders) {
                    this.internalGetLabelFieldBuilder();
                    this.internalGetGaugeFieldBuilder();
                    this.internalGetCounterFieldBuilder();
                    this.internalGetSummaryFieldBuilder();
                    this.internalGetUntypedFieldBuilder();
                    this.internalGetHistogramFieldBuilder();
                }
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                if (this.labelBuilder_ == null) {
                    this.label_ = Collections.emptyList();
                } else {
                    this.label_ = null;
                    this.labelBuilder_.clear();
                }
                this.bitField0_ &= 0xFFFFFFFE;
                this.gauge_ = null;
                if (this.gaugeBuilder_ != null) {
                    this.gaugeBuilder_.dispose();
                    this.gaugeBuilder_ = null;
                }
                this.counter_ = null;
                if (this.counterBuilder_ != null) {
                    this.counterBuilder_.dispose();
                    this.counterBuilder_ = null;
                }
                this.summary_ = null;
                if (this.summaryBuilder_ != null) {
                    this.summaryBuilder_.dispose();
                    this.summaryBuilder_ = null;
                }
                this.untyped_ = null;
                if (this.untypedBuilder_ != null) {
                    this.untypedBuilder_.dispose();
                    this.untypedBuilder_ = null;
                }
                this.histogram_ = null;
                if (this.histogramBuilder_ != null) {
                    this.histogramBuilder_.dispose();
                    this.histogramBuilder_ = null;
                }
                this.timestampMs_ = 0L;
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_Metric_descriptor;
            }

            @Override
            public Metric getDefaultInstanceForType() {
                return Metric.getDefaultInstance();
            }

            @Override
            public Metric build() {
                Metric result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public Metric buildPartial() {
                Metric result = new Metric(this);
                this.buildPartialRepeatedFields(result);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartialRepeatedFields(Metric result) {
                if (this.labelBuilder_ == null) {
                    if ((this.bitField0_ & 1) != 0) {
                        this.label_ = Collections.unmodifiableList(this.label_);
                        this.bitField0_ &= 0xFFFFFFFE;
                    }
                    result.label_ = this.label_;
                } else {
                    result.label_ = this.labelBuilder_.build();
                }
            }

            private void buildPartial0(Metric result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 2) != 0) {
                    result.gauge_ = this.gaugeBuilder_ == null ? this.gauge_ : this.gaugeBuilder_.build();
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 4) != 0) {
                    result.counter_ = this.counterBuilder_ == null ? this.counter_ : this.counterBuilder_.build();
                    to_bitField0_ |= 2;
                }
                if ((from_bitField0_ & 8) != 0) {
                    result.summary_ = this.summaryBuilder_ == null ? this.summary_ : this.summaryBuilder_.build();
                    to_bitField0_ |= 4;
                }
                if ((from_bitField0_ & 0x10) != 0) {
                    result.untyped_ = this.untypedBuilder_ == null ? this.untyped_ : this.untypedBuilder_.build();
                    to_bitField0_ |= 8;
                }
                if ((from_bitField0_ & 0x20) != 0) {
                    result.histogram_ = this.histogramBuilder_ == null ? this.histogram_ : this.histogramBuilder_.build();
                    to_bitField0_ |= 0x10;
                }
                if ((from_bitField0_ & 0x40) != 0) {
                    result.timestampMs_ = this.timestampMs_;
                    to_bitField0_ |= 0x20;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof Metric) {
                    return this.mergeFrom((Metric)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(Metric other) {
                if (other == Metric.getDefaultInstance()) {
                    return this;
                }
                if (this.labelBuilder_ == null) {
                    if (!other.label_.isEmpty()) {
                        if (this.label_.isEmpty()) {
                            this.label_ = other.label_;
                            this.bitField0_ &= 0xFFFFFFFE;
                        } else {
                            this.ensureLabelIsMutable();
                            this.label_.addAll(other.label_);
                        }
                        this.onChanged();
                    }
                } else if (!other.label_.isEmpty()) {
                    if (this.labelBuilder_.isEmpty()) {
                        this.labelBuilder_.dispose();
                        this.labelBuilder_ = null;
                        this.label_ = other.label_;
                        this.bitField0_ &= 0xFFFFFFFE;
                        this.labelBuilder_ = alwaysUseFieldBuilders ? this.internalGetLabelFieldBuilder() : null;
                    } else {
                        this.labelBuilder_.addAllMessages(other.label_);
                    }
                }
                if (other.hasGauge()) {
                    this.mergeGauge(other.getGauge());
                }
                if (other.hasCounter()) {
                    this.mergeCounter(other.getCounter());
                }
                if (other.hasSummary()) {
                    this.mergeSummary(other.getSummary());
                }
                if (other.hasUntyped()) {
                    this.mergeUntyped(other.getUntyped());
                }
                if (other.hasHistogram()) {
                    this.mergeHistogram(other.getHistogram());
                }
                if (other.hasTimestampMs()) {
                    this.setTimestampMs(other.getTimestampMs());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block15: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block15;
                            }
                            case 10: {
                                LabelPair m = input.readMessage(LabelPair.parser(), extensionRegistry);
                                if (this.labelBuilder_ == null) {
                                    this.ensureLabelIsMutable();
                                    this.label_.add(m);
                                    continue block15;
                                }
                                this.labelBuilder_.addMessage(m);
                                continue block15;
                            }
                            case 18: {
                                input.readMessage(this.internalGetGaugeFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 2;
                                continue block15;
                            }
                            case 26: {
                                input.readMessage(this.internalGetCounterFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 4;
                                continue block15;
                            }
                            case 34: {
                                input.readMessage(this.internalGetSummaryFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 8;
                                continue block15;
                            }
                            case 42: {
                                input.readMessage(this.internalGetUntypedFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 0x10;
                                continue block15;
                            }
                            case 48: {
                                this.timestampMs_ = input.readInt64();
                                this.bitField0_ |= 0x40;
                                continue block15;
                            }
                            case 58: {
                                input.readMessage(this.internalGetHistogramFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 0x20;
                                continue block15;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            private void ensureLabelIsMutable() {
                if ((this.bitField0_ & 1) == 0) {
                    this.label_ = new ArrayList<LabelPair>(this.label_);
                    this.bitField0_ |= 1;
                }
            }

            @Override
            public List<LabelPair> getLabelList() {
                if (this.labelBuilder_ == null) {
                    return Collections.unmodifiableList(this.label_);
                }
                return this.labelBuilder_.getMessageList();
            }

            @Override
            public int getLabelCount() {
                if (this.labelBuilder_ == null) {
                    return this.label_.size();
                }
                return this.labelBuilder_.getCount();
            }

            @Override
            public LabelPair getLabel(int index) {
                if (this.labelBuilder_ == null) {
                    return this.label_.get(index);
                }
                return this.labelBuilder_.getMessage(index);
            }

            public Builder setLabel(int index, LabelPair value) {
                if (this.labelBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureLabelIsMutable();
                    this.label_.set(index, value);
                    this.onChanged();
                } else {
                    this.labelBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setLabel(int index, LabelPair.Builder builderForValue) {
                if (this.labelBuilder_ == null) {
                    this.ensureLabelIsMutable();
                    this.label_.set(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.labelBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addLabel(LabelPair value) {
                if (this.labelBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureLabelIsMutable();
                    this.label_.add(value);
                    this.onChanged();
                } else {
                    this.labelBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addLabel(int index, LabelPair value) {
                if (this.labelBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureLabelIsMutable();
                    this.label_.add(index, value);
                    this.onChanged();
                } else {
                    this.labelBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addLabel(LabelPair.Builder builderForValue) {
                if (this.labelBuilder_ == null) {
                    this.ensureLabelIsMutable();
                    this.label_.add(builderForValue.build());
                    this.onChanged();
                } else {
                    this.labelBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addLabel(int index, LabelPair.Builder builderForValue) {
                if (this.labelBuilder_ == null) {
                    this.ensureLabelIsMutable();
                    this.label_.add(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.labelBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllLabel(Iterable<? extends LabelPair> values2) {
                if (this.labelBuilder_ == null) {
                    this.ensureLabelIsMutable();
                    AbstractMessageLite.Builder.addAll(values2, this.label_);
                    this.onChanged();
                } else {
                    this.labelBuilder_.addAllMessages(values2);
                }
                return this;
            }

            public Builder clearLabel() {
                if (this.labelBuilder_ == null) {
                    this.label_ = Collections.emptyList();
                    this.bitField0_ &= 0xFFFFFFFE;
                    this.onChanged();
                } else {
                    this.labelBuilder_.clear();
                }
                return this;
            }

            public Builder removeLabel(int index) {
                if (this.labelBuilder_ == null) {
                    this.ensureLabelIsMutable();
                    this.label_.remove(index);
                    this.onChanged();
                } else {
                    this.labelBuilder_.remove(index);
                }
                return this;
            }

            public LabelPair.Builder getLabelBuilder(int index) {
                return this.internalGetLabelFieldBuilder().getBuilder(index);
            }

            @Override
            public LabelPairOrBuilder getLabelOrBuilder(int index) {
                if (this.labelBuilder_ == null) {
                    return this.label_.get(index);
                }
                return this.labelBuilder_.getMessageOrBuilder(index);
            }

            @Override
            public List<? extends LabelPairOrBuilder> getLabelOrBuilderList() {
                if (this.labelBuilder_ != null) {
                    return this.labelBuilder_.getMessageOrBuilderList();
                }
                return Collections.unmodifiableList(this.label_);
            }

            public LabelPair.Builder addLabelBuilder() {
                return this.internalGetLabelFieldBuilder().addBuilder(LabelPair.getDefaultInstance());
            }

            public LabelPair.Builder addLabelBuilder(int index) {
                return this.internalGetLabelFieldBuilder().addBuilder(index, LabelPair.getDefaultInstance());
            }

            public List<LabelPair.Builder> getLabelBuilderList() {
                return this.internalGetLabelFieldBuilder().getBuilderList();
            }

            private RepeatedFieldBuilder<LabelPair, LabelPair.Builder, LabelPairOrBuilder> internalGetLabelFieldBuilder() {
                if (this.labelBuilder_ == null) {
                    this.labelBuilder_ = new RepeatedFieldBuilder(this.label_, (this.bitField0_ & 1) != 0, this.getParentForChildren(), this.isClean());
                    this.label_ = null;
                }
                return this.labelBuilder_;
            }

            @Override
            public boolean hasGauge() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public Gauge getGauge() {
                if (this.gaugeBuilder_ == null) {
                    return this.gauge_ == null ? Gauge.getDefaultInstance() : this.gauge_;
                }
                return this.gaugeBuilder_.getMessage();
            }

            public Builder setGauge(Gauge value) {
                if (this.gaugeBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.gauge_ = value;
                } else {
                    this.gaugeBuilder_.setMessage(value);
                }
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder setGauge(Gauge.Builder builderForValue) {
                if (this.gaugeBuilder_ == null) {
                    this.gauge_ = builderForValue.build();
                } else {
                    this.gaugeBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder mergeGauge(Gauge value) {
                if (this.gaugeBuilder_ == null) {
                    if ((this.bitField0_ & 2) != 0 && this.gauge_ != null && this.gauge_ != Gauge.getDefaultInstance()) {
                        this.getGaugeBuilder().mergeFrom(value);
                    } else {
                        this.gauge_ = value;
                    }
                } else {
                    this.gaugeBuilder_.mergeFrom(value);
                }
                if (this.gauge_ != null) {
                    this.bitField0_ |= 2;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearGauge() {
                this.bitField0_ &= 0xFFFFFFFD;
                this.gauge_ = null;
                if (this.gaugeBuilder_ != null) {
                    this.gaugeBuilder_.dispose();
                    this.gaugeBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Gauge.Builder getGaugeBuilder() {
                this.bitField0_ |= 2;
                this.onChanged();
                return this.internalGetGaugeFieldBuilder().getBuilder();
            }

            @Override
            public GaugeOrBuilder getGaugeOrBuilder() {
                if (this.gaugeBuilder_ != null) {
                    return this.gaugeBuilder_.getMessageOrBuilder();
                }
                return this.gauge_ == null ? Gauge.getDefaultInstance() : this.gauge_;
            }

            private SingleFieldBuilder<Gauge, Gauge.Builder, GaugeOrBuilder> internalGetGaugeFieldBuilder() {
                if (this.gaugeBuilder_ == null) {
                    this.gaugeBuilder_ = new SingleFieldBuilder(this.getGauge(), this.getParentForChildren(), this.isClean());
                    this.gauge_ = null;
                }
                return this.gaugeBuilder_;
            }

            @Override
            public boolean hasCounter() {
                return (this.bitField0_ & 4) != 0;
            }

            @Override
            public Counter getCounter() {
                if (this.counterBuilder_ == null) {
                    return this.counter_ == null ? Counter.getDefaultInstance() : this.counter_;
                }
                return this.counterBuilder_.getMessage();
            }

            public Builder setCounter(Counter value) {
                if (this.counterBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.counter_ = value;
                } else {
                    this.counterBuilder_.setMessage(value);
                }
                this.bitField0_ |= 4;
                this.onChanged();
                return this;
            }

            public Builder setCounter(Counter.Builder builderForValue) {
                if (this.counterBuilder_ == null) {
                    this.counter_ = builderForValue.build();
                } else {
                    this.counterBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 4;
                this.onChanged();
                return this;
            }

            public Builder mergeCounter(Counter value) {
                if (this.counterBuilder_ == null) {
                    if ((this.bitField0_ & 4) != 0 && this.counter_ != null && this.counter_ != Counter.getDefaultInstance()) {
                        this.getCounterBuilder().mergeFrom(value);
                    } else {
                        this.counter_ = value;
                    }
                } else {
                    this.counterBuilder_.mergeFrom(value);
                }
                if (this.counter_ != null) {
                    this.bitField0_ |= 4;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearCounter() {
                this.bitField0_ &= 0xFFFFFFFB;
                this.counter_ = null;
                if (this.counterBuilder_ != null) {
                    this.counterBuilder_.dispose();
                    this.counterBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Counter.Builder getCounterBuilder() {
                this.bitField0_ |= 4;
                this.onChanged();
                return this.internalGetCounterFieldBuilder().getBuilder();
            }

            @Override
            public CounterOrBuilder getCounterOrBuilder() {
                if (this.counterBuilder_ != null) {
                    return this.counterBuilder_.getMessageOrBuilder();
                }
                return this.counter_ == null ? Counter.getDefaultInstance() : this.counter_;
            }

            private SingleFieldBuilder<Counter, Counter.Builder, CounterOrBuilder> internalGetCounterFieldBuilder() {
                if (this.counterBuilder_ == null) {
                    this.counterBuilder_ = new SingleFieldBuilder(this.getCounter(), this.getParentForChildren(), this.isClean());
                    this.counter_ = null;
                }
                return this.counterBuilder_;
            }

            @Override
            public boolean hasSummary() {
                return (this.bitField0_ & 8) != 0;
            }

            @Override
            public Summary getSummary() {
                if (this.summaryBuilder_ == null) {
                    return this.summary_ == null ? Summary.getDefaultInstance() : this.summary_;
                }
                return this.summaryBuilder_.getMessage();
            }

            public Builder setSummary(Summary value) {
                if (this.summaryBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.summary_ = value;
                } else {
                    this.summaryBuilder_.setMessage(value);
                }
                this.bitField0_ |= 8;
                this.onChanged();
                return this;
            }

            public Builder setSummary(Summary.Builder builderForValue) {
                if (this.summaryBuilder_ == null) {
                    this.summary_ = builderForValue.build();
                } else {
                    this.summaryBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 8;
                this.onChanged();
                return this;
            }

            public Builder mergeSummary(Summary value) {
                if (this.summaryBuilder_ == null) {
                    if ((this.bitField0_ & 8) != 0 && this.summary_ != null && this.summary_ != Summary.getDefaultInstance()) {
                        this.getSummaryBuilder().mergeFrom(value);
                    } else {
                        this.summary_ = value;
                    }
                } else {
                    this.summaryBuilder_.mergeFrom(value);
                }
                if (this.summary_ != null) {
                    this.bitField0_ |= 8;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearSummary() {
                this.bitField0_ &= 0xFFFFFFF7;
                this.summary_ = null;
                if (this.summaryBuilder_ != null) {
                    this.summaryBuilder_.dispose();
                    this.summaryBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Summary.Builder getSummaryBuilder() {
                this.bitField0_ |= 8;
                this.onChanged();
                return this.internalGetSummaryFieldBuilder().getBuilder();
            }

            @Override
            public SummaryOrBuilder getSummaryOrBuilder() {
                if (this.summaryBuilder_ != null) {
                    return this.summaryBuilder_.getMessageOrBuilder();
                }
                return this.summary_ == null ? Summary.getDefaultInstance() : this.summary_;
            }

            private SingleFieldBuilder<Summary, Summary.Builder, SummaryOrBuilder> internalGetSummaryFieldBuilder() {
                if (this.summaryBuilder_ == null) {
                    this.summaryBuilder_ = new SingleFieldBuilder(this.getSummary(), this.getParentForChildren(), this.isClean());
                    this.summary_ = null;
                }
                return this.summaryBuilder_;
            }

            @Override
            public boolean hasUntyped() {
                return (this.bitField0_ & 0x10) != 0;
            }

            @Override
            public Untyped getUntyped() {
                if (this.untypedBuilder_ == null) {
                    return this.untyped_ == null ? Untyped.getDefaultInstance() : this.untyped_;
                }
                return this.untypedBuilder_.getMessage();
            }

            public Builder setUntyped(Untyped value) {
                if (this.untypedBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.untyped_ = value;
                } else {
                    this.untypedBuilder_.setMessage(value);
                }
                this.bitField0_ |= 0x10;
                this.onChanged();
                return this;
            }

            public Builder setUntyped(Untyped.Builder builderForValue) {
                if (this.untypedBuilder_ == null) {
                    this.untyped_ = builderForValue.build();
                } else {
                    this.untypedBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 0x10;
                this.onChanged();
                return this;
            }

            public Builder mergeUntyped(Untyped value) {
                if (this.untypedBuilder_ == null) {
                    if ((this.bitField0_ & 0x10) != 0 && this.untyped_ != null && this.untyped_ != Untyped.getDefaultInstance()) {
                        this.getUntypedBuilder().mergeFrom(value);
                    } else {
                        this.untyped_ = value;
                    }
                } else {
                    this.untypedBuilder_.mergeFrom(value);
                }
                if (this.untyped_ != null) {
                    this.bitField0_ |= 0x10;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearUntyped() {
                this.bitField0_ &= 0xFFFFFFEF;
                this.untyped_ = null;
                if (this.untypedBuilder_ != null) {
                    this.untypedBuilder_.dispose();
                    this.untypedBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Untyped.Builder getUntypedBuilder() {
                this.bitField0_ |= 0x10;
                this.onChanged();
                return this.internalGetUntypedFieldBuilder().getBuilder();
            }

            @Override
            public UntypedOrBuilder getUntypedOrBuilder() {
                if (this.untypedBuilder_ != null) {
                    return this.untypedBuilder_.getMessageOrBuilder();
                }
                return this.untyped_ == null ? Untyped.getDefaultInstance() : this.untyped_;
            }

            private SingleFieldBuilder<Untyped, Untyped.Builder, UntypedOrBuilder> internalGetUntypedFieldBuilder() {
                if (this.untypedBuilder_ == null) {
                    this.untypedBuilder_ = new SingleFieldBuilder(this.getUntyped(), this.getParentForChildren(), this.isClean());
                    this.untyped_ = null;
                }
                return this.untypedBuilder_;
            }

            @Override
            public boolean hasHistogram() {
                return (this.bitField0_ & 0x20) != 0;
            }

            @Override
            public Histogram getHistogram() {
                if (this.histogramBuilder_ == null) {
                    return this.histogram_ == null ? Histogram.getDefaultInstance() : this.histogram_;
                }
                return this.histogramBuilder_.getMessage();
            }

            public Builder setHistogram(Histogram value) {
                if (this.histogramBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.histogram_ = value;
                } else {
                    this.histogramBuilder_.setMessage(value);
                }
                this.bitField0_ |= 0x20;
                this.onChanged();
                return this;
            }

            public Builder setHistogram(Histogram.Builder builderForValue) {
                if (this.histogramBuilder_ == null) {
                    this.histogram_ = builderForValue.build();
                } else {
                    this.histogramBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 0x20;
                this.onChanged();
                return this;
            }

            public Builder mergeHistogram(Histogram value) {
                if (this.histogramBuilder_ == null) {
                    if ((this.bitField0_ & 0x20) != 0 && this.histogram_ != null && this.histogram_ != Histogram.getDefaultInstance()) {
                        this.getHistogramBuilder().mergeFrom(value);
                    } else {
                        this.histogram_ = value;
                    }
                } else {
                    this.histogramBuilder_.mergeFrom(value);
                }
                if (this.histogram_ != null) {
                    this.bitField0_ |= 0x20;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearHistogram() {
                this.bitField0_ &= 0xFFFFFFDF;
                this.histogram_ = null;
                if (this.histogramBuilder_ != null) {
                    this.histogramBuilder_.dispose();
                    this.histogramBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Histogram.Builder getHistogramBuilder() {
                this.bitField0_ |= 0x20;
                this.onChanged();
                return this.internalGetHistogramFieldBuilder().getBuilder();
            }

            @Override
            public HistogramOrBuilder getHistogramOrBuilder() {
                if (this.histogramBuilder_ != null) {
                    return this.histogramBuilder_.getMessageOrBuilder();
                }
                return this.histogram_ == null ? Histogram.getDefaultInstance() : this.histogram_;
            }

            private SingleFieldBuilder<Histogram, Histogram.Builder, HistogramOrBuilder> internalGetHistogramFieldBuilder() {
                if (this.histogramBuilder_ == null) {
                    this.histogramBuilder_ = new SingleFieldBuilder(this.getHistogram(), this.getParentForChildren(), this.isClean());
                    this.histogram_ = null;
                }
                return this.histogramBuilder_;
            }

            @Override
            public boolean hasTimestampMs() {
                return (this.bitField0_ & 0x40) != 0;
            }

            @Override
            public long getTimestampMs() {
                return this.timestampMs_;
            }

            public Builder setTimestampMs(long value) {
                this.timestampMs_ = value;
                this.bitField0_ |= 0x40;
                this.onChanged();
                return this;
            }

            public Builder clearTimestampMs() {
                this.bitField0_ &= 0xFFFFFFBF;
                this.timestampMs_ = 0L;
                this.onChanged();
                return this;
            }
        }
    }

    public static interface MetricOrBuilder
    extends MessageOrBuilder {
        public List<LabelPair> getLabelList();

        public LabelPair getLabel(int var1);

        public int getLabelCount();

        public List<? extends LabelPairOrBuilder> getLabelOrBuilderList();

        public LabelPairOrBuilder getLabelOrBuilder(int var1);

        public boolean hasGauge();

        public Gauge getGauge();

        public GaugeOrBuilder getGaugeOrBuilder();

        public boolean hasCounter();

        public Counter getCounter();

        public CounterOrBuilder getCounterOrBuilder();

        public boolean hasSummary();

        public Summary getSummary();

        public SummaryOrBuilder getSummaryOrBuilder();

        public boolean hasUntyped();

        public Untyped getUntyped();

        public UntypedOrBuilder getUntypedOrBuilder();

        public boolean hasHistogram();

        public Histogram getHistogram();

        public HistogramOrBuilder getHistogramOrBuilder();

        public boolean hasTimestampMs();

        public long getTimestampMs();
    }

    public static final class Exemplar
    extends GeneratedMessage
    implements ExemplarOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int LABEL_FIELD_NUMBER = 1;
        private List<LabelPair> label_;
        public static final int VALUE_FIELD_NUMBER = 2;
        private double value_ = 0.0;
        public static final int TIMESTAMP_FIELD_NUMBER = 3;
        private Timestamp timestamp_;
        private byte memoizedIsInitialized = (byte)-1;
        private static final Exemplar DEFAULT_INSTANCE;
        private static final Parser<Exemplar> PARSER;

        private Exemplar(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private Exemplar() {
            this.label_ = Collections.emptyList();
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_Exemplar_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_Exemplar_fieldAccessorTable.ensureFieldAccessorsInitialized(Exemplar.class, Builder.class);
        }

        @Override
        public List<LabelPair> getLabelList() {
            return this.label_;
        }

        @Override
        public List<? extends LabelPairOrBuilder> getLabelOrBuilderList() {
            return this.label_;
        }

        @Override
        public int getLabelCount() {
            return this.label_.size();
        }

        @Override
        public LabelPair getLabel(int index) {
            return this.label_.get(index);
        }

        @Override
        public LabelPairOrBuilder getLabelOrBuilder(int index) {
            return this.label_.get(index);
        }

        @Override
        public boolean hasValue() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public double getValue() {
            return this.value_;
        }

        @Override
        public boolean hasTimestamp() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public Timestamp getTimestamp() {
            return this.timestamp_ == null ? Timestamp.getDefaultInstance() : this.timestamp_;
        }

        @Override
        public TimestampOrBuilder getTimestampOrBuilder() {
            return this.timestamp_ == null ? Timestamp.getDefaultInstance() : this.timestamp_;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            for (int i = 0; i < this.label_.size(); ++i) {
                output.writeMessage(1, this.label_.get(i));
            }
            if ((this.bitField0_ & 1) != 0) {
                output.writeDouble(2, this.value_);
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeMessage(3, this.getTimestamp());
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            for (int i = 0; i < this.label_.size(); ++i) {
                size += CodedOutputStream.computeMessageSize(1, this.label_.get(i));
            }
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputStream.computeDoubleSize(2, this.value_);
            }
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputStream.computeMessageSize(3, this.getTimestamp());
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Exemplar)) {
                return super.equals(obj);
            }
            Exemplar other = (Exemplar)obj;
            if (!this.getLabelList().equals(other.getLabelList())) {
                return false;
            }
            if (this.hasValue() != other.hasValue()) {
                return false;
            }
            if (this.hasValue() && Double.doubleToLongBits(this.getValue()) != Double.doubleToLongBits(other.getValue())) {
                return false;
            }
            if (this.hasTimestamp() != other.hasTimestamp()) {
                return false;
            }
            if (this.hasTimestamp() && !this.getTimestamp().equals(other.getTimestamp())) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + Exemplar.getDescriptor().hashCode();
            if (this.getLabelCount() > 0) {
                hash = 37 * hash + 1;
                hash = 53 * hash + this.getLabelList().hashCode();
            }
            if (this.hasValue()) {
                hash = 37 * hash + 2;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getValue()));
            }
            if (this.hasTimestamp()) {
                hash = 37 * hash + 3;
                hash = 53 * hash + this.getTimestamp().hashCode();
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static Exemplar parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Exemplar parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Exemplar parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Exemplar parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Exemplar parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Exemplar parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Exemplar parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Exemplar parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Exemplar parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static Exemplar parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static Exemplar parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Exemplar parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return Exemplar.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Exemplar prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static Exemplar getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Exemplar> parser() {
            return PARSER;
        }

        public Parser<Exemplar> getParserForType() {
            return PARSER;
        }

        @Override
        public Exemplar getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Exemplar.class.getName());
            DEFAULT_INSTANCE = new Exemplar();
            PARSER = new AbstractParser<Exemplar>(){

                @Override
                public Exemplar parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = Exemplar.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements ExemplarOrBuilder {
            private int bitField0_;
            private List<LabelPair> label_ = Collections.emptyList();
            private RepeatedFieldBuilder<LabelPair, LabelPair.Builder, LabelPairOrBuilder> labelBuilder_;
            private double value_;
            private Timestamp timestamp_;
            private SingleFieldBuilder<Timestamp, Timestamp.Builder, TimestampOrBuilder> timestampBuilder_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_Exemplar_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_Exemplar_fieldAccessorTable.ensureFieldAccessorsInitialized(Exemplar.class, Builder.class);
            }

            private Builder() {
                this.maybeForceBuilderInitialization();
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
                this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (alwaysUseFieldBuilders) {
                    this.internalGetLabelFieldBuilder();
                    this.internalGetTimestampFieldBuilder();
                }
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                if (this.labelBuilder_ == null) {
                    this.label_ = Collections.emptyList();
                } else {
                    this.label_ = null;
                    this.labelBuilder_.clear();
                }
                this.bitField0_ &= 0xFFFFFFFE;
                this.value_ = 0.0;
                this.timestamp_ = null;
                if (this.timestampBuilder_ != null) {
                    this.timestampBuilder_.dispose();
                    this.timestampBuilder_ = null;
                }
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_Exemplar_descriptor;
            }

            @Override
            public Exemplar getDefaultInstanceForType() {
                return Exemplar.getDefaultInstance();
            }

            @Override
            public Exemplar build() {
                Exemplar result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public Exemplar buildPartial() {
                Exemplar result = new Exemplar(this);
                this.buildPartialRepeatedFields(result);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartialRepeatedFields(Exemplar result) {
                if (this.labelBuilder_ == null) {
                    if ((this.bitField0_ & 1) != 0) {
                        this.label_ = Collections.unmodifiableList(this.label_);
                        this.bitField0_ &= 0xFFFFFFFE;
                    }
                    result.label_ = this.label_;
                } else {
                    result.label_ = this.labelBuilder_.build();
                }
            }

            private void buildPartial0(Exemplar result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 2) != 0) {
                    result.value_ = this.value_;
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 4) != 0) {
                    result.timestamp_ = this.timestampBuilder_ == null ? this.timestamp_ : this.timestampBuilder_.build();
                    to_bitField0_ |= 2;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof Exemplar) {
                    return this.mergeFrom((Exemplar)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(Exemplar other) {
                if (other == Exemplar.getDefaultInstance()) {
                    return this;
                }
                if (this.labelBuilder_ == null) {
                    if (!other.label_.isEmpty()) {
                        if (this.label_.isEmpty()) {
                            this.label_ = other.label_;
                            this.bitField0_ &= 0xFFFFFFFE;
                        } else {
                            this.ensureLabelIsMutable();
                            this.label_.addAll(other.label_);
                        }
                        this.onChanged();
                    }
                } else if (!other.label_.isEmpty()) {
                    if (this.labelBuilder_.isEmpty()) {
                        this.labelBuilder_.dispose();
                        this.labelBuilder_ = null;
                        this.label_ = other.label_;
                        this.bitField0_ &= 0xFFFFFFFE;
                        this.labelBuilder_ = alwaysUseFieldBuilders ? this.internalGetLabelFieldBuilder() : null;
                    } else {
                        this.labelBuilder_.addAllMessages(other.label_);
                    }
                }
                if (other.hasValue()) {
                    this.setValue(other.getValue());
                }
                if (other.hasTimestamp()) {
                    this.mergeTimestamp(other.getTimestamp());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block11: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block11;
                            }
                            case 10: {
                                LabelPair m = input.readMessage(LabelPair.parser(), extensionRegistry);
                                if (this.labelBuilder_ == null) {
                                    this.ensureLabelIsMutable();
                                    this.label_.add(m);
                                    continue block11;
                                }
                                this.labelBuilder_.addMessage(m);
                                continue block11;
                            }
                            case 17: {
                                this.value_ = input.readDouble();
                                this.bitField0_ |= 2;
                                continue block11;
                            }
                            case 26: {
                                input.readMessage(this.internalGetTimestampFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 4;
                                continue block11;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            private void ensureLabelIsMutable() {
                if ((this.bitField0_ & 1) == 0) {
                    this.label_ = new ArrayList<LabelPair>(this.label_);
                    this.bitField0_ |= 1;
                }
            }

            @Override
            public List<LabelPair> getLabelList() {
                if (this.labelBuilder_ == null) {
                    return Collections.unmodifiableList(this.label_);
                }
                return this.labelBuilder_.getMessageList();
            }

            @Override
            public int getLabelCount() {
                if (this.labelBuilder_ == null) {
                    return this.label_.size();
                }
                return this.labelBuilder_.getCount();
            }

            @Override
            public LabelPair getLabel(int index) {
                if (this.labelBuilder_ == null) {
                    return this.label_.get(index);
                }
                return this.labelBuilder_.getMessage(index);
            }

            public Builder setLabel(int index, LabelPair value) {
                if (this.labelBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureLabelIsMutable();
                    this.label_.set(index, value);
                    this.onChanged();
                } else {
                    this.labelBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setLabel(int index, LabelPair.Builder builderForValue) {
                if (this.labelBuilder_ == null) {
                    this.ensureLabelIsMutable();
                    this.label_.set(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.labelBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addLabel(LabelPair value) {
                if (this.labelBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureLabelIsMutable();
                    this.label_.add(value);
                    this.onChanged();
                } else {
                    this.labelBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addLabel(int index, LabelPair value) {
                if (this.labelBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureLabelIsMutable();
                    this.label_.add(index, value);
                    this.onChanged();
                } else {
                    this.labelBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addLabel(LabelPair.Builder builderForValue) {
                if (this.labelBuilder_ == null) {
                    this.ensureLabelIsMutable();
                    this.label_.add(builderForValue.build());
                    this.onChanged();
                } else {
                    this.labelBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addLabel(int index, LabelPair.Builder builderForValue) {
                if (this.labelBuilder_ == null) {
                    this.ensureLabelIsMutable();
                    this.label_.add(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.labelBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllLabel(Iterable<? extends LabelPair> values2) {
                if (this.labelBuilder_ == null) {
                    this.ensureLabelIsMutable();
                    AbstractMessageLite.Builder.addAll(values2, this.label_);
                    this.onChanged();
                } else {
                    this.labelBuilder_.addAllMessages(values2);
                }
                return this;
            }

            public Builder clearLabel() {
                if (this.labelBuilder_ == null) {
                    this.label_ = Collections.emptyList();
                    this.bitField0_ &= 0xFFFFFFFE;
                    this.onChanged();
                } else {
                    this.labelBuilder_.clear();
                }
                return this;
            }

            public Builder removeLabel(int index) {
                if (this.labelBuilder_ == null) {
                    this.ensureLabelIsMutable();
                    this.label_.remove(index);
                    this.onChanged();
                } else {
                    this.labelBuilder_.remove(index);
                }
                return this;
            }

            public LabelPair.Builder getLabelBuilder(int index) {
                return this.internalGetLabelFieldBuilder().getBuilder(index);
            }

            @Override
            public LabelPairOrBuilder getLabelOrBuilder(int index) {
                if (this.labelBuilder_ == null) {
                    return this.label_.get(index);
                }
                return this.labelBuilder_.getMessageOrBuilder(index);
            }

            @Override
            public List<? extends LabelPairOrBuilder> getLabelOrBuilderList() {
                if (this.labelBuilder_ != null) {
                    return this.labelBuilder_.getMessageOrBuilderList();
                }
                return Collections.unmodifiableList(this.label_);
            }

            public LabelPair.Builder addLabelBuilder() {
                return this.internalGetLabelFieldBuilder().addBuilder(LabelPair.getDefaultInstance());
            }

            public LabelPair.Builder addLabelBuilder(int index) {
                return this.internalGetLabelFieldBuilder().addBuilder(index, LabelPair.getDefaultInstance());
            }

            public List<LabelPair.Builder> getLabelBuilderList() {
                return this.internalGetLabelFieldBuilder().getBuilderList();
            }

            private RepeatedFieldBuilder<LabelPair, LabelPair.Builder, LabelPairOrBuilder> internalGetLabelFieldBuilder() {
                if (this.labelBuilder_ == null) {
                    this.labelBuilder_ = new RepeatedFieldBuilder(this.label_, (this.bitField0_ & 1) != 0, this.getParentForChildren(), this.isClean());
                    this.label_ = null;
                }
                return this.labelBuilder_;
            }

            @Override
            public boolean hasValue() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public double getValue() {
                return this.value_;
            }

            public Builder setValue(double value) {
                this.value_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder clearValue() {
                this.bitField0_ &= 0xFFFFFFFD;
                this.value_ = 0.0;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasTimestamp() {
                return (this.bitField0_ & 4) != 0;
            }

            @Override
            public Timestamp getTimestamp() {
                if (this.timestampBuilder_ == null) {
                    return this.timestamp_ == null ? Timestamp.getDefaultInstance() : this.timestamp_;
                }
                return this.timestampBuilder_.getMessage();
            }

            public Builder setTimestamp(Timestamp value) {
                if (this.timestampBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.timestamp_ = value;
                } else {
                    this.timestampBuilder_.setMessage(value);
                }
                this.bitField0_ |= 4;
                this.onChanged();
                return this;
            }

            public Builder setTimestamp(Timestamp.Builder builderForValue) {
                if (this.timestampBuilder_ == null) {
                    this.timestamp_ = builderForValue.build();
                } else {
                    this.timestampBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 4;
                this.onChanged();
                return this;
            }

            public Builder mergeTimestamp(Timestamp value) {
                if (this.timestampBuilder_ == null) {
                    if ((this.bitField0_ & 4) != 0 && this.timestamp_ != null && this.timestamp_ != Timestamp.getDefaultInstance()) {
                        this.getTimestampBuilder().mergeFrom(value);
                    } else {
                        this.timestamp_ = value;
                    }
                } else {
                    this.timestampBuilder_.mergeFrom(value);
                }
                if (this.timestamp_ != null) {
                    this.bitField0_ |= 4;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearTimestamp() {
                this.bitField0_ &= 0xFFFFFFFB;
                this.timestamp_ = null;
                if (this.timestampBuilder_ != null) {
                    this.timestampBuilder_.dispose();
                    this.timestampBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Timestamp.Builder getTimestampBuilder() {
                this.bitField0_ |= 4;
                this.onChanged();
                return this.internalGetTimestampFieldBuilder().getBuilder();
            }

            @Override
            public TimestampOrBuilder getTimestampOrBuilder() {
                if (this.timestampBuilder_ != null) {
                    return this.timestampBuilder_.getMessageOrBuilder();
                }
                return this.timestamp_ == null ? Timestamp.getDefaultInstance() : this.timestamp_;
            }

            private SingleFieldBuilder<Timestamp, Timestamp.Builder, TimestampOrBuilder> internalGetTimestampFieldBuilder() {
                if (this.timestampBuilder_ == null) {
                    this.timestampBuilder_ = new SingleFieldBuilder(this.getTimestamp(), this.getParentForChildren(), this.isClean());
                    this.timestamp_ = null;
                }
                return this.timestampBuilder_;
            }
        }
    }

    public static interface ExemplarOrBuilder
    extends MessageOrBuilder {
        public List<LabelPair> getLabelList();

        public LabelPair getLabel(int var1);

        public int getLabelCount();

        public List<? extends LabelPairOrBuilder> getLabelOrBuilderList();

        public LabelPairOrBuilder getLabelOrBuilder(int var1);

        public boolean hasValue();

        public double getValue();

        public boolean hasTimestamp();

        public Timestamp getTimestamp();

        public TimestampOrBuilder getTimestampOrBuilder();
    }

    public static final class BucketSpan
    extends GeneratedMessage
    implements BucketSpanOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int OFFSET_FIELD_NUMBER = 1;
        private int offset_ = 0;
        public static final int LENGTH_FIELD_NUMBER = 2;
        private int length_ = 0;
        private byte memoizedIsInitialized = (byte)-1;
        private static final BucketSpan DEFAULT_INSTANCE;
        private static final Parser<BucketSpan> PARSER;

        private BucketSpan(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private BucketSpan() {
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_BucketSpan_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_BucketSpan_fieldAccessorTable.ensureFieldAccessorsInitialized(BucketSpan.class, Builder.class);
        }

        @Override
        public boolean hasOffset() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public int getOffset() {
            return this.offset_;
        }

        @Override
        public boolean hasLength() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public int getLength() {
            return this.length_;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                output.writeSInt32(1, this.offset_);
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeUInt32(2, this.length_);
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputStream.computeSInt32Size(1, this.offset_);
            }
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputStream.computeUInt32Size(2, this.length_);
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof BucketSpan)) {
                return super.equals(obj);
            }
            BucketSpan other = (BucketSpan)obj;
            if (this.hasOffset() != other.hasOffset()) {
                return false;
            }
            if (this.hasOffset() && this.getOffset() != other.getOffset()) {
                return false;
            }
            if (this.hasLength() != other.hasLength()) {
                return false;
            }
            if (this.hasLength() && this.getLength() != other.getLength()) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + BucketSpan.getDescriptor().hashCode();
            if (this.hasOffset()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + this.getOffset();
            }
            if (this.hasLength()) {
                hash = 37 * hash + 2;
                hash = 53 * hash + this.getLength();
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static BucketSpan parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static BucketSpan parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static BucketSpan parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static BucketSpan parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static BucketSpan parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static BucketSpan parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static BucketSpan parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static BucketSpan parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static BucketSpan parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static BucketSpan parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static BucketSpan parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static BucketSpan parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return BucketSpan.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(BucketSpan prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static BucketSpan getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<BucketSpan> parser() {
            return PARSER;
        }

        public Parser<BucketSpan> getParserForType() {
            return PARSER;
        }

        @Override
        public BucketSpan getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", BucketSpan.class.getName());
            DEFAULT_INSTANCE = new BucketSpan();
            PARSER = new AbstractParser<BucketSpan>(){

                @Override
                public BucketSpan parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = BucketSpan.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements BucketSpanOrBuilder {
            private int bitField0_;
            private int offset_;
            private int length_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_BucketSpan_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_BucketSpan_fieldAccessorTable.ensureFieldAccessorsInitialized(BucketSpan.class, Builder.class);
            }

            private Builder() {
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.offset_ = 0;
                this.length_ = 0;
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_BucketSpan_descriptor;
            }

            @Override
            public BucketSpan getDefaultInstanceForType() {
                return BucketSpan.getDefaultInstance();
            }

            @Override
            public BucketSpan build() {
                BucketSpan result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public BucketSpan buildPartial() {
                BucketSpan result = new BucketSpan(this);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartial0(BucketSpan result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.offset_ = this.offset_;
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 2) != 0) {
                    result.length_ = this.length_;
                    to_bitField0_ |= 2;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof BucketSpan) {
                    return this.mergeFrom((BucketSpan)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(BucketSpan other) {
                if (other == BucketSpan.getDefaultInstance()) {
                    return this;
                }
                if (other.hasOffset()) {
                    this.setOffset(other.getOffset());
                }
                if (other.hasLength()) {
                    this.setLength(other.getLength());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block10: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block10;
                            }
                            case 8: {
                                this.offset_ = input.readSInt32();
                                this.bitField0_ |= 1;
                                continue block10;
                            }
                            case 16: {
                                this.length_ = input.readUInt32();
                                this.bitField0_ |= 2;
                                continue block10;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasOffset() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public int getOffset() {
                return this.offset_;
            }

            public Builder setOffset(int value) {
                this.offset_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearOffset() {
                this.bitField0_ &= 0xFFFFFFFE;
                this.offset_ = 0;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasLength() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public int getLength() {
                return this.length_;
            }

            public Builder setLength(int value) {
                this.length_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder clearLength() {
                this.bitField0_ &= 0xFFFFFFFD;
                this.length_ = 0;
                this.onChanged();
                return this;
            }
        }
    }

    public static interface BucketSpanOrBuilder
    extends MessageOrBuilder {
        public boolean hasOffset();

        public int getOffset();

        public boolean hasLength();

        public int getLength();
    }

    public static final class Bucket
    extends GeneratedMessage
    implements BucketOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int CUMULATIVE_COUNT_FIELD_NUMBER = 1;
        private long cumulativeCount_ = 0L;
        public static final int CUMULATIVE_COUNT_FLOAT_FIELD_NUMBER = 4;
        private double cumulativeCountFloat_ = 0.0;
        public static final int UPPER_BOUND_FIELD_NUMBER = 2;
        private double upperBound_ = 0.0;
        public static final int EXEMPLAR_FIELD_NUMBER = 3;
        private Exemplar exemplar_;
        private byte memoizedIsInitialized = (byte)-1;
        private static final Bucket DEFAULT_INSTANCE;
        private static final Parser<Bucket> PARSER;

        private Bucket(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private Bucket() {
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_Bucket_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_Bucket_fieldAccessorTable.ensureFieldAccessorsInitialized(Bucket.class, Builder.class);
        }

        @Override
        public boolean hasCumulativeCount() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public long getCumulativeCount() {
            return this.cumulativeCount_;
        }

        @Override
        public boolean hasCumulativeCountFloat() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public double getCumulativeCountFloat() {
            return this.cumulativeCountFloat_;
        }

        @Override
        public boolean hasUpperBound() {
            return (this.bitField0_ & 4) != 0;
        }

        @Override
        public double getUpperBound() {
            return this.upperBound_;
        }

        @Override
        public boolean hasExemplar() {
            return (this.bitField0_ & 8) != 0;
        }

        @Override
        public Exemplar getExemplar() {
            return this.exemplar_ == null ? Exemplar.getDefaultInstance() : this.exemplar_;
        }

        @Override
        public ExemplarOrBuilder getExemplarOrBuilder() {
            return this.exemplar_ == null ? Exemplar.getDefaultInstance() : this.exemplar_;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                output.writeUInt64(1, this.cumulativeCount_);
            }
            if ((this.bitField0_ & 4) != 0) {
                output.writeDouble(2, this.upperBound_);
            }
            if ((this.bitField0_ & 8) != 0) {
                output.writeMessage(3, this.getExemplar());
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeDouble(4, this.cumulativeCountFloat_);
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputStream.computeUInt64Size(1, this.cumulativeCount_);
            }
            if ((this.bitField0_ & 4) != 0) {
                size += CodedOutputStream.computeDoubleSize(2, this.upperBound_);
            }
            if ((this.bitField0_ & 8) != 0) {
                size += CodedOutputStream.computeMessageSize(3, this.getExemplar());
            }
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputStream.computeDoubleSize(4, this.cumulativeCountFloat_);
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Bucket)) {
                return super.equals(obj);
            }
            Bucket other = (Bucket)obj;
            if (this.hasCumulativeCount() != other.hasCumulativeCount()) {
                return false;
            }
            if (this.hasCumulativeCount() && this.getCumulativeCount() != other.getCumulativeCount()) {
                return false;
            }
            if (this.hasCumulativeCountFloat() != other.hasCumulativeCountFloat()) {
                return false;
            }
            if (this.hasCumulativeCountFloat() && Double.doubleToLongBits(this.getCumulativeCountFloat()) != Double.doubleToLongBits(other.getCumulativeCountFloat())) {
                return false;
            }
            if (this.hasUpperBound() != other.hasUpperBound()) {
                return false;
            }
            if (this.hasUpperBound() && Double.doubleToLongBits(this.getUpperBound()) != Double.doubleToLongBits(other.getUpperBound())) {
                return false;
            }
            if (this.hasExemplar() != other.hasExemplar()) {
                return false;
            }
            if (this.hasExemplar() && !this.getExemplar().equals(other.getExemplar())) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + Bucket.getDescriptor().hashCode();
            if (this.hasCumulativeCount()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + Internal.hashLong(this.getCumulativeCount());
            }
            if (this.hasCumulativeCountFloat()) {
                hash = 37 * hash + 4;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getCumulativeCountFloat()));
            }
            if (this.hasUpperBound()) {
                hash = 37 * hash + 2;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getUpperBound()));
            }
            if (this.hasExemplar()) {
                hash = 37 * hash + 3;
                hash = 53 * hash + this.getExemplar().hashCode();
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static Bucket parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Bucket parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Bucket parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Bucket parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Bucket parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Bucket parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Bucket parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Bucket parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Bucket parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static Bucket parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static Bucket parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Bucket parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return Bucket.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Bucket prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static Bucket getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Bucket> parser() {
            return PARSER;
        }

        public Parser<Bucket> getParserForType() {
            return PARSER;
        }

        @Override
        public Bucket getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Bucket.class.getName());
            DEFAULT_INSTANCE = new Bucket();
            PARSER = new AbstractParser<Bucket>(){

                @Override
                public Bucket parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = Bucket.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements BucketOrBuilder {
            private int bitField0_;
            private long cumulativeCount_;
            private double cumulativeCountFloat_;
            private double upperBound_;
            private Exemplar exemplar_;
            private SingleFieldBuilder<Exemplar, Exemplar.Builder, ExemplarOrBuilder> exemplarBuilder_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_Bucket_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_Bucket_fieldAccessorTable.ensureFieldAccessorsInitialized(Bucket.class, Builder.class);
            }

            private Builder() {
                this.maybeForceBuilderInitialization();
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
                this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (alwaysUseFieldBuilders) {
                    this.internalGetExemplarFieldBuilder();
                }
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.cumulativeCount_ = 0L;
                this.cumulativeCountFloat_ = 0.0;
                this.upperBound_ = 0.0;
                this.exemplar_ = null;
                if (this.exemplarBuilder_ != null) {
                    this.exemplarBuilder_.dispose();
                    this.exemplarBuilder_ = null;
                }
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_Bucket_descriptor;
            }

            @Override
            public Bucket getDefaultInstanceForType() {
                return Bucket.getDefaultInstance();
            }

            @Override
            public Bucket build() {
                Bucket result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public Bucket buildPartial() {
                Bucket result = new Bucket(this);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartial0(Bucket result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.cumulativeCount_ = this.cumulativeCount_;
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 2) != 0) {
                    result.cumulativeCountFloat_ = this.cumulativeCountFloat_;
                    to_bitField0_ |= 2;
                }
                if ((from_bitField0_ & 4) != 0) {
                    result.upperBound_ = this.upperBound_;
                    to_bitField0_ |= 4;
                }
                if ((from_bitField0_ & 8) != 0) {
                    result.exemplar_ = this.exemplarBuilder_ == null ? this.exemplar_ : this.exemplarBuilder_.build();
                    to_bitField0_ |= 8;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof Bucket) {
                    return this.mergeFrom((Bucket)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(Bucket other) {
                if (other == Bucket.getDefaultInstance()) {
                    return this;
                }
                if (other.hasCumulativeCount()) {
                    this.setCumulativeCount(other.getCumulativeCount());
                }
                if (other.hasCumulativeCountFloat()) {
                    this.setCumulativeCountFloat(other.getCumulativeCountFloat());
                }
                if (other.hasUpperBound()) {
                    this.setUpperBound(other.getUpperBound());
                }
                if (other.hasExemplar()) {
                    this.mergeExemplar(other.getExemplar());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block12: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block12;
                            }
                            case 8: {
                                this.cumulativeCount_ = input.readUInt64();
                                this.bitField0_ |= 1;
                                continue block12;
                            }
                            case 17: {
                                this.upperBound_ = input.readDouble();
                                this.bitField0_ |= 4;
                                continue block12;
                            }
                            case 26: {
                                input.readMessage(this.internalGetExemplarFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 8;
                                continue block12;
                            }
                            case 33: {
                                this.cumulativeCountFloat_ = input.readDouble();
                                this.bitField0_ |= 2;
                                continue block12;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasCumulativeCount() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public long getCumulativeCount() {
                return this.cumulativeCount_;
            }

            public Builder setCumulativeCount(long value) {
                this.cumulativeCount_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearCumulativeCount() {
                this.bitField0_ &= 0xFFFFFFFE;
                this.cumulativeCount_ = 0L;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasCumulativeCountFloat() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public double getCumulativeCountFloat() {
                return this.cumulativeCountFloat_;
            }

            public Builder setCumulativeCountFloat(double value) {
                this.cumulativeCountFloat_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder clearCumulativeCountFloat() {
                this.bitField0_ &= 0xFFFFFFFD;
                this.cumulativeCountFloat_ = 0.0;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasUpperBound() {
                return (this.bitField0_ & 4) != 0;
            }

            @Override
            public double getUpperBound() {
                return this.upperBound_;
            }

            public Builder setUpperBound(double value) {
                this.upperBound_ = value;
                this.bitField0_ |= 4;
                this.onChanged();
                return this;
            }

            public Builder clearUpperBound() {
                this.bitField0_ &= 0xFFFFFFFB;
                this.upperBound_ = 0.0;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasExemplar() {
                return (this.bitField0_ & 8) != 0;
            }

            @Override
            public Exemplar getExemplar() {
                if (this.exemplarBuilder_ == null) {
                    return this.exemplar_ == null ? Exemplar.getDefaultInstance() : this.exemplar_;
                }
                return this.exemplarBuilder_.getMessage();
            }

            public Builder setExemplar(Exemplar value) {
                if (this.exemplarBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.exemplar_ = value;
                } else {
                    this.exemplarBuilder_.setMessage(value);
                }
                this.bitField0_ |= 8;
                this.onChanged();
                return this;
            }

            public Builder setExemplar(Exemplar.Builder builderForValue) {
                if (this.exemplarBuilder_ == null) {
                    this.exemplar_ = builderForValue.build();
                } else {
                    this.exemplarBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 8;
                this.onChanged();
                return this;
            }

            public Builder mergeExemplar(Exemplar value) {
                if (this.exemplarBuilder_ == null) {
                    if ((this.bitField0_ & 8) != 0 && this.exemplar_ != null && this.exemplar_ != Exemplar.getDefaultInstance()) {
                        this.getExemplarBuilder().mergeFrom(value);
                    } else {
                        this.exemplar_ = value;
                    }
                } else {
                    this.exemplarBuilder_.mergeFrom(value);
                }
                if (this.exemplar_ != null) {
                    this.bitField0_ |= 8;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearExemplar() {
                this.bitField0_ &= 0xFFFFFFF7;
                this.exemplar_ = null;
                if (this.exemplarBuilder_ != null) {
                    this.exemplarBuilder_.dispose();
                    this.exemplarBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Exemplar.Builder getExemplarBuilder() {
                this.bitField0_ |= 8;
                this.onChanged();
                return this.internalGetExemplarFieldBuilder().getBuilder();
            }

            @Override
            public ExemplarOrBuilder getExemplarOrBuilder() {
                if (this.exemplarBuilder_ != null) {
                    return this.exemplarBuilder_.getMessageOrBuilder();
                }
                return this.exemplar_ == null ? Exemplar.getDefaultInstance() : this.exemplar_;
            }

            private SingleFieldBuilder<Exemplar, Exemplar.Builder, ExemplarOrBuilder> internalGetExemplarFieldBuilder() {
                if (this.exemplarBuilder_ == null) {
                    this.exemplarBuilder_ = new SingleFieldBuilder(this.getExemplar(), this.getParentForChildren(), this.isClean());
                    this.exemplar_ = null;
                }
                return this.exemplarBuilder_;
            }
        }
    }

    public static interface BucketOrBuilder
    extends MessageOrBuilder {
        public boolean hasCumulativeCount();

        public long getCumulativeCount();

        public boolean hasCumulativeCountFloat();

        public double getCumulativeCountFloat();

        public boolean hasUpperBound();

        public double getUpperBound();

        public boolean hasExemplar();

        public Exemplar getExemplar();

        public ExemplarOrBuilder getExemplarOrBuilder();
    }

    public static final class Histogram
    extends GeneratedMessage
    implements HistogramOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int SAMPLE_COUNT_FIELD_NUMBER = 1;
        private long sampleCount_ = 0L;
        public static final int SAMPLE_COUNT_FLOAT_FIELD_NUMBER = 4;
        private double sampleCountFloat_ = 0.0;
        public static final int SAMPLE_SUM_FIELD_NUMBER = 2;
        private double sampleSum_ = 0.0;
        public static final int BUCKET_FIELD_NUMBER = 3;
        private List<Bucket> bucket_;
        public static final int CREATED_TIMESTAMP_FIELD_NUMBER = 15;
        private Timestamp createdTimestamp_;
        public static final int SCHEMA_FIELD_NUMBER = 5;
        private int schema_ = 0;
        public static final int ZERO_THRESHOLD_FIELD_NUMBER = 6;
        private double zeroThreshold_ = 0.0;
        public static final int ZERO_COUNT_FIELD_NUMBER = 7;
        private long zeroCount_ = 0L;
        public static final int ZERO_COUNT_FLOAT_FIELD_NUMBER = 8;
        private double zeroCountFloat_ = 0.0;
        public static final int NEGATIVE_SPAN_FIELD_NUMBER = 9;
        private List<BucketSpan> negativeSpan_;
        public static final int NEGATIVE_DELTA_FIELD_NUMBER = 10;
        private Internal.LongList negativeDelta_ = Histogram.emptyLongList();
        public static final int NEGATIVE_COUNT_FIELD_NUMBER = 11;
        private Internal.DoubleList negativeCount_ = Histogram.emptyDoubleList();
        public static final int POSITIVE_SPAN_FIELD_NUMBER = 12;
        private List<BucketSpan> positiveSpan_;
        public static final int POSITIVE_DELTA_FIELD_NUMBER = 13;
        private Internal.LongList positiveDelta_ = Histogram.emptyLongList();
        public static final int POSITIVE_COUNT_FIELD_NUMBER = 14;
        private Internal.DoubleList positiveCount_ = Histogram.emptyDoubleList();
        public static final int EXEMPLARS_FIELD_NUMBER = 16;
        private List<Exemplar> exemplars_;
        private byte memoizedIsInitialized = (byte)-1;
        private static final Histogram DEFAULT_INSTANCE;
        private static final Parser<Histogram> PARSER;

        private Histogram(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private Histogram() {
            this.bucket_ = Collections.emptyList();
            this.negativeSpan_ = Collections.emptyList();
            this.negativeDelta_ = Histogram.emptyLongList();
            this.negativeCount_ = Histogram.emptyDoubleList();
            this.positiveSpan_ = Collections.emptyList();
            this.positiveDelta_ = Histogram.emptyLongList();
            this.positiveCount_ = Histogram.emptyDoubleList();
            this.exemplars_ = Collections.emptyList();
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_Histogram_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_Histogram_fieldAccessorTable.ensureFieldAccessorsInitialized(Histogram.class, Builder.class);
        }

        @Override
        public boolean hasSampleCount() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public long getSampleCount() {
            return this.sampleCount_;
        }

        @Override
        public boolean hasSampleCountFloat() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public double getSampleCountFloat() {
            return this.sampleCountFloat_;
        }

        @Override
        public boolean hasSampleSum() {
            return (this.bitField0_ & 4) != 0;
        }

        @Override
        public double getSampleSum() {
            return this.sampleSum_;
        }

        @Override
        public List<Bucket> getBucketList() {
            return this.bucket_;
        }

        @Override
        public List<? extends BucketOrBuilder> getBucketOrBuilderList() {
            return this.bucket_;
        }

        @Override
        public int getBucketCount() {
            return this.bucket_.size();
        }

        @Override
        public Bucket getBucket(int index) {
            return this.bucket_.get(index);
        }

        @Override
        public BucketOrBuilder getBucketOrBuilder(int index) {
            return this.bucket_.get(index);
        }

        @Override
        public boolean hasCreatedTimestamp() {
            return (this.bitField0_ & 8) != 0;
        }

        @Override
        public Timestamp getCreatedTimestamp() {
            return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
        }

        @Override
        public TimestampOrBuilder getCreatedTimestampOrBuilder() {
            return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
        }

        @Override
        public boolean hasSchema() {
            return (this.bitField0_ & 0x10) != 0;
        }

        @Override
        public int getSchema() {
            return this.schema_;
        }

        @Override
        public boolean hasZeroThreshold() {
            return (this.bitField0_ & 0x20) != 0;
        }

        @Override
        public double getZeroThreshold() {
            return this.zeroThreshold_;
        }

        @Override
        public boolean hasZeroCount() {
            return (this.bitField0_ & 0x40) != 0;
        }

        @Override
        public long getZeroCount() {
            return this.zeroCount_;
        }

        @Override
        public boolean hasZeroCountFloat() {
            return (this.bitField0_ & 0x80) != 0;
        }

        @Override
        public double getZeroCountFloat() {
            return this.zeroCountFloat_;
        }

        @Override
        public List<BucketSpan> getNegativeSpanList() {
            return this.negativeSpan_;
        }

        @Override
        public List<? extends BucketSpanOrBuilder> getNegativeSpanOrBuilderList() {
            return this.negativeSpan_;
        }

        @Override
        public int getNegativeSpanCount() {
            return this.negativeSpan_.size();
        }

        @Override
        public BucketSpan getNegativeSpan(int index) {
            return this.negativeSpan_.get(index);
        }

        @Override
        public BucketSpanOrBuilder getNegativeSpanOrBuilder(int index) {
            return this.negativeSpan_.get(index);
        }

        @Override
        public List<Long> getNegativeDeltaList() {
            return this.negativeDelta_;
        }

        @Override
        public int getNegativeDeltaCount() {
            return this.negativeDelta_.size();
        }

        @Override
        public long getNegativeDelta(int index) {
            return this.negativeDelta_.getLong(index);
        }

        @Override
        public List<Double> getNegativeCountList() {
            return this.negativeCount_;
        }

        @Override
        public int getNegativeCountCount() {
            return this.negativeCount_.size();
        }

        @Override
        public double getNegativeCount(int index) {
            return this.negativeCount_.getDouble(index);
        }

        @Override
        public List<BucketSpan> getPositiveSpanList() {
            return this.positiveSpan_;
        }

        @Override
        public List<? extends BucketSpanOrBuilder> getPositiveSpanOrBuilderList() {
            return this.positiveSpan_;
        }

        @Override
        public int getPositiveSpanCount() {
            return this.positiveSpan_.size();
        }

        @Override
        public BucketSpan getPositiveSpan(int index) {
            return this.positiveSpan_.get(index);
        }

        @Override
        public BucketSpanOrBuilder getPositiveSpanOrBuilder(int index) {
            return this.positiveSpan_.get(index);
        }

        @Override
        public List<Long> getPositiveDeltaList() {
            return this.positiveDelta_;
        }

        @Override
        public int getPositiveDeltaCount() {
            return this.positiveDelta_.size();
        }

        @Override
        public long getPositiveDelta(int index) {
            return this.positiveDelta_.getLong(index);
        }

        @Override
        public List<Double> getPositiveCountList() {
            return this.positiveCount_;
        }

        @Override
        public int getPositiveCountCount() {
            return this.positiveCount_.size();
        }

        @Override
        public double getPositiveCount(int index) {
            return this.positiveCount_.getDouble(index);
        }

        @Override
        public List<Exemplar> getExemplarsList() {
            return this.exemplars_;
        }

        @Override
        public List<? extends ExemplarOrBuilder> getExemplarsOrBuilderList() {
            return this.exemplars_;
        }

        @Override
        public int getExemplarsCount() {
            return this.exemplars_.size();
        }

        @Override
        public Exemplar getExemplars(int index) {
            return this.exemplars_.get(index);
        }

        @Override
        public ExemplarOrBuilder getExemplarsOrBuilder(int index) {
            return this.exemplars_.get(index);
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            int i;
            if ((this.bitField0_ & 1) != 0) {
                output.writeUInt64(1, this.sampleCount_);
            }
            if ((this.bitField0_ & 4) != 0) {
                output.writeDouble(2, this.sampleSum_);
            }
            for (i = 0; i < this.bucket_.size(); ++i) {
                output.writeMessage(3, this.bucket_.get(i));
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeDouble(4, this.sampleCountFloat_);
            }
            if ((this.bitField0_ & 0x10) != 0) {
                output.writeSInt32(5, this.schema_);
            }
            if ((this.bitField0_ & 0x20) != 0) {
                output.writeDouble(6, this.zeroThreshold_);
            }
            if ((this.bitField0_ & 0x40) != 0) {
                output.writeUInt64(7, this.zeroCount_);
            }
            if ((this.bitField0_ & 0x80) != 0) {
                output.writeDouble(8, this.zeroCountFloat_);
            }
            for (i = 0; i < this.negativeSpan_.size(); ++i) {
                output.writeMessage(9, this.negativeSpan_.get(i));
            }
            for (i = 0; i < this.negativeDelta_.size(); ++i) {
                output.writeSInt64(10, this.negativeDelta_.getLong(i));
            }
            for (i = 0; i < this.negativeCount_.size(); ++i) {
                output.writeDouble(11, this.negativeCount_.getDouble(i));
            }
            for (i = 0; i < this.positiveSpan_.size(); ++i) {
                output.writeMessage(12, this.positiveSpan_.get(i));
            }
            for (i = 0; i < this.positiveDelta_.size(); ++i) {
                output.writeSInt64(13, this.positiveDelta_.getLong(i));
            }
            for (i = 0; i < this.positiveCount_.size(); ++i) {
                output.writeDouble(14, this.positiveCount_.getDouble(i));
            }
            if ((this.bitField0_ & 8) != 0) {
                output.writeMessage(15, this.getCreatedTimestamp());
            }
            for (i = 0; i < this.exemplars_.size(); ++i) {
                output.writeMessage(16, this.exemplars_.get(i));
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int i;
            int i2;
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputStream.computeUInt64Size(1, this.sampleCount_);
            }
            if ((this.bitField0_ & 4) != 0) {
                size += CodedOutputStream.computeDoubleSize(2, this.sampleSum_);
            }
            for (i2 = 0; i2 < this.bucket_.size(); ++i2) {
                size += CodedOutputStream.computeMessageSize(3, this.bucket_.get(i2));
            }
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputStream.computeDoubleSize(4, this.sampleCountFloat_);
            }
            if ((this.bitField0_ & 0x10) != 0) {
                size += CodedOutputStream.computeSInt32Size(5, this.schema_);
            }
            if ((this.bitField0_ & 0x20) != 0) {
                size += CodedOutputStream.computeDoubleSize(6, this.zeroThreshold_);
            }
            if ((this.bitField0_ & 0x40) != 0) {
                size += CodedOutputStream.computeUInt64Size(7, this.zeroCount_);
            }
            if ((this.bitField0_ & 0x80) != 0) {
                size += CodedOutputStream.computeDoubleSize(8, this.zeroCountFloat_);
            }
            for (i2 = 0; i2 < this.negativeSpan_.size(); ++i2) {
                size += CodedOutputStream.computeMessageSize(9, this.negativeSpan_.get(i2));
            }
            int dataSize = 0;
            for (i = 0; i < this.negativeDelta_.size(); ++i) {
                dataSize += CodedOutputStream.computeSInt64SizeNoTag(this.negativeDelta_.getLong(i));
            }
            size += dataSize;
            size += 1 * this.getNegativeDeltaList().size();
            dataSize = 0;
            dataSize = 8 * this.getNegativeCountList().size();
            size += dataSize;
            size += 1 * this.getNegativeCountList().size();
            for (i2 = 0; i2 < this.positiveSpan_.size(); ++i2) {
                size += CodedOutputStream.computeMessageSize(12, this.positiveSpan_.get(i2));
            }
            dataSize = 0;
            for (i = 0; i < this.positiveDelta_.size(); ++i) {
                dataSize += CodedOutputStream.computeSInt64SizeNoTag(this.positiveDelta_.getLong(i));
            }
            size += dataSize;
            size += 1 * this.getPositiveDeltaList().size();
            dataSize = 0;
            dataSize = 8 * this.getPositiveCountList().size();
            size += dataSize;
            size += 1 * this.getPositiveCountList().size();
            if ((this.bitField0_ & 8) != 0) {
                size += CodedOutputStream.computeMessageSize(15, this.getCreatedTimestamp());
            }
            for (i2 = 0; i2 < this.exemplars_.size(); ++i2) {
                size += CodedOutputStream.computeMessageSize(16, this.exemplars_.get(i2));
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Histogram)) {
                return super.equals(obj);
            }
            Histogram other = (Histogram)obj;
            if (this.hasSampleCount() != other.hasSampleCount()) {
                return false;
            }
            if (this.hasSampleCount() && this.getSampleCount() != other.getSampleCount()) {
                return false;
            }
            if (this.hasSampleCountFloat() != other.hasSampleCountFloat()) {
                return false;
            }
            if (this.hasSampleCountFloat() && Double.doubleToLongBits(this.getSampleCountFloat()) != Double.doubleToLongBits(other.getSampleCountFloat())) {
                return false;
            }
            if (this.hasSampleSum() != other.hasSampleSum()) {
                return false;
            }
            if (this.hasSampleSum() && Double.doubleToLongBits(this.getSampleSum()) != Double.doubleToLongBits(other.getSampleSum())) {
                return false;
            }
            if (!this.getBucketList().equals(other.getBucketList())) {
                return false;
            }
            if (this.hasCreatedTimestamp() != other.hasCreatedTimestamp()) {
                return false;
            }
            if (this.hasCreatedTimestamp() && !this.getCreatedTimestamp().equals(other.getCreatedTimestamp())) {
                return false;
            }
            if (this.hasSchema() != other.hasSchema()) {
                return false;
            }
            if (this.hasSchema() && this.getSchema() != other.getSchema()) {
                return false;
            }
            if (this.hasZeroThreshold() != other.hasZeroThreshold()) {
                return false;
            }
            if (this.hasZeroThreshold() && Double.doubleToLongBits(this.getZeroThreshold()) != Double.doubleToLongBits(other.getZeroThreshold())) {
                return false;
            }
            if (this.hasZeroCount() != other.hasZeroCount()) {
                return false;
            }
            if (this.hasZeroCount() && this.getZeroCount() != other.getZeroCount()) {
                return false;
            }
            if (this.hasZeroCountFloat() != other.hasZeroCountFloat()) {
                return false;
            }
            if (this.hasZeroCountFloat() && Double.doubleToLongBits(this.getZeroCountFloat()) != Double.doubleToLongBits(other.getZeroCountFloat())) {
                return false;
            }
            if (!this.getNegativeSpanList().equals(other.getNegativeSpanList())) {
                return false;
            }
            if (!this.getNegativeDeltaList().equals(other.getNegativeDeltaList())) {
                return false;
            }
            if (!this.getNegativeCountList().equals(other.getNegativeCountList())) {
                return false;
            }
            if (!this.getPositiveSpanList().equals(other.getPositiveSpanList())) {
                return false;
            }
            if (!this.getPositiveDeltaList().equals(other.getPositiveDeltaList())) {
                return false;
            }
            if (!this.getPositiveCountList().equals(other.getPositiveCountList())) {
                return false;
            }
            if (!this.getExemplarsList().equals(other.getExemplarsList())) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + Histogram.getDescriptor().hashCode();
            if (this.hasSampleCount()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + Internal.hashLong(this.getSampleCount());
            }
            if (this.hasSampleCountFloat()) {
                hash = 37 * hash + 4;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getSampleCountFloat()));
            }
            if (this.hasSampleSum()) {
                hash = 37 * hash + 2;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getSampleSum()));
            }
            if (this.getBucketCount() > 0) {
                hash = 37 * hash + 3;
                hash = 53 * hash + this.getBucketList().hashCode();
            }
            if (this.hasCreatedTimestamp()) {
                hash = 37 * hash + 15;
                hash = 53 * hash + this.getCreatedTimestamp().hashCode();
            }
            if (this.hasSchema()) {
                hash = 37 * hash + 5;
                hash = 53 * hash + this.getSchema();
            }
            if (this.hasZeroThreshold()) {
                hash = 37 * hash + 6;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getZeroThreshold()));
            }
            if (this.hasZeroCount()) {
                hash = 37 * hash + 7;
                hash = 53 * hash + Internal.hashLong(this.getZeroCount());
            }
            if (this.hasZeroCountFloat()) {
                hash = 37 * hash + 8;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getZeroCountFloat()));
            }
            if (this.getNegativeSpanCount() > 0) {
                hash = 37 * hash + 9;
                hash = 53 * hash + this.getNegativeSpanList().hashCode();
            }
            if (this.getNegativeDeltaCount() > 0) {
                hash = 37 * hash + 10;
                hash = 53 * hash + this.getNegativeDeltaList().hashCode();
            }
            if (this.getNegativeCountCount() > 0) {
                hash = 37 * hash + 11;
                hash = 53 * hash + this.getNegativeCountList().hashCode();
            }
            if (this.getPositiveSpanCount() > 0) {
                hash = 37 * hash + 12;
                hash = 53 * hash + this.getPositiveSpanList().hashCode();
            }
            if (this.getPositiveDeltaCount() > 0) {
                hash = 37 * hash + 13;
                hash = 53 * hash + this.getPositiveDeltaList().hashCode();
            }
            if (this.getPositiveCountCount() > 0) {
                hash = 37 * hash + 14;
                hash = 53 * hash + this.getPositiveCountList().hashCode();
            }
            if (this.getExemplarsCount() > 0) {
                hash = 37 * hash + 16;
                hash = 53 * hash + this.getExemplarsList().hashCode();
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static Histogram parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Histogram parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Histogram parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Histogram parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Histogram parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Histogram parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Histogram parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Histogram parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Histogram parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static Histogram parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static Histogram parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Histogram parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return Histogram.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Histogram prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static Histogram getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Histogram> parser() {
            return PARSER;
        }

        public Parser<Histogram> getParserForType() {
            return PARSER;
        }

        @Override
        public Histogram getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static /* synthetic */ Internal.LongList access$8300() {
            return Histogram.emptyLongList();
        }

        static /* synthetic */ Internal.DoubleList access$8600() {
            return Histogram.emptyDoubleList();
        }

        static /* synthetic */ Internal.LongList access$9000() {
            return Histogram.emptyLongList();
        }

        static /* synthetic */ Internal.DoubleList access$9300() {
            return Histogram.emptyDoubleList();
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Histogram.class.getName());
            DEFAULT_INSTANCE = new Histogram();
            PARSER = new AbstractParser<Histogram>(){

                @Override
                public Histogram parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = Histogram.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements HistogramOrBuilder {
            private int bitField0_;
            private long sampleCount_;
            private double sampleCountFloat_;
            private double sampleSum_;
            private List<Bucket> bucket_ = Collections.emptyList();
            private RepeatedFieldBuilder<Bucket, Bucket.Builder, BucketOrBuilder> bucketBuilder_;
            private Timestamp createdTimestamp_;
            private SingleFieldBuilder<Timestamp, Timestamp.Builder, TimestampOrBuilder> createdTimestampBuilder_;
            private int schema_;
            private double zeroThreshold_;
            private long zeroCount_;
            private double zeroCountFloat_;
            private List<BucketSpan> negativeSpan_ = Collections.emptyList();
            private RepeatedFieldBuilder<BucketSpan, BucketSpan.Builder, BucketSpanOrBuilder> negativeSpanBuilder_;
            private Internal.LongList negativeDelta_ = Histogram.access$8300();
            private Internal.DoubleList negativeCount_ = Histogram.access$8600();
            private List<BucketSpan> positiveSpan_ = Collections.emptyList();
            private RepeatedFieldBuilder<BucketSpan, BucketSpan.Builder, BucketSpanOrBuilder> positiveSpanBuilder_;
            private Internal.LongList positiveDelta_ = Histogram.access$9000();
            private Internal.DoubleList positiveCount_ = Histogram.access$9300();
            private List<Exemplar> exemplars_ = Collections.emptyList();
            private RepeatedFieldBuilder<Exemplar, Exemplar.Builder, ExemplarOrBuilder> exemplarsBuilder_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_Histogram_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_Histogram_fieldAccessorTable.ensureFieldAccessorsInitialized(Histogram.class, Builder.class);
            }

            private Builder() {
                this.maybeForceBuilderInitialization();
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
                this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (alwaysUseFieldBuilders) {
                    this.internalGetBucketFieldBuilder();
                    this.internalGetCreatedTimestampFieldBuilder();
                    this.internalGetNegativeSpanFieldBuilder();
                    this.internalGetPositiveSpanFieldBuilder();
                    this.internalGetExemplarsFieldBuilder();
                }
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.sampleCount_ = 0L;
                this.sampleCountFloat_ = 0.0;
                this.sampleSum_ = 0.0;
                if (this.bucketBuilder_ == null) {
                    this.bucket_ = Collections.emptyList();
                } else {
                    this.bucket_ = null;
                    this.bucketBuilder_.clear();
                }
                this.bitField0_ &= 0xFFFFFFF7;
                this.createdTimestamp_ = null;
                if (this.createdTimestampBuilder_ != null) {
                    this.createdTimestampBuilder_.dispose();
                    this.createdTimestampBuilder_ = null;
                }
                this.schema_ = 0;
                this.zeroThreshold_ = 0.0;
                this.zeroCount_ = 0L;
                this.zeroCountFloat_ = 0.0;
                if (this.negativeSpanBuilder_ == null) {
                    this.negativeSpan_ = Collections.emptyList();
                } else {
                    this.negativeSpan_ = null;
                    this.negativeSpanBuilder_.clear();
                }
                this.bitField0_ &= 0xFFFFFDFF;
                this.negativeDelta_ = Histogram.emptyLongList();
                this.negativeCount_ = Histogram.emptyDoubleList();
                if (this.positiveSpanBuilder_ == null) {
                    this.positiveSpan_ = Collections.emptyList();
                } else {
                    this.positiveSpan_ = null;
                    this.positiveSpanBuilder_.clear();
                }
                this.bitField0_ &= 0xFFFFEFFF;
                this.positiveDelta_ = Histogram.emptyLongList();
                this.positiveCount_ = Histogram.emptyDoubleList();
                if (this.exemplarsBuilder_ == null) {
                    this.exemplars_ = Collections.emptyList();
                } else {
                    this.exemplars_ = null;
                    this.exemplarsBuilder_.clear();
                }
                this.bitField0_ &= 0xFFFF7FFF;
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_Histogram_descriptor;
            }

            @Override
            public Histogram getDefaultInstanceForType() {
                return Histogram.getDefaultInstance();
            }

            @Override
            public Histogram build() {
                Histogram result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public Histogram buildPartial() {
                Histogram result = new Histogram(this);
                this.buildPartialRepeatedFields(result);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartialRepeatedFields(Histogram result) {
                if (this.bucketBuilder_ == null) {
                    if ((this.bitField0_ & 8) != 0) {
                        this.bucket_ = Collections.unmodifiableList(this.bucket_);
                        this.bitField0_ &= 0xFFFFFFF7;
                    }
                    result.bucket_ = this.bucket_;
                } else {
                    result.bucket_ = this.bucketBuilder_.build();
                }
                if (this.negativeSpanBuilder_ == null) {
                    if ((this.bitField0_ & 0x200) != 0) {
                        this.negativeSpan_ = Collections.unmodifiableList(this.negativeSpan_);
                        this.bitField0_ &= 0xFFFFFDFF;
                    }
                    result.negativeSpan_ = this.negativeSpan_;
                } else {
                    result.negativeSpan_ = this.negativeSpanBuilder_.build();
                }
                if (this.positiveSpanBuilder_ == null) {
                    if ((this.bitField0_ & 0x1000) != 0) {
                        this.positiveSpan_ = Collections.unmodifiableList(this.positiveSpan_);
                        this.bitField0_ &= 0xFFFFEFFF;
                    }
                    result.positiveSpan_ = this.positiveSpan_;
                } else {
                    result.positiveSpan_ = this.positiveSpanBuilder_.build();
                }
                if (this.exemplarsBuilder_ == null) {
                    if ((this.bitField0_ & 0x8000) != 0) {
                        this.exemplars_ = Collections.unmodifiableList(this.exemplars_);
                        this.bitField0_ &= 0xFFFF7FFF;
                    }
                    result.exemplars_ = this.exemplars_;
                } else {
                    result.exemplars_ = this.exemplarsBuilder_.build();
                }
            }

            private void buildPartial0(Histogram result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.sampleCount_ = this.sampleCount_;
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 2) != 0) {
                    result.sampleCountFloat_ = this.sampleCountFloat_;
                    to_bitField0_ |= 2;
                }
                if ((from_bitField0_ & 4) != 0) {
                    result.sampleSum_ = this.sampleSum_;
                    to_bitField0_ |= 4;
                }
                if ((from_bitField0_ & 0x10) != 0) {
                    result.createdTimestamp_ = this.createdTimestampBuilder_ == null ? this.createdTimestamp_ : this.createdTimestampBuilder_.build();
                    to_bitField0_ |= 8;
                }
                if ((from_bitField0_ & 0x20) != 0) {
                    result.schema_ = this.schema_;
                    to_bitField0_ |= 0x10;
                }
                if ((from_bitField0_ & 0x40) != 0) {
                    result.zeroThreshold_ = this.zeroThreshold_;
                    to_bitField0_ |= 0x20;
                }
                if ((from_bitField0_ & 0x80) != 0) {
                    result.zeroCount_ = this.zeroCount_;
                    to_bitField0_ |= 0x40;
                }
                if ((from_bitField0_ & 0x100) != 0) {
                    result.zeroCountFloat_ = this.zeroCountFloat_;
                    to_bitField0_ |= 0x80;
                }
                if ((from_bitField0_ & 0x400) != 0) {
                    this.negativeDelta_.makeImmutable();
                    result.negativeDelta_ = this.negativeDelta_;
                }
                if ((from_bitField0_ & 0x800) != 0) {
                    this.negativeCount_.makeImmutable();
                    result.negativeCount_ = this.negativeCount_;
                }
                if ((from_bitField0_ & 0x2000) != 0) {
                    this.positiveDelta_.makeImmutable();
                    result.positiveDelta_ = this.positiveDelta_;
                }
                if ((from_bitField0_ & 0x4000) != 0) {
                    this.positiveCount_.makeImmutable();
                    result.positiveCount_ = this.positiveCount_;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof Histogram) {
                    return this.mergeFrom((Histogram)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(Histogram other) {
                if (other == Histogram.getDefaultInstance()) {
                    return this;
                }
                if (other.hasSampleCount()) {
                    this.setSampleCount(other.getSampleCount());
                }
                if (other.hasSampleCountFloat()) {
                    this.setSampleCountFloat(other.getSampleCountFloat());
                }
                if (other.hasSampleSum()) {
                    this.setSampleSum(other.getSampleSum());
                }
                if (this.bucketBuilder_ == null) {
                    if (!other.bucket_.isEmpty()) {
                        if (this.bucket_.isEmpty()) {
                            this.bucket_ = other.bucket_;
                            this.bitField0_ &= 0xFFFFFFF7;
                        } else {
                            this.ensureBucketIsMutable();
                            this.bucket_.addAll(other.bucket_);
                        }
                        this.onChanged();
                    }
                } else if (!other.bucket_.isEmpty()) {
                    if (this.bucketBuilder_.isEmpty()) {
                        this.bucketBuilder_.dispose();
                        this.bucketBuilder_ = null;
                        this.bucket_ = other.bucket_;
                        this.bitField0_ &= 0xFFFFFFF7;
                        this.bucketBuilder_ = alwaysUseFieldBuilders ? this.internalGetBucketFieldBuilder() : null;
                    } else {
                        this.bucketBuilder_.addAllMessages(other.bucket_);
                    }
                }
                if (other.hasCreatedTimestamp()) {
                    this.mergeCreatedTimestamp(other.getCreatedTimestamp());
                }
                if (other.hasSchema()) {
                    this.setSchema(other.getSchema());
                }
                if (other.hasZeroThreshold()) {
                    this.setZeroThreshold(other.getZeroThreshold());
                }
                if (other.hasZeroCount()) {
                    this.setZeroCount(other.getZeroCount());
                }
                if (other.hasZeroCountFloat()) {
                    this.setZeroCountFloat(other.getZeroCountFloat());
                }
                if (this.negativeSpanBuilder_ == null) {
                    if (!other.negativeSpan_.isEmpty()) {
                        if (this.negativeSpan_.isEmpty()) {
                            this.negativeSpan_ = other.negativeSpan_;
                            this.bitField0_ &= 0xFFFFFDFF;
                        } else {
                            this.ensureNegativeSpanIsMutable();
                            this.negativeSpan_.addAll(other.negativeSpan_);
                        }
                        this.onChanged();
                    }
                } else if (!other.negativeSpan_.isEmpty()) {
                    if (this.negativeSpanBuilder_.isEmpty()) {
                        this.negativeSpanBuilder_.dispose();
                        this.negativeSpanBuilder_ = null;
                        this.negativeSpan_ = other.negativeSpan_;
                        this.bitField0_ &= 0xFFFFFDFF;
                        this.negativeSpanBuilder_ = alwaysUseFieldBuilders ? this.internalGetNegativeSpanFieldBuilder() : null;
                    } else {
                        this.negativeSpanBuilder_.addAllMessages(other.negativeSpan_);
                    }
                }
                if (!other.negativeDelta_.isEmpty()) {
                    if (this.negativeDelta_.isEmpty()) {
                        this.negativeDelta_ = other.negativeDelta_;
                        this.negativeDelta_.makeImmutable();
                        this.bitField0_ |= 0x400;
                    } else {
                        this.ensureNegativeDeltaIsMutable();
                        this.negativeDelta_.addAll(other.negativeDelta_);
                    }
                    this.onChanged();
                }
                if (!other.negativeCount_.isEmpty()) {
                    if (this.negativeCount_.isEmpty()) {
                        this.negativeCount_ = other.negativeCount_;
                        this.negativeCount_.makeImmutable();
                        this.bitField0_ |= 0x800;
                    } else {
                        this.ensureNegativeCountIsMutable();
                        this.negativeCount_.addAll(other.negativeCount_);
                    }
                    this.onChanged();
                }
                if (this.positiveSpanBuilder_ == null) {
                    if (!other.positiveSpan_.isEmpty()) {
                        if (this.positiveSpan_.isEmpty()) {
                            this.positiveSpan_ = other.positiveSpan_;
                            this.bitField0_ &= 0xFFFFEFFF;
                        } else {
                            this.ensurePositiveSpanIsMutable();
                            this.positiveSpan_.addAll(other.positiveSpan_);
                        }
                        this.onChanged();
                    }
                } else if (!other.positiveSpan_.isEmpty()) {
                    if (this.positiveSpanBuilder_.isEmpty()) {
                        this.positiveSpanBuilder_.dispose();
                        this.positiveSpanBuilder_ = null;
                        this.positiveSpan_ = other.positiveSpan_;
                        this.bitField0_ &= 0xFFFFEFFF;
                        this.positiveSpanBuilder_ = alwaysUseFieldBuilders ? this.internalGetPositiveSpanFieldBuilder() : null;
                    } else {
                        this.positiveSpanBuilder_.addAllMessages(other.positiveSpan_);
                    }
                }
                if (!other.positiveDelta_.isEmpty()) {
                    if (this.positiveDelta_.isEmpty()) {
                        this.positiveDelta_ = other.positiveDelta_;
                        this.positiveDelta_.makeImmutable();
                        this.bitField0_ |= 0x2000;
                    } else {
                        this.ensurePositiveDeltaIsMutable();
                        this.positiveDelta_.addAll(other.positiveDelta_);
                    }
                    this.onChanged();
                }
                if (!other.positiveCount_.isEmpty()) {
                    if (this.positiveCount_.isEmpty()) {
                        this.positiveCount_ = other.positiveCount_;
                        this.positiveCount_.makeImmutable();
                        this.bitField0_ |= 0x4000;
                    } else {
                        this.ensurePositiveCountIsMutable();
                        this.positiveCount_.addAll(other.positiveCount_);
                    }
                    this.onChanged();
                }
                if (this.exemplarsBuilder_ == null) {
                    if (!other.exemplars_.isEmpty()) {
                        if (this.exemplars_.isEmpty()) {
                            this.exemplars_ = other.exemplars_;
                            this.bitField0_ &= 0xFFFF7FFF;
                        } else {
                            this.ensureExemplarsIsMutable();
                            this.exemplars_.addAll(other.exemplars_);
                        }
                        this.onChanged();
                    }
                } else if (!other.exemplars_.isEmpty()) {
                    if (this.exemplarsBuilder_.isEmpty()) {
                        this.exemplarsBuilder_.dispose();
                        this.exemplarsBuilder_ = null;
                        this.exemplars_ = other.exemplars_;
                        this.bitField0_ &= 0xFFFF7FFF;
                        this.exemplarsBuilder_ = alwaysUseFieldBuilders ? this.internalGetExemplarsFieldBuilder() : null;
                    } else {
                        this.exemplarsBuilder_.addAllMessages(other.exemplars_);
                    }
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block28: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block28;
                            }
                            case 8: {
                                this.sampleCount_ = input.readUInt64();
                                this.bitField0_ |= 1;
                                continue block28;
                            }
                            case 17: {
                                this.sampleSum_ = input.readDouble();
                                this.bitField0_ |= 4;
                                continue block28;
                            }
                            case 26: {
                                Bucket m = input.readMessage(Bucket.parser(), extensionRegistry);
                                if (this.bucketBuilder_ == null) {
                                    this.ensureBucketIsMutable();
                                    this.bucket_.add(m);
                                    continue block28;
                                }
                                this.bucketBuilder_.addMessage(m);
                                continue block28;
                            }
                            case 33: {
                                this.sampleCountFloat_ = input.readDouble();
                                this.bitField0_ |= 2;
                                continue block28;
                            }
                            case 40: {
                                this.schema_ = input.readSInt32();
                                this.bitField0_ |= 0x20;
                                continue block28;
                            }
                            case 49: {
                                this.zeroThreshold_ = input.readDouble();
                                this.bitField0_ |= 0x40;
                                continue block28;
                            }
                            case 56: {
                                this.zeroCount_ = input.readUInt64();
                                this.bitField0_ |= 0x80;
                                continue block28;
                            }
                            case 65: {
                                this.zeroCountFloat_ = input.readDouble();
                                this.bitField0_ |= 0x100;
                                continue block28;
                            }
                            case 74: {
                                BucketSpan m = input.readMessage(BucketSpan.parser(), extensionRegistry);
                                if (this.negativeSpanBuilder_ == null) {
                                    this.ensureNegativeSpanIsMutable();
                                    this.negativeSpan_.add(m);
                                    continue block28;
                                }
                                this.negativeSpanBuilder_.addMessage(m);
                                continue block28;
                            }
                            case 80: {
                                long v = input.readSInt64();
                                this.ensureNegativeDeltaIsMutable();
                                this.negativeDelta_.addLong(v);
                                continue block28;
                            }
                            case 82: {
                                int length = input.readRawVarint32();
                                int limit = input.pushLimit(length);
                                this.ensureNegativeDeltaIsMutable();
                                while (input.getBytesUntilLimit() > 0) {
                                    this.negativeDelta_.addLong(input.readSInt64());
                                }
                                input.popLimit(limit);
                                continue block28;
                            }
                            case 89: {
                                double v = input.readDouble();
                                this.ensureNegativeCountIsMutable();
                                this.negativeCount_.addDouble(v);
                                continue block28;
                            }
                            case 90: {
                                int length = input.readRawVarint32();
                                int limit = input.pushLimit(length);
                                int alloc = length > 4096 ? 4096 : length;
                                this.ensureNegativeCountIsMutable(alloc / 8);
                                while (input.getBytesUntilLimit() > 0) {
                                    this.negativeCount_.addDouble(input.readDouble());
                                }
                                input.popLimit(limit);
                                continue block28;
                            }
                            case 98: {
                                BucketSpan m = input.readMessage(BucketSpan.parser(), extensionRegistry);
                                if (this.positiveSpanBuilder_ == null) {
                                    this.ensurePositiveSpanIsMutable();
                                    this.positiveSpan_.add(m);
                                    continue block28;
                                }
                                this.positiveSpanBuilder_.addMessage(m);
                                continue block28;
                            }
                            case 104: {
                                long v = input.readSInt64();
                                this.ensurePositiveDeltaIsMutable();
                                this.positiveDelta_.addLong(v);
                                continue block28;
                            }
                            case 106: {
                                int length = input.readRawVarint32();
                                int limit = input.pushLimit(length);
                                this.ensurePositiveDeltaIsMutable();
                                while (input.getBytesUntilLimit() > 0) {
                                    this.positiveDelta_.addLong(input.readSInt64());
                                }
                                input.popLimit(limit);
                                continue block28;
                            }
                            case 113: {
                                double v = input.readDouble();
                                this.ensurePositiveCountIsMutable();
                                this.positiveCount_.addDouble(v);
                                continue block28;
                            }
                            case 114: {
                                int length = input.readRawVarint32();
                                int limit = input.pushLimit(length);
                                int alloc = length > 4096 ? 4096 : length;
                                this.ensurePositiveCountIsMutable(alloc / 8);
                                while (input.getBytesUntilLimit() > 0) {
                                    this.positiveCount_.addDouble(input.readDouble());
                                }
                                input.popLimit(limit);
                                continue block28;
                            }
                            case 122: {
                                input.readMessage(this.internalGetCreatedTimestampFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 0x10;
                                continue block28;
                            }
                            case 130: {
                                Exemplar m = input.readMessage(Exemplar.parser(), extensionRegistry);
                                if (this.exemplarsBuilder_ == null) {
                                    this.ensureExemplarsIsMutable();
                                    this.exemplars_.add(m);
                                    continue block28;
                                }
                                this.exemplarsBuilder_.addMessage(m);
                                continue block28;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasSampleCount() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public long getSampleCount() {
                return this.sampleCount_;
            }

            public Builder setSampleCount(long value) {
                this.sampleCount_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearSampleCount() {
                this.bitField0_ &= 0xFFFFFFFE;
                this.sampleCount_ = 0L;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasSampleCountFloat() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public double getSampleCountFloat() {
                return this.sampleCountFloat_;
            }

            public Builder setSampleCountFloat(double value) {
                this.sampleCountFloat_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder clearSampleCountFloat() {
                this.bitField0_ &= 0xFFFFFFFD;
                this.sampleCountFloat_ = 0.0;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasSampleSum() {
                return (this.bitField0_ & 4) != 0;
            }

            @Override
            public double getSampleSum() {
                return this.sampleSum_;
            }

            public Builder setSampleSum(double value) {
                this.sampleSum_ = value;
                this.bitField0_ |= 4;
                this.onChanged();
                return this;
            }

            public Builder clearSampleSum() {
                this.bitField0_ &= 0xFFFFFFFB;
                this.sampleSum_ = 0.0;
                this.onChanged();
                return this;
            }

            private void ensureBucketIsMutable() {
                if ((this.bitField0_ & 8) == 0) {
                    this.bucket_ = new ArrayList<Bucket>(this.bucket_);
                    this.bitField0_ |= 8;
                }
            }

            @Override
            public List<Bucket> getBucketList() {
                if (this.bucketBuilder_ == null) {
                    return Collections.unmodifiableList(this.bucket_);
                }
                return this.bucketBuilder_.getMessageList();
            }

            @Override
            public int getBucketCount() {
                if (this.bucketBuilder_ == null) {
                    return this.bucket_.size();
                }
                return this.bucketBuilder_.getCount();
            }

            @Override
            public Bucket getBucket(int index) {
                if (this.bucketBuilder_ == null) {
                    return this.bucket_.get(index);
                }
                return this.bucketBuilder_.getMessage(index);
            }

            public Builder setBucket(int index, Bucket value) {
                if (this.bucketBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureBucketIsMutable();
                    this.bucket_.set(index, value);
                    this.onChanged();
                } else {
                    this.bucketBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setBucket(int index, Bucket.Builder builderForValue) {
                if (this.bucketBuilder_ == null) {
                    this.ensureBucketIsMutable();
                    this.bucket_.set(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.bucketBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addBucket(Bucket value) {
                if (this.bucketBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureBucketIsMutable();
                    this.bucket_.add(value);
                    this.onChanged();
                } else {
                    this.bucketBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addBucket(int index, Bucket value) {
                if (this.bucketBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureBucketIsMutable();
                    this.bucket_.add(index, value);
                    this.onChanged();
                } else {
                    this.bucketBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addBucket(Bucket.Builder builderForValue) {
                if (this.bucketBuilder_ == null) {
                    this.ensureBucketIsMutable();
                    this.bucket_.add(builderForValue.build());
                    this.onChanged();
                } else {
                    this.bucketBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addBucket(int index, Bucket.Builder builderForValue) {
                if (this.bucketBuilder_ == null) {
                    this.ensureBucketIsMutable();
                    this.bucket_.add(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.bucketBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllBucket(Iterable<? extends Bucket> values2) {
                if (this.bucketBuilder_ == null) {
                    this.ensureBucketIsMutable();
                    AbstractMessageLite.Builder.addAll(values2, this.bucket_);
                    this.onChanged();
                } else {
                    this.bucketBuilder_.addAllMessages(values2);
                }
                return this;
            }

            public Builder clearBucket() {
                if (this.bucketBuilder_ == null) {
                    this.bucket_ = Collections.emptyList();
                    this.bitField0_ &= 0xFFFFFFF7;
                    this.onChanged();
                } else {
                    this.bucketBuilder_.clear();
                }
                return this;
            }

            public Builder removeBucket(int index) {
                if (this.bucketBuilder_ == null) {
                    this.ensureBucketIsMutable();
                    this.bucket_.remove(index);
                    this.onChanged();
                } else {
                    this.bucketBuilder_.remove(index);
                }
                return this;
            }

            public Bucket.Builder getBucketBuilder(int index) {
                return this.internalGetBucketFieldBuilder().getBuilder(index);
            }

            @Override
            public BucketOrBuilder getBucketOrBuilder(int index) {
                if (this.bucketBuilder_ == null) {
                    return this.bucket_.get(index);
                }
                return this.bucketBuilder_.getMessageOrBuilder(index);
            }

            @Override
            public List<? extends BucketOrBuilder> getBucketOrBuilderList() {
                if (this.bucketBuilder_ != null) {
                    return this.bucketBuilder_.getMessageOrBuilderList();
                }
                return Collections.unmodifiableList(this.bucket_);
            }

            public Bucket.Builder addBucketBuilder() {
                return this.internalGetBucketFieldBuilder().addBuilder(Bucket.getDefaultInstance());
            }

            public Bucket.Builder addBucketBuilder(int index) {
                return this.internalGetBucketFieldBuilder().addBuilder(index, Bucket.getDefaultInstance());
            }

            public List<Bucket.Builder> getBucketBuilderList() {
                return this.internalGetBucketFieldBuilder().getBuilderList();
            }

            private RepeatedFieldBuilder<Bucket, Bucket.Builder, BucketOrBuilder> internalGetBucketFieldBuilder() {
                if (this.bucketBuilder_ == null) {
                    this.bucketBuilder_ = new RepeatedFieldBuilder(this.bucket_, (this.bitField0_ & 8) != 0, this.getParentForChildren(), this.isClean());
                    this.bucket_ = null;
                }
                return this.bucketBuilder_;
            }

            @Override
            public boolean hasCreatedTimestamp() {
                return (this.bitField0_ & 0x10) != 0;
            }

            @Override
            public Timestamp getCreatedTimestamp() {
                if (this.createdTimestampBuilder_ == null) {
                    return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
                }
                return this.createdTimestampBuilder_.getMessage();
            }

            public Builder setCreatedTimestamp(Timestamp value) {
                if (this.createdTimestampBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.createdTimestamp_ = value;
                } else {
                    this.createdTimestampBuilder_.setMessage(value);
                }
                this.bitField0_ |= 0x10;
                this.onChanged();
                return this;
            }

            public Builder setCreatedTimestamp(Timestamp.Builder builderForValue) {
                if (this.createdTimestampBuilder_ == null) {
                    this.createdTimestamp_ = builderForValue.build();
                } else {
                    this.createdTimestampBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 0x10;
                this.onChanged();
                return this;
            }

            public Builder mergeCreatedTimestamp(Timestamp value) {
                if (this.createdTimestampBuilder_ == null) {
                    if ((this.bitField0_ & 0x10) != 0 && this.createdTimestamp_ != null && this.createdTimestamp_ != Timestamp.getDefaultInstance()) {
                        this.getCreatedTimestampBuilder().mergeFrom(value);
                    } else {
                        this.createdTimestamp_ = value;
                    }
                } else {
                    this.createdTimestampBuilder_.mergeFrom(value);
                }
                if (this.createdTimestamp_ != null) {
                    this.bitField0_ |= 0x10;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearCreatedTimestamp() {
                this.bitField0_ &= 0xFFFFFFEF;
                this.createdTimestamp_ = null;
                if (this.createdTimestampBuilder_ != null) {
                    this.createdTimestampBuilder_.dispose();
                    this.createdTimestampBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Timestamp.Builder getCreatedTimestampBuilder() {
                this.bitField0_ |= 0x10;
                this.onChanged();
                return this.internalGetCreatedTimestampFieldBuilder().getBuilder();
            }

            @Override
            public TimestampOrBuilder getCreatedTimestampOrBuilder() {
                if (this.createdTimestampBuilder_ != null) {
                    return this.createdTimestampBuilder_.getMessageOrBuilder();
                }
                return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
            }

            private SingleFieldBuilder<Timestamp, Timestamp.Builder, TimestampOrBuilder> internalGetCreatedTimestampFieldBuilder() {
                if (this.createdTimestampBuilder_ == null) {
                    this.createdTimestampBuilder_ = new SingleFieldBuilder(this.getCreatedTimestamp(), this.getParentForChildren(), this.isClean());
                    this.createdTimestamp_ = null;
                }
                return this.createdTimestampBuilder_;
            }

            @Override
            public boolean hasSchema() {
                return (this.bitField0_ & 0x20) != 0;
            }

            @Override
            public int getSchema() {
                return this.schema_;
            }

            public Builder setSchema(int value) {
                this.schema_ = value;
                this.bitField0_ |= 0x20;
                this.onChanged();
                return this;
            }

            public Builder clearSchema() {
                this.bitField0_ &= 0xFFFFFFDF;
                this.schema_ = 0;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasZeroThreshold() {
                return (this.bitField0_ & 0x40) != 0;
            }

            @Override
            public double getZeroThreshold() {
                return this.zeroThreshold_;
            }

            public Builder setZeroThreshold(double value) {
                this.zeroThreshold_ = value;
                this.bitField0_ |= 0x40;
                this.onChanged();
                return this;
            }

            public Builder clearZeroThreshold() {
                this.bitField0_ &= 0xFFFFFFBF;
                this.zeroThreshold_ = 0.0;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasZeroCount() {
                return (this.bitField0_ & 0x80) != 0;
            }

            @Override
            public long getZeroCount() {
                return this.zeroCount_;
            }

            public Builder setZeroCount(long value) {
                this.zeroCount_ = value;
                this.bitField0_ |= 0x80;
                this.onChanged();
                return this;
            }

            public Builder clearZeroCount() {
                this.bitField0_ &= 0xFFFFFF7F;
                this.zeroCount_ = 0L;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasZeroCountFloat() {
                return (this.bitField0_ & 0x100) != 0;
            }

            @Override
            public double getZeroCountFloat() {
                return this.zeroCountFloat_;
            }

            public Builder setZeroCountFloat(double value) {
                this.zeroCountFloat_ = value;
                this.bitField0_ |= 0x100;
                this.onChanged();
                return this;
            }

            public Builder clearZeroCountFloat() {
                this.bitField0_ &= 0xFFFFFEFF;
                this.zeroCountFloat_ = 0.0;
                this.onChanged();
                return this;
            }

            private void ensureNegativeSpanIsMutable() {
                if ((this.bitField0_ & 0x200) == 0) {
                    this.negativeSpan_ = new ArrayList<BucketSpan>(this.negativeSpan_);
                    this.bitField0_ |= 0x200;
                }
            }

            @Override
            public List<BucketSpan> getNegativeSpanList() {
                if (this.negativeSpanBuilder_ == null) {
                    return Collections.unmodifiableList(this.negativeSpan_);
                }
                return this.negativeSpanBuilder_.getMessageList();
            }

            @Override
            public int getNegativeSpanCount() {
                if (this.negativeSpanBuilder_ == null) {
                    return this.negativeSpan_.size();
                }
                return this.negativeSpanBuilder_.getCount();
            }

            @Override
            public BucketSpan getNegativeSpan(int index) {
                if (this.negativeSpanBuilder_ == null) {
                    return this.negativeSpan_.get(index);
                }
                return this.negativeSpanBuilder_.getMessage(index);
            }

            public Builder setNegativeSpan(int index, BucketSpan value) {
                if (this.negativeSpanBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureNegativeSpanIsMutable();
                    this.negativeSpan_.set(index, value);
                    this.onChanged();
                } else {
                    this.negativeSpanBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setNegativeSpan(int index, BucketSpan.Builder builderForValue) {
                if (this.negativeSpanBuilder_ == null) {
                    this.ensureNegativeSpanIsMutable();
                    this.negativeSpan_.set(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.negativeSpanBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addNegativeSpan(BucketSpan value) {
                if (this.negativeSpanBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureNegativeSpanIsMutable();
                    this.negativeSpan_.add(value);
                    this.onChanged();
                } else {
                    this.negativeSpanBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addNegativeSpan(int index, BucketSpan value) {
                if (this.negativeSpanBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureNegativeSpanIsMutable();
                    this.negativeSpan_.add(index, value);
                    this.onChanged();
                } else {
                    this.negativeSpanBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addNegativeSpan(BucketSpan.Builder builderForValue) {
                if (this.negativeSpanBuilder_ == null) {
                    this.ensureNegativeSpanIsMutable();
                    this.negativeSpan_.add(builderForValue.build());
                    this.onChanged();
                } else {
                    this.negativeSpanBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addNegativeSpan(int index, BucketSpan.Builder builderForValue) {
                if (this.negativeSpanBuilder_ == null) {
                    this.ensureNegativeSpanIsMutable();
                    this.negativeSpan_.add(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.negativeSpanBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllNegativeSpan(Iterable<? extends BucketSpan> values2) {
                if (this.negativeSpanBuilder_ == null) {
                    this.ensureNegativeSpanIsMutable();
                    AbstractMessageLite.Builder.addAll(values2, this.negativeSpan_);
                    this.onChanged();
                } else {
                    this.negativeSpanBuilder_.addAllMessages(values2);
                }
                return this;
            }

            public Builder clearNegativeSpan() {
                if (this.negativeSpanBuilder_ == null) {
                    this.negativeSpan_ = Collections.emptyList();
                    this.bitField0_ &= 0xFFFFFDFF;
                    this.onChanged();
                } else {
                    this.negativeSpanBuilder_.clear();
                }
                return this;
            }

            public Builder removeNegativeSpan(int index) {
                if (this.negativeSpanBuilder_ == null) {
                    this.ensureNegativeSpanIsMutable();
                    this.negativeSpan_.remove(index);
                    this.onChanged();
                } else {
                    this.negativeSpanBuilder_.remove(index);
                }
                return this;
            }

            public BucketSpan.Builder getNegativeSpanBuilder(int index) {
                return this.internalGetNegativeSpanFieldBuilder().getBuilder(index);
            }

            @Override
            public BucketSpanOrBuilder getNegativeSpanOrBuilder(int index) {
                if (this.negativeSpanBuilder_ == null) {
                    return this.negativeSpan_.get(index);
                }
                return this.negativeSpanBuilder_.getMessageOrBuilder(index);
            }

            @Override
            public List<? extends BucketSpanOrBuilder> getNegativeSpanOrBuilderList() {
                if (this.negativeSpanBuilder_ != null) {
                    return this.negativeSpanBuilder_.getMessageOrBuilderList();
                }
                return Collections.unmodifiableList(this.negativeSpan_);
            }

            public BucketSpan.Builder addNegativeSpanBuilder() {
                return this.internalGetNegativeSpanFieldBuilder().addBuilder(BucketSpan.getDefaultInstance());
            }

            public BucketSpan.Builder addNegativeSpanBuilder(int index) {
                return this.internalGetNegativeSpanFieldBuilder().addBuilder(index, BucketSpan.getDefaultInstance());
            }

            public List<BucketSpan.Builder> getNegativeSpanBuilderList() {
                return this.internalGetNegativeSpanFieldBuilder().getBuilderList();
            }

            private RepeatedFieldBuilder<BucketSpan, BucketSpan.Builder, BucketSpanOrBuilder> internalGetNegativeSpanFieldBuilder() {
                if (this.negativeSpanBuilder_ == null) {
                    this.negativeSpanBuilder_ = new RepeatedFieldBuilder(this.negativeSpan_, (this.bitField0_ & 0x200) != 0, this.getParentForChildren(), this.isClean());
                    this.negativeSpan_ = null;
                }
                return this.negativeSpanBuilder_;
            }

            private void ensureNegativeDeltaIsMutable() {
                if (!this.negativeDelta_.isModifiable()) {
                    this.negativeDelta_ = (Internal.LongList)Histogram.makeMutableCopy(this.negativeDelta_);
                }
                this.bitField0_ |= 0x400;
            }

            @Override
            public List<Long> getNegativeDeltaList() {
                this.negativeDelta_.makeImmutable();
                return this.negativeDelta_;
            }

            @Override
            public int getNegativeDeltaCount() {
                return this.negativeDelta_.size();
            }

            @Override
            public long getNegativeDelta(int index) {
                return this.negativeDelta_.getLong(index);
            }

            public Builder setNegativeDelta(int index, long value) {
                this.ensureNegativeDeltaIsMutable();
                this.negativeDelta_.setLong(index, value);
                this.bitField0_ |= 0x400;
                this.onChanged();
                return this;
            }

            public Builder addNegativeDelta(long value) {
                this.ensureNegativeDeltaIsMutable();
                this.negativeDelta_.addLong(value);
                this.bitField0_ |= 0x400;
                this.onChanged();
                return this;
            }

            public Builder addAllNegativeDelta(Iterable<? extends Long> values2) {
                this.ensureNegativeDeltaIsMutable();
                AbstractMessageLite.Builder.addAll(values2, this.negativeDelta_);
                this.bitField0_ |= 0x400;
                this.onChanged();
                return this;
            }

            public Builder clearNegativeDelta() {
                this.negativeDelta_ = Histogram.emptyLongList();
                this.bitField0_ &= 0xFFFFFBFF;
                this.onChanged();
                return this;
            }

            private void ensureNegativeCountIsMutable() {
                if (!this.negativeCount_.isModifiable()) {
                    this.negativeCount_ = (Internal.DoubleList)Histogram.makeMutableCopy(this.negativeCount_);
                }
                this.bitField0_ |= 0x800;
            }

            private void ensureNegativeCountIsMutable(int capacity) {
                if (!this.negativeCount_.isModifiable()) {
                    this.negativeCount_ = (Internal.DoubleList)Histogram.makeMutableCopy(this.negativeCount_, capacity);
                }
                this.bitField0_ |= 0x800;
            }

            @Override
            public List<Double> getNegativeCountList() {
                this.negativeCount_.makeImmutable();
                return this.negativeCount_;
            }

            @Override
            public int getNegativeCountCount() {
                return this.negativeCount_.size();
            }

            @Override
            public double getNegativeCount(int index) {
                return this.negativeCount_.getDouble(index);
            }

            public Builder setNegativeCount(int index, double value) {
                this.ensureNegativeCountIsMutable();
                this.negativeCount_.setDouble(index, value);
                this.bitField0_ |= 0x800;
                this.onChanged();
                return this;
            }

            public Builder addNegativeCount(double value) {
                this.ensureNegativeCountIsMutable();
                this.negativeCount_.addDouble(value);
                this.bitField0_ |= 0x800;
                this.onChanged();
                return this;
            }

            public Builder addAllNegativeCount(Iterable<? extends Double> values2) {
                this.ensureNegativeCountIsMutable();
                AbstractMessageLite.Builder.addAll(values2, this.negativeCount_);
                this.bitField0_ |= 0x800;
                this.onChanged();
                return this;
            }

            public Builder clearNegativeCount() {
                this.negativeCount_ = Histogram.emptyDoubleList();
                this.bitField0_ &= 0xFFFFF7FF;
                this.onChanged();
                return this;
            }

            private void ensurePositiveSpanIsMutable() {
                if ((this.bitField0_ & 0x1000) == 0) {
                    this.positiveSpan_ = new ArrayList<BucketSpan>(this.positiveSpan_);
                    this.bitField0_ |= 0x1000;
                }
            }

            @Override
            public List<BucketSpan> getPositiveSpanList() {
                if (this.positiveSpanBuilder_ == null) {
                    return Collections.unmodifiableList(this.positiveSpan_);
                }
                return this.positiveSpanBuilder_.getMessageList();
            }

            @Override
            public int getPositiveSpanCount() {
                if (this.positiveSpanBuilder_ == null) {
                    return this.positiveSpan_.size();
                }
                return this.positiveSpanBuilder_.getCount();
            }

            @Override
            public BucketSpan getPositiveSpan(int index) {
                if (this.positiveSpanBuilder_ == null) {
                    return this.positiveSpan_.get(index);
                }
                return this.positiveSpanBuilder_.getMessage(index);
            }

            public Builder setPositiveSpan(int index, BucketSpan value) {
                if (this.positiveSpanBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensurePositiveSpanIsMutable();
                    this.positiveSpan_.set(index, value);
                    this.onChanged();
                } else {
                    this.positiveSpanBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setPositiveSpan(int index, BucketSpan.Builder builderForValue) {
                if (this.positiveSpanBuilder_ == null) {
                    this.ensurePositiveSpanIsMutable();
                    this.positiveSpan_.set(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.positiveSpanBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addPositiveSpan(BucketSpan value) {
                if (this.positiveSpanBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensurePositiveSpanIsMutable();
                    this.positiveSpan_.add(value);
                    this.onChanged();
                } else {
                    this.positiveSpanBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addPositiveSpan(int index, BucketSpan value) {
                if (this.positiveSpanBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensurePositiveSpanIsMutable();
                    this.positiveSpan_.add(index, value);
                    this.onChanged();
                } else {
                    this.positiveSpanBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addPositiveSpan(BucketSpan.Builder builderForValue) {
                if (this.positiveSpanBuilder_ == null) {
                    this.ensurePositiveSpanIsMutable();
                    this.positiveSpan_.add(builderForValue.build());
                    this.onChanged();
                } else {
                    this.positiveSpanBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addPositiveSpan(int index, BucketSpan.Builder builderForValue) {
                if (this.positiveSpanBuilder_ == null) {
                    this.ensurePositiveSpanIsMutable();
                    this.positiveSpan_.add(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.positiveSpanBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllPositiveSpan(Iterable<? extends BucketSpan> values2) {
                if (this.positiveSpanBuilder_ == null) {
                    this.ensurePositiveSpanIsMutable();
                    AbstractMessageLite.Builder.addAll(values2, this.positiveSpan_);
                    this.onChanged();
                } else {
                    this.positiveSpanBuilder_.addAllMessages(values2);
                }
                return this;
            }

            public Builder clearPositiveSpan() {
                if (this.positiveSpanBuilder_ == null) {
                    this.positiveSpan_ = Collections.emptyList();
                    this.bitField0_ &= 0xFFFFEFFF;
                    this.onChanged();
                } else {
                    this.positiveSpanBuilder_.clear();
                }
                return this;
            }

            public Builder removePositiveSpan(int index) {
                if (this.positiveSpanBuilder_ == null) {
                    this.ensurePositiveSpanIsMutable();
                    this.positiveSpan_.remove(index);
                    this.onChanged();
                } else {
                    this.positiveSpanBuilder_.remove(index);
                }
                return this;
            }

            public BucketSpan.Builder getPositiveSpanBuilder(int index) {
                return this.internalGetPositiveSpanFieldBuilder().getBuilder(index);
            }

            @Override
            public BucketSpanOrBuilder getPositiveSpanOrBuilder(int index) {
                if (this.positiveSpanBuilder_ == null) {
                    return this.positiveSpan_.get(index);
                }
                return this.positiveSpanBuilder_.getMessageOrBuilder(index);
            }

            @Override
            public List<? extends BucketSpanOrBuilder> getPositiveSpanOrBuilderList() {
                if (this.positiveSpanBuilder_ != null) {
                    return this.positiveSpanBuilder_.getMessageOrBuilderList();
                }
                return Collections.unmodifiableList(this.positiveSpan_);
            }

            public BucketSpan.Builder addPositiveSpanBuilder() {
                return this.internalGetPositiveSpanFieldBuilder().addBuilder(BucketSpan.getDefaultInstance());
            }

            public BucketSpan.Builder addPositiveSpanBuilder(int index) {
                return this.internalGetPositiveSpanFieldBuilder().addBuilder(index, BucketSpan.getDefaultInstance());
            }

            public List<BucketSpan.Builder> getPositiveSpanBuilderList() {
                return this.internalGetPositiveSpanFieldBuilder().getBuilderList();
            }

            private RepeatedFieldBuilder<BucketSpan, BucketSpan.Builder, BucketSpanOrBuilder> internalGetPositiveSpanFieldBuilder() {
                if (this.positiveSpanBuilder_ == null) {
                    this.positiveSpanBuilder_ = new RepeatedFieldBuilder(this.positiveSpan_, (this.bitField0_ & 0x1000) != 0, this.getParentForChildren(), this.isClean());
                    this.positiveSpan_ = null;
                }
                return this.positiveSpanBuilder_;
            }

            private void ensurePositiveDeltaIsMutable() {
                if (!this.positiveDelta_.isModifiable()) {
                    this.positiveDelta_ = (Internal.LongList)Histogram.makeMutableCopy(this.positiveDelta_);
                }
                this.bitField0_ |= 0x2000;
            }

            @Override
            public List<Long> getPositiveDeltaList() {
                this.positiveDelta_.makeImmutable();
                return this.positiveDelta_;
            }

            @Override
            public int getPositiveDeltaCount() {
                return this.positiveDelta_.size();
            }

            @Override
            public long getPositiveDelta(int index) {
                return this.positiveDelta_.getLong(index);
            }

            public Builder setPositiveDelta(int index, long value) {
                this.ensurePositiveDeltaIsMutable();
                this.positiveDelta_.setLong(index, value);
                this.bitField0_ |= 0x2000;
                this.onChanged();
                return this;
            }

            public Builder addPositiveDelta(long value) {
                this.ensurePositiveDeltaIsMutable();
                this.positiveDelta_.addLong(value);
                this.bitField0_ |= 0x2000;
                this.onChanged();
                return this;
            }

            public Builder addAllPositiveDelta(Iterable<? extends Long> values2) {
                this.ensurePositiveDeltaIsMutable();
                AbstractMessageLite.Builder.addAll(values2, this.positiveDelta_);
                this.bitField0_ |= 0x2000;
                this.onChanged();
                return this;
            }

            public Builder clearPositiveDelta() {
                this.positiveDelta_ = Histogram.emptyLongList();
                this.bitField0_ &= 0xFFFFDFFF;
                this.onChanged();
                return this;
            }

            private void ensurePositiveCountIsMutable() {
                if (!this.positiveCount_.isModifiable()) {
                    this.positiveCount_ = (Internal.DoubleList)Histogram.makeMutableCopy(this.positiveCount_);
                }
                this.bitField0_ |= 0x4000;
            }

            private void ensurePositiveCountIsMutable(int capacity) {
                if (!this.positiveCount_.isModifiable()) {
                    this.positiveCount_ = (Internal.DoubleList)Histogram.makeMutableCopy(this.positiveCount_, capacity);
                }
                this.bitField0_ |= 0x4000;
            }

            @Override
            public List<Double> getPositiveCountList() {
                this.positiveCount_.makeImmutable();
                return this.positiveCount_;
            }

            @Override
            public int getPositiveCountCount() {
                return this.positiveCount_.size();
            }

            @Override
            public double getPositiveCount(int index) {
                return this.positiveCount_.getDouble(index);
            }

            public Builder setPositiveCount(int index, double value) {
                this.ensurePositiveCountIsMutable();
                this.positiveCount_.setDouble(index, value);
                this.bitField0_ |= 0x4000;
                this.onChanged();
                return this;
            }

            public Builder addPositiveCount(double value) {
                this.ensurePositiveCountIsMutable();
                this.positiveCount_.addDouble(value);
                this.bitField0_ |= 0x4000;
                this.onChanged();
                return this;
            }

            public Builder addAllPositiveCount(Iterable<? extends Double> values2) {
                this.ensurePositiveCountIsMutable();
                AbstractMessageLite.Builder.addAll(values2, this.positiveCount_);
                this.bitField0_ |= 0x4000;
                this.onChanged();
                return this;
            }

            public Builder clearPositiveCount() {
                this.positiveCount_ = Histogram.emptyDoubleList();
                this.bitField0_ &= 0xFFFFBFFF;
                this.onChanged();
                return this;
            }

            private void ensureExemplarsIsMutable() {
                if ((this.bitField0_ & 0x8000) == 0) {
                    this.exemplars_ = new ArrayList<Exemplar>(this.exemplars_);
                    this.bitField0_ |= 0x8000;
                }
            }

            @Override
            public List<Exemplar> getExemplarsList() {
                if (this.exemplarsBuilder_ == null) {
                    return Collections.unmodifiableList(this.exemplars_);
                }
                return this.exemplarsBuilder_.getMessageList();
            }

            @Override
            public int getExemplarsCount() {
                if (this.exemplarsBuilder_ == null) {
                    return this.exemplars_.size();
                }
                return this.exemplarsBuilder_.getCount();
            }

            @Override
            public Exemplar getExemplars(int index) {
                if (this.exemplarsBuilder_ == null) {
                    return this.exemplars_.get(index);
                }
                return this.exemplarsBuilder_.getMessage(index);
            }

            public Builder setExemplars(int index, Exemplar value) {
                if (this.exemplarsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureExemplarsIsMutable();
                    this.exemplars_.set(index, value);
                    this.onChanged();
                } else {
                    this.exemplarsBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setExemplars(int index, Exemplar.Builder builderForValue) {
                if (this.exemplarsBuilder_ == null) {
                    this.ensureExemplarsIsMutable();
                    this.exemplars_.set(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.exemplarsBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addExemplars(Exemplar value) {
                if (this.exemplarsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureExemplarsIsMutable();
                    this.exemplars_.add(value);
                    this.onChanged();
                } else {
                    this.exemplarsBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addExemplars(int index, Exemplar value) {
                if (this.exemplarsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureExemplarsIsMutable();
                    this.exemplars_.add(index, value);
                    this.onChanged();
                } else {
                    this.exemplarsBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addExemplars(Exemplar.Builder builderForValue) {
                if (this.exemplarsBuilder_ == null) {
                    this.ensureExemplarsIsMutable();
                    this.exemplars_.add(builderForValue.build());
                    this.onChanged();
                } else {
                    this.exemplarsBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addExemplars(int index, Exemplar.Builder builderForValue) {
                if (this.exemplarsBuilder_ == null) {
                    this.ensureExemplarsIsMutable();
                    this.exemplars_.add(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.exemplarsBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllExemplars(Iterable<? extends Exemplar> values2) {
                if (this.exemplarsBuilder_ == null) {
                    this.ensureExemplarsIsMutable();
                    AbstractMessageLite.Builder.addAll(values2, this.exemplars_);
                    this.onChanged();
                } else {
                    this.exemplarsBuilder_.addAllMessages(values2);
                }
                return this;
            }

            public Builder clearExemplars() {
                if (this.exemplarsBuilder_ == null) {
                    this.exemplars_ = Collections.emptyList();
                    this.bitField0_ &= 0xFFFF7FFF;
                    this.onChanged();
                } else {
                    this.exemplarsBuilder_.clear();
                }
                return this;
            }

            public Builder removeExemplars(int index) {
                if (this.exemplarsBuilder_ == null) {
                    this.ensureExemplarsIsMutable();
                    this.exemplars_.remove(index);
                    this.onChanged();
                } else {
                    this.exemplarsBuilder_.remove(index);
                }
                return this;
            }

            public Exemplar.Builder getExemplarsBuilder(int index) {
                return this.internalGetExemplarsFieldBuilder().getBuilder(index);
            }

            @Override
            public ExemplarOrBuilder getExemplarsOrBuilder(int index) {
                if (this.exemplarsBuilder_ == null) {
                    return this.exemplars_.get(index);
                }
                return this.exemplarsBuilder_.getMessageOrBuilder(index);
            }

            @Override
            public List<? extends ExemplarOrBuilder> getExemplarsOrBuilderList() {
                if (this.exemplarsBuilder_ != null) {
                    return this.exemplarsBuilder_.getMessageOrBuilderList();
                }
                return Collections.unmodifiableList(this.exemplars_);
            }

            public Exemplar.Builder addExemplarsBuilder() {
                return this.internalGetExemplarsFieldBuilder().addBuilder(Exemplar.getDefaultInstance());
            }

            public Exemplar.Builder addExemplarsBuilder(int index) {
                return this.internalGetExemplarsFieldBuilder().addBuilder(index, Exemplar.getDefaultInstance());
            }

            public List<Exemplar.Builder> getExemplarsBuilderList() {
                return this.internalGetExemplarsFieldBuilder().getBuilderList();
            }

            private RepeatedFieldBuilder<Exemplar, Exemplar.Builder, ExemplarOrBuilder> internalGetExemplarsFieldBuilder() {
                if (this.exemplarsBuilder_ == null) {
                    this.exemplarsBuilder_ = new RepeatedFieldBuilder(this.exemplars_, (this.bitField0_ & 0x8000) != 0, this.getParentForChildren(), this.isClean());
                    this.exemplars_ = null;
                }
                return this.exemplarsBuilder_;
            }
        }
    }

    public static interface HistogramOrBuilder
    extends MessageOrBuilder {
        public boolean hasSampleCount();

        public long getSampleCount();

        public boolean hasSampleCountFloat();

        public double getSampleCountFloat();

        public boolean hasSampleSum();

        public double getSampleSum();

        public List<Bucket> getBucketList();

        public Bucket getBucket(int var1);

        public int getBucketCount();

        public List<? extends BucketOrBuilder> getBucketOrBuilderList();

        public BucketOrBuilder getBucketOrBuilder(int var1);

        public boolean hasCreatedTimestamp();

        public Timestamp getCreatedTimestamp();

        public TimestampOrBuilder getCreatedTimestampOrBuilder();

        public boolean hasSchema();

        public int getSchema();

        public boolean hasZeroThreshold();

        public double getZeroThreshold();

        public boolean hasZeroCount();

        public long getZeroCount();

        public boolean hasZeroCountFloat();

        public double getZeroCountFloat();

        public List<BucketSpan> getNegativeSpanList();

        public BucketSpan getNegativeSpan(int var1);

        public int getNegativeSpanCount();

        public List<? extends BucketSpanOrBuilder> getNegativeSpanOrBuilderList();

        public BucketSpanOrBuilder getNegativeSpanOrBuilder(int var1);

        public List<Long> getNegativeDeltaList();

        public int getNegativeDeltaCount();

        public long getNegativeDelta(int var1);

        public List<Double> getNegativeCountList();

        public int getNegativeCountCount();

        public double getNegativeCount(int var1);

        public List<BucketSpan> getPositiveSpanList();

        public BucketSpan getPositiveSpan(int var1);

        public int getPositiveSpanCount();

        public List<? extends BucketSpanOrBuilder> getPositiveSpanOrBuilderList();

        public BucketSpanOrBuilder getPositiveSpanOrBuilder(int var1);

        public List<Long> getPositiveDeltaList();

        public int getPositiveDeltaCount();

        public long getPositiveDelta(int var1);

        public List<Double> getPositiveCountList();

        public int getPositiveCountCount();

        public double getPositiveCount(int var1);

        public List<Exemplar> getExemplarsList();

        public Exemplar getExemplars(int var1);

        public int getExemplarsCount();

        public List<? extends ExemplarOrBuilder> getExemplarsOrBuilderList();

        public ExemplarOrBuilder getExemplarsOrBuilder(int var1);
    }

    public static final class Untyped
    extends GeneratedMessage
    implements UntypedOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int VALUE_FIELD_NUMBER = 1;
        private double value_ = 0.0;
        private byte memoizedIsInitialized = (byte)-1;
        private static final Untyped DEFAULT_INSTANCE;
        private static final Parser<Untyped> PARSER;

        private Untyped(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private Untyped() {
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_Untyped_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_Untyped_fieldAccessorTable.ensureFieldAccessorsInitialized(Untyped.class, Builder.class);
        }

        @Override
        public boolean hasValue() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public double getValue() {
            return this.value_;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                output.writeDouble(1, this.value_);
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputStream.computeDoubleSize(1, this.value_);
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Untyped)) {
                return super.equals(obj);
            }
            Untyped other = (Untyped)obj;
            if (this.hasValue() != other.hasValue()) {
                return false;
            }
            if (this.hasValue() && Double.doubleToLongBits(this.getValue()) != Double.doubleToLongBits(other.getValue())) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + Untyped.getDescriptor().hashCode();
            if (this.hasValue()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getValue()));
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static Untyped parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Untyped parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Untyped parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Untyped parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Untyped parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Untyped parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Untyped parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Untyped parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Untyped parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static Untyped parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static Untyped parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Untyped parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return Untyped.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Untyped prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static Untyped getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Untyped> parser() {
            return PARSER;
        }

        public Parser<Untyped> getParserForType() {
            return PARSER;
        }

        @Override
        public Untyped getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Untyped.class.getName());
            DEFAULT_INSTANCE = new Untyped();
            PARSER = new AbstractParser<Untyped>(){

                @Override
                public Untyped parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = Untyped.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements UntypedOrBuilder {
            private int bitField0_;
            private double value_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_Untyped_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_Untyped_fieldAccessorTable.ensureFieldAccessorsInitialized(Untyped.class, Builder.class);
            }

            private Builder() {
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.value_ = 0.0;
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_Untyped_descriptor;
            }

            @Override
            public Untyped getDefaultInstanceForType() {
                return Untyped.getDefaultInstance();
            }

            @Override
            public Untyped build() {
                Untyped result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public Untyped buildPartial() {
                Untyped result = new Untyped(this);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartial0(Untyped result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.value_ = this.value_;
                    to_bitField0_ |= 1;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof Untyped) {
                    return this.mergeFrom((Untyped)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(Untyped other) {
                if (other == Untyped.getDefaultInstance()) {
                    return this;
                }
                if (other.hasValue()) {
                    this.setValue(other.getValue());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block9: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block9;
                            }
                            case 9: {
                                this.value_ = input.readDouble();
                                this.bitField0_ |= 1;
                                continue block9;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasValue() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public double getValue() {
                return this.value_;
            }

            public Builder setValue(double value) {
                this.value_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearValue() {
                this.bitField0_ &= 0xFFFFFFFE;
                this.value_ = 0.0;
                this.onChanged();
                return this;
            }
        }
    }

    public static interface UntypedOrBuilder
    extends MessageOrBuilder {
        public boolean hasValue();

        public double getValue();
    }

    public static final class Summary
    extends GeneratedMessage
    implements SummaryOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int SAMPLE_COUNT_FIELD_NUMBER = 1;
        private long sampleCount_ = 0L;
        public static final int SAMPLE_SUM_FIELD_NUMBER = 2;
        private double sampleSum_ = 0.0;
        public static final int QUANTILE_FIELD_NUMBER = 3;
        private List<Quantile> quantile_;
        public static final int CREATED_TIMESTAMP_FIELD_NUMBER = 4;
        private Timestamp createdTimestamp_;
        private byte memoizedIsInitialized = (byte)-1;
        private static final Summary DEFAULT_INSTANCE;
        private static final Parser<Summary> PARSER;

        private Summary(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private Summary() {
            this.quantile_ = Collections.emptyList();
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_Summary_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_Summary_fieldAccessorTable.ensureFieldAccessorsInitialized(Summary.class, Builder.class);
        }

        @Override
        public boolean hasSampleCount() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public long getSampleCount() {
            return this.sampleCount_;
        }

        @Override
        public boolean hasSampleSum() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public double getSampleSum() {
            return this.sampleSum_;
        }

        @Override
        public List<Quantile> getQuantileList() {
            return this.quantile_;
        }

        @Override
        public List<? extends QuantileOrBuilder> getQuantileOrBuilderList() {
            return this.quantile_;
        }

        @Override
        public int getQuantileCount() {
            return this.quantile_.size();
        }

        @Override
        public Quantile getQuantile(int index) {
            return this.quantile_.get(index);
        }

        @Override
        public QuantileOrBuilder getQuantileOrBuilder(int index) {
            return this.quantile_.get(index);
        }

        @Override
        public boolean hasCreatedTimestamp() {
            return (this.bitField0_ & 4) != 0;
        }

        @Override
        public Timestamp getCreatedTimestamp() {
            return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
        }

        @Override
        public TimestampOrBuilder getCreatedTimestampOrBuilder() {
            return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                output.writeUInt64(1, this.sampleCount_);
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeDouble(2, this.sampleSum_);
            }
            for (int i = 0; i < this.quantile_.size(); ++i) {
                output.writeMessage(3, this.quantile_.get(i));
            }
            if ((this.bitField0_ & 4) != 0) {
                output.writeMessage(4, this.getCreatedTimestamp());
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputStream.computeUInt64Size(1, this.sampleCount_);
            }
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputStream.computeDoubleSize(2, this.sampleSum_);
            }
            for (int i = 0; i < this.quantile_.size(); ++i) {
                size += CodedOutputStream.computeMessageSize(3, this.quantile_.get(i));
            }
            if ((this.bitField0_ & 4) != 0) {
                size += CodedOutputStream.computeMessageSize(4, this.getCreatedTimestamp());
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Summary)) {
                return super.equals(obj);
            }
            Summary other = (Summary)obj;
            if (this.hasSampleCount() != other.hasSampleCount()) {
                return false;
            }
            if (this.hasSampleCount() && this.getSampleCount() != other.getSampleCount()) {
                return false;
            }
            if (this.hasSampleSum() != other.hasSampleSum()) {
                return false;
            }
            if (this.hasSampleSum() && Double.doubleToLongBits(this.getSampleSum()) != Double.doubleToLongBits(other.getSampleSum())) {
                return false;
            }
            if (!this.getQuantileList().equals(other.getQuantileList())) {
                return false;
            }
            if (this.hasCreatedTimestamp() != other.hasCreatedTimestamp()) {
                return false;
            }
            if (this.hasCreatedTimestamp() && !this.getCreatedTimestamp().equals(other.getCreatedTimestamp())) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + Summary.getDescriptor().hashCode();
            if (this.hasSampleCount()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + Internal.hashLong(this.getSampleCount());
            }
            if (this.hasSampleSum()) {
                hash = 37 * hash + 2;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getSampleSum()));
            }
            if (this.getQuantileCount() > 0) {
                hash = 37 * hash + 3;
                hash = 53 * hash + this.getQuantileList().hashCode();
            }
            if (this.hasCreatedTimestamp()) {
                hash = 37 * hash + 4;
                hash = 53 * hash + this.getCreatedTimestamp().hashCode();
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static Summary parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Summary parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Summary parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Summary parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Summary parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Summary parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Summary parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Summary parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Summary parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static Summary parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static Summary parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Summary parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return Summary.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Summary prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static Summary getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Summary> parser() {
            return PARSER;
        }

        public Parser<Summary> getParserForType() {
            return PARSER;
        }

        @Override
        public Summary getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Summary.class.getName());
            DEFAULT_INSTANCE = new Summary();
            PARSER = new AbstractParser<Summary>(){

                @Override
                public Summary parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = Summary.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements SummaryOrBuilder {
            private int bitField0_;
            private long sampleCount_;
            private double sampleSum_;
            private List<Quantile> quantile_ = Collections.emptyList();
            private RepeatedFieldBuilder<Quantile, Quantile.Builder, QuantileOrBuilder> quantileBuilder_;
            private Timestamp createdTimestamp_;
            private SingleFieldBuilder<Timestamp, Timestamp.Builder, TimestampOrBuilder> createdTimestampBuilder_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_Summary_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_Summary_fieldAccessorTable.ensureFieldAccessorsInitialized(Summary.class, Builder.class);
            }

            private Builder() {
                this.maybeForceBuilderInitialization();
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
                this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (alwaysUseFieldBuilders) {
                    this.internalGetQuantileFieldBuilder();
                    this.internalGetCreatedTimestampFieldBuilder();
                }
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.sampleCount_ = 0L;
                this.sampleSum_ = 0.0;
                if (this.quantileBuilder_ == null) {
                    this.quantile_ = Collections.emptyList();
                } else {
                    this.quantile_ = null;
                    this.quantileBuilder_.clear();
                }
                this.bitField0_ &= 0xFFFFFFFB;
                this.createdTimestamp_ = null;
                if (this.createdTimestampBuilder_ != null) {
                    this.createdTimestampBuilder_.dispose();
                    this.createdTimestampBuilder_ = null;
                }
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_Summary_descriptor;
            }

            @Override
            public Summary getDefaultInstanceForType() {
                return Summary.getDefaultInstance();
            }

            @Override
            public Summary build() {
                Summary result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public Summary buildPartial() {
                Summary result = new Summary(this);
                this.buildPartialRepeatedFields(result);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartialRepeatedFields(Summary result) {
                if (this.quantileBuilder_ == null) {
                    if ((this.bitField0_ & 4) != 0) {
                        this.quantile_ = Collections.unmodifiableList(this.quantile_);
                        this.bitField0_ &= 0xFFFFFFFB;
                    }
                    result.quantile_ = this.quantile_;
                } else {
                    result.quantile_ = this.quantileBuilder_.build();
                }
            }

            private void buildPartial0(Summary result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.sampleCount_ = this.sampleCount_;
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 2) != 0) {
                    result.sampleSum_ = this.sampleSum_;
                    to_bitField0_ |= 2;
                }
                if ((from_bitField0_ & 8) != 0) {
                    result.createdTimestamp_ = this.createdTimestampBuilder_ == null ? this.createdTimestamp_ : this.createdTimestampBuilder_.build();
                    to_bitField0_ |= 4;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof Summary) {
                    return this.mergeFrom((Summary)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(Summary other) {
                if (other == Summary.getDefaultInstance()) {
                    return this;
                }
                if (other.hasSampleCount()) {
                    this.setSampleCount(other.getSampleCount());
                }
                if (other.hasSampleSum()) {
                    this.setSampleSum(other.getSampleSum());
                }
                if (this.quantileBuilder_ == null) {
                    if (!other.quantile_.isEmpty()) {
                        if (this.quantile_.isEmpty()) {
                            this.quantile_ = other.quantile_;
                            this.bitField0_ &= 0xFFFFFFFB;
                        } else {
                            this.ensureQuantileIsMutable();
                            this.quantile_.addAll(other.quantile_);
                        }
                        this.onChanged();
                    }
                } else if (!other.quantile_.isEmpty()) {
                    if (this.quantileBuilder_.isEmpty()) {
                        this.quantileBuilder_.dispose();
                        this.quantileBuilder_ = null;
                        this.quantile_ = other.quantile_;
                        this.bitField0_ &= 0xFFFFFFFB;
                        this.quantileBuilder_ = alwaysUseFieldBuilders ? this.internalGetQuantileFieldBuilder() : null;
                    } else {
                        this.quantileBuilder_.addAllMessages(other.quantile_);
                    }
                }
                if (other.hasCreatedTimestamp()) {
                    this.mergeCreatedTimestamp(other.getCreatedTimestamp());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block12: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block12;
                            }
                            case 8: {
                                this.sampleCount_ = input.readUInt64();
                                this.bitField0_ |= 1;
                                continue block12;
                            }
                            case 17: {
                                this.sampleSum_ = input.readDouble();
                                this.bitField0_ |= 2;
                                continue block12;
                            }
                            case 26: {
                                Quantile m = input.readMessage(Quantile.parser(), extensionRegistry);
                                if (this.quantileBuilder_ == null) {
                                    this.ensureQuantileIsMutable();
                                    this.quantile_.add(m);
                                    continue block12;
                                }
                                this.quantileBuilder_.addMessage(m);
                                continue block12;
                            }
                            case 34: {
                                input.readMessage(this.internalGetCreatedTimestampFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 8;
                                continue block12;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasSampleCount() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public long getSampleCount() {
                return this.sampleCount_;
            }

            public Builder setSampleCount(long value) {
                this.sampleCount_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearSampleCount() {
                this.bitField0_ &= 0xFFFFFFFE;
                this.sampleCount_ = 0L;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasSampleSum() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public double getSampleSum() {
                return this.sampleSum_;
            }

            public Builder setSampleSum(double value) {
                this.sampleSum_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder clearSampleSum() {
                this.bitField0_ &= 0xFFFFFFFD;
                this.sampleSum_ = 0.0;
                this.onChanged();
                return this;
            }

            private void ensureQuantileIsMutable() {
                if ((this.bitField0_ & 4) == 0) {
                    this.quantile_ = new ArrayList<Quantile>(this.quantile_);
                    this.bitField0_ |= 4;
                }
            }

            @Override
            public List<Quantile> getQuantileList() {
                if (this.quantileBuilder_ == null) {
                    return Collections.unmodifiableList(this.quantile_);
                }
                return this.quantileBuilder_.getMessageList();
            }

            @Override
            public int getQuantileCount() {
                if (this.quantileBuilder_ == null) {
                    return this.quantile_.size();
                }
                return this.quantileBuilder_.getCount();
            }

            @Override
            public Quantile getQuantile(int index) {
                if (this.quantileBuilder_ == null) {
                    return this.quantile_.get(index);
                }
                return this.quantileBuilder_.getMessage(index);
            }

            public Builder setQuantile(int index, Quantile value) {
                if (this.quantileBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureQuantileIsMutable();
                    this.quantile_.set(index, value);
                    this.onChanged();
                } else {
                    this.quantileBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setQuantile(int index, Quantile.Builder builderForValue) {
                if (this.quantileBuilder_ == null) {
                    this.ensureQuantileIsMutable();
                    this.quantile_.set(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.quantileBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addQuantile(Quantile value) {
                if (this.quantileBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureQuantileIsMutable();
                    this.quantile_.add(value);
                    this.onChanged();
                } else {
                    this.quantileBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addQuantile(int index, Quantile value) {
                if (this.quantileBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.ensureQuantileIsMutable();
                    this.quantile_.add(index, value);
                    this.onChanged();
                } else {
                    this.quantileBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addQuantile(Quantile.Builder builderForValue) {
                if (this.quantileBuilder_ == null) {
                    this.ensureQuantileIsMutable();
                    this.quantile_.add(builderForValue.build());
                    this.onChanged();
                } else {
                    this.quantileBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addQuantile(int index, Quantile.Builder builderForValue) {
                if (this.quantileBuilder_ == null) {
                    this.ensureQuantileIsMutable();
                    this.quantile_.add(index, builderForValue.build());
                    this.onChanged();
                } else {
                    this.quantileBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllQuantile(Iterable<? extends Quantile> values2) {
                if (this.quantileBuilder_ == null) {
                    this.ensureQuantileIsMutable();
                    AbstractMessageLite.Builder.addAll(values2, this.quantile_);
                    this.onChanged();
                } else {
                    this.quantileBuilder_.addAllMessages(values2);
                }
                return this;
            }

            public Builder clearQuantile() {
                if (this.quantileBuilder_ == null) {
                    this.quantile_ = Collections.emptyList();
                    this.bitField0_ &= 0xFFFFFFFB;
                    this.onChanged();
                } else {
                    this.quantileBuilder_.clear();
                }
                return this;
            }

            public Builder removeQuantile(int index) {
                if (this.quantileBuilder_ == null) {
                    this.ensureQuantileIsMutable();
                    this.quantile_.remove(index);
                    this.onChanged();
                } else {
                    this.quantileBuilder_.remove(index);
                }
                return this;
            }

            public Quantile.Builder getQuantileBuilder(int index) {
                return this.internalGetQuantileFieldBuilder().getBuilder(index);
            }

            @Override
            public QuantileOrBuilder getQuantileOrBuilder(int index) {
                if (this.quantileBuilder_ == null) {
                    return this.quantile_.get(index);
                }
                return this.quantileBuilder_.getMessageOrBuilder(index);
            }

            @Override
            public List<? extends QuantileOrBuilder> getQuantileOrBuilderList() {
                if (this.quantileBuilder_ != null) {
                    return this.quantileBuilder_.getMessageOrBuilderList();
                }
                return Collections.unmodifiableList(this.quantile_);
            }

            public Quantile.Builder addQuantileBuilder() {
                return this.internalGetQuantileFieldBuilder().addBuilder(Quantile.getDefaultInstance());
            }

            public Quantile.Builder addQuantileBuilder(int index) {
                return this.internalGetQuantileFieldBuilder().addBuilder(index, Quantile.getDefaultInstance());
            }

            public List<Quantile.Builder> getQuantileBuilderList() {
                return this.internalGetQuantileFieldBuilder().getBuilderList();
            }

            private RepeatedFieldBuilder<Quantile, Quantile.Builder, QuantileOrBuilder> internalGetQuantileFieldBuilder() {
                if (this.quantileBuilder_ == null) {
                    this.quantileBuilder_ = new RepeatedFieldBuilder(this.quantile_, (this.bitField0_ & 4) != 0, this.getParentForChildren(), this.isClean());
                    this.quantile_ = null;
                }
                return this.quantileBuilder_;
            }

            @Override
            public boolean hasCreatedTimestamp() {
                return (this.bitField0_ & 8) != 0;
            }

            @Override
            public Timestamp getCreatedTimestamp() {
                if (this.createdTimestampBuilder_ == null) {
                    return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
                }
                return this.createdTimestampBuilder_.getMessage();
            }

            public Builder setCreatedTimestamp(Timestamp value) {
                if (this.createdTimestampBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.createdTimestamp_ = value;
                } else {
                    this.createdTimestampBuilder_.setMessage(value);
                }
                this.bitField0_ |= 8;
                this.onChanged();
                return this;
            }

            public Builder setCreatedTimestamp(Timestamp.Builder builderForValue) {
                if (this.createdTimestampBuilder_ == null) {
                    this.createdTimestamp_ = builderForValue.build();
                } else {
                    this.createdTimestampBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 8;
                this.onChanged();
                return this;
            }

            public Builder mergeCreatedTimestamp(Timestamp value) {
                if (this.createdTimestampBuilder_ == null) {
                    if ((this.bitField0_ & 8) != 0 && this.createdTimestamp_ != null && this.createdTimestamp_ != Timestamp.getDefaultInstance()) {
                        this.getCreatedTimestampBuilder().mergeFrom(value);
                    } else {
                        this.createdTimestamp_ = value;
                    }
                } else {
                    this.createdTimestampBuilder_.mergeFrom(value);
                }
                if (this.createdTimestamp_ != null) {
                    this.bitField0_ |= 8;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearCreatedTimestamp() {
                this.bitField0_ &= 0xFFFFFFF7;
                this.createdTimestamp_ = null;
                if (this.createdTimestampBuilder_ != null) {
                    this.createdTimestampBuilder_.dispose();
                    this.createdTimestampBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Timestamp.Builder getCreatedTimestampBuilder() {
                this.bitField0_ |= 8;
                this.onChanged();
                return this.internalGetCreatedTimestampFieldBuilder().getBuilder();
            }

            @Override
            public TimestampOrBuilder getCreatedTimestampOrBuilder() {
                if (this.createdTimestampBuilder_ != null) {
                    return this.createdTimestampBuilder_.getMessageOrBuilder();
                }
                return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
            }

            private SingleFieldBuilder<Timestamp, Timestamp.Builder, TimestampOrBuilder> internalGetCreatedTimestampFieldBuilder() {
                if (this.createdTimestampBuilder_ == null) {
                    this.createdTimestampBuilder_ = new SingleFieldBuilder(this.getCreatedTimestamp(), this.getParentForChildren(), this.isClean());
                    this.createdTimestamp_ = null;
                }
                return this.createdTimestampBuilder_;
            }
        }
    }

    public static interface SummaryOrBuilder
    extends MessageOrBuilder {
        public boolean hasSampleCount();

        public long getSampleCount();

        public boolean hasSampleSum();

        public double getSampleSum();

        public List<Quantile> getQuantileList();

        public Quantile getQuantile(int var1);

        public int getQuantileCount();

        public List<? extends QuantileOrBuilder> getQuantileOrBuilderList();

        public QuantileOrBuilder getQuantileOrBuilder(int var1);

        public boolean hasCreatedTimestamp();

        public Timestamp getCreatedTimestamp();

        public TimestampOrBuilder getCreatedTimestampOrBuilder();
    }

    public static final class Quantile
    extends GeneratedMessage
    implements QuantileOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int QUANTILE_FIELD_NUMBER = 1;
        private double quantile_ = 0.0;
        public static final int VALUE_FIELD_NUMBER = 2;
        private double value_ = 0.0;
        private byte memoizedIsInitialized = (byte)-1;
        private static final Quantile DEFAULT_INSTANCE;
        private static final Parser<Quantile> PARSER;

        private Quantile(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private Quantile() {
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_Quantile_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_Quantile_fieldAccessorTable.ensureFieldAccessorsInitialized(Quantile.class, Builder.class);
        }

        @Override
        public boolean hasQuantile() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public double getQuantile() {
            return this.quantile_;
        }

        @Override
        public boolean hasValue() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public double getValue() {
            return this.value_;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                output.writeDouble(1, this.quantile_);
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeDouble(2, this.value_);
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputStream.computeDoubleSize(1, this.quantile_);
            }
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputStream.computeDoubleSize(2, this.value_);
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Quantile)) {
                return super.equals(obj);
            }
            Quantile other = (Quantile)obj;
            if (this.hasQuantile() != other.hasQuantile()) {
                return false;
            }
            if (this.hasQuantile() && Double.doubleToLongBits(this.getQuantile()) != Double.doubleToLongBits(other.getQuantile())) {
                return false;
            }
            if (this.hasValue() != other.hasValue()) {
                return false;
            }
            if (this.hasValue() && Double.doubleToLongBits(this.getValue()) != Double.doubleToLongBits(other.getValue())) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + Quantile.getDescriptor().hashCode();
            if (this.hasQuantile()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getQuantile()));
            }
            if (this.hasValue()) {
                hash = 37 * hash + 2;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getValue()));
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static Quantile parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Quantile parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Quantile parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Quantile parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Quantile parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Quantile parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Quantile parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Quantile parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Quantile parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static Quantile parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static Quantile parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Quantile parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return Quantile.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Quantile prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static Quantile getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Quantile> parser() {
            return PARSER;
        }

        public Parser<Quantile> getParserForType() {
            return PARSER;
        }

        @Override
        public Quantile getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Quantile.class.getName());
            DEFAULT_INSTANCE = new Quantile();
            PARSER = new AbstractParser<Quantile>(){

                @Override
                public Quantile parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = Quantile.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements QuantileOrBuilder {
            private int bitField0_;
            private double quantile_;
            private double value_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_Quantile_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_Quantile_fieldAccessorTable.ensureFieldAccessorsInitialized(Quantile.class, Builder.class);
            }

            private Builder() {
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.quantile_ = 0.0;
                this.value_ = 0.0;
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_Quantile_descriptor;
            }

            @Override
            public Quantile getDefaultInstanceForType() {
                return Quantile.getDefaultInstance();
            }

            @Override
            public Quantile build() {
                Quantile result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public Quantile buildPartial() {
                Quantile result = new Quantile(this);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartial0(Quantile result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.quantile_ = this.quantile_;
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 2) != 0) {
                    result.value_ = this.value_;
                    to_bitField0_ |= 2;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof Quantile) {
                    return this.mergeFrom((Quantile)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(Quantile other) {
                if (other == Quantile.getDefaultInstance()) {
                    return this;
                }
                if (other.hasQuantile()) {
                    this.setQuantile(other.getQuantile());
                }
                if (other.hasValue()) {
                    this.setValue(other.getValue());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block10: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block10;
                            }
                            case 9: {
                                this.quantile_ = input.readDouble();
                                this.bitField0_ |= 1;
                                continue block10;
                            }
                            case 17: {
                                this.value_ = input.readDouble();
                                this.bitField0_ |= 2;
                                continue block10;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasQuantile() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public double getQuantile() {
                return this.quantile_;
            }

            public Builder setQuantile(double value) {
                this.quantile_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearQuantile() {
                this.bitField0_ &= 0xFFFFFFFE;
                this.quantile_ = 0.0;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasValue() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public double getValue() {
                return this.value_;
            }

            public Builder setValue(double value) {
                this.value_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder clearValue() {
                this.bitField0_ &= 0xFFFFFFFD;
                this.value_ = 0.0;
                this.onChanged();
                return this;
            }
        }
    }

    public static interface QuantileOrBuilder
    extends MessageOrBuilder {
        public boolean hasQuantile();

        public double getQuantile();

        public boolean hasValue();

        public double getValue();
    }

    public static final class Counter
    extends GeneratedMessage
    implements CounterOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int VALUE_FIELD_NUMBER = 1;
        private double value_ = 0.0;
        public static final int EXEMPLAR_FIELD_NUMBER = 2;
        private Exemplar exemplar_;
        public static final int CREATED_TIMESTAMP_FIELD_NUMBER = 3;
        private Timestamp createdTimestamp_;
        private byte memoizedIsInitialized = (byte)-1;
        private static final Counter DEFAULT_INSTANCE;
        private static final Parser<Counter> PARSER;

        private Counter(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private Counter() {
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_Counter_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_Counter_fieldAccessorTable.ensureFieldAccessorsInitialized(Counter.class, Builder.class);
        }

        @Override
        public boolean hasValue() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public double getValue() {
            return this.value_;
        }

        @Override
        public boolean hasExemplar() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public Exemplar getExemplar() {
            return this.exemplar_ == null ? Exemplar.getDefaultInstance() : this.exemplar_;
        }

        @Override
        public ExemplarOrBuilder getExemplarOrBuilder() {
            return this.exemplar_ == null ? Exemplar.getDefaultInstance() : this.exemplar_;
        }

        @Override
        public boolean hasCreatedTimestamp() {
            return (this.bitField0_ & 4) != 0;
        }

        @Override
        public Timestamp getCreatedTimestamp() {
            return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
        }

        @Override
        public TimestampOrBuilder getCreatedTimestampOrBuilder() {
            return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                output.writeDouble(1, this.value_);
            }
            if ((this.bitField0_ & 2) != 0) {
                output.writeMessage(2, this.getExemplar());
            }
            if ((this.bitField0_ & 4) != 0) {
                output.writeMessage(3, this.getCreatedTimestamp());
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputStream.computeDoubleSize(1, this.value_);
            }
            if ((this.bitField0_ & 2) != 0) {
                size += CodedOutputStream.computeMessageSize(2, this.getExemplar());
            }
            if ((this.bitField0_ & 4) != 0) {
                size += CodedOutputStream.computeMessageSize(3, this.getCreatedTimestamp());
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Counter)) {
                return super.equals(obj);
            }
            Counter other = (Counter)obj;
            if (this.hasValue() != other.hasValue()) {
                return false;
            }
            if (this.hasValue() && Double.doubleToLongBits(this.getValue()) != Double.doubleToLongBits(other.getValue())) {
                return false;
            }
            if (this.hasExemplar() != other.hasExemplar()) {
                return false;
            }
            if (this.hasExemplar() && !this.getExemplar().equals(other.getExemplar())) {
                return false;
            }
            if (this.hasCreatedTimestamp() != other.hasCreatedTimestamp()) {
                return false;
            }
            if (this.hasCreatedTimestamp() && !this.getCreatedTimestamp().equals(other.getCreatedTimestamp())) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + Counter.getDescriptor().hashCode();
            if (this.hasValue()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getValue()));
            }
            if (this.hasExemplar()) {
                hash = 37 * hash + 2;
                hash = 53 * hash + this.getExemplar().hashCode();
            }
            if (this.hasCreatedTimestamp()) {
                hash = 37 * hash + 3;
                hash = 53 * hash + this.getCreatedTimestamp().hashCode();
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static Counter parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Counter parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Counter parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Counter parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Counter parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Counter parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Counter parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Counter parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Counter parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static Counter parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static Counter parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Counter parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return Counter.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Counter prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static Counter getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Counter> parser() {
            return PARSER;
        }

        public Parser<Counter> getParserForType() {
            return PARSER;
        }

        @Override
        public Counter getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Counter.class.getName());
            DEFAULT_INSTANCE = new Counter();
            PARSER = new AbstractParser<Counter>(){

                @Override
                public Counter parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = Counter.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements CounterOrBuilder {
            private int bitField0_;
            private double value_;
            private Exemplar exemplar_;
            private SingleFieldBuilder<Exemplar, Exemplar.Builder, ExemplarOrBuilder> exemplarBuilder_;
            private Timestamp createdTimestamp_;
            private SingleFieldBuilder<Timestamp, Timestamp.Builder, TimestampOrBuilder> createdTimestampBuilder_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_Counter_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_Counter_fieldAccessorTable.ensureFieldAccessorsInitialized(Counter.class, Builder.class);
            }

            private Builder() {
                this.maybeForceBuilderInitialization();
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
                this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (alwaysUseFieldBuilders) {
                    this.internalGetExemplarFieldBuilder();
                    this.internalGetCreatedTimestampFieldBuilder();
                }
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.value_ = 0.0;
                this.exemplar_ = null;
                if (this.exemplarBuilder_ != null) {
                    this.exemplarBuilder_.dispose();
                    this.exemplarBuilder_ = null;
                }
                this.createdTimestamp_ = null;
                if (this.createdTimestampBuilder_ != null) {
                    this.createdTimestampBuilder_.dispose();
                    this.createdTimestampBuilder_ = null;
                }
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_Counter_descriptor;
            }

            @Override
            public Counter getDefaultInstanceForType() {
                return Counter.getDefaultInstance();
            }

            @Override
            public Counter build() {
                Counter result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public Counter buildPartial() {
                Counter result = new Counter(this);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartial0(Counter result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.value_ = this.value_;
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 2) != 0) {
                    result.exemplar_ = this.exemplarBuilder_ == null ? this.exemplar_ : this.exemplarBuilder_.build();
                    to_bitField0_ |= 2;
                }
                if ((from_bitField0_ & 4) != 0) {
                    result.createdTimestamp_ = this.createdTimestampBuilder_ == null ? this.createdTimestamp_ : this.createdTimestampBuilder_.build();
                    to_bitField0_ |= 4;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof Counter) {
                    return this.mergeFrom((Counter)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(Counter other) {
                if (other == Counter.getDefaultInstance()) {
                    return this;
                }
                if (other.hasValue()) {
                    this.setValue(other.getValue());
                }
                if (other.hasExemplar()) {
                    this.mergeExemplar(other.getExemplar());
                }
                if (other.hasCreatedTimestamp()) {
                    this.mergeCreatedTimestamp(other.getCreatedTimestamp());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block11: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block11;
                            }
                            case 9: {
                                this.value_ = input.readDouble();
                                this.bitField0_ |= 1;
                                continue block11;
                            }
                            case 18: {
                                input.readMessage(this.internalGetExemplarFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 2;
                                continue block11;
                            }
                            case 26: {
                                input.readMessage(this.internalGetCreatedTimestampFieldBuilder().getBuilder(), extensionRegistry);
                                this.bitField0_ |= 4;
                                continue block11;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasValue() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public double getValue() {
                return this.value_;
            }

            public Builder setValue(double value) {
                this.value_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearValue() {
                this.bitField0_ &= 0xFFFFFFFE;
                this.value_ = 0.0;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasExemplar() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public Exemplar getExemplar() {
                if (this.exemplarBuilder_ == null) {
                    return this.exemplar_ == null ? Exemplar.getDefaultInstance() : this.exemplar_;
                }
                return this.exemplarBuilder_.getMessage();
            }

            public Builder setExemplar(Exemplar value) {
                if (this.exemplarBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.exemplar_ = value;
                } else {
                    this.exemplarBuilder_.setMessage(value);
                }
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder setExemplar(Exemplar.Builder builderForValue) {
                if (this.exemplarBuilder_ == null) {
                    this.exemplar_ = builderForValue.build();
                } else {
                    this.exemplarBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder mergeExemplar(Exemplar value) {
                if (this.exemplarBuilder_ == null) {
                    if ((this.bitField0_ & 2) != 0 && this.exemplar_ != null && this.exemplar_ != Exemplar.getDefaultInstance()) {
                        this.getExemplarBuilder().mergeFrom(value);
                    } else {
                        this.exemplar_ = value;
                    }
                } else {
                    this.exemplarBuilder_.mergeFrom(value);
                }
                if (this.exemplar_ != null) {
                    this.bitField0_ |= 2;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearExemplar() {
                this.bitField0_ &= 0xFFFFFFFD;
                this.exemplar_ = null;
                if (this.exemplarBuilder_ != null) {
                    this.exemplarBuilder_.dispose();
                    this.exemplarBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Exemplar.Builder getExemplarBuilder() {
                this.bitField0_ |= 2;
                this.onChanged();
                return this.internalGetExemplarFieldBuilder().getBuilder();
            }

            @Override
            public ExemplarOrBuilder getExemplarOrBuilder() {
                if (this.exemplarBuilder_ != null) {
                    return this.exemplarBuilder_.getMessageOrBuilder();
                }
                return this.exemplar_ == null ? Exemplar.getDefaultInstance() : this.exemplar_;
            }

            private SingleFieldBuilder<Exemplar, Exemplar.Builder, ExemplarOrBuilder> internalGetExemplarFieldBuilder() {
                if (this.exemplarBuilder_ == null) {
                    this.exemplarBuilder_ = new SingleFieldBuilder(this.getExemplar(), this.getParentForChildren(), this.isClean());
                    this.exemplar_ = null;
                }
                return this.exemplarBuilder_;
            }

            @Override
            public boolean hasCreatedTimestamp() {
                return (this.bitField0_ & 4) != 0;
            }

            @Override
            public Timestamp getCreatedTimestamp() {
                if (this.createdTimestampBuilder_ == null) {
                    return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
                }
                return this.createdTimestampBuilder_.getMessage();
            }

            public Builder setCreatedTimestamp(Timestamp value) {
                if (this.createdTimestampBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    this.createdTimestamp_ = value;
                } else {
                    this.createdTimestampBuilder_.setMessage(value);
                }
                this.bitField0_ |= 4;
                this.onChanged();
                return this;
            }

            public Builder setCreatedTimestamp(Timestamp.Builder builderForValue) {
                if (this.createdTimestampBuilder_ == null) {
                    this.createdTimestamp_ = builderForValue.build();
                } else {
                    this.createdTimestampBuilder_.setMessage(builderForValue.build());
                }
                this.bitField0_ |= 4;
                this.onChanged();
                return this;
            }

            public Builder mergeCreatedTimestamp(Timestamp value) {
                if (this.createdTimestampBuilder_ == null) {
                    if ((this.bitField0_ & 4) != 0 && this.createdTimestamp_ != null && this.createdTimestamp_ != Timestamp.getDefaultInstance()) {
                        this.getCreatedTimestampBuilder().mergeFrom(value);
                    } else {
                        this.createdTimestamp_ = value;
                    }
                } else {
                    this.createdTimestampBuilder_.mergeFrom(value);
                }
                if (this.createdTimestamp_ != null) {
                    this.bitField0_ |= 4;
                    this.onChanged();
                }
                return this;
            }

            public Builder clearCreatedTimestamp() {
                this.bitField0_ &= 0xFFFFFFFB;
                this.createdTimestamp_ = null;
                if (this.createdTimestampBuilder_ != null) {
                    this.createdTimestampBuilder_.dispose();
                    this.createdTimestampBuilder_ = null;
                }
                this.onChanged();
                return this;
            }

            public Timestamp.Builder getCreatedTimestampBuilder() {
                this.bitField0_ |= 4;
                this.onChanged();
                return this.internalGetCreatedTimestampFieldBuilder().getBuilder();
            }

            @Override
            public TimestampOrBuilder getCreatedTimestampOrBuilder() {
                if (this.createdTimestampBuilder_ != null) {
                    return this.createdTimestampBuilder_.getMessageOrBuilder();
                }
                return this.createdTimestamp_ == null ? Timestamp.getDefaultInstance() : this.createdTimestamp_;
            }

            private SingleFieldBuilder<Timestamp, Timestamp.Builder, TimestampOrBuilder> internalGetCreatedTimestampFieldBuilder() {
                if (this.createdTimestampBuilder_ == null) {
                    this.createdTimestampBuilder_ = new SingleFieldBuilder(this.getCreatedTimestamp(), this.getParentForChildren(), this.isClean());
                    this.createdTimestamp_ = null;
                }
                return this.createdTimestampBuilder_;
            }
        }
    }

    public static interface CounterOrBuilder
    extends MessageOrBuilder {
        public boolean hasValue();

        public double getValue();

        public boolean hasExemplar();

        public Exemplar getExemplar();

        public ExemplarOrBuilder getExemplarOrBuilder();

        public boolean hasCreatedTimestamp();

        public Timestamp getCreatedTimestamp();

        public TimestampOrBuilder getCreatedTimestampOrBuilder();
    }

    public static final class Gauge
    extends GeneratedMessage
    implements GaugeOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int VALUE_FIELD_NUMBER = 1;
        private double value_ = 0.0;
        private byte memoizedIsInitialized = (byte)-1;
        private static final Gauge DEFAULT_INSTANCE;
        private static final Parser<Gauge> PARSER;

        private Gauge(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private Gauge() {
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_Gauge_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_Gauge_fieldAccessorTable.ensureFieldAccessorsInitialized(Gauge.class, Builder.class);
        }

        @Override
        public boolean hasValue() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public double getValue() {
            return this.value_;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                output.writeDouble(1, this.value_);
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
                size += CodedOutputStream.computeDoubleSize(1, this.value_);
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Gauge)) {
                return super.equals(obj);
            }
            Gauge other = (Gauge)obj;
            if (this.hasValue() != other.hasValue()) {
                return false;
            }
            if (this.hasValue() && Double.doubleToLongBits(this.getValue()) != Double.doubleToLongBits(other.getValue())) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + Gauge.getDescriptor().hashCode();
            if (this.hasValue()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getValue()));
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static Gauge parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Gauge parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Gauge parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Gauge parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Gauge parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static Gauge parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static Gauge parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Gauge parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static Gauge parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static Gauge parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static Gauge parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static Gauge parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return Gauge.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(Gauge prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static Gauge getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<Gauge> parser() {
            return PARSER;
        }

        public Parser<Gauge> getParserForType() {
            return PARSER;
        }

        @Override
        public Gauge getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", Gauge.class.getName());
            DEFAULT_INSTANCE = new Gauge();
            PARSER = new AbstractParser<Gauge>(){

                @Override
                public Gauge parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = Gauge.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements GaugeOrBuilder {
            private int bitField0_;
            private double value_;

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_Gauge_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_Gauge_fieldAccessorTable.ensureFieldAccessorsInitialized(Gauge.class, Builder.class);
            }

            private Builder() {
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.value_ = 0.0;
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_Gauge_descriptor;
            }

            @Override
            public Gauge getDefaultInstanceForType() {
                return Gauge.getDefaultInstance();
            }

            @Override
            public Gauge build() {
                Gauge result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public Gauge buildPartial() {
                Gauge result = new Gauge(this);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartial0(Gauge result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.value_ = this.value_;
                    to_bitField0_ |= 1;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof Gauge) {
                    return this.mergeFrom((Gauge)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(Gauge other) {
                if (other == Gauge.getDefaultInstance()) {
                    return this;
                }
                if (other.hasValue()) {
                    this.setValue(other.getValue());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block9: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block9;
                            }
                            case 9: {
                                this.value_ = input.readDouble();
                                this.bitField0_ |= 1;
                                continue block9;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasValue() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public double getValue() {
                return this.value_;
            }

            public Builder setValue(double value) {
                this.value_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearValue() {
                this.bitField0_ &= 0xFFFFFFFE;
                this.value_ = 0.0;
                this.onChanged();
                return this;
            }
        }
    }

    public static interface GaugeOrBuilder
    extends MessageOrBuilder {
        public boolean hasValue();

        public double getValue();
    }

    public static final class LabelPair
    extends GeneratedMessage
    implements LabelPairOrBuilder {
        private static final long serialVersionUID = 0L;
        private int bitField0_;
        public static final int NAME_FIELD_NUMBER = 1;
        private volatile Object name_ = "";
        public static final int VALUE_FIELD_NUMBER = 2;
        private volatile Object value_ = "";
        private byte memoizedIsInitialized = (byte)-1;
        private static final LabelPair DEFAULT_INSTANCE;
        private static final Parser<LabelPair> PARSER;

        private LabelPair(GeneratedMessage.Builder<?> builder) {
            super(builder);
        }

        private LabelPair() {
            this.name_ = "";
            this.value_ = "";
        }

        public static final Descriptors.Descriptor getDescriptor() {
            return internal_static_io_prometheus_client_LabelPair_descriptor;
        }

        @Override
        protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return internal_static_io_prometheus_client_LabelPair_fieldAccessorTable.ensureFieldAccessorsInitialized(LabelPair.class, Builder.class);
        }

        @Override
        public boolean hasName() {
            return (this.bitField0_ & 1) != 0;
        }

        @Override
        public String getName() {
            Object ref = this.name_;
            if (ref instanceof String) {
                return (String)ref;
            }
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
                this.name_ = s;
            }
            return s;
        }

        @Override
        public ByteString getNameBytes() {
            Object ref = this.name_;
            if (ref instanceof String) {
                ByteString b = ByteString.copyFromUtf8((String)ref);
                this.name_ = b;
                return b;
            }
            return (ByteString)ref;
        }

        @Override
        public boolean hasValue() {
            return (this.bitField0_ & 2) != 0;
        }

        @Override
        public String getValue() {
            Object ref = this.value_;
            if (ref instanceof String) {
                return (String)ref;
            }
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
                this.value_ = s;
            }
            return s;
        }

        @Override
        public ByteString getValueBytes() {
            Object ref = this.value_;
            if (ref instanceof String) {
                ByteString b = ByteString.copyFromUtf8((String)ref);
                this.value_ = b;
                return b;
            }
            return (ByteString)ref;
        }

        @Override
        public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
                return true;
            }
            if (isInitialized == 0) {
                return false;
            }
            this.memoizedIsInitialized = 1;
            return true;
        }

        @Override
        public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
                GeneratedMessage.writeString(output, 1, this.name_);
            }
            if ((this.bitField0_ & 2) != 0) {
                GeneratedMessage.writeString(output, 2, this.value_);
            }
            this.getUnknownFields().writeTo(output);
        }

        @Override
        public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
                return size;
            }
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
                size += GeneratedMessage.computeStringSize(1, this.name_);
            }
            if ((this.bitField0_ & 2) != 0) {
                size += GeneratedMessage.computeStringSize(2, this.value_);
            }
            this.memoizedSize = size += this.getUnknownFields().getSerializedSize();
            return size;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof LabelPair)) {
                return super.equals(obj);
            }
            LabelPair other = (LabelPair)obj;
            if (this.hasName() != other.hasName()) {
                return false;
            }
            if (this.hasName() && !this.getName().equals(other.getName())) {
                return false;
            }
            if (this.hasValue() != other.hasValue()) {
                return false;
            }
            if (this.hasValue() && !this.getValue().equals(other.getValue())) {
                return false;
            }
            return this.getUnknownFields().equals(other.getUnknownFields());
        }

        @Override
        public int hashCode() {
            if (this.memoizedHashCode != 0) {
                return this.memoizedHashCode;
            }
            int hash = 41;
            hash = 19 * hash + LabelPair.getDescriptor().hashCode();
            if (this.hasName()) {
                hash = 37 * hash + 1;
                hash = 53 * hash + this.getName().hashCode();
            }
            if (this.hasValue()) {
                hash = 37 * hash + 2;
                hash = 53 * hash + this.getValue().hashCode();
            }
            this.memoizedHashCode = hash = 29 * hash + this.getUnknownFields().hashCode();
            return hash;
        }

        public static LabelPair parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static LabelPair parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static LabelPair parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static LabelPair parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static LabelPair parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static LabelPair parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static LabelPair parseFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static LabelPair parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static LabelPair parseDelimitedFrom(InputStream input) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
        }

        public static LabelPair parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static LabelPair parseFrom(CodedInputStream input) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input);
        }

        public static LabelPair parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
        }

        @Override
        public Builder newBuilderForType() {
            return LabelPair.newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(LabelPair prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
        }

        @Override
        protected Builder newBuilderForType(AbstractMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static LabelPair getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Parser<LabelPair> parser() {
            return PARSER;
        }

        public Parser<LabelPair> getParserForType() {
            return PARSER;
        }

        @Override
        public LabelPair getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", LabelPair.class.getName());
            DEFAULT_INSTANCE = new LabelPair();
            PARSER = new AbstractParser<LabelPair>(){

                @Override
                public LabelPair parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
                    Builder builder = LabelPair.newBuilder();
                    try {
                        builder.mergeFrom(input, extensionRegistry);
                    }
                    catch (InvalidProtocolBufferException e) {
                        throw e.setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (UninitializedMessageException e) {
                        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
                    }
                    catch (IOException e) {
                        throw new InvalidProtocolBufferException(e).setUnfinishedMessage(builder.buildPartial());
                    }
                    return builder.buildPartial();
                }
            };
        }

        public static final class Builder
        extends GeneratedMessage.Builder<Builder>
        implements LabelPairOrBuilder {
            private int bitField0_;
            private Object name_ = "";
            private Object value_ = "";

            public static final Descriptors.Descriptor getDescriptor() {
                return internal_static_io_prometheus_client_LabelPair_descriptor;
            }

            @Override
            protected GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return internal_static_io_prometheus_client_LabelPair_fieldAccessorTable.ensureFieldAccessorsInitialized(LabelPair.class, Builder.class);
            }

            private Builder() {
            }

            private Builder(AbstractMessage.BuilderParent parent) {
                super(parent);
            }

            @Override
            public Builder clear() {
                super.clear();
                this.bitField0_ = 0;
                this.name_ = "";
                this.value_ = "";
                return this;
            }

            @Override
            public Descriptors.Descriptor getDescriptorForType() {
                return internal_static_io_prometheus_client_LabelPair_descriptor;
            }

            @Override
            public LabelPair getDefaultInstanceForType() {
                return LabelPair.getDefaultInstance();
            }

            @Override
            public LabelPair build() {
                LabelPair result = this.buildPartial();
                if (!result.isInitialized()) {
                    throw Builder.newUninitializedMessageException(result);
                }
                return result;
            }

            @Override
            public LabelPair buildPartial() {
                LabelPair result = new LabelPair(this);
                if (this.bitField0_ != 0) {
                    this.buildPartial0(result);
                }
                this.onBuilt();
                return result;
            }

            private void buildPartial0(LabelPair result) {
                int from_bitField0_ = this.bitField0_;
                int to_bitField0_ = 0;
                if ((from_bitField0_ & 1) != 0) {
                    result.name_ = this.name_;
                    to_bitField0_ |= 1;
                }
                if ((from_bitField0_ & 2) != 0) {
                    result.value_ = this.value_;
                    to_bitField0_ |= 2;
                }
                result.bitField0_ |= to_bitField0_;
            }

            @Override
            public Builder mergeFrom(Message other) {
                if (other instanceof LabelPair) {
                    return this.mergeFrom((LabelPair)other);
                }
                super.mergeFrom(other);
                return this;
            }

            public Builder mergeFrom(LabelPair other) {
                if (other == LabelPair.getDefaultInstance()) {
                    return this;
                }
                if (other.hasName()) {
                    this.name_ = other.name_;
                    this.bitField0_ |= 1;
                    this.onChanged();
                }
                if (other.hasValue()) {
                    this.value_ = other.value_;
                    this.bitField0_ |= 2;
                    this.onChanged();
                }
                this.mergeUnknownFields(other.getUnknownFields());
                this.onChanged();
                return this;
            }

            @Override
            public final boolean isInitialized() {
                return true;
            }

            @Override
            public Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
                if (extensionRegistry == null) {
                    throw new NullPointerException();
                }
                try {
                    boolean done = false;
                    block10: while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                            case 0: {
                                done = true;
                                continue block10;
                            }
                            case 10: {
                                this.name_ = input.readBytes();
                                this.bitField0_ |= 1;
                                continue block10;
                            }
                            case 18: {
                                this.value_ = input.readBytes();
                                this.bitField0_ |= 2;
                                continue block10;
                            }
                        }
                        if (super.parseUnknownField(input, extensionRegistry, tag)) continue;
                        done = true;
                    }
                }
                catch (InvalidProtocolBufferException e) {
                    throw e.unwrapIOException();
                }
                finally {
                    this.onChanged();
                }
                return this;
            }

            @Override
            public boolean hasName() {
                return (this.bitField0_ & 1) != 0;
            }

            @Override
            public String getName() {
                Object ref = this.name_;
                if (!(ref instanceof String)) {
                    ByteString bs = (ByteString)ref;
                    String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        this.name_ = s;
                    }
                    return s;
                }
                return (String)ref;
            }

            @Override
            public ByteString getNameBytes() {
                Object ref = this.name_;
                if (ref instanceof String) {
                    ByteString b = ByteString.copyFromUtf8((String)ref);
                    this.name_ = b;
                    return b;
                }
                return (ByteString)ref;
            }

            public Builder setName(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.name_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            public Builder clearName() {
                this.name_ = LabelPair.getDefaultInstance().getName();
                this.bitField0_ &= 0xFFFFFFFE;
                this.onChanged();
                return this;
            }

            public Builder setNameBytes(ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.name_ = value;
                this.bitField0_ |= 1;
                this.onChanged();
                return this;
            }

            @Override
            public boolean hasValue() {
                return (this.bitField0_ & 2) != 0;
            }

            @Override
            public String getValue() {
                Object ref = this.value_;
                if (!(ref instanceof String)) {
                    ByteString bs = (ByteString)ref;
                    String s = bs.toStringUtf8();
                    if (bs.isValidUtf8()) {
                        this.value_ = s;
                    }
                    return s;
                }
                return (String)ref;
            }

            @Override
            public ByteString getValueBytes() {
                Object ref = this.value_;
                if (ref instanceof String) {
                    ByteString b = ByteString.copyFromUtf8((String)ref);
                    this.value_ = b;
                    return b;
                }
                return (ByteString)ref;
            }

            public Builder setValue(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.value_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }

            public Builder clearValue() {
                this.value_ = LabelPair.getDefaultInstance().getValue();
                this.bitField0_ &= 0xFFFFFFFD;
                this.onChanged();
                return this;
            }

            public Builder setValueBytes(ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                this.value_ = value;
                this.bitField0_ |= 2;
                this.onChanged();
                return this;
            }
        }
    }

    public static interface LabelPairOrBuilder
    extends MessageOrBuilder {
        public boolean hasName();

        public String getName();

        public ByteString getNameBytes();

        public boolean hasValue();

        public String getValue();

        public ByteString getValueBytes();
    }

    public static enum MetricType implements ProtocolMessageEnum
    {
        COUNTER(0),
        GAUGE(1),
        SUMMARY(2),
        UNTYPED(3),
        HISTOGRAM(4),
        GAUGE_HISTOGRAM(5);

        public static final int COUNTER_VALUE = 0;
        public static final int GAUGE_VALUE = 1;
        public static final int SUMMARY_VALUE = 2;
        public static final int UNTYPED_VALUE = 3;
        public static final int HISTOGRAM_VALUE = 4;
        public static final int GAUGE_HISTOGRAM_VALUE = 5;
        private static final Internal.EnumLiteMap<MetricType> internalValueMap;
        private static final MetricType[] VALUES;
        private final int value;

        @Override
        public final int getNumber() {
            return this.value;
        }

        @Deprecated
        public static MetricType valueOf(int value) {
            return MetricType.forNumber(value);
        }

        public static MetricType forNumber(int value) {
            switch (value) {
                case 0: {
                    return COUNTER;
                }
                case 1: {
                    return GAUGE;
                }
                case 2: {
                    return SUMMARY;
                }
                case 3: {
                    return UNTYPED;
                }
                case 4: {
                    return HISTOGRAM;
                }
                case 5: {
                    return GAUGE_HISTOGRAM;
                }
            }
            return null;
        }

        public static Internal.EnumLiteMap<MetricType> internalGetValueMap() {
            return internalValueMap;
        }

        @Override
        public final Descriptors.EnumValueDescriptor getValueDescriptor() {
            return MetricType.getDescriptor().getValues().get(this.ordinal());
        }

        @Override
        public final Descriptors.EnumDescriptor getDescriptorForType() {
            return MetricType.getDescriptor();
        }

        public static Descriptors.EnumDescriptor getDescriptor() {
            return Metrics.getDescriptor().getEnumTypes().get(0);
        }

        public static MetricType valueOf(Descriptors.EnumValueDescriptor desc) {
            if (desc.getType() != MetricType.getDescriptor()) {
                throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            }
            return VALUES[desc.getIndex()];
        }

        private MetricType(int value) {
            this.value = value;
        }

        static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeVersion.RuntimeDomain.PUBLIC, 4, 31, 1, "", MetricType.class.getName());
            internalValueMap = new Internal.EnumLiteMap<MetricType>(){

                @Override
                public MetricType findValueByNumber(int number) {
                    return MetricType.forNumber(number);
                }
            };
            VALUES = MetricType.values();
        }
    }
}

