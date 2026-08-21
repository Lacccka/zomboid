/*
 * Decompiled with CFR 0.152.
 */
package org.lwjglx.input;

public class KeyCodes {
    public static int toLwjglKey(int glfwKeyCode) {
        switch (glfwKeyCode) {
            case -1: {
                return 0;
            }
            case 256: {
                return 1;
            }
            case 259: {
                return 14;
            }
            case 258: {
                return 15;
            }
            case 257: {
                return 28;
            }
            case 32: {
                return 57;
            }
            case 341: {
                return 29;
            }
            case 340: {
                return 42;
            }
            case 342: {
                return 56;
            }
            case 343: {
                return 219;
            }
            case 345: {
                return 157;
            }
            case 344: {
                return 54;
            }
            case 346: {
                return 184;
            }
            case 347: {
                return 220;
            }
            case 49: {
                return 2;
            }
            case 50: {
                return 3;
            }
            case 51: {
                return 4;
            }
            case 52: {
                return 5;
            }
            case 53: {
                return 6;
            }
            case 54: {
                return 7;
            }
            case 55: {
                return 8;
            }
            case 56: {
                return 9;
            }
            case 57: {
                return 10;
            }
            case 48: {
                return 11;
            }
            case 65: {
                return 30;
            }
            case 66: {
                return 48;
            }
            case 67: {
                return 46;
            }
            case 68: {
                return 32;
            }
            case 69: {
                return 18;
            }
            case 70: {
                return 33;
            }
            case 71: {
                return 34;
            }
            case 72: {
                return 35;
            }
            case 73: {
                return 23;
            }
            case 74: {
                return 36;
            }
            case 75: {
                return 37;
            }
            case 76: {
                return 38;
            }
            case 77: {
                return 50;
            }
            case 78: {
                return 49;
            }
            case 79: {
                return 24;
            }
            case 80: {
                return 25;
            }
            case 81: {
                return 16;
            }
            case 82: {
                return 19;
            }
            case 83: {
                return 31;
            }
            case 84: {
                return 20;
            }
            case 85: {
                return 22;
            }
            case 86: {
                return 47;
            }
            case 87: {
                return 17;
            }
            case 88: {
                return 45;
            }
            case 89: {
                return 21;
            }
            case 90: {
                return 44;
            }
            case 265: {
                return 200;
            }
            case 264: {
                return 208;
            }
            case 263: {
                return 203;
            }
            case 262: {
                return 205;
            }
            case 260: {
                return 210;
            }
            case 261: {
                return 211;
            }
            case 268: {
                return 199;
            }
            case 269: {
                return 207;
            }
            case 266: {
                return 201;
            }
            case 267: {
                return 209;
            }
            case 290: {
                return 59;
            }
            case 291: {
                return 60;
            }
            case 292: {
                return 61;
            }
            case 293: {
                return 62;
            }
            case 294: {
                return 63;
            }
            case 295: {
                return 64;
            }
            case 296: {
                return 65;
            }
            case 297: {
                return 66;
            }
            case 298: {
                return 67;
            }
            case 299: {
                return 68;
            }
            case 300: {
                return 87;
            }
            case 301: {
                return 88;
            }
            case 302: {
                return 100;
            }
            case 303: {
                return 101;
            }
            case 304: {
                return 102;
            }
            case 305: {
                return 103;
            }
            case 306: {
                return 104;
            }
            case 307: {
                return 105;
            }
            case 308: {
                return 113;
            }
            case 321: {
                return 79;
            }
            case 322: {
                return 80;
            }
            case 323: {
                return 81;
            }
            case 324: {
                return 75;
            }
            case 325: {
                return 76;
            }
            case 326: {
                return 77;
            }
            case 327: {
                return 71;
            }
            case 328: {
                return 72;
            }
            case 329: {
                return 73;
            }
            case 320: {
                return 82;
            }
            case 334: {
                return 78;
            }
            case 333: {
                return 74;
            }
            case 332: {
                return 55;
            }
            case 331: {
                return 181;
            }
            case 330: {
                return 83;
            }
            case 336: {
                return 141;
            }
            case 335: {
                return 156;
            }
            case 282: {
                return 69;
            }
            case 59: {
                return 39;
            }
            case 92: {
                return 43;
            }
            case 44: {
                return 51;
            }
            case 46: {
                return 52;
            }
            case 47: {
                return 53;
            }
            case 96: {
                return 41;
            }
            case 280: {
                return 58;
            }
            case 281: {
                return 70;
            }
            case 161: {
                return 144;
            }
            case 284: {
                return 197;
            }
            case 45: {
                return 12;
            }
            case 61: {
                return 13;
            }
            case 91: {
                return 26;
            }
            case 93: {
                return 27;
            }
            case 39: {
                return 40;
            }
        }
        System.out.println("UNKNOWN GLFW KEY CODE: " + glfwKeyCode);
        return 0;
    }

