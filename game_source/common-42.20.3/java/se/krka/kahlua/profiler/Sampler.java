/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.profiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import se.krka.kahlua.profiler.JavaStacktraceElement;
import se.krka.kahlua.profiler.LuaStacktraceElement;
import se.krka.kahlua.profiler.Profiler;
import se.krka.kahlua.profiler.Sample;
import se.krka.kahlua.profiler.StacktraceElement;
import se.krka.kahlua.vm.Coroutine;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;

public class Sampler {
    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private final KahluaThread thread;
    private final Timer timer;
    private final long period;
    private final Profiler profiler;

    public Sampler(KahluaThread thread2, long period, Profiler profiler) {
        this.thread = thread2;
        this.period = period;
        this.profiler = profiler;
        this.timer = new Timer("Kahlua Sampler-" + NEXT_ID.incrementAndGet(), true);
    }

    public void start() {
        TimerTask timerTask2 = new TimerTask(this){
            final /* synthetic */ Sampler this$0;
            {
                Sampler sampler = this$0;
                Objects.requireNonNull(sampler);
                this.this$0 = sampler;
            }

            @Override
            public void run() {
                ArrayList<StacktraceElement> list = new ArrayList<StacktraceElement>();
                this.this$0.appendList(list, this.this$0.thread.currentCoroutine);
                this.this$0.profiler.getSample(new Sample(list, this.this$0.period));
            }
        };
        this.timer.scheduleAtFixedRate(timerTask2, 0L, this.period);
    }

    private void appendList(List<StacktraceElement> list, Coroutine coroutine) {
        while (coroutine != null) {
            LuaCallFrame[] stack = coroutine.getCallframeStack();
            int top = Math.min(stack.length, coroutine.getCallframeTop());
            for (int i = top - 1; i >= 0; --i) {
                LuaCallFrame frame = stack[i];
                int pc = frame.pc - 1;
                LuaClosure closure = frame.closure;
                JavaFunction javaFunction = frame.javaFunction;
                if (closure != null) {
                    list.add(new LuaStacktraceElement(pc, closure.prototype));
                    continue;
                }
                if (javaFunction == null) continue;
                list.add(new JavaStacktraceElement(javaFunction));
            }
            coroutine = coroutine.getParent();
        }
    }

    public void stop() {
        this.timer.cancel();
    }
}

