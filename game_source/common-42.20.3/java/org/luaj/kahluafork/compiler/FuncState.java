/*
 * Decompiled with CFR 0.152.
 */
package org.luaj.kahluafork.compiler;

import java.util.Hashtable;
import org.luaj.kahluafork.compiler.BlockCnt;
import org.luaj.kahluafork.compiler.ConsControl;
import org.luaj.kahluafork.compiler.ExpDesc;
import org.luaj.kahluafork.compiler.InstructionPtr;
import org.luaj.kahluafork.compiler.LexState;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.Prototype;

public final class FuncState {
    private static final Object NULL_OBJECT = new Object();
    public String[] locvars;
    public String[] upvalues;
    public int linedefined;
    public int lastlinedefined;
    public int isVararg;
    Prototype f;
    Hashtable<Object, Integer> htable;
    FuncState prev;
    LexState ls;
    BlockCnt bl;
    int pc;
    int lasttarget;
    int jpc;
    int freereg;
    int nk;
    int np;
    int nlocvars;
    int nactvar;
    int[] upvaluesK = new int[60];
    int[] upvaluesInfo = new int[60];
    short[] actvar = new short[200];
    int[] actvarline = new int[200];
    int nactvarline;
    public static String currentFile;
    public static String currentfullFile;
    public static final int MAXSTACK = 250;
    static final int LUAI_MAXUPVALUES = 60;
    static final int LUAI_MAXVARS = 200;
    static final int OpArgN = 0;
    static final int OpArgU = 1;
    static final int OpArgR = 2;
    static final int OpArgK = 3;
    public static final int LUA_MULTRET = -1;
    public static final int VARARG_HASARG = 1;
    public static final int VARARG_ISVARARG = 2;
    public static final int VARARG_NEEDSARG = 4;
    public static final int iABC = 0;
    public static final int iABx = 1;
    public static final int iAsBx = 2;
    public static final int SIZE_C = 9;
    public static final int SIZE_B = 9;
    public static final int SIZE_Bx = 18;
    public static final int SIZE_A = 8;
    public static final int SIZE_OP = 6;
    public static final int POS_OP = 0;
    public static final int POS_A = 6;
    public static final int POS_C = 14;
    public static final int POS_B = 23;
    public static final int POS_Bx = 14;
    public static final int MAX_OP = 63;
    public static final int MAXARG_A = 255;
    public static final int MAXARG_B = 511;
    public static final int MAXARG_C = 511;
    public static final int MAXARG_Bx = 262143;
    public static final int MAXARG_sBx = 131071;
    public static final int MASK_OP = 63;
    public static final int MASK_A = 16320;
    public static final int MASK_B = -8388608;
    public static final int MASK_C = 8372224;
    public static final int MASK_Bx = -16384;
    public static final int MASK_NOT_OP = -64;
    public static final int MASK_NOT_A = -16321;
    public static final int MASK_NOT_B = 0x7FFFFF;
    public static final int MASK_NOT_C = -8372225;
    public static final int MASK_NOT_Bx = 16383;
    public static final int BITRK = 256;
    public static final int MAXINDEXRK = 255;
    public static final int NO_REG = 255;
    public static final int OP_MOVE = 0;
    public static final int OP_LOADK = 1;
    public static final int OP_LOADBOOL = 2;
    public static final int OP_LOADNIL = 3;
    public static final int OP_GETUPVAL = 4;
    public static final int OP_GETGLOBAL = 5;
    public static final int OP_GETTABLE = 6;
    public static final int OP_SETGLOBAL = 7;
    public static final int OP_SETUPVAL = 8;
    public static final int OP_SETTABLE = 9;
    public static final int OP_NEWTABLE = 10;
    public static final int OP_SELF = 11;
    public static final int OP_ADD = 12;
    public static final int OP_SUB = 13;
    public static final int OP_MUL = 14;
    public static final int OP_DIV = 15;
    public static final int OP_MOD = 16;
    public static final int OP_POW = 17;
    public static final int OP_UNM = 18;
    public static final int OP_NOT = 19;
    public static final int OP_LEN = 20;
    public static final int OP_CONCAT = 21;
    public static final int OP_JMP = 22;
    public static final int OP_EQ = 23;
    public static final int OP_LT = 24;
    public static final int OP_LE = 25;
    public static final int OP_TEST = 26;
    public static final int OP_TESTSET = 27;
    public static final int OP_CALL = 28;
    public static final int OP_TAILCALL = 29;
    public static final int OP_RETURN = 30;
    public static final int OP_FORLOOP = 31;
    public static final int OP_FORPREP = 32;
    public static final int OP_TFORLOOP = 33;
    public static final int OP_SETLIST = 34;
    public static final int OP_CLOSE = 35;
    public static final int OP_CLOSURE = 36;
    public static final int OP_VARARG = 37;
    public static final int NUM_OPCODES = 38;
    public static final int[] luaP_opmodes;
    public static final int LFIELDS_PER_FLUSH = 50;

