package maia.jose.measurementserver.beans;

public class GridCell {
    public int indexX;
    public int indexY;

    public GridCell(int x, int y){
        indexX = x;
        indexY = y;
    }

    @Override
    public String toString() {
        return ("Grid cell is ("+indexX+","+indexY+")");
    }
}
