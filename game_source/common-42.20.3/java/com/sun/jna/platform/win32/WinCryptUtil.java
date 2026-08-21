/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.platform.win32;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinCrypt;

public abstract class WinCryptUtil {

    public static class MANAGED_CRYPT_SIGN_MESSAGE_PARA
    extends WinCrypt.CRYPT_SIGN_MESSAGE_PARA {
        private WinCrypt.CERT_CONTEXT[] rgpMsgCerts;
        private WinCrypt.CRL_CONTEXT[] rgpMsgCrls;
        private WinCrypt.CRYPT_ATTRIBUTE[] rgAuthAttrs;
        private WinCrypt.CRYPT_ATTRIBUTE[] rgUnauthAttrs;

        public void setRgpMsgCert(WinCrypt.CERT_CONTEXT[] rgpMsgCerts) {
            this.rgpMsgCerts = rgpMsgCerts;
            if (rgpMsgCerts == null || rgpMsgCerts.length == 0) {
                this.rgpMsgCert = null;
                this.cMsgCert = 0;
            } else {
                this.cMsgCert = rgpMsgCerts.length;
                Memory mem = new Memory(Native.POINTER_SIZE * rgpMsgCerts.length);
                for (int i = 0; i < rgpMsgCerts.length; ++i) {
                    mem.setPointer(i * Native.POINTER_SIZE, rgpMsgCerts[i].getPointer());
                }
                this.rgpMsgCert = mem;
            }
        }

        @Override
        public WinCrypt.CERT_CONTEXT[] getRgpMsgCert() {
            return this.rgpMsgCerts;
        }

        public void setRgpMsgCrl(WinCrypt.CRL_CONTEXT[] rgpMsgCrls) {
            this.rgpMsgCrls = rgpMsgCrls;
            if (rgpMsgCrls == null || rgpMsgCrls.length == 0) {
                this.rgpMsgCert = null;
                this.cMsgCert = 0;
            } else {
                this.cMsgCert = rgpMsgCrls.length;
                Memory mem = new Memory(Native.POINTER_SIZE * rgpMsgCrls.length);
                for (int i = 0; i < rgpMsgCrls.length; ++i) {
                    mem.setPointer(i * Native.POINTER_SIZE, rgpMsgCrls[i].getPointer());
                }
                this.rgpMsgCert = mem;
            }
        }

        @Override
        public WinCrypt.CRL_CONTEXT[] getRgpMsgCrl() {
            return this.rgpMsgCrls;
        }

        public void setRgAuthAttr(WinCrypt.CRYPT_ATTRIBUTE[] rgAuthAttrs) {
            this.rgAuthAttrs = rgAuthAttrs;
            if (rgAuthAttrs == null || rgAuthAttrs.length == 0) {
                this.rgAuthAttr = null;
                this.cMsgCert = 0;
            } else {
                this.cMsgCert = this.rgpMsgCerts.length;
                this.rgAuthAttr = rgAuthAttrs[0].getPointer();
            }
        }

        @Override
        public WinCrypt.CRYPT_ATTRIBUTE[] getRgAuthAttr() {
            return this.rgAuthAttrs;
        }

        public void setRgUnauthAttr(WinCrypt.CRYPT_ATTRIBUTE[] rgUnauthAttrs) {
            this.rgUnauthAttrs = rgUnauthAttrs;
            if (rgUnauthAttrs == null || rgUnauthAttrs.length == 0) {
                this.rgUnauthAttr = null;
                this.cMsgCert = 0;
            } else {
                this.cMsgCert = this.rgpMsgCerts.length;
                this.rgUnauthAttr = rgUnauthAttrs[0].getPointer();
            }
        }

        @Override
        public WinCrypt.CRYPT_ATTRIBUTE[] getRgUnauthAttr() {
            return this.rgUnauthAttrs;
        }

        @Override
        public void write() {
            if (this.rgpMsgCerts != null) {
                for (Structure structure : this.rgpMsgCerts) {
                    structure.write();
                }
            }
            if (this.rgpMsgCrls != null) {
                for (Structure structure : this.rgpMsgCrls) {
                    structure.write();
                }
            }
            if (this.rgAuthAttrs != null) {
                for (Structure structure : this.rgAuthAttrs) {
                    structure.write();
                }
            }
            if (this.rgUnauthAttrs != null) {
                for (Structure structure : this.rgUnauthAttrs) {
                    structure.write();
                }
            }
            this.cbSize = this.size();
            super.write();
        }

        @Override
        public void read() {
            if (this.rgpMsgCerts != null) {
                for (Structure structure : this.rgpMsgCerts) {
                    structure.read();
                }
            }
            if (this.rgpMsgCrls != null) {
                for (Structure structure : this.rgpMsgCrls) {
                    structure.read();
                }
            }
            if (this.rgAuthAttrs != null) {
                for (Structure structure : this.rgAuthAttrs) {
                    structure.read();
                }
            }
            if (this.rgUnauthAttrs != null) {
                for (Structure structure : this.rgUnauthAttrs) {
                    structure.read();
                }
            }
            super.read();
        }
    }
}