    FuncState(LexState lexState) {
        Prototype f = new Prototype();
        if (lexState.fs != null) {
            f.name = lexState.fs.f.name;
        }
        this.f = f;
        this.prev = lexState.fs;
        this.ls = lexState;
        lexState.fs = this;
        this.pc = 0;
        this.lasttarget = -1;
        this.jpc = -1;
        this.freereg = 0;
        this.nk = 0;
        this.np = 0;
        this.nlocvars = 0;
        this.nactvar = 0;
        this.bl = null;
        f.maxStacksize = 2;
        this.htable = new Hashtable();
    }

    FuncState(LexState lexState, String name) {
        Prototype f = new Prototype();
        f.name = name == null ? null : name.intern();
        this.f = f;
        this.prev = lexState.fs;
        this.ls = lexState;
        lexState.fs = this;
        this.pc = 0;
        this.lasttarget = -1;
        this.jpc = -1;
        this.freereg = 0;
        this.nk = 0;
        this.np = 0;
        this.nlocvars = 0;
        this.nactvar = 0;
        this.bl = null;
        f.maxStacksize = 2;
        this.htable = new Hashtable();
    }

    InstructionPtr getcodePtr(ExpDesc e) {
        return new InstructionPtr(this.f.code, e.info);
    }

    int getcode(ExpDesc e) {
        return this.f.code[e.info];
    }

    int codeAsBx(int o, int A, int sBx) {
        return this.codeABx(o, A, sBx + 131071);
    }

    void setmultret(ExpDesc e) {
        this.setreturns(e, -1);
    }

    String getlocvar(int i) {
        return this.locvars[this.actvar[i]];
    }

    void checklimit(int v, int l, String msg) {
        if (v > l) {
            this.errorlimit(l, msg);
        }
    }

    void errorlimit(int limit, String what) {
        String msg = this.linedefined == 0 ? "main function has more than " + limit + " " + what : "function at line " + this.linedefined + " has more than " + limit + " " + what;
        this.ls.lexerror(msg, 0);
    }

    int indexupvalue(String name, ExpDesc v) {
        for (int i = 0; i < this.f.numUpvalues; ++i) {
            if (this.upvaluesK[i] != v.k || this.upvaluesInfo[i] != v.info) continue;
            FuncState._assert(this.upvalues[i].equals(name));
            return i;
        }
        this.checklimit(this.f.numUpvalues + 1, 60, "upvalues");
        if (this.upvalues == null || this.f.numUpvalues + 1 > this.upvalues.length) {
            this.upvalues = FuncState.realloc(this.upvalues, this.f.numUpvalues * 2 + 1);
        }
        FuncState._assert(v.k == 6 || v.k == 7);
        int numUpvalues = this.f.numUpvalues++;
        this.upvalues[numUpvalues] = name;
        this.upvaluesK[numUpvalues] = v.k;
        this.upvaluesInfo[numUpvalues] = v.info;
        return numUpvalues;
    }

    int searchvar(String n) {
        for (int i = this.nactvar - 1; i >= 0; --i) {
            if (!n.equals(this.getlocvar(i))) continue;
            return i;
        }
        return -1;
    }

    void markupval(int level) {
        BlockCnt bl = this.bl;
        while (bl != null && bl.nactvar > level) {
            bl = bl.previous;
        }
        if (bl != null) {
            bl.upval = true;
        }
    }

    int singlevaraux(String n, ExpDesc var, int base) {
        int v = this.searchvar(n);
        if (v >= 0) {
            var.init(6, v);
            if (base == 0) {
                this.markupval(v);
            }
            return 6;
        }
        if (this.prev == null) {
            var.init(8, 255);
            return 8;
        }
        if (this.prev.singlevaraux(n, var, 0) == 8) {
            return 8;
        }
        var.info = this.indexupvalue(n, var);
        var.k = 7;
        return 7;
    }

