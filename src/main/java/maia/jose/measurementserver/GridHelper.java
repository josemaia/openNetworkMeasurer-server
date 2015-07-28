package maia.jose.measurementserver;

import maia.jose.measurementserver.beans.GridCell;

public class GridHelper {
    public double MAXIMUM_X = 180.0;
    public double MINIMUM_X = -180;
    public double TOTAL_X = 360.0;
    public double MAXIMUM_Y = 85.0014;
    public double MINIMUM_Y = -85.0014;
    public double TOTAL_Y = 170.0028;
    public double DELTA_X = 0.0045;
    public double DELTA_Y = 0.0044;
    private double xCellTotal;
    private double yCellTotal;

    public GridHelper(double maxX, double minX, double maxY, double minY, double deltaX, double deltaY){
        MAXIMUM_X = maxX;
        MINIMUM_X = minX;
        TOTAL_X = Math.abs(maxX-minX);
        MAXIMUM_Y = maxY;
        MINIMUM_Y = minY;
        TOTAL_Y = Math.abs(maxY-minY);
        DELTA_X = deltaX;
        DELTA_Y = deltaY;

        xCellTotal = TOTAL_X/DELTA_X; //80000.0
        yCellTotal = TOTAL_Y/DELTA_Y; //38637.0
    }

    public GridHelper(){
        xCellTotal = TOTAL_X/DELTA_X;
        yCellTotal = TOTAL_Y/DELTA_Y;
    }

    public GridCell findGridCell(double x, double y){
        int indexX = 0;
        int indexY = 0;

        if (x!=MINIMUM_X) indexX = (int) Math.floor((x+MAXIMUM_X) / TOTAL_X*xCellTotal );
        if (y!=MINIMUM_Y) indexY = (int) Math.floor((y+MAXIMUM_Y) / TOTAL_Y*yCellTotal);


        return new GridCell(indexX,indexY);

    }

    public String getPolygon(GridCell gc){
        return ("[[["+(gc.indexX*DELTA_X-MAXIMUM_X)+","+(gc.indexY*DELTA_Y-MAXIMUM_Y)+"],"+
                        "["+((gc.indexX+1)*DELTA_X-MAXIMUM_X)+","+(gc.indexY*DELTA_Y-MAXIMUM_Y)+"],"+
                        "["+((gc.indexX+1)*DELTA_X-MAXIMUM_X)+","+((1+gc.indexY)*DELTA_Y-MAXIMUM_Y)+"],"+
                        "["+(gc.indexX*DELTA_X-MAXIMUM_X)+","+((1+gc.indexY)*DELTA_Y-MAXIMUM_Y)+"],"+
                        "["+(gc.indexX*DELTA_X-MAXIMUM_X)+","+(gc.indexY*DELTA_Y-MAXIMUM_Y)+"]]]"
        );

    }

    public String getSQLPolygon(GridCell gc) {
        return ("POLYGON(("+(gc.indexX*DELTA_X-MAXIMUM_X)+" "+(gc.indexY*DELTA_Y-MAXIMUM_Y)+","+
                ((gc.indexX+1)*DELTA_X-MAXIMUM_X)+" "+(gc.indexY*DELTA_Y-MAXIMUM_Y)+","+
                ((gc.indexX+1)*DELTA_X-MAXIMUM_X)+" "+((1+gc.indexY)*DELTA_Y-MAXIMUM_Y)+","+
                (gc.indexX*DELTA_X-MAXIMUM_X)+" "+((1+gc.indexY)*DELTA_Y-MAXIMUM_Y)+","+
                (gc.indexX*DELTA_X-MAXIMUM_X)+" "+(gc.indexY*DELTA_Y-MAXIMUM_Y)+"))"
        );
    }

    public static void main(String[] args){
        GridHelper gh = new GridHelper();
        GridCell cell;
        cell = gh.findGridCell(180,84);
        cell = gh.findGridCell(-8.5397231,39.4790179);

        GeoJSONProducer producer = new GeoJSONProducer();

        System.out.println(producer.produce(cell));
    }

}
