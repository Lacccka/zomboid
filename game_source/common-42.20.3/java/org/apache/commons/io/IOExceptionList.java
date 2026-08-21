/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class IOExceptionList
extends IOException
implements Iterable<Throwable> {
    private static final long serialVersionUID = 1L;
    private final List<? extends Throwable> causeList;

    public static void checkEmpty(List<? extends Throwable> causeList, Object message) throws IOExceptionList {
        if (!IOExceptionList.isEmpty(causeList)) {
            throw new IOExceptionList(Objects.toString(message, null), causeList);
        }
    }

    private static boolean isEmpty(List<? extends Throwable> causeList) {
        return IOExceptionList.size(causeList) == 0;
    }

    private static int size(List<? extends Throwable> causeList) {
        return causeList != null ? causeList.size() : 0;
    }

    private static String toMessage(List<? extends Throwable> causeList) {
        return String.format("%,d exception(s): %s", IOExceptionList.size(causeList), causeList);
    }

    public IOExceptionList(List<? extends Throwable> causeList) {
        this(IOExceptionList.toMessage(causeList), causeList);
    }

    public IOExceptionList(String message, List<? extends Throwable> causeList) {
        super(message != null ? message : IOExceptionList.toMessage(causeList), IOExceptionList.isEmpty(causeList) ? null : causeList.get(0));
        this.causeList = causeList == null ? Collections.emptyList() : causeList;
    }

    public <T extends Throwable> T getCause(int index) {
        return (T)this.causeList.get(index);
    }

    public <T extends Throwable> T getCause(int index, Class<T> clazz) {
        return (T)((Throwable)clazz.cast(this.getCause(index)));
    }

    public <T extends Throwable> List<T> getCauseList() {
        return new ArrayList<Throwable>(this.causeList);
    }

    public <T extends Throwable> List<T> getCauseList(Class<T> clazz) {
        return new ArrayList<Throwable>(this.causeList);
    }

    @Override
    public Iterator<Throwable> iterator() {
        return this.getCauseList().iterator();
    }
}