    void enterblock(BlockCnt bl, boolean isbreakable) {
        bl.breaklist = -1;
        bl.isbreakable = isbreakable;
        bl.nactvar = this.nactvar;
        bl.upval = false;
        bl.previous = this.bl;
        this.bl = bl;
        FuncState._assert(this.freereg == this.nactvar);
    }

    void leaveblock() {
        BlockCnt bl = this.bl;
        this.bl = bl.previous;
        this.ls.removevars(bl.nactvar);
        if (bl.upval) {
            this.codeABC(35, bl.nactvar, 0, 0);
        }
        FuncState._assert(!bl.isbreakable || !bl.upval);
        FuncState._assert(bl.nactvar == this.nactvar);
        this.freereg = this.nactvar;
        this.patchtohere(bl.breaklist);
    }

    void closelistfield(ConsControl cc) {
        if (cc.v.k == 0) {
            return;
        }
        this.exp2nextreg(cc.v);
        cc.v.k = 0;
        if (cc.tostore == 50) {
            this.setlist(cc.t.info, cc.na, cc.tostore);
            cc.tostore = 0;
        }
    }

    boolean hasmultret(int k) {
        return k == 13 || k == 14;
    }

    void lastlistfield(ConsControl cc) {
        if (cc.tostore == 0) {
            return;
        }
        if (this.hasmultret(cc.v.k)) {
            this.setmultret(cc.v);
            this.setlist(cc.t.info, cc.na, -1);
            --cc.na;
        } else {
            if (cc.v.k != 0) {
                this.exp2nextreg(cc.v);
            }
            this.setlist(cc.t.info, cc.na, cc.tostore);
        }
    }

    void nil(int from, int n) {
        if (this.pc > this.lasttarget) {
            if (this.pc == 0) {
                if (from >= this.nactvar) {
                    return;
                }
            } else {
                InstructionPtr previous = new InstructionPtr(this.f.code, this.pc - 1);
                if (FuncState.GET_OPCODE(previous.get()) == 3) {
                    int pfrom = FuncState.GETARG_A(previous.get());
                    int pto = FuncState.GETARG_B(previous.get());
                    if (pfrom <= from && from <= pto + 1) {
                        if (from + n - 1 > pto) {
                            FuncState.SETARG_B(previous, from + n - 1);
                        }
                        return;
                    }
                }
            }
        }
        this.codeABC(3, from, from + n - 1, 0);
    }

    int jump() {
        int jpc = this.jpc;
        this.jpc = -1;
        int j = this.codeAsBx(22, 0, -1);
        j = this.concat(j, jpc);
        return j;
    }

    void ret(int first, int nret) {
        this.codeABC(30, first, nret + 1, 0);
    }

    int condjump(int op, int A, int B, int C) {
        this.codeABC(op, A, B, C);
        return this.jump();
    }

    void fixjump(int pc, int dest) {
        InstructionPtr jmp = new InstructionPtr(this.f.code, pc);
        int offset = dest - (pc + 1);
        FuncState._assert(dest != -1);
        if (Math.abs(offset) > 131071) {
            this.ls.syntaxerror("control structure too long");
        }
        FuncState.SETARG_sBx(jmp, offset);
    }

    int getlabel() {
        this.lasttarget = this.pc;
        return this.pc;
    }

    int getjump(int pc) {
        int offset = FuncState.GETARG_sBx(this.f.code[pc]);
        if (offset == -1) {
            return -1;
        }
        return pc + 1 + offset;
    }

    InstructionPtr getjumpcontrol(int pc) {
        InstructionPtr pi = new InstructionPtr(this.f.code, pc);
        if (pc >= 1 && FuncState.testTMode(FuncState.GET_OPCODE(pi.code[pi.idx - 1]))) {
            return new InstructionPtr(pi.code, pi.idx - 1);
        }
        return pi;
    }

    boolean need_value(int list) {
        while (list != -1) {
            int i = this.getjumpcontrol(list).get();
            if (FuncState.GET_OPCODE(i) != 27) {
                return true;
            }
            list = this.getjump(list);
        }
        return false;
    }

