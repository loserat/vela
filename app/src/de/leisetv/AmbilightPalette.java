package de.leisetv;

/** Canonical palette and the TV's 8-bit HSB protocol conversion. No Android dependencies. */
final class AmbilightPalette {
    static final class Entry {
        final String name;final int hue,saturation,rgb;
        Entry(String name,int degrees,int percent){this.name=name;hue=hue8(degrees);saturation=saturation8(percent);rgb=rgb(hue,saturation,255);}
    }
    static final Entry[] COLORS={
        new Entry("Warmweiß",38,28),new Entry("Neutralweiß",0,0),new Entry("Kaltweiß",215,12),new Entry("Kerze",28,62),
        new Entry("Rot",0,100),new Entry("Orange",24,100),new Entry("Amber",40,100),new Entry("Gelb",60,100),
        new Entry("Limette",85,95),new Entry("Grün",120,100),new Entry("Minze",150,55),new Entry("Türkis",170,85),
        new Entry("Cyan",185,90),new Entry("Blau",240,100),new Entry("Violett",275,85),new Entry("Rosa",325,65)
    };
    static int hue8(int degrees){if(degrees<0||degrees>359)throw new IllegalArgumentException("Hue degrees");return Math.round(degrees*255f/360);}
    static int saturation8(int percent){if(percent<0||percent>100)throw new IllegalArgumentException("Saturation percent");return Math.round(percent*255f/100);}
    static int match(int hue,int saturation){
        for(int i=0;i<COLORS.length;i++)if(Math.abs(COLORS[i].saturation-saturation)<=1&&(saturation==0||Math.abs(COLORS[i].hue-hue)<=1))return i;
        return -1;
    }
    static int rgb(int hue,int saturation,int brightness){
        if(hue<0||hue>255||saturation<0||saturation>255||brightness<0||brightness>255)throw new IllegalArgumentException("HSB byte range");
        float h=hue*6f/255,s=saturation/255f,v=brightness/255f;int sector=(int)Math.floor(h)%6;float f=h-(float)Math.floor(h),p=v*(1-s),q=v*(1-f*s),t=v*(1-(1-f)*s);
        float r,g,b;switch(sector){case 0:r=v;g=t;b=p;break;case 1:r=q;g=v;b=p;break;case 2:r=p;g=v;b=t;break;case 3:r=p;g=q;b=v;break;case 4:r=t;g=p;b=v;break;default:r=v;g=p;b=q;}
        return 0xff000000|(Math.round(r*255)<<16)|(Math.round(g*255)<<8)|Math.round(b*255);
    }
}
