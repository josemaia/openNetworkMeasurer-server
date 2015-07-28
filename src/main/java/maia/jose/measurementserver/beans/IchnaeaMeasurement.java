package maia.jose.measurementserver.beans;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"radio","mcc","net","area","cell",
        "unit","lon","lat","range","samples","changeable",
        "created","updated","averageSignal"})
public class IchnaeaMeasurement {
    public String radio;
    public int mcc;
    public int net;
    public int area;
    public int cell;
    public int unit;
    public float lon;
    public float lat;
    public int range;
    public int samples;
    public int changeable;
    public int created;
    public int updated;
    public int averageSignal;

    @Override
    public String toString() {
        return radio+" tower from operator "+mcc+" "+net+" at latlong "+lat+" "+lon+" with average signal of "+averageSignal;
    }
}