    boolean patchtestreg(int node, int reg) {
        InstructionPtr i = this.getjumpcontrol(node);
        if (FuncState.GET_OPCODE(i.get()) != 27) {
            return false;
        }
        if (reg != 255 && reg != FuncState.GETARG_B(i.get())) {
            FuncState.SETARG_A(i, reg);
        } else {
            i.set(FuncState.CREATE_ABC(26, FuncState.GETARG_B(i.get()), 0, FuncState.GETARG_C(i.get())));
        }
        return true;
    }

    void removevalues(int list) {
        while (list != -1) {
            this.patchtestreg(list, 255);
            list = this.getjump(list);
        }
    }

    void patchlistaux(int list, int vtarget, int reg, int dtarget) {
        while (list != -1) {
            int next = this.getjump(list);
            if (this.patchtestreg(list, reg)) {
                this.fixjump(list, vtarget);
            } else {
                this.fixjump(list, dtarget);
            }
            list = next;
        }
    }

    void dischargejpc() {
        this.patchlistaux(this.jpc, this.pc, 255, this.pc);
        this.jpc = -1;
    }

    void patchlist(int list, int target) {
        if (target == this.pc) {
            this.patchtohere(list);
        } else {
            FuncState._assert(target < this.pc);
            this.patchlistaux(list, target, 255, target);
        }
    }

    void patchtohere(int list) {
        this.getlabel();
        this.jpc = this.concat(this.jpc, list);
    }

    int concat(int l1, int l2) {
        if (l2 == -1) {
            return l1;
        }
        if (l1 == -1) {
            l1 = l2;
        } else {
            int next;
            int list = l1;
            while ((next = this.getjump(list)) != -1) {
                list = next;
            }
            this.fixjump(list, l2);
        }
        return l1;
    }

    void checkstack(int n) {
        int newstack = this.freereg + n;
        if (newstack > this.f.maxStacksize) {
            if (newstack >= 250) {
                this.ls.syntaxerror("function or expression too complex");
            }
            this.f.maxStacksize = newstack;
        }
    }

    void reserveregs(int n) {
        this.checkstack(n);
        this.freereg += n;
    }

    void freereg(int reg) {
        if (!FuncState.ISK(reg) && reg >= this.nactvar) {
            --this.freereg;
            FuncState._assert(reg == this.freereg);
        }
    }

    void freeexp(ExpDesc e) {
        if (e.k == 12) {
            this.freereg(e.info);
        }
    }

    int addk(Object v) {
        int idx;
        if (this.htable.containsKey(v)) {
            idx = this.htable.get(v);
        } else {
            idx = this.nk;
            this.htable.put(v, idx);
            Prototype f = this.f;
            if (f.constants == null || this.nk + 1 >= f.constants.length) {
                f.constants = FuncState.realloc(f.constants, this.nk * 2 + 1);
            }
            if (v == NULL_OBJECT) {
                v = null;
            }
            f.constants[this.nk++] = v;
        }
        return idx;
    }

    int stringK(String s) {
        return this.addk(s);
    }

    int numberK(double r) {
        return this.addk(r);
    }

    int boolK(boolean b) {
        return this.addk(b ? Boolean.TRUE : Boolean.FALSE);
    }

    int nilK() {
        return this.addk(NULL_OBJECT);
    }

    void setreturns(ExpDesc e, int nresults) {
        if (e.k == 13) {
            FuncState.SETARG_C(this.getcodePtr(e), nresults + 1);
        } else if (e.k == 14) {
            FuncState.SETARG_B(this.getcodePtr(e), nresults + 1);
            FuncState.SETARG_A(this.getcodePtr(e), this.freereg);
            this.reserveregs(1);
        }
    }

    void setoneret(ExpDesc e) {
        if (e.k == 13) {
            e.k = 12;
            e.info = FuncState.GETARG_A(this.getcode(e));
        } else if (e.k == 14) {
            FuncState.SETARG_B(this.getcodePtr(e), 2);
            e.k = 11;
        }
    }

    void dischargevars(ExpDesc e) {
        switch (e.k) {
            case 6: {
                e.k = 12;
                break;
            }
            case 7: {
                e.info = this.codeABC(4, 0, e.info, 0);
                e.k = 11;
                break;
            }
            case 8: {
                e.info = this.codeABx(5, 0, e.info);
                e.k = 11;
                break;
            }
            case 9: {
                this.freereg(e.aux);
                this.freereg(e.info);
                e.info = this.codeABC(6, 0, e.info, e.aux);
                e.k = 11;
                break;
            }
            case 13: 
            case 14: {
                this.setoneret(e);
                break;
            }
        }
    }

