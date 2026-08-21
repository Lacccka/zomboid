/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.util.glu.tessellation;

import org.lwjglx.util.glu.tessellation.GLUface;
import org.lwjglx.util.glu.tessellation.GLUhalfEdge;
import org.lwjglx.util.glu.tessellation.GLUmesh;
import org.lwjglx.util.glu.tessellation.GLUvertex;

class Mesh {
    private Mesh() {
    }

    static GLUhalfEdge MakeEdge(GLUhalfEdge eNext) {
        GLUhalfEdge ePrev;
        GLUhalfEdge e = new GLUhalfEdge(true);
        GLUhalfEdge eSym = new GLUhalfEdge(false);
        if (!eNext.first) {
            eNext = eNext.sym;
        }
        eSym.next = ePrev = eNext.sym.next;
        ePrev.sym.next = e;
        e.next = eNext;
        eNext.sym.next = eSym;
        e.sym = eSym;
        e.oNext = e;
        e.lNext = eSym;
        e.org = null;
        e.lFace = null;
        e.winding = 0;
        e.activeRegion = null;
        eSym.sym = e;
        eSym.oNext = eSym;
        eSym.lNext = e;
        eSym.org = null;
        eSym.lFace = null;
        eSym.winding = 0;
        eSym.activeRegion = null;
        return e;
    }

    static void Splice(GLUhalfEdge a, GLUhalfEdge b) {
        GLUhalfEdge aOnext = a.oNext;
        GLUhalfEdge bOnext = b.oNext;
        aOnext.sym.lNext = b;
        bOnext.sym.lNext = a;
        a.oNext = bOnext;
        b.oNext = aOnext;
    }

    static void MakeVertex(GLUvertex newVertex, GLUhalfEdge eOrig, GLUvertex vNext) {
        GLUvertex vPrev;
        GLUvertex vNew = newVertex;
        assert (vNew != null);
        vNew.prev = vPrev = vNext.prev;
        vPrev.next = vNew;
        vNew.next = vNext;
        vNext.prev = vNew;
        vNew.anEdge = eOrig;
        vNew.data = null;
        GLUhalfEdge e = eOrig;
        do {
            e.org = vNew;
        } while ((e = e.oNext) != eOrig);
    }

    static void MakeFace(GLUface newFace, GLUhalfEdge eOrig, GLUface fNext) {
        GLUface fPrev;
        GLUface fNew = newFace;
        assert (fNew != null);
        fNew.prev = fPrev = fNext.prev;
        fPrev.next = fNew;
        fNew.next = fNext;
        fNext.prev = fNew;
        fNew.anEdge = eOrig;
        fNew.data = null;
        fNew.trail = null;
        fNew.marked = false;
        fNew.inside = fNext.inside;
        GLUhalfEdge e = eOrig;
        do {
            e.lFace = fNew;
        } while ((e = e.lNext) != eOrig);
    }

    static void KillEdge(GLUhalfEdge eDel) {
        GLUhalfEdge ePrev;
        if (!eDel.first) {
            eDel = eDel.sym;
        }
        GLUhalfEdge eNext = eDel.next;
        eNext.sym.next = ePrev = eDel.sym.next;
        ePrev.sym.next = eNext;
    }

    static void KillVertex(GLUvertex vDel, GLUvertex newOrg) {
        GLUhalfEdge eStart;
        GLUhalfEdge e = eStart = vDel.anEdge;
        do {
            e.org = newOrg;
        } while ((e = e.oNext) != eStart);
        GLUvertex vPrev = vDel.prev;
        GLUvertex vNext = vDel.next;
        vNext.prev = vPrev;
        vPrev.next = vNext;
    }

    static void KillFace(GLUface fDel, GLUface newLface) {
        GLUhalfEdge eStart;
        GLUhalfEdge e = eStart = fDel.anEdge;
        do {
            e.lFace = newLface;
        } while ((e = e.lNext) != eStart);
        GLUface fPrev = fDel.prev;
        GLUface fNext = fDel.next;
        fNext.prev = fPrev;
        fPrev.next = fNext;
    }

    public static GLUhalfEdge __gl_meshMakeEdge(GLUmesh mesh) {
        GLUvertex newVertex1 = new GLUvertex();
        GLUvertex newVertex2 = new GLUvertex();
        GLUface newFace = new GLUface();
        GLUhalfEdge e = Mesh.MakeEdge(mesh.eHead);
        if (e == null) {
            return null;
        }
        Mesh.MakeVertex(newVertex1, e, mesh.vHead);
        Mesh.MakeVertex(newVertex2, e.sym, mesh.vHead);
        Mesh.MakeFace(newFace, e, mesh.fHead);
        return e;
    }

