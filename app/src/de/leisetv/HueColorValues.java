package de.leisetv;

/** Hue Bridge v1 uses 16-bit hue and 0..254 saturation/brightness, unlike the TV's byte HSB. */
final class HueColorValues {
    static int hue16(int byteHue){if(byteHue<0||byteHue>255)throw new IllegalArgumentException("Hue byte");return Math.round(byteHue*65535f/255);}
    static int saturation254(int byteSaturation){if(byteSaturation<0||byteSaturation>255)throw new IllegalArgumentException("Saturation byte");return Math.round(byteSaturation*254f/255);}
    static int brightness254(int percent){if(percent<1||percent>100)throw new IllegalArgumentException("Brightness percent");return Math.max(1,Math.round(percent*254f/100));}
}
