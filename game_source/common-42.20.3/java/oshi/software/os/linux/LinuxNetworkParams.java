/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.linux;

import com.sun.jna.Native;
import com.sun.jna.platform.linux.LibC;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.platform.linux.LinuxLibc;
import oshi.software.common.AbstractNetworkParams;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@ThreadSafe
final class LinuxNetworkParams
extends AbstractNetworkParams {
    private static final Logger LOG = LoggerFactory.getLogger(LinuxNetworkParams.class);
    private static final LinuxLibc LIBC = LinuxLibc.INSTANCE;
    private static final String IPV4_DEFAULT_DEST = "0.0.0.0";
    private static final String IPV6_DEFAULT_DEST = "::/0";

    LinuxNetworkParams() {
    }

    /*
     * Exception decompiling
     */
    @Override
    public String getDomainName() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [3[TRYBLOCK]], but top level block is 6[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    @Override
    public String getHostName() {
        byte[] hostnameBuffer = new byte[256];
        if (0 != LibC.INSTANCE.gethostname(hostnameBuffer, hostnameBuffer.length)) {
            return super.getHostName();
        }
        return Native.toString(hostnameBuffer);
    }

    @Override
    public String getIpv4DefaultGateway() {
        List<String> routes = ExecutingCommand.runNative("route -A inet -n");
        if (routes.size() <= 2) {
            return "";
        }
        String gateway = "";
        int minMetric = Integer.MAX_VALUE;
        for (int i = 2; i < routes.size(); ++i) {
            String[] fields = ParseUtil.whitespaces.split(routes.get(i));
            if (fields.length <= 4 || !fields[0].equals(IPV4_DEFAULT_DEST)) continue;
            boolean isGateway = fields[3].indexOf(71) != -1;
            int metric = ParseUtil.parseIntOrDefault(fields[4], Integer.MAX_VALUE);
            if (!isGateway || metric >= minMetric) continue;
            minMetric = metric;
            gateway = fields[1];
        }
        return gateway;
    }

    @Override
    public String getIpv6DefaultGateway() {
        List<String> routes = ExecutingCommand.runNative("route -A inet6 -n");
        if (routes.size() <= 2) {
            return "";
        }
        String gateway = "";
        int minMetric = Integer.MAX_VALUE;
        for (int i = 2; i < routes.size(); ++i) {
            String[] fields = ParseUtil.whitespaces.split(routes.get(i));
            if (fields.length <= 3 || !fields[0].equals(IPV6_DEFAULT_DEST)) continue;
            boolean isGateway = fields[2].indexOf(71) != -1;
            int metric = ParseUtil.parseIntOrDefault(fields[3], Integer.MAX_VALUE);
            if (!isGateway || metric >= minMetric) continue;
            minMetric = metric;
            gateway = fields[1];
        }
        return gateway;
    }
}

