/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.threading;

import java.io.PrintStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.Platform;

public class VerifiedSingleKahluaThread
extends KahluaThread {
    private final Lock lock = new ReentrantLock();

    public VerifiedSingleKahluaThread(Platform platform, KahluaTable environment) {
        super(platform, environment);
    }

    public VerifiedSingleKahluaThread(PrintStream stream, Platform platform, KahluaTable environment) {
        super(stream, platform, environment);
    }

    private void lock() {
        if (!this.lock.tryLock()) {
            throw new IllegalStateException("Multiple threads may not access the same lua thread");
        }
    }

    private void unlock() {
        this.lock.unlock();
    }

    @Override
    public int call(int arguments) {
        this.lock();
        try {
            int n = super.call(arguments);
            return n;
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public int pcall(int arguments) {
        this.lock();
        try {
            int n = super.pcall(arguments);
            return n;
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public Object[] pcall(Object fun) {
        this.lock();
        try {
            Object[] objectArray = super.pcall(fun);
            return objectArray;
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final Object[] pcall(Object fun, Object[] args2) {
        this.lock();
        try {
            Object[] objectArray = super.pcall(fun, args2);
            return objectArray;
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public void setmetatable(Object o, KahluaTable metatable) {
        this.lock();
        try {
            super.setmetatable(o, metatable);
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object call(Object fun, Object arg1, Object arg2, Object arg3) {
        this.lock();
        try {
            Object object = super.call(fun, arg1, arg2, arg3);
            return object;
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object call(Object fun, Object[] args2) {
        this.lock();
        try {
            Object object = super.call(fun, args2);
            return object;
        }
        finally {
            this.unlock();
        }
    }

    @Override
    public KahluaTable getEnvironment() {
        this.lock();
        try {
            KahluaTable kahluaTable = super.getEnvironment();
            return kahluaTable;
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object getMetaOp(Object o, String meta_op) {
        this.lock();
        try {
            Object object = super.getMetaOp(o, meta_op);
            return object;
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object getmetatable(Object o, boolean raw) {
        this.lock();
        try {
            Object object = super.getmetatable(o, raw);
            return object;
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object tableget(Object table, Object key) {
        this.lock();
        try {
            Object object = super.tableget(table, key);
            return object;
        }
        finally {
            this.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void tableSet(Object table, Object key, Object value) {
        this.lock();
        try {
            super.tableSet(table, key, value);
        }
        finally {
            this.unlock();
        }
    }
}

