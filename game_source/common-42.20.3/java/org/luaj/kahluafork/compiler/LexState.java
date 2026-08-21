/*
 * Decompiled with CFR 0.152.
 */
package org.luaj.kahluafork.compiler;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import org.luaj.kahluafork.compiler.BlockCnt;
import org.luaj.kahluafork.compiler.ConsControl;
import org.luaj.kahluafork.compiler.ExpDesc;
import org.luaj.kahluafork.compiler.FuncState;
import org.luaj.kahluafork.compiler.InstructionPtr;
import org.luaj.kahluafork.compiler.LHS_assign;
import org.luaj.kahluafork.compiler.Token;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.Prototype;
import zombie.core.Core;

public final class LexState {
    public int nCcalls;
    protected static final String RESERVED_LOCAL_VAR_FOR_CONTROL = "(for control)";
    protected static final String RESERVED_LOCAL_VAR_FOR_STATE = "(for state)";
    protected static final String RESERVED_LOCAL_VAR_FOR_GENERATOR = "(for generator)";
    protected static final String RESERVED_LOCAL_VAR_FOR_STEP = "(for step)";
    protected static final String RESERVED_LOCAL_VAR_FOR_LIMIT = "(for limit)";
    protected static final String RESERVED_LOCAL_VAR_FOR_INDEX = "(for index)";
    protected static final String[] RESERVED_LOCAL_VAR_KEYWORDS;
    private static final Hashtable<String, Boolean> RESERVED_LOCAL_VAR_KEYWORDS_TABLE;
    private static final int EOZ = -1;
    private static final int MAXSRC = 80;
    private static final int MAX_INT = 0x7FFFFFFD;
    private static final int UCHAR_MAX = 255;
    private static final int LUAI_MAXCCALLS = 200;
    static final int NO_JUMP = -1;
    static final int OPR_ADD = 0;
    static final int OPR_SUB = 1;
    static final int OPR_MUL = 2;
    static final int OPR_DIV = 3;
    static final int OPR_MOD = 4;
    static final int OPR_POW = 5;
    static final int OPR_CONCAT = 6;
    static final int OPR_NE = 7;
    static final int OPR_EQ = 8;
    static final int OPR_LT = 9;
    static final int OPR_LE = 10;
    static final int OPR_GT = 11;
    static final int OPR_GE = 12;
    static final int OPR_AND = 13;
    static final int OPR_OR = 14;
    static final int OPR_NOBINOPR = 15;
    static final int OPR_MINUS = 0;
    static final int OPR_NOT = 1;
    static final int OPR_LEN = 2;
    static final int OPR_NOUNOPR = 3;
    static final int VVOID = 0;
    static final int VNIL = 1;
    static final int VTRUE = 2;
    static final int VFALSE = 3;
    static final int VK = 4;
    static final int VKNUM = 5;
    static final int VLOCAL = 6;
    static final int VUPVAL = 7;
    static final int VGLOBAL = 8;
    static final int VINDEXED = 9;
    static final int VJMP = 10;
    static final int VRELOCABLE = 11;
    static final int VNONRELOC = 12;
    static final int VCALL = 13;
    static final int VVARARG = 14;
    int current;
    int linenumber;
    int lastline;
    final Token t = new Token();
    final Token lookahead = new Token();
    FuncState fs;
    Reader z;
    byte[] buff;
    int nbuff;
    String source;
    static final String[] luaX_tokens;
    static final int TK_AND = 257;
    static final int TK_BREAK = 258;
    static final int TK_DO = 259;
    static final int TK_ELSE = 260;
    static final int TK_ELSEIF = 261;
    static final int TK_END = 262;
    static final int TK_FALSE = 263;
    static final int TK_FOR = 264;
    static final int TK_FUNCTION = 265;
    static final int TK_IF = 266;
    static final int TK_IN = 267;
    static final int TK_LOCAL = 268;
    static final int TK_NIL = 269;
    static final int TK_NOT = 270;
    static final int TK_OR = 271;
    static final int TK_REPEAT = 272;
    static final int TK_RETURN = 273;
    static final int TK_THEN = 274;
    static final int TK_TRUE = 275;
    static final int TK_UNTIL = 276;
    static final int TK_WHILE = 277;
    static final int TK_CONCAT = 278;
    static final int TK_DOTS = 279;
    static final int TK_EQ = 280;
    static final int TK_GE = 281;
    static final int TK_LE = 282;
    static final int TK_NE = 283;
    static final int TK_NUMBER = 284;
    static final int TK_NAME = 285;
    static final int TK_STRING = 286;
    static final int TK_EOS = 287;
    static final int FIRST_RESERVED = 257;
    static final int NUM_RESERVED = 21;
    static final Hashtable<String, Integer> RESERVED;
    static final int[] priorityLeft;
    static final int[] priorityRight;
    static final int UNARY_PRIORITY = 8;

    private static final String LUA_QS(String s) {
        return "'" + s + "'";
    }

    private static final String LUA_QL(Object o) {
        return LexState.LUA_QS(String.valueOf(o));
    }

    public static boolean isReservedKeyword(String varName) {
        return RESERVED_LOCAL_VAR_KEYWORDS_TABLE.containsKey(varName);
    }

    private boolean isalnum(int c) {
        return c >= 48 && c <= 57 || c >= 97 && c <= 122 || c >= 65 && c <= 90 || c == 95;
    }

    private boolean isalpha(int c) {
        return c >= 97 && c <= 122 || c >= 65 && c <= 90;
    }

