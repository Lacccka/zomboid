/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.schemagen;

import com.sun.xml.bind.v2.schemagen.GroupKind;
import com.sun.xml.bind.v2.schemagen.xmlschema.ContentModelContainer;
import com.sun.xml.bind.v2.schemagen.xmlschema.Occurs;
import com.sun.xml.bind.v2.schemagen.xmlschema.Particle;
import com.sun.xml.bind.v2.schemagen.xmlschema.TypeDefParticle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class Tree {
    Tree() {
    }

    Tree makeOptional(boolean really) {
        return really ? new Optional(this) : this;
    }

    Tree makeRepeated(boolean really) {
        return really ? new Repeated(this) : this;
    }

    static Tree makeGroup(GroupKind kind, List<Tree> children) {
        if (children.size() == 1) {
            return children.get(0);
        }
        ArrayList<Tree> normalizedChildren = new ArrayList<Tree>(children.size());
        for (Tree t : children) {
            Group g;
            if (t instanceof Group && (g = (Group)t).kind == kind) {
                normalizedChildren.addAll(Arrays.asList(g.children));
                continue;
            }
            normalizedChildren.add(t);
        }
        return new Group(kind, normalizedChildren.toArray(new Tree[normalizedChildren.size()]));
    }

    abstract boolean isNullable();

    boolean canBeTopLevel() {
        return false;
    }

    protected abstract void write(ContentModelContainer var1, boolean var2, boolean var3);

    protected void write(TypeDefParticle ct) {
        if (this.canBeTopLevel()) {
            this.write((ContentModelContainer)ct._cast(ContentModelContainer.class), false, false);
        } else {
            new Group(GroupKind.SEQUENCE, new Tree[]{this}).write(ct);
        }
    }

    protected final void writeOccurs(Occurs o, boolean isOptional, boolean repeated) {
        if (isOptional) {
            o.minOccurs(0);
        }
        if (repeated) {
            o.maxOccurs("unbounded");
        }
    }

    private static final class Group
    extends Tree {
        private final GroupKind kind;
        private final Tree[] children;

        private Group(GroupKind kind, Tree ... children) {
            this.kind = kind;
            this.children = children;
        }

        @Override
        boolean canBeTopLevel() {
            return true;
        }

        @Override
        boolean isNullable() {
            if (this.kind == GroupKind.CHOICE) {
                for (Tree t : this.children) {
                    if (!t.isNullable()) continue;
                    return true;
                }
                return false;
            }
            for (Tree t : this.children) {
                if (t.isNullable()) continue;
                return false;
            }
            return true;
        }

        @Override
        protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated) {
            Particle c = this.kind.write(parent);
            this.writeOccurs(c, isOptional, repeated);
            for (Tree child : this.children) {
                child.write(c, false, false);
            }
        }
    }

    private static final class Repeated
    extends Tree {
        private final Tree body;

        private Repeated(Tree body) {
            this.body = body;
        }

        @Override
        boolean isNullable() {
            return this.body.isNullable();
        }

        @Override
        Tree makeRepeated(boolean really) {
            return this;
        }

        @Override
        protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated) {
            this.body.write(parent, isOptional, true);
        }
    }

    private static final class Optional
    extends Tree {
        private final Tree body;

        private Optional(Tree body) {
            this.body = body;
        }

        @Override
        boolean isNullable() {
            return true;
        }

        @Override
        Tree makeOptional(boolean really) {
            return this;
        }

        @Override
        protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated) {
            this.body.write(parent, true, repeated);
        }
    }

    static abstract class Term
    extends Tree {
        Term() {
        }

        @Override
        boolean isNullable() {
            return false;
        }
    }
}

