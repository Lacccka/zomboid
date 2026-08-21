/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.runtime.Location;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;

public class IllegalAnnotationException
extends JAXBException {
    private final List<List<Location>> pos;
    private static final long serialVersionUID = 1L;

    public IllegalAnnotationException(String message, Locatable src) {
        super(message);
        this.pos = this.build(src);
    }

    public IllegalAnnotationException(String message, Annotation src) {
        this(message, IllegalAnnotationException.cast(src));
    }

    public IllegalAnnotationException(String message, Locatable src1, Locatable src2) {
        super(message);
        this.pos = this.build(src1, src2);
    }

    public IllegalAnnotationException(String message, Annotation src1, Annotation src2) {
        this(message, IllegalAnnotationException.cast(src1), IllegalAnnotationException.cast(src2));
    }

    public IllegalAnnotationException(String message, Annotation src1, Locatable src2) {
        this(message, IllegalAnnotationException.cast(src1), src2);
    }

    public IllegalAnnotationException(String message, Throwable cause, Locatable src) {
        super(message, cause);
        this.pos = this.build(src);
    }

    private static Locatable cast(Annotation a) {
        if (a instanceof Locatable) {
            return (Locatable)((Object)a);
        }
        return null;
    }

    private List<List<Location>> build(Locatable ... srcs) {
        ArrayList<List<Location>> r = new ArrayList<List<Location>>();
        for (Locatable l : srcs) {
            List<Location> ll;
            if (l == null || (ll = this.convert(l)) == null || ll.isEmpty()) continue;
            r.add(ll);
        }
        return Collections.unmodifiableList(r);
    }

    private List<Location> convert(Locatable src) {
        if (src == null) {
            return null;
        }
        ArrayList<Location> r = new ArrayList<Location>();
        while (src != null) {
            r.add(src.getLocation());
            src = src.getUpstream();
        }
        return Collections.unmodifiableList(r);
    }

    public List<List<Location>> getSourcePos() {
        return this.pos;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getMessage());
        for (List<Location> locs : this.pos) {
            sb.append("\n\tthis problem is related to the following location:");
            for (Location loc : locs) {
                sb.append("\n\t\tat ").append(loc.toString());
            }
        }
        return sb.toString();
    }
}