    public static boolean __gl_meshSplice(GLUhalfEdge eOrg, GLUhalfEdge eDst) {
        boolean joiningLoops = false;
        boolean joiningVertices = false;
        if (eOrg == eDst) {
            return true;
        }
        if (eDst.org != eOrg.org) {
            joiningVertices = true;
            Mesh.KillVertex(eDst.org, eOrg.org);
        }
        if (eDst.lFace != eOrg.lFace) {
            joiningLoops = true;
            Mesh.KillFace(eDst.lFace, eOrg.lFace);
        }
        Mesh.Splice(eDst, eOrg);
        if (!joiningVertices) {
            GLUvertex newVertex = new GLUvertex();
            Mesh.MakeVertex(newVertex, eDst, eOrg.org);
            eOrg.org.anEdge = eOrg;
        }
        if (!joiningLoops) {
            GLUface newFace = new GLUface();
            Mesh.MakeFace(newFace, eDst, eOrg.lFace);
            eOrg.lFace.anEdge = eOrg;
        }
        return true;
    }

    static boolean __gl_meshDelete(GLUhalfEdge eDel) {
        GLUhalfEdge eDelSym = eDel.sym;
        boolean joiningLoops = false;
        if (eDel.lFace != eDel.sym.lFace) {
            joiningLoops = true;
            Mesh.KillFace(eDel.lFace, eDel.sym.lFace);
        }
        if (eDel.oNext == eDel) {
            Mesh.KillVertex(eDel.org, null);
        } else {
            eDel.sym.lFace.anEdge = eDel.sym.lNext;
            eDel.org.anEdge = eDel.oNext;
            Mesh.Splice(eDel, eDel.sym.lNext);
            if (!joiningLoops) {
                GLUface newFace = new GLUface();
                Mesh.MakeFace(newFace, eDel, eDel.lFace);
            }
        }
        if (eDelSym.oNext == eDelSym) {
            Mesh.KillVertex(eDelSym.org, null);
            Mesh.KillFace(eDelSym.lFace, null);
        } else {
            eDel.lFace.anEdge = eDelSym.sym.lNext;
            eDelSym.org.anEdge = eDelSym.oNext;
            Mesh.Splice(eDelSym, eDelSym.sym.lNext);
        }
        Mesh.KillEdge(eDel);
        return true;
    }

    static GLUhalfEdge __gl_meshAddEdgeVertex(GLUhalfEdge eOrg) {
        GLUhalfEdge eNew = Mesh.MakeEdge(eOrg);
        GLUhalfEdge eNewSym = eNew.sym;
        Mesh.Splice(eNew, eOrg.lNext);
        eNew.org = eOrg.sym.org;
        GLUvertex newVertex = new GLUvertex();
        Mesh.MakeVertex(newVertex, eNewSym, eNew.org);
        eNew.lFace = eNewSym.lFace = eOrg.lFace;
        return eNew;
    }

    public static GLUhalfEdge __gl_meshSplitEdge(GLUhalfEdge eOrg) {
        GLUhalfEdge tempHalfEdge = Mesh.__gl_meshAddEdgeVertex(eOrg);
        GLUhalfEdge eNew = tempHalfEdge.sym;
        Mesh.Splice(eOrg.sym, eOrg.sym.sym.lNext);
        Mesh.Splice(eOrg.sym, eNew);
        eOrg.sym.org = eNew.org;
        eNew.sym.org.anEdge = eNew.sym;
        eNew.sym.lFace = eOrg.sym.lFace;
        eNew.winding = eOrg.winding;
        eNew.sym.winding = eOrg.sym.winding;
        return eNew;
    }

    static GLUhalfEdge __gl_meshConnect(GLUhalfEdge eOrg, GLUhalfEdge eDst) {
        boolean joiningLoops = false;
        GLUhalfEdge eNew = Mesh.MakeEdge(eOrg);
        GLUhalfEdge eNewSym = eNew.sym;
        if (eDst.lFace != eOrg.lFace) {
            joiningLoops = true;
            Mesh.KillFace(eDst.lFace, eOrg.lFace);
        }
        Mesh.Splice(eNew, eOrg.lNext);
        Mesh.Splice(eNewSym, eDst);
        eNew.org = eOrg.sym.org;
        eNewSym.org = eDst.org;
        eNew.lFace = eNewSym.lFace = eOrg.lFace;
        eOrg.lFace.anEdge = eNewSym;
        if (!joiningLoops) {
            GLUface newFace = new GLUface();
            Mesh.MakeFace(newFace, eNew, eOrg.lFace);
        }
        return eNew;
    }

    static void __gl_meshZapFace(GLUface fZap) {
        GLUhalfEdge e;
        GLUhalfEdge eStart = fZap.anEdge;
        GLUhalfEdge eNext = eStart.lNext;
        do {
            e = eNext;
            eNext = e.lNext;
            e.lFace = null;
            if (e.sym.lFace != null) continue;
            if (e.oNext == e) {
                Mesh.KillVertex(e.org, null);
            } else {
                e.org.anEdge = e.oNext;
                Mesh.Splice(e, e.sym.lNext);
            }
            GLUhalfEdge eSym = e.sym;
            if (eSym.oNext == eSym) {
                Mesh.KillVertex(eSym.org, null);
            } else {
                eSym.org.anEdge = eSym.oNext;
                Mesh.Splice(eSym, eSym.sym.lNext);
            }
            Mesh.KillEdge(e);
        } while (e != eStart);
        GLUface fPrev = fZap.prev;
        GLUface fNext = fZap.next;
        fNext.prev = fPrev;
        fPrev.next = fNext;
    }