    private boolean isdigit(int c) {
        return c >= 48 && c <= 57;
    }

    private boolean isspace(int c) {
        return c <= 32;
    }

    public static Prototype compile(int firstByte, Reader z, String name, String source2) {
        if (name != null) {
            source2 = name;
        } else {
            name = "stdin";
            source2 = "[string \"" + LexState.trim((String)source2, 80) + "\"]";
        }
        LexState lexstate = new LexState(z, firstByte, (String)source2);
        FuncState funcstate = new FuncState(lexstate);
        funcstate.isVararg = 2;
        funcstate.f.name = name.intern();
        lexstate.next();
        lexstate.chunk();
        lexstate.check(287);
        lexstate.close_func();
        FuncState._assert(funcstate.prev == null);
        FuncState._assert(funcstate.f.numUpvalues == 0);
        FuncState._assert(lexstate.fs == null);
        return funcstate.f;
    }

    public LexState(Reader stream, int firstByte, String source2) {
        this.z = stream;
        this.buff = new byte[32];
        this.lookahead.token = 287;
        this.fs = null;
        this.linenumber = 1;
        this.lastline = 1;
        this.source = source2;
        this.nbuff = 0;
        this.current = firstByte;
        this.skipShebang();
    }

    void nextChar() {
        try {
            this.current = this.z.read();
        }
        catch (IOException e) {
            e.printStackTrace();
            this.current = -1;
        }
    }

    boolean currIsNewline() {
        return this.current == 10 || this.current == 13;
    }

    void save_and_next() {
        this.save(this.current);
        this.nextChar();
    }

    void save(int c) {
        if (this.buff == null || this.nbuff + 1 > this.buff.length) {
            this.buff = FuncState.realloc(this.buff, this.nbuff * 2 + 1);
        }
        this.buff[this.nbuff++] = (byte)c;
    }

    String token2str(int token) {
        if (token < 257) {
            return LexState.iscntrl(token) ? "char(" + token + ")" : String.valueOf((char)token);
        }
        return luaX_tokens[token - 257];
    }

    private static boolean iscntrl(int token) {
        return token < 32;
    }

    String txtToken(int token) {
        switch (token) {
            case 284: 
            case 285: 
            case 286: {
                return new String(this.buff, 0, this.nbuff);
            }
        }
        return this.token2str(token);
    }

    void lexerror(String msg, int token) {
        String cid = this.source;
        String errorMessage = token != 0 ? cid + ":" + this.linenumber + ": " + msg + " near `" + this.txtToken(token) + "`" : cid + ":" + this.linenumber + ": " + msg;
        throw new KahluaException(errorMessage, cid, this.linenumber);
    }

    private static String trim(String s, int max) {
        if (s.length() > max) {
            return s.substring(0, max - 3) + "...";
        }
        return s;
    }

    void syntaxerror(String msg) {
        this.lexerror(msg, this.t.token);
    }

