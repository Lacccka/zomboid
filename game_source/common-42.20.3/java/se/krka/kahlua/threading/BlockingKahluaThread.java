/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.threading;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;

public class BlockingKahluaThread
extends KahluaThread {
    private final Lock lock = new ReentrantLock();

    public BlockingKahluaThread(Platform platform, KahluaTable environment) {
        super(platform, environment);
    }

    public BlockingKahluaThread(PrintStream stream, Platform platform, KahluaTable environment) {
        super(stream, platform, environment);
    }

    private void lock() {
        this.lock.lock();
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

    static void main(String[] args2) throws IOException, InterruptedException {
        J2SEPlatform platform = new J2SEPlatform();
        KahluaTable env = platform.newEnvironment();
        final BlockingKahluaThread state = new BlockingKahluaThread(platform, env);
        final LuaClosure c = LuaCompiler.loadstring("x = (x or 0) + 1", "", state.getEnvironment());
        final AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 100; ++i) {
            new Thread(new Runnable(){

                @Override
                public void run() {
                    for (int i = 0; i < 100; ++i) {
                        try {
                            Thread.sleep((long)(Math.random() * 10.0));
                        }
                        catch (InterruptedException e) {
                            DebugType.General.printException(e, LogSeverity.Error);
                        }
                        state.pcall(c);
                    }
                    counter.incrementAndGet();
                }
            }).start();
        }
        while (counter.get() != 100) {
            Thread.sleep(100L);
        }
        LuaClosure c2 = LuaCompiler.loadstring("print('x='..x)", "", state.getEnvironment());
        state.pcall(c2);
    }
}

