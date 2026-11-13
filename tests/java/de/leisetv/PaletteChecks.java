package de.leisetv;
public final class PaletteChecks {
    static int checks=0;
    static void check(boolean value,String name){checks++;if(!value)throw new AssertionError(name);}
    public static void main(String[] args){
        check(AmbilightPalette.hue8(0)==0,"red hue");check(AmbilightPalette.hue8(120)==85,"green hue");check(AmbilightPalette.hue8(240)==170,"blue hue");check(AmbilightPalette.hue8(325)==230,"pink hue");
        check(AmbilightPalette.saturation8(100)==255,"full saturation");check(AmbilightPalette.saturation8(0)==0,"white saturation");
        check(AmbilightPalette.rgb(0,255,255)==0xffff0000,"red preview");check(AmbilightPalette.rgb(85,255,255)==0xff00ff00,"green preview");check(AmbilightPalette.rgb(170,255,255)==0xff0000ff,"blue preview");check(AmbilightPalette.rgb(0,0,255)==0xffffffff,"white preview");
        check(AmbilightPalette.COLORS.length==16,"sixteen colors");
        for(int i=0;i<AmbilightPalette.COLORS.length;i++){
            AmbilightPalette.Entry color=AmbilightPalette.COLORS[i];check(AmbilightPalette.match(color.hue,color.saturation)==i,"preset selection "+color.name);
            check(color.hue>=0&&color.hue<=255&&color.saturation>=0&&color.saturation<=255,"wire bounds "+color.name);
        }
        boolean rejected=false;try{AmbilightPalette.hue8(360);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"invalid hue rejected");
        rejected=false;try{AmbilightPalette.saturation8(101);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"invalid saturation rejected");
        System.out.println(checks+" palette checks passed against the actual production class.");
    }
}