    String newstring(byte[] chars, int offset, int len) {
        try {
            String s = new String(chars, offset, len, "UTF-8");
            return s.intern();
        }
        catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    void inclinenumber() {
        int old = this.current;
        FuncState._assert(this.currIsNewline());
        this.nextChar();
        if (this.currIsNewline() && this.current != old) {
            this.nextChar();
        }
        if (++this.linenumber >= 0x7FFFFFFD) {
            this.syntaxerror("chunk has too many lines");
        }
    }

    private void skipShebang() {
        if (this.current == 35) {
            while (!this.currIsNewline() && this.current != -1) {
                this.nextChar();
            }
        }
    }

    boolean check_next(String set) {
        if (set.indexOf(this.current) < 0) {
            return false;
        }
        this.save_and_next();
        return true;
    }

    void str2d(String str, Token token) {
        try {
            double d = str.startsWith("0x") ? (double)Long.parseLong(str.substring(2), 16) : Double.parseDouble(str);
            token.r = d;
        }
        catch (NumberFormatException e) {
            this.lexerror("malformed number", 284);
        }
    }

    void read_numeral(Token token) {
        FuncState._assert(this.isdigit(this.current));
        do {
            this.save_and_next();
        } while (this.isdigit(this.current) || this.current == 46);
        if (this.check_next("Ee")) {
            this.check_next("+-");
        }
        while (this.isalnum(this.current) || this.current == 95) {
            this.save_and_next();
        }
        String str = new String(this.buff, 0, this.nbuff);
        this.str2d(str, token);
    }

    int skip_sep() {
        int count = 0;
        int s = this.current;
        FuncState._assert(s == 91 || s == 93);
        this.save_and_next();
        while (this.current == 61) {
            this.save_and_next();
            ++count;
        }
        return this.current == s ? count : -count - 1;
    }

    void read_long_string(Token token, int sep) {
        int cont = 0;
        this.save_and_next();
        if (this.currIsNewline()) {
            this.inclinenumber();
        }
        boolean endloop = false;
        block6: while (!endloop) {
            switch (this.current) {
                case -1: {
                    this.lexerror(token != null ? "unfinished long string" : "unfinished long comment", 287);
                    continue block6;
                }
                case 91: {
                    if (this.skip_sep() != sep) continue block6;
                    this.save_and_next();
                    ++cont;
                    continue block6;
                }
                case 93: {
                    if (this.skip_sep() != sep) continue block6;
                    this.save_and_next();
                    endloop = true;
                    continue block6;
                }
                case 10: 
                case 13: {
                    this.save(10);
                    this.inclinenumber();
                    if (token != null) continue block6;
                    this.nbuff = 0;
                    continue block6;
                }
            }
            if (token != null) {
                this.save_and_next();
                continue;
            }
            this.nextChar();
        }
        if (token != null) {
            token.ts = this.newstring(this.buff, 2 + sep, this.nbuff - 2 * (2 + sep));
        }
    }

    void read_string(int del, Token token) {
        this.save_and_next();
        block16: while (this.current != del) {
            switch (this.current) {
                case -1: {
                    this.lexerror("unfinished string", 287);
                    continue block16;
                }
                case 10: 
                case 13: {
                    this.lexerror("unfinished string", 286);
                    continue block16;
                }
                case 92: {
                    int c;
                    this.nextChar();
                    switch (this.current) {
                        case 97: {
                            c = 7;
                            break;
                        }
                        case 98: {
                            c = 8;
                            break;
                        }
                        case 102: {
                            c = 12;
                            break;
                        }
                        case 110: {
                            c = 10;
                            break;
                        }
                        case 114: {
                            c = 13;
                            break;
                        }
                        case 116: {
                            c = 9;
                            break;
                        }
                        case 118: {
                            c = 11;
                            break;
                        }
                        case 10: 
                        case 13: {
                            this.save(10);
                            this.inclinenumber();
                            continue block16;
                        }
                        case -1: {
                            continue block16;
                        }
                        default: {
                            if (!this.isdigit(this.current)) {
                                this.save_and_next();
                                continue block16;
                            }
                            int i = 0;
                            c = 0;
                            do {
                                c = 10 * c + this.current - 48;
                                this.nextChar();
                            } while (++i < 3 && this.isdigit(this.current));
                            if (c > 255) {
                                this.lexerror("escape sequence too large", 286);
                            }
                            this.save(c);
                            continue block16;
                        }
                    }
                    this.save(c);
                    this.nextChar();
                    continue block16;
                }
            }
            this.save_and_next();
        }
        this.save_and_next();
        token.ts = this.newstring(this.buff, 1, this.nbuff - 2);
    }

    int llex(Token token) {
        this.nbuff = 0;
        block12: while (true) {
            switch (this.current) {
                case 10: 
                case 13: {
                    this.inclinenumber();
                    continue block12;
                }
                case 45: {
                    int sep;
                    this.nextChar();
                    if (this.current != 45) {
                        return 45;
                    }
                    this.nextChar();
                    if (this.current == 91) {
                        sep = this.skip_sep();
                        this.nbuff = 0;
                        if (sep >= 0) {
                            this.read_long_string(null, sep);
                            this.nbuff = 0;
                            continue block12;
                        }
                    }
                    while (true) {
                        if (this.currIsNewline() || this.current == -1) continue block12;
                        this.nextChar();
                    }
                }
                case 91: {
                    int sep = this.skip_sep();
                    if (sep >= 0) {
                        this.read_long_string(token, sep);
                        return 286;
                    }
                    if (sep == -1) {
                        return 91;
                    }
                    this.lexerror("invalid long string delimiter", 286);
                }
                case 61: {
                    this.nextChar();
                    if (this.current != 61) {
                        return 61;
                    }
                    this.nextChar();
                    return 280;
                }
                case 60: {
                    this.nextChar();
                    if (this.current != 61) {
                        return 60;
                    }
                    this.nextChar();
                    return 282;
                }
                case 62: {
                    this.nextChar();
                    if (this.current != 61) {
                        return 62;
                    }
                    this.nextChar();
                    return 281;
                }
                case 126: {
                    this.nextChar();
                    if (this.current != 61) {
                        return 126;
                    }
                    this.nextChar();
                    return 283;
                }
                case 34: 
                case 39: {
                    this.read_string(this.current, token);
                    return 286;
                }
                case 46: {
                    this.save_and_next();
                    if (this.check_next(".")) {
                        if (this.check_next(".")) {
                            return 279;
                        }
                        return 278;
                    }
                    if (!this.isdigit(this.current)) {
                        return 46;
                    }
                    this.read_numeral(token);
                    return 284;
                }
                case -1: {
                    return 287;
                }
            }
            if (!this.isspace(this.current)) break;
            FuncState._assert(!this.currIsNewline());
            this.nextChar();
        }
        if (this.isdigit(this.current)) {
            this.read_numeral(token);
            return 284;
        }
        if (this.isalpha(this.current) || this.current == 95) {
            do {
                this.save_and_next();
            } while (this.isalnum(this.current) || this.current == 95);
            String ts = this.newstring(this.buff, 0, this.nbuff);
            if (RESERVED.containsKey(ts)) {
                return RESERVED.get(ts);
            }
            token.ts = ts;
            return 285;
        }
        int c = this.current;
        this.nextChar();
        return c;
    }

    void next() {
        this.lastline = this.linenumber;
        if (this.lookahead.token != 287) {
            this.t.set(this.lookahead);
            this.lookahead.token = 287;
        } else {
            this.t.token = this.llex(this.t);
        }
    }

    void lookahead() {
        FuncState._assert(this.lookahead.token == 287);
        this.lookahead.token = this.llex(this.lookahead);
    }

    boolean hasmultret(int k) {
        return k == 13 || k == 14;
    }

    void error_expected(int token) {
        this.syntaxerror(LexState.LUA_QS(this.token2str(token)) + " expected");
    }

    boolean testnext(int c) {
        if (this.t.token == c) {
            this.next();
            return true;
        }
        return false;
    }

    void check(int c) {
        if (this.t.token != c) {
            this.error_expected(c);
        }
    }

    void checknext(int c) {
        this.check(c);
        this.next();
    }

    void check_condition(boolean c, String msg) {
        if (!c) {
            this.syntaxerror(msg);
        }
    }

    void check_match(int what, int who, int where) {
        if (!this.testnext(what)) {
            if (where == this.linenumber) {
                this.error_expected(what);
            } else {
                this.syntaxerror(LexState.LUA_QS(this.token2str(what)) + " expected (to close " + LexState.LUA_QS(this.token2str(who)) + " at line " + where + ")");
            }
        }
    }

    String str_checkname() {
        this.check(285);
        String ts = this.t.ts;
        this.next();
        return ts;
    }

    void codestring(ExpDesc e, String s) {
        e.init(4, this.fs.stringK(s));
    }

    void checkname(ExpDesc e) {
        this.codestring(e, this.str_checkname());
    }

    int registerlocalvar(String varname) {
        FuncState fs = this.fs;
        if (fs.locvars == null || fs.nlocvars + 1 > fs.locvars.length) {
            fs.locvars = FuncState.realloc(fs.locvars, fs.nlocvars * 2 + 1);
        }
        fs.locvars[fs.nlocvars] = varname;
        return fs.nlocvars++;
    }

    void new_localvarliteral(String v, int n) {
        this.new_localvar(v, n);
    }

    void new_localvar(String name, int n, int line) {
        FuncState fs = this.fs;
        fs.checklimit(fs.nactvar + n + 1, 200, "local variables");
        fs.actvar[fs.nactvar + n] = (short)this.registerlocalvar(name);
        if (Core.debug) {
            fs.actvarline[fs.actvar[fs.nactvar + n]] = this.linenumber;
            fs.nactvarline = Math.max(fs.nactvarline, fs.actvar[fs.nactvar + n] + 1);
        }
    }

    void new_localvar(String name, int n) {
        FuncState fs = this.fs;
        fs.checklimit(fs.nactvar + n + 1, 200, "local variables");
        fs.actvar[fs.nactvar + n] = (short)this.registerlocalvar(name);
        if (Core.debug) {
            fs.actvarline[fs.actvar[fs.nactvar + n]] = this.linenumber;
            fs.nactvarline = Math.max(fs.nactvarline, fs.actvar[fs.nactvar + n] + 1);
        }
    }

    void adjustlocalvars(int nvars) {
        FuncState fs = this.fs;
        fs.nactvar += nvars;
    }

    void removevars(int tolevel) {
        FuncState fs = this.fs;
        fs.nactvar = tolevel;
    }

    void singlevar(ExpDesc var) {
        FuncState fs = this.fs;
        String varname = this.str_checkname();
        if (fs.singlevaraux(varname, var, 1) == 8) {
            var.info = fs.stringK(varname);
        }
    }

    void adjust_assign(int nvars, int nexps, ExpDesc e) {
        FuncState fs = this.fs;
        int extra = nvars - nexps;
        if (this.hasmultret(e.k)) {
            if (++extra < 0) {
                extra = 0;
            }
            fs.setreturns(e, extra);
            if (extra > 1) {
                fs.reserveregs(extra - 1);
            }
        } else {
            if (e.k != 0) {
                fs.exp2nextreg(e);
            }
            if (extra > 0) {
                int reg = fs.freereg;
                fs.reserveregs(extra);
                fs.nil(reg, extra);
            }
        }
    }

    void enterlevel() {
        if (++this.nCcalls > 200) {
            this.lexerror("chunk has too many syntax levels", 0);
        }
    }

    void leavelevel() {
        --this.nCcalls;
    }

    void pushclosure(FuncState func, ExpDesc v) {
        FuncState fs = this.fs;
        Prototype f = fs.f;
        if (f.prototypes == null || fs.np + 1 > f.prototypes.length) {
            f.prototypes = FuncState.realloc(f.prototypes, fs.np * 2 + 1);
        }
        f.prototypes[fs.np++] = func.f;
        v.init(11, fs.codeABx(36, 0, fs.np - 1));
        for (int i = 0; i < func.f.numUpvalues; ++i) {
            int o = func.upvaluesK[i] == 6 ? 0 : 4;
            fs.codeABC(o, 0, func.upvaluesInfo[i], 0);
        }
    }

    void close_func() {
        FuncState fs = this.fs;
        Prototype f = fs.f;
        f.isVararg = fs.isVararg != 0;
        this.removevars(0);
        fs.ret(0, 0);
        f.code = FuncState.realloc(f.code, fs.pc);
        f.lines = FuncState.realloc(f.lines, fs.pc);
        f.constants = FuncState.realloc(f.constants, fs.nk);
        f.prototypes = FuncState.realloc(f.prototypes, fs.np);
        fs.locvars = FuncState.realloc(fs.locvars, fs.nlocvars);
        f.locvars = fs.locvars;
        if (Core.debug && fs.nactvarline > 0) {
            f.locvarlines = FuncState.realloc(fs.actvarline, fs.nactvarline);
        }
        fs.upvalues = FuncState.realloc(fs.upvalues, f.numUpvalues);
        FuncState._assert(fs.bl == null);
        this.fs = fs.prev;
    }

    void field(ExpDesc v) {
        FuncState fs = this.fs;
        ExpDesc key = new ExpDesc();
        fs.exp2anyreg(v);
        this.next();
        this.checkname(key);
        fs.indexed(v, key);
    }

    void yindex(ExpDesc v) {
        this.next();
        this.expr(v);
        this.fs.exp2val(v);
        this.checknext(93);
    }

    void recfield(ConsControl cc) {
        FuncState fs = this.fs;
        int reg = this.fs.freereg;
        ExpDesc key = new ExpDesc();
        ExpDesc val = new ExpDesc();
        if (this.t.token == 285) {
            fs.checklimit(cc.nh, 0x7FFFFFFD, "items in a constructor");
            this.checkname(key);
        } else {
            this.yindex(key);
        }
        ++cc.nh;
        this.checknext(61);
        int rkkey = fs.exp2RK(key);
        this.expr(val);
        fs.codeABC(9, cc.t.info, rkkey, fs.exp2RK(val));
        fs.freereg = reg;
    }

    void listfield(ConsControl cc) {
        this.expr(cc.v);
        this.fs.checklimit(cc.na, 0x7FFFFFFD, "items in a constructor");
        ++cc.na;
        ++cc.tostore;
    }

    void constructor(ExpDesc t) {
        FuncState fs = this.fs;
        int line = this.linenumber;
        int pc = fs.codeABC(10, 0, 0, 0);
        ConsControl cc = new ConsControl();
        cc.tostore = 0;
        cc.nh = 0;
        cc.na = 0;
        cc.t = t;
        t.init(11, pc);
        cc.v.init(0, 0);
        fs.exp2nextreg(t);
        this.checknext(123);
        do {
            FuncState._assert(cc.v.k == 0 || cc.tostore > 0);
            if (this.t.token == 125) break;
            fs.closelistfield(cc);
            switch (this.t.token) {
                case 285: {
                    this.lookahead();
                    if (this.lookahead.token != 61) {
                        this.listfield(cc);
                        break;
                    }
                    this.recfield(cc);
                    break;
                }
                case 91: {
                    this.recfield(cc);
                    break;
                }
                default: {
                    this.listfield(cc);
                }
            }
        } while (this.testnext(44) || this.testnext(59));
        this.check_match(125, 123, line);
        fs.lastlistfield(cc);
        InstructionPtr i = new InstructionPtr(fs.f.code, pc);
        FuncState.SETARG_B(i, LexState.luaO_int2fb(cc.na));
        FuncState.SETARG_C(i, LexState.luaO_int2fb(cc.nh));
    }

    static int luaO_int2fb(int x) {
        int e = 0;
        while (x >= 16) {
            x = x + 1 >> 1;
            ++e;
        }
        if (x < 8) {
            return x;
        }
        return e + 1 << 3 | x - 8;
    }

    void parlist() {
        FuncState fs = this.fs;
        Prototype f = fs.f;
        int nparams = 0;
        fs.isVararg = 0;
        if (this.t.token != 41) {
            do {
                switch (this.t.token) {
                    case 285: {
                        this.new_localvar(this.str_checkname(), nparams++);
                        break;
                    }
                    case 279: {
                        this.next();
                        fs.isVararg |= 2;
                        break;
                    }
                    default: {
                        this.syntaxerror("<name> or " + LexState.LUA_QL("...") + " expected");
                    }
                }
            } while (fs.isVararg == 0 && this.testnext(44));
        }
        this.adjustlocalvars(nparams);
        f.numParams = fs.nactvar - (fs.isVararg & 1);
        fs.reserveregs(fs.nactvar);
    }

    void body(ExpDesc e, boolean needself, int line) {
        FuncState new_fs = new FuncState(this, this.t.ts);
        new_fs.linedefined = line;
        this.checknext(40);
        if (needself) {
            this.new_localvarliteral("self", 0);
            this.adjustlocalvars(1);
        }
        this.parlist();
        this.checknext(41);
        this.chunk();
        new_fs.lastlinedefined = this.linenumber;
        this.check_match(262, 265, line);
        this.close_func();
        this.pushclosure(new_fs, e);
    }

    int explist1(ExpDesc v) {
        int n = 1;
        this.expr(v);
        while (this.testnext(44)) {
            this.fs.exp2nextreg(v);
            this.expr(v);
            ++n;
        }
        return n;
    }

    void funcargs(ExpDesc f) {
        int nparams;
        FuncState fs = this.fs;
        ExpDesc args2 = new ExpDesc();
        int line = this.linenumber;
        switch (this.t.token) {
            case 40: {
                if (line != this.lastline) {
                    this.syntaxerror("ambiguous syntax (function call x new statement)");
                }
                this.next();
                if (this.t.token == 41) {
                    args2.k = 0;
                } else {
                    this.explist1(args2);
                    fs.setmultret(args2);
                }
                this.check_match(41, 40, line);
                break;
            }
            case 123: {
                this.constructor(args2);
                break;
            }
            case 286: {
                this.codestring(args2, this.t.ts);
                this.next();
                break;
            }
            default: {
                this.syntaxerror("function arguments expected");
                return;
            }
        }
        FuncState._assert(f.k == 12);
        int base = f.info;
        if (this.hasmultret(args2.k)) {
            nparams = -1;
        } else {
            if (args2.k != 0) {
                fs.exp2nextreg(args2);
            }
            nparams = fs.freereg - (base + 1);
        }
        f.init(13, fs.codeABC(28, base, nparams + 1, 2));
        fs.fixline(line);
        fs.freereg = base + 1;
    }

    void prefixexp(ExpDesc v) {
        switch (this.t.token) {
            case 40: {
                int line = this.linenumber;
                this.next();
                this.expr(v);
                this.check_match(41, 40, line);
                this.fs.dischargevars(v);
                return;
            }
            case 285: {
                this.singlevar(v);
                return;
            }
        }
        this.syntaxerror("unexpected symbol");
    }

    void primaryexp(ExpDesc v) {
        FuncState fs = this.fs;
        this.prefixexp(v);
        block6: while (true) {
            switch (this.t.token) {
                case 46: {
                    this.field(v);
                    continue block6;
                }
                case 91: {
                    ExpDesc key = new ExpDesc();
                    fs.exp2anyreg(v);
                    this.yindex(key);
                    fs.indexed(v, key);
                    continue block6;
                }
                case 58: {
                    ExpDesc key = new ExpDesc();
                    this.next();
                    this.checkname(key);
                    fs.self(v, key);
                    this.funcargs(v);
                    continue block6;
                }
                case 40: 
                case 123: 
                case 286: {
                    fs.exp2nextreg(v);
                    this.funcargs(v);
                    continue block6;
                }
            }
            break;
        }
    }

    void simpleexp(ExpDesc v) {
        switch (this.t.token) {
            case 284: {
                v.init(5, 0);
                v.setNval(this.t.r);
                break;
            }
            case 286: {
                this.codestring(v, this.t.ts);
                break;
            }
            case 269: {
                v.init(1, 0);
                break;
            }
            case 275: {
                v.init(2, 0);
                break;
            }
            case 263: {
                v.init(3, 0);
                break;
            }
            case 279: {
                FuncState fs = this.fs;
                this.check_condition(fs.isVararg != 0, "cannot use " + LexState.LUA_QL("...") + " outside a vararg function");
                fs.isVararg &= 0xFFFFFFFB;
                v.init(14, fs.codeABC(37, 0, 1, 0));
                break;
            }
            case 123: {
                this.constructor(v);
                return;
            }
            case 265: {
                this.next();
                this.body(v, false, this.linenumber);
                return;
            }
            default: {
                this.primaryexp(v);
                return;
            }
        }
        this.next();
    }

    int getunopr(int op) {
        switch (op) {
            case 270: {
                return 1;
            }
            case 45: {
                return 0;
            }
            case 35: {
                return 2;
            }
        }
        return 3;
    }

    int getbinopr(int op) {
        switch (op) {
            case 43: {
                return 0;
            }
            case 45: {
                return 1;
            }
            case 42: {
                return 2;
            }
            case 47: {
                return 3;
            }
            case 37: {
                return 4;
            }
            case 94: {
                return 5;
            }
            case 278: {
                return 6;
            }
            case 283: {
                return 7;
            }
            case 280: {
                return 8;
            }
            case 60: {
                return 9;
            }
            case 282: {
                return 10;
            }
            case 62: {
                return 11;
            }
            case 281: {
                return 12;
            }
            case 257: {
                return 13;
            }
            case 271: {
                return 14;
            }
        }
        return 15;
    }

    int subexpr(ExpDesc v, int limit) {
        this.enterlevel();
        int uop = this.getunopr(this.t.token);
        if (uop != 3) {
            this.next();
            this.subexpr(v, 8);
            this.fs.prefix(uop, v);
        } else {
            this.simpleexp(v);
        }
        int op = this.getbinopr(this.t.token);
        while (op != 15 && priorityLeft[op] > limit) {
            ExpDesc v2 = new ExpDesc();
            this.next();
            this.fs.infix(op, v);
            int nextop = this.subexpr(v2, priorityRight[op]);
            this.fs.posfix(op, v, v2);
            op = nextop;
        }
        this.leavelevel();
        return op;
    }

    void expr(ExpDesc v) {
        this.subexpr(v, 0);
    }

    boolean block_follow(int token) {
        switch (token) {
            case 260: 
            case 261: 
            case 262: 
            case 276: 
            case 287: {
                return true;
            }
        }
        return false;
    }

    void block() {
        FuncState fs = this.fs;
        BlockCnt bl = new BlockCnt();
        fs.enterblock(bl, false);
        this.chunk();
        FuncState._assert(bl.breaklist == -1);
        fs.leaveblock();
    }

    void check_conflict(LHS_assign lh, ExpDesc v) {
        FuncState fs = this.fs;
        int extra = fs.freereg;
        boolean conflict = false;
        while (lh != null) {
            if (lh.v.k == 9) {
                if (lh.v.info == v.info) {
                    conflict = true;
                    lh.v.info = extra;
                }
                if (lh.v.aux == v.info) {
                    conflict = true;
                    lh.v.aux = extra;
                }
            }
            lh = lh.prev;
        }
        if (conflict) {
            fs.codeABC(0, fs.freereg, v.info, 0);
            fs.reserveregs(1);
        }
    }

    void assignment(LHS_assign lh, int nvars) {
        ExpDesc e = new ExpDesc();
        this.check_condition(6 <= lh.v.k && lh.v.k <= 9, "syntax error");
        if (this.testnext(44)) {
            LHS_assign nv = new LHS_assign();
            nv.prev = lh;
            this.primaryexp(nv.v);
            if (nv.v.k == 6) {
                this.check_conflict(lh, nv.v);
            }
            this.assignment(nv, nvars + 1);
        } else {
            this.checknext(61);
            int nexps = this.explist1(e);
            if (nexps != nvars) {
                this.adjust_assign(nvars, nexps, e);
                if (nexps > nvars) {
                    this.fs.freereg -= nexps - nvars;
                }
            } else {
                this.fs.setoneret(e);
                this.fs.storevar(lh.v, e);
                return;
            }
        }
        e.init(12, this.fs.freereg - 1);
        this.fs.storevar(lh.v, e);
    }

    int cond() {
        ExpDesc v = new ExpDesc();
        this.expr(v);
        if (v.k == 1) {
            v.k = 3;
        }
        this.fs.goiftrue(v);
        return v.f;
    }

    void breakstat() {
        FuncState fs = this.fs;
        BlockCnt bl = fs.bl;
        boolean upval = false;
        while (bl != null && !bl.isbreakable) {
            upval |= bl.upval;
            bl = bl.previous;
        }
        if (bl == null) {
            this.syntaxerror("no loop to break");
        }
        if (upval) {
            fs.codeABC(35, bl.nactvar, 0, 0);
        }
        bl.breaklist = fs.concat(bl.breaklist, fs.jump());
    }

    void whilestat(int line) {
        FuncState fs = this.fs;
        BlockCnt bl = new BlockCnt();
        this.next();
        int whileinit = fs.getlabel();
        int condexit = this.cond();
        fs.enterblock(bl, true);
        this.checknext(259);
        this.block();
        fs.patchlist(fs.jump(), whileinit);
        this.check_match(262, 277, line);
        fs.leaveblock();
        fs.patchtohere(condexit);
    }

    void repeatstat(int line) {
        FuncState fs = this.fs;
        int repeat_init = fs.getlabel();
        BlockCnt bl1 = new BlockCnt();
        BlockCnt bl2 = new BlockCnt();
        fs.enterblock(bl1, true);
        fs.enterblock(bl2, false);
        this.next();
        this.chunk();
        this.check_match(276, 272, line);
        int condexit = this.cond();
        if (!bl2.upval) {
            fs.leaveblock();
            fs.patchlist(condexit, repeat_init);
        } else {
            this.breakstat();
            fs.patchtohere(condexit);
            fs.leaveblock();
            fs.patchlist(fs.jump(), repeat_init);
        }
        fs.leaveblock();
    }

    int exp1() {
        ExpDesc e = new ExpDesc();
        this.expr(e);
        int k = e.k;
        this.fs.exp2nextreg(e);
        return k;
    }

    void forbody(int base, int line, int nvars, boolean isnum) {
        BlockCnt bl = new BlockCnt();
        FuncState fs = this.fs;
        this.adjustlocalvars(3);
        this.checknext(259);
        int prep = isnum ? fs.codeAsBx(32, base, -1) : fs.jump();
        fs.enterblock(bl, false);
        this.adjustlocalvars(nvars);
        fs.reserveregs(nvars);
        this.block();
        fs.leaveblock();
        fs.patchtohere(prep);
        int endfor = isnum ? fs.codeAsBx(31, base, -1) : fs.codeABC(33, base, 0, nvars);
        fs.fixline(line);
        fs.patchlist(isnum ? endfor : fs.jump(), prep + 1);
    }

    void fornum(String varname, int line) {
        FuncState fs = this.fs;
        int base = fs.freereg;
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_INDEX, 0);
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_LIMIT, 1);
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_STEP, 2);
        this.new_localvar(varname, 3);
        this.checknext(61);
        this.exp1();
        this.checknext(44);
        this.exp1();
        if (this.testnext(44)) {
            this.exp1();
        } else {
            fs.codeABx(1, fs.freereg, fs.numberK(1.0));
            fs.reserveregs(1);
        }
        this.forbody(base, line, 1, true);
    }

    void forlist(String indexname) {
        FuncState fs = this.fs;
        ExpDesc e = new ExpDesc();
        int nvars = 0;
        int base = fs.freereg;
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_GENERATOR, nvars++);
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_STATE, nvars++);
        this.new_localvarliteral(RESERVED_LOCAL_VAR_FOR_CONTROL, nvars++);
        this.new_localvar(indexname, nvars++);
        while (this.testnext(44)) {
            this.new_localvar(this.str_checkname(), nvars++);
        }
        this.checknext(267);
        int line = this.linenumber;
        this.adjust_assign(3, this.explist1(e), e);
        fs.checkstack(3);
        this.forbody(base, line, nvars - 3, false);
    }

    void forstat(int line) {
        FuncState fs = this.fs;
        BlockCnt bl = new BlockCnt();
        fs.enterblock(bl, true);
        this.next();
        String varname = this.str_checkname();
        switch (this.t.token) {
            case 61: {
                this.fornum(varname, line);
                break;
            }
            case 44: 
            case 267: {
                this.forlist(varname);
                break;
            }
            default: {
                this.syntaxerror(LexState.LUA_QL("=") + " or " + LexState.LUA_QL("in") + " expected");
            }
        }
        this.check_match(262, 264, line);
        fs.leaveblock();
    }

    int test_then_block() {
        this.next();
        int condexit = this.cond();
        this.checknext(274);
        this.block();
        return condexit;
    }

    void ifstat(int line) {
        FuncState fs = this.fs;
        int escapelist = -1;
        int flist = this.test_then_block();
        while (this.t.token == 261) {
            escapelist = fs.concat(escapelist, fs.jump());
            fs.patchtohere(flist);
            flist = this.test_then_block();
        }
        if (this.t.token == 260) {
            escapelist = fs.concat(escapelist, fs.jump());
            fs.patchtohere(flist);
            this.next();
            this.block();
        } else {
            escapelist = fs.concat(escapelist, flist);
        }
        fs.patchtohere(escapelist);
        this.check_match(262, 266, line);
    }

    void localfunc() {
        ExpDesc v = new ExpDesc();
        ExpDesc b = new ExpDesc();
        FuncState fs = this.fs;
        this.new_localvar(this.str_checkname(), 0);
        v.init(6, fs.freereg);
        fs.reserveregs(1);
        this.adjustlocalvars(1);
        this.body(b, false, this.linenumber);
        fs.storevar(v, b);
    }

    void localstat(int line) {
        int nexps;
        int nvars = 0;
        ExpDesc e = new ExpDesc();
        do {
            this.new_localvar(this.str_checkname(), nvars++, line);
        } while (this.testnext(44));
        if (this.testnext(61)) {
            nexps = this.explist1(e);
        } else {
            e.k = 0;
            nexps = 0;
        }
        this.adjust_assign(nvars, nexps, e);
        this.adjustlocalvars(nvars);
    }

    boolean funcname(ExpDesc v) {
        boolean needself = false;
        this.singlevar(v);
        while (this.t.token == 46) {
            this.field(v);
        }
        if (this.t.token == 58) {
            needself = true;
            this.field(v);
        }
        return needself;
    }

    void funcstat(int line) {
        ExpDesc v = new ExpDesc();
        ExpDesc b = new ExpDesc();
        this.next();
        boolean needself = this.funcname(v);
        this.body(b, needself, line);
        this.fs.storevar(v, b);
        this.fs.fixline(line);
    }

    void exprstat() {
        FuncState fs = this.fs;
        LHS_assign v = new LHS_assign();
        this.primaryexp(v.v);
        if (v.v.k == 13) {
            FuncState.SETARG_C(fs.getcodePtr(v.v), 1);
        } else {
            v.prev = null;
            this.assignment(v, 1);
        }
    }

    void retstat() {
        int first;
        int nret;
        FuncState fs = this.fs;
        ExpDesc e = new ExpDesc();
        this.next();
        if (this.block_follow(this.t.token) || this.t.token == 59) {
            nret = 0;
            first = 0;
        } else {
            nret = this.explist1(e);
            if (this.hasmultret(e.k)) {
                fs.setmultret(e);
                if (e.k == 13 && nret == 1) {
                    FuncState.SET_OPCODE(fs.getcodePtr(e), 29);
                    FuncState._assert(FuncState.GETARG_A(fs.getcode(e)) == fs.nactvar);
                }
                first = fs.nactvar;
                nret = -1;
            } else if (nret == 1) {
                first = fs.exp2anyreg(e);
            } else {
                fs.exp2nextreg(e);
                first = fs.nactvar;
                FuncState._assert(nret == fs.freereg - first);
            }
        }
        fs.ret(first, nret);
    }

    boolean statement() {
        int line = this.linenumber;
        switch (this.t.token) {
            case 266: {
                this.ifstat(line);
                return false;
            }
            case 277: {
                this.whilestat(line);
                return false;
            }
            case 259: {
                this.next();
                this.block();
                this.check_match(262, 259, line);
                return false;
            }
            case 264: {
                this.forstat(line);
                return false;
            }
            case 272: {
                this.repeatstat(line);
                return false;
            }
            case 265: {
                this.funcstat(line);
                return false;
            }
            case 268: {
                this.next();
                if (this.testnext(265)) {
                    this.localfunc();
                } else {
                    this.localstat(line);
                }
                return false;
            }
            case 273: {
                this.retstat();
                return true;
            }
            case 258: {
                this.next();
                this.breakstat();
                return true;
            }
        }
        this.exprstat();
        return false;
    }

    void chunk() {
        boolean islast = false;
        this.enterlevel();
        while (!islast && !this.block_follow(this.t.token)) {
            islast = this.statement();
            this.testnext(59);
            FuncState._assert(this.fs.f.maxStacksize >= this.fs.freereg && this.fs.freereg >= this.fs.nactvar);
            this.fs.freereg = this.fs.nactvar;
        }
        this.leavelevel();
    }

    static {
        int i;
        RESERVED_LOCAL_VAR_KEYWORDS = new String[]{RESERVED_LOCAL_VAR_FOR_CONTROL, RESERVED_LOCAL_VAR_FOR_GENERATOR, RESERVED_LOCAL_VAR_FOR_INDEX, RESERVED_LOCAL_VAR_FOR_LIMIT, RESERVED_LOCAL_VAR_FOR_STATE, RESERVED_LOCAL_VAR_FOR_STEP};
        RESERVED_LOCAL_VAR_KEYWORDS_TABLE = new Hashtable();
        for (i = 0; i < RESERVED_LOCAL_VAR_KEYWORDS.length; ++i) {
            RESERVED_LOCAL_VAR_KEYWORDS_TABLE.put(RESERVED_LOCAL_VAR_KEYWORDS[i], Boolean.TRUE);
        }
        luaX_tokens = new String[]{"and", "break", "do", "else", "elseif", "end", "false", "for", "function", "if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "true", "until", "while", "..", "...", "==", ">=", "<=", "~=", "<number>", "<name>", "<string>", "<eof>"};
        RESERVED = new Hashtable();
        for (i = 0; i < 21; ++i) {
            String ts = luaX_tokens[i];
            RESERVED.put(ts, 257 + i);
        }
        priorityLeft = new int[]{6, 6, 7, 7, 7, 10, 5, 3, 3, 3, 3, 3, 3, 2, 1};
        priorityRight = new int[]{6, 6, 7, 7, 7, 9, 4, 3, 3, 3, 3, 3, 3, 2, 1};
    }
}