    int code_label(int A, int b, int jump) {
        this.getlabel();
        return this.codeABC(2, A, b, jump);
    }

    void discharge2reg(ExpDesc e, int reg) {
        this.dischargevars(e);
        switch (e.k) {
            case 1: {
                this.nil(reg, 1);
                break;
            }
            case 2: 
            case 3: {
                this.codeABC(2, reg, e.k == 2 ? 1 : 0, 0);
                break;
            }
            case 4: {
                this.codeABx(1, reg, e.info);
                break;
            }
            case 5: {
                this.codeABx(1, reg, this.numberK(e.nval()));
                break;
            }
            case 11: {
                InstructionPtr pc = this.getcodePtr(e);
                FuncState.SETARG_A(pc, reg);
                break;
            }
            case 12: {
                if (reg == e.info) break;
                this.codeABC(0, reg, e.info, 0);
                break;
            }
            default: {
                FuncState._assert(e.k == 0 || e.k == 10);
                return;
            }
        }
        e.info = reg;
        e.k = 12;
    }

    void discharge2anyreg(ExpDesc e) {
        if (e.k != 12) {
            this.reserveregs(1);
            this.discharge2reg(e, this.freereg - 1);
        }
    }

    void exp2reg(ExpDesc e, int reg) {
        this.discharge2reg(e, reg);
        if (e.k == 10) {
            e.t = this.concat(e.t, e.info);
        }
        if (e.hasjumps()) {
            int p_f = -1;
            int p_t = -1;
            if (this.need_value(e.t) || this.need_value(e.f)) {
                int fj = e.k == 10 ? -1 : this.jump();
                p_f = this.code_label(reg, 0, 1);
                p_t = this.code_label(reg, 1, 0);
                this.patchtohere(fj);
            }
            int _final = this.getlabel();
            this.patchlistaux(e.f, _final, reg, p_f);
            this.patchlistaux(e.t, _final, reg, p_t);
        }
        e.t = -1;
        e.f = -1;
        e.info = reg;
        e.k = 12;
    }

    void exp2nextreg(ExpDesc e) {
        this.dischargevars(e);
        this.freeexp(e);
        this.reserveregs(1);
        this.exp2reg(e, this.freereg - 1);
    }

    int exp2anyreg(ExpDesc e) {
        this.dischargevars(e);
        if (e.k == 12) {
            if (!e.hasjumps()) {
                return e.info;
            }
            if (e.info >= this.nactvar) {
                this.exp2reg(e, e.info);
                return e.info;
            }
        }
        this.exp2nextreg(e);
        return e.info;
    }

    void exp2val(ExpDesc e) {
        if (e.hasjumps()) {
            this.exp2anyreg(e);
        } else {
            this.dischargevars(e);
        }
    }

    int exp2RK(ExpDesc e) {
        this.exp2val(e);
        switch (e.k) {
            case 1: 
            case 2: 
            case 3: 
            case 5: {
                if (this.nk > 255) break;
                e.info = e.k == 1 ? this.nilK() : (e.k == 5 ? this.numberK(e.nval()) : this.boolK(e.k == 2));
                e.k = 4;
                return FuncState.RKASK(e.info);
            }
            case 4: {
                if (e.info > 255) break;
                return FuncState.RKASK(e.info);
            }
        }
        return this.exp2anyreg(e);
    }

    void storevar(ExpDesc var, ExpDesc ex) {
        switch (var.k) {
            case 6: {
                this.freeexp(ex);
                this.exp2reg(ex, var.info);
                return;
            }
            case 7: {
                int e = this.exp2anyreg(ex);
                this.codeABC(8, e, var.info, 0);
                break;
            }
            case 8: {
                int e = this.exp2anyreg(ex);
                this.codeABx(7, e, var.info);
                break;
            }
            case 9: {
                int e = this.exp2RK(ex);
                this.codeABC(9, var.info, var.aux, e);
                break;
            }
            default: {
                FuncState._assert(false);
            }
        }
        this.freeexp(ex);
    }

    void self(ExpDesc e, ExpDesc key) {
        this.exp2anyreg(e);
        this.freeexp(e);
        int func = this.freereg;
        this.reserveregs(2);
        this.codeABC(11, func, e.info, this.exp2RK(key));
        this.freeexp(key);
        e.info = func;
        e.k = 12;
    }

