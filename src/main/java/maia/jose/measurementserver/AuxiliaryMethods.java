package maia.jose.measurementserver;

public class AuxiliaryMethods {

    public static String getSQLPolygon(double left, double bottom, double right, double top) {
        return ("POLYGON(("+left+" "+bottom+","+
                left+" "+top+","+
                right+" "+top+","+
                right+" "+bottom+","+
                left+" "+bottom+"))"
        );
    }
}
