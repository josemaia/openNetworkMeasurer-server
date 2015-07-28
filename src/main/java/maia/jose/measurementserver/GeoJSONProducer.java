package maia.jose.measurementserver;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import maia.jose.measurementserver.beans.GridCell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GeoJSONProducer {
    String produce(GridCell gridCell){

        JsonFactory f = new JsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator g;
        GridHelper gh = new GridHelper();
        try {
            g = f.createGenerator(baos);
            g.writeStartObject();

            g.writeStringField("type", "Feature");
            g.writeObjectFieldStart("geometry");
            g.writeStringField("type", "Polygon");
            g.writeRaw(",\"coordinates\":");
            g.writeRaw(gh.getPolygon(gridCell));
            g.writeEndObject();
            g.writeObjectFieldStart("properties");
            g.writeStringField("level", "4"); //TODO: get level
            g.writeEndObject();
            g.writeEndObject();
            g.close();
        } catch (IOException e) {
            e.printStackTrace();
            return baos.toString();
        }
        return baos.toString();
    }
}