    public static int toGlfwKey(int lwjglKeyCode) {
        switch (lwjglKeyCode) {
            case 1: {
                return 256;
            }
            case 14: {
                return 259;
            }
            case 15: {
                return 258;
            }
            case 28: {
                return 257;
            }
            case 57: {
                return 32;
            }
            case 29: {
                return 341;
            }
            case 42: {
                return 340;
            }
            case 56: {
                return 342;
            }
            case 219: {
                return 343;
            }
            case 157: {
                return 345;
            }
            case 54: {
                return 344;
            }
            case 184: {
                return 346;
            }
            case 220: {
                return 347;
            }
            case 2: {
                return 49;
            }
            case 3: {
                return 50;
            }
            case 4: {
                return 51;
            }
            case 5: {
                return 52;
            }
            case 6: {
                return 53;
            }
            case 7: {
                return 54;
            }
            case 8: {
                return 55;
            }
            case 9: {
                return 56;
            }
            case 10: {
                return 57;
            }
            case 11: {
                return 48;
            }
            case 30: {
                return 65;
            }
            case 48: {
                return 66;
            }
            case 46: {
                return 67;
            }
            case 32: {
                return 68;
            }
            case 18: {
                return 69;
            }
            case 33: {
                return 70;
            }
            case 34: {
                return 71;
            }
            case 35: {
                return 72;
            }
            case 23: {
                return 73;
            }
            case 36: {
                return 74;
            }
            case 37: {
                return 75;
            }
            case 38: {
                return 76;
            }
            case 50: {
                return 77;
            }
            case 49: {
                return 78;
            }
            case 24: {
                return 79;
            }
            case 25: {
                return 80;
            }
            case 16: {
                return 81;
            }
            case 19: {
                return 82;
            }
            case 31: {
                return 83;
            }
            case 20: {
                return 84;
            }
            case 22: {
                return 85;
            }
            case 47: {
                return 86;
            }
            case 17: {
                return 87;
            }
            case 45: {
                return 88;
            }
            case 21: {
                return 89;
            }
            case 44: {
                return 90;
            }
            case 200: {
                return 265;
            }
            case 208: {
                return 264;
            }
            case 203: {
                return 263;
            }
            case 205: {
                return 262;
            }
            case 210: {
                return 260;
            }
            case 211: {
                return 261;
            }
            case 199: {
                return 268;
            }
            case 207: {
                return 269;
            }
            case 201: {
                return 266;
            }
            case 209: {
                return 267;
            }
            case 59: {
                return 290;
            }
            case 60: {
                return 291;
            }
            case 61: {
                return 292;
            }
            case 62: {
                return 293;
            }
            case 63: {
                return 294;
            }
            case 64: {
                return 295;
            }
            case 65: {
                return 296;
            }
            case 66: {
                return 297;
            }
            case 67: {
                return 298;
            }
            case 68: {
                return 299;
            }
            case 87: {
                return 300;
            }
            case 88: {
                return 301;
            }
            case 100: {
                return 302;
            }
            case 101: {
                return 303;
            }
            case 102: {
                return 304;
            }
            case 103: {
                return 305;
            }
            case 104: {
                return 306;
            }
            case 105: {
                return 307;
            }
            case 113: {
                return 308;
            }
            case 79: {
                return 321;
            }
            case 80: {
                return 322;
            }
            case 81: {
                return 323;
            }
            case 75: {
                return 324;
            }
            case 76: {
                return 325;
            }
            case 77: {
                return 326;
            }
            case 71: {
                return 327;
            }
            case 72: {
                return 328;
            }
            case 73: {
                return 329;
            }
            case 82: {
                return 320;
            }
            case 78: {
                return 334;
            }
            case 74: {
                return 333;
            }
            case 55: {
                return 332;
            }
            case 181: {
                return 331;
            }
            case 83: {
                return 330;
            }
            case 141: {
                return 336;
            }
            case 156: {
                return 335;
            }
            case 69: {
                return 282;
            }
            case 39: {
                return 59;
            }
            case 43: {
                return 92;
            }
            case 51: {
                return 44;
            }
            case 52: {
                return 46;
            }
            case 53: {
                return 47;
            }
            case 41: {
                return 96;
            }
            case 58: {
                return 280;
            }
            case 70: {
                return 281;
            }
            case 197: {
                return 284;
            }
            case 144: {
                return 161;
            }
            case 12: {
                return 45;
            }
            case 13: {
                return 61;
            }
            case 26: {
                return 91;
            }
            case 27: {
                return 93;
            }
            case 40: {
                return 39;
            }
        }
        return -1;
    }
}

