/*
 * Decompiled with CFR 0.152.
 */
package kotlin.text.jdk8;

import kotlin.Metadata;
import kotlin.SinceKotlin;
import kotlin.jvm.JvmName;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.MatchGroup;
import kotlin.text.MatchGroupCollection;
import kotlin.text.MatchNamedGroupCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(mv={1, 4, 0}, bv={1, 0, 3}, k=2, d1={"\u0000\u0012\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\u001a\u0017\u0010\u0000\u001a\u0004\u0018\u00010\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u0004H\u0087\u0002\u00a8\u0006\u0005"}, d2={"get", "Lkotlin/text/MatchGroup;", "Lkotlin/text/MatchGroupCollection;", "name", "", "kotlin-stdlib-jdk8"}, pn="kotlin.text")
@JvmName(name="RegexExtensionsJDK8Kt")
public final class RegexExtensionsJDK8Kt {
    @SinceKotlin(version="1.2")
    @Nullable
    public static final MatchGroup get(@NotNull MatchGroupCollection $this$get, @NotNull String name) {
        Intrinsics.checkNotNullParameter($this$get, "$this$get");
        Intrinsics.checkNotNullParameter(name, "name");
        MatchGroupCollection matchGroupCollection = $this$get;
        if (!(matchGroupCollection instanceof MatchNamedGroupCollection)) {
            matchGroupCollection = null;
        }
        MatchNamedGroupCollection matchNamedGroupCollection = (MatchNamedGroupCollection)matchGroupCollection;
        if (matchNamedGroupCollection == null) {
            throw (Throwable)new UnsupportedOperationException("Retrieving groups by name is not supported on this platform.");
        }
        MatchNamedGroupCollection namedGroups = matchNamedGroupCollection;
        return namedGroups.get(name);
    }
}

