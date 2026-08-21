/*
 * Decompiled with CFR 0.152.
 */
package javax.annotation.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.sql.DataSourceDefinition;

@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface DataSourceDefinitions {
    public DataSourceDefinition[] value();
}