    void invertjump(ExpDesc e) {
        InstructionPtr pc = this.getjumpcontrol(e.info);
        FuncState._assert(FuncState.testTMode(FuncState.GET_OPCODE(pc.get())) && FuncState.GET_OPCODE(pc.get()) != 27 && FuncState.GET_OPCODE(pc.get()) != 26);
        int a = FuncState.GETARG_A(pc.get());
        int nota = a != 0 ? 0 : 1;
        FuncState.SETARG_A(pc, nota);
    }

    int jumponcond(ExpDesc e, int cond) {
        int ie;
        if (e.k == 11 && FuncState.GET_OPCODE(ie = this.getcode(e)) == 19) {
            --this.pc;
            return this.condjump(26, FuncState.GETARG_B(ie), 0, cond != 0 ? 0 : 1);
        }
        this.discharge2anyreg(e);
        this.freeexp(e);
        return this.condjump(27, 255, e.info, cond);
    }

    void goiftrue(ExpDesc e) {
        this.dischargevars(e);
        e.f = this.concat(e.f, switch (e.k) {
            case 2, 4, 5 -> -1;
            case 3 -> this.jump();
            case 10 -> {
                this.invertjump(e);
                yield e.info;
            }
            default -> this.jumponcond(e, 0);
        });
        this.patchtohere(e.t);
        e.t = -1;
    }

    void goiffalse(ExpDesc e) {
        this.dischargevars(e);
        e.t = this.concat(e.t, switch (e.k) {
            case 1, 3 -> -1;
            case 2 -> this.jump();
            case 10 -> e.info;
            default -> this.jumponcond(e, 1);
        });
        this.patchtohere(e.f);
        e.f = -1;
    }

    void codenot(ExpDesc e) {
        this.dischargevars(e);
        switch (e.k) {
            case 1: 
            case 3: {
                e.k = 2;
                break;
            }
            case 2: 
            case 4: 
            case 5: {
                e.k = 3;
                break;
            }
            case 10: {
                this.invertjump(e);
                break;
            }
            case 11: 
            case 12: {
                this.discharge2anyreg(e);
                this.freeexp(e);
                e.info = this.codeABC(19, 0, e.info, 0);
                e.k = 11;
                break;
            }
            default: {
                FuncState._assert(false);
            }
        }
        int temp = e.f;
        e.f = e.t;
        e.t = temp;
        this.removevalues(e.f);
        this.removevalues(e.t);
    }

    void indexed(ExpDesc t, ExpDesc k) {
        t.aux = this.exp2RK(k);
        t.k = 9;
    }

    boolean constfolding(int op, ExpDesc e1, ExpDesc e2) {
        double r;
        if (!e1.isnumeral() || !e2.isnumeral()) {
            return false;
        }
        double v1 = e1.nval();
        double v2 = e2.nval();
        switch (op) {
            case 12: {
                r = v1 + v2;
                break;
            }
            case 13: {
                r = v1 - v2;
                break;
            }
            case 14: {
                r = v1 * v2;
                break;
            }
            case 15: {
                r = v1 / v2;
                break;
            }
            case 16: {
                r = v1 % v2;
                break;
            }
            case 17: {
                return false;
            }
            case 18: {
                r = -v1;
                break;
            }
            case 20: {
                return false;
            }
            default: {
                FuncState._assert(false);
                return false;
            }
        }
        if (Double.isNaN(r) || Double.isInfinite(r)) {
            return false;
        }
        e1.setNval(r);
        return true;
    }

    void codearith(int op, ExpDesc e1, ExpDesc e2) {
        if (this.constfolding(op, e1, e2)) {
            return;
        }
        int o2 = op != 18 && op != 20 ? this.exp2RK(e2) : 0;
        int o1 = this.exp2RK(e1);
        if (o1 > o2) {
            this.freeexp(e1);
            this.freeexp(e2);
        } else {
            this.freeexp(e2);
            this.freeexp(e1);
        }
        e1.info = this.codeABC(op, 0, o1, o2);
        e1.k = 11;
    }

    void codecomp(int op, int cond, ExpDesc e1, ExpDesc e2) {
        int o1 = this.exp2RK(e1);
        int o2 = this.exp2RK(e2);
        this.freeexp(e2);
        this.freeexp(e1);
        if (cond == 0 && op != 23) {
            int temp = o1;
            o1 = o2;
            o2 = temp;
            cond = 1;
        }
        e1.info = this.condjump(op, cond, o1, o2);
        e1.k = 10;
    }