    public static GLUmesh __gl_meshNewMesh() {
        GLUmesh mesh = new GLUmesh();
        GLUvertex v = mesh.vHead;
        GLUface f = mesh.fHead;
        GLUhalfEdge e = mesh.eHead;
        GLUhalfEdge eSym = mesh.eHeadSym;
        v.next = v.prev = v;
        v.anEdge = null;
        v.data = null;
        f.next = f.prev = f;
        f.anEdge = null;
        f.data = null;
        f.trail = null;
        f.marked = false;
        f.inside = false;
        e.next = e;
        e.sym = eSym;
        e.oNext = null;
        e.lNext = null;
        e.org = null;
        e.lFace = null;
        e.winding = 0;
        e.activeRegion = null;
        eSym.next = eSym;
        eSym.sym = e;
        eSym.oNext = null;
        eSym.lNext = null;
        eSym.org = null;
        eSym.lFace = null;
        eSym.winding = 0;
        eSym.activeRegion = null;
        return mesh;
    }

    static GLUmesh __gl_meshUnion(GLUmesh mesh1, GLUmesh mesh2) {
        GLUface f1 = mesh1.fHead;
        GLUvertex v1 = mesh1.vHead;
        GLUhalfEdge e1 = mesh1.eHead;
        GLUface f2 = mesh2.fHead;
        GLUvertex v2 = mesh2.vHead;
        GLUhalfEdge e2 = mesh2.eHead;
        if (f2.next != f2) {
            f1.prev.next = f2.next;
            f2.next.prev = f1.prev;
            f2.prev.next = f1;
            f1.prev = f2.prev;
        }
        if (v2.next != v2) {
            v1.prev.next = v2.next;
            v2.next.prev = v1.prev;
            v2.prev.next = v1;
            v1.prev = v2.prev;
        }
        if (e2.next != e2) {
            e1.sym.next.sym.next = e2.next;
            e2.next.sym.next = e1.sym.next;
            e2.sym.next.sym.next = e1;
            e1.sym.next = e2.sym.next;
        }
        return mesh1;
    }

    static void __gl_meshDeleteMeshZap(GLUmesh mesh) {
        GLUface fHead = mesh.fHead;
        while (fHead.next != fHead) {
            Mesh.__gl_meshZapFace(fHead.next);
        }
        assert (mesh.vHead.next == mesh.vHead);
    }

    public static void __gl_meshDeleteMesh(GLUmesh mesh) {
        GLUface f = mesh.fHead.next;
        while (f != mesh.fHead) {
            GLUface fNext;
            f = fNext = f.next;
        }
        GLUvertex v = mesh.vHead.next;
        while (v != mesh.vHead) {
            GLUvertex vNext;
            v = vNext = v.next;
        }
        GLUhalfEdge e = mesh.eHead.next;
        while (e != mesh.eHead) {
            GLUhalfEdge eNext;
            e = eNext = e.next;
        }
    }

    public static void __gl_meshCheckMesh(GLUmesh mesh) {
        GLUvertex v;
        GLUhalfEdge e;
        GLUface f;
        GLUface fHead = mesh.fHead;
        GLUvertex vHead = mesh.vHead;
        GLUhalfEdge eHead = mesh.eHead;
        GLUface fPrev = fHead;
        fPrev = fHead;
        while ((f = fPrev.next) != fHead) {
            assert (f.prev == fPrev);
            e = f.anEdge;
            do {
                assert (e.sym != e);
                assert (e.sym.sym == e);
                assert (e.lNext.oNext.sym == e);
                assert (e.oNext.sym.lNext == e);
                assert (e.lFace == f);
            } while ((e = e.lNext) != f.anEdge);
            fPrev = f;
        }
        assert (f.prev == fPrev && f.anEdge == null && f.data == null);
        GLUvertex vPrev = vHead;
        vPrev = vHead;
        while ((v = vPrev.next) != vHead) {
            assert (v.prev == vPrev);
            e = v.anEdge;
            do {
                assert (e.sym != e);
                assert (e.sym.sym == e);
                assert (e.lNext.oNext.sym == e);
                assert (e.oNext.sym.lNext == e);
                assert (e.org == v);
            } while ((e = e.oNext) != v.anEdge);
            vPrev = v;
        }
        assert (v.prev == vPrev && v.anEdge == null && v.data == null);
        GLUhalfEdge ePrev = eHead;
        ePrev = eHead;
        while ((e = ePrev.next) != eHead) {
            assert (e.sym.next == ePrev.sym);
            assert (e.sym != e);
            assert (e.sym.sym == e);
            assert (e.org != null);
            assert (e.sym.org != null);
            assert (e.lNext.oNext.sym == e);
            assert (e.oNext.sym.lNext == e);
            ePrev = e;
        }
        assert (e.sym.next == ePrev.sym && e.sym == mesh.eHeadSym && e.sym.sym == e && e.org == null && e.sym.org == null && e.lFace == null && e.sym.lFace == null);
    }
}