    void prefix(int op, ExpDesc e) {
        ExpDesc e2 = new ExpDesc();
        e2.init(5, 0);
        switch (op) {
            case 0: {
                if (e.k == 4) {
                    this.exp2anyreg(e);
                }
                this.codearith(18, e, e2);
                break;
            }
            case 1: {
                this.codenot(e);
                break;
            }
            case 2: {
                this.exp2anyreg(e);
                this.codearith(20, e, e2);
                break;
            }
            default: {
                FuncState._assert(false);
            }
        }
    }

    void infix(int op, ExpDesc v) {
        switch (op) {
            case 13: {
                this.goiftrue(v);
                break;
            }
            case 14: {
                this.goiffalse(v);
                break;
            }
            case 6: {
                this.exp2nextreg(v);
                break;
            }
            case 0: 
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: {
                if (v.isnumeral()) break;
                this.exp2RK(v);
                break;
            }
            default: {
                this.exp2RK(v);
            }
        }
    }

    void posfix(int op, ExpDesc e1, ExpDesc e2) {
        switch (op) {
            case 13: {
                FuncState._assert(e1.t == -1);
                this.dischargevars(e2);
                e2.f = this.concat(e2.f, e1.f);
                e1.setvalue(e2);
                break;
            }
            case 14: {
                FuncState._assert(e1.f == -1);
                this.dischargevars(e2);
                e2.t = this.concat(e2.t, e1.t);
                e1.setvalue(e2);
                break;
            }
            case 6: {
                this.exp2val(e2);
                if (e2.k == 11 && FuncState.GET_OPCODE(this.getcode(e2)) == 21) {
                    FuncState._assert(e1.info == FuncState.GETARG_B(this.getcode(e2)) - 1);
                    this.freeexp(e1);
                    FuncState.SETARG_B(this.getcodePtr(e2), e1.info);
                    e1.k = 11;
                    e1.info = e2.info;
                    break;
                }
                this.exp2nextreg(e2);
                this.codearith(21, e1, e2);
                break;
            }
            case 0: {
                this.codearith(12, e1, e2);
                break;
            }
            case 1: {
                this.codearith(13, e1, e2);
                break;
            }
            case 2: {
                this.codearith(14, e1, e2);
                break;
            }
            case 3: {
                this.codearith(15, e1, e2);
                break;
            }
            case 4: {
                this.codearith(16, e1, e2);
                break;
            }
            case 5: {
                this.codearith(17, e1, e2);
                break;
            }
            case 8: {
                this.codecomp(23, 1, e1, e2);
                break;
            }
            case 7: {
                this.codecomp(23, 0, e1, e2);
                break;
            }
            case 9: {
                this.codecomp(24, 1, e1, e2);
                break;
            }
            case 10: {
                this.codecomp(25, 1, e1, e2);
                break;
            }
            case 11: {
                this.codecomp(24, 0, e1, e2);
                break;
            }
            case 12: {
                this.codecomp(25, 0, e1, e2);
                break;
            }
            default: {
                FuncState._assert(false);
            }
        }
    }

    void fixline(int line) {
        this.f.lines[this.pc - 1] = line;
    }

    int code(int instruction, int line) {
        Prototype f = this.f;
        this.dischargejpc();
        if (f.code == null || this.pc + 1 > f.code.length) {
            f.code = FuncState.realloc(f.code, this.pc * 2 + 1);
        }
        f.code[this.pc] = instruction;
        if (f.lines == null || this.pc + 1 > f.lines.length) {
            f.lines = FuncState.realloc(f.lines, this.pc * 2 + 1);
        }
        f.lines[this.pc] = line;
        f.file = currentFile;
        f.filename = currentfullFile;
        return this.pc++;
    }

    int codeABC(int o, int a, int b, int c) {
        FuncState._assert(FuncState.getOpMode(o) == 0);
        FuncState._assert(FuncState.getBMode(o) != 0 || b == 0);
        FuncState._assert(FuncState.getCMode(o) != 0 || c == 0);
        return this.code(FuncState.CREATE_ABC(o, a, b, c), this.ls.lastline);
    }

    int codeABx(int o, int a, int bc) {
        FuncState._assert(FuncState.getOpMode(o) == 1 || FuncState.getOpMode(o) == 2);
        FuncState._assert(FuncState.getCMode(o) == 0);
        return this.code(FuncState.CREATE_ABx(o, a, bc), this.ls.lastline);
    }

    void setlist(int base, int nelems, int tostore) {
        int c = (nelems - 1) / 50 + 1;
        int b = tostore == -1 ? 0 : tostore;
        FuncState._assert(tostore != 0);
        if (c <= 511) {
            this.codeABC(34, base, b, c);
            this.freereg = base + 1;
        } else {
            this.codeABC(34, base, b, 0);
            this.code(c, this.ls.lastline);
            this.freereg = base + 1;
        }
    }

    protected static void _assert(boolean b) {
        if (!b) {
            throw new KahluaException((Object)"compiler assert failed");
        }
    }

    static void SET_OPCODE(InstructionPtr i, int o) {
        i.set(i.get() & 0xFFFFFFC0 | o << 0 & 0x3F);
    }

    static void SETARG_A(InstructionPtr i, int u) {
        i.set(i.get() & 0xFFFFC03F | u << 6 & 0x3FC0);
    }

    static void SETARG_B(InstructionPtr i, int u) {
        i.set(i.get() & 0x7FFFFF | u << 23 & 0xFF800000);
    }

    static void SETARG_C(InstructionPtr i, int u) {
        i.set(i.get() & 0xFF803FFF | u << 14 & 0x7FC000);
    }

    static void SETARG_Bx(InstructionPtr i, int u) {
        i.set(i.get() & 0x3FFF | u << 14 & 0xFFFFC000);
    }

    static void SETARG_sBx(InstructionPtr i, int u) {
        FuncState.SETARG_Bx(i, u + 131071);
    }

    static int CREATE_ABC(int o, int a, int b, int c) {
        return o << 0 & 0x3F | a << 6 & 0x3FC0 | b << 23 & 0xFF800000 | c << 14 & 0x7FC000;
    }

    static int CREATE_ABx(int o, int a, int bc) {
        return o << 0 & 0x3F | a << 6 & 0x3FC0 | bc << 14 & 0xFFFFC000;
    }

    static Object[] realloc(Object[] v, int n) {
        Object[] a = new Object[n];
        if (v != null) {
            System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
        }
        return a;
    }

    static String[] realloc(String[] v, int n) {
        String[] a = new String[n];
        if (v != null) {
            System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
        }
        return a;
    }

    static Prototype[] realloc(Prototype[] v, int n) {
        Prototype[] a = new Prototype[n];
        if (v != null) {
            System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
        }
        return a;
    }

    static int[] realloc(int[] v, int n) {
        int[] a = new int[n];
        if (v != null) {
            System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
        }
        return a;
    }

    static byte[] realloc(byte[] v, int n) {
        byte[] a = new byte[n];
        if (v != null) {
            System.arraycopy(v, 0, a, 0, Math.min(v.length, n));
        }
        return a;
    }

    public static int GET_OPCODE(int i) {
        return i >> 0 & 0x3F;
    }

    public static int GETARG_A(int i) {
        return i >> 6 & 0xFF;
    }

    public static int GETARG_B(int i) {
        return i >> 23 & 0x1FF;
    }

    public static int GETARG_C(int i) {
        return i >> 14 & 0x1FF;
    }

    public static int GETARG_Bx(int i) {
        return i >> 14 & 0x3FFFF;
    }

    public static int GETARG_sBx(int i) {
        return (i >> 14 & 0x3FFFF) - 131071;
    }

    public static boolean ISK(int x) {
        return 0 != (x & 0x100);
    }

    public static int INDEXK(int r) {
        return r & 0xFFFFFEFF;
    }

    public static int RKASK(int x) {
        return x | 0x100;
    }

    public static int getOpMode(int m) {
        return luaP_opmodes[m] & 3;
    }

    public static int getBMode(int m) {
        return luaP_opmodes[m] >> 4 & 3;
    }

    public static int getCMode(int m) {
        return luaP_opmodes[m] >> 2 & 3;
    }

    public static boolean testTMode(int m) {
        return 0 != (luaP_opmodes[m] & 0x80);
    }

    static {
        luaP_opmodes = new int[]{96, 113, 84, 96, 80, 113, 108, 49, 16, 60, 84, 108, 124, 124, 124, 124, 124, 124, 96, 96, 96, 104, 34, 188, 188, 188, 228, 228, 84, 84, 16, 98, 98, 132, 20, 0, 81, 80};
    }
}

