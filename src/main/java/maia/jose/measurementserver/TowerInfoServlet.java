package maia.jose.measurementserver;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;

public class TowerInfoServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection db = null;
        try {
            db = DriverManager.getConnection("jdbc:postgresql://" + myPrivateValues.sqlServer + ":" + myPrivateValues.sqlPort + "/" + myPrivateValues.sqlDatabase, myPrivateValues.sqlUser, myPrivateValues.sqlPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (db != null) {
                PreparedStatement statement = db.prepareStatement("SELECT * FROM cell_data WHERE cid=? AND lac=?");
                int cid = Integer.parseInt(req.getParameter("cell"));
                int lac = Integer.parseInt(req.getParameter("area"));
                int mcc = Integer.parseInt(req.getParameter("mcc"));
                int net = Integer.parseInt(req.getParameter("net"));
                if (cid==-1 || lac==-1) return; //TODO: more elegant way to crash?
                statement.setInt(1,cid);
                statement.setInt(2,lac);
                ResultSet rs = statement.executeQuery();
                LinkedList<Integer> measurementKeys = new LinkedList<>();
                while (rs.next()){
                    measurementKeys.add(rs.getInt("measurement"));
                }

                PreparedStatement statement2 = db.prepareStatement("SELECT ST_AsGeoJson(ST_Buffer" +
                        "(ST_Centroid(ST_Union(c.geog::geometry))::geography, " +
                        "GREATEST(50,ST_MaxDistance(ST_Centroid(ST_Union(c.geog::geometry)),ST_Union(c.geog::geometry)))))," +
                        "St_X(ST_Centroid(ST_Union(c.geog::geometry)))," +
                        "St_Y(ST_Centroid(ST_Union(c.geog::geometry)))" +
                        "FROM raw_cell_measurements c WHERE cell_measurement_key = ANY (?)");

                Integer[] keys = measurementKeys.toArray(new Integer[measurementKeys.size()]);
                statement2.setArray(1, db.createArrayOf("bigint", keys));
                rs.close();

                ResultSet rs2 = null;
                if (keys.length!=0)
                    rs2 = statement2.executeQuery();

                statement = db.prepareStatement("SELECT ST_AsGeoJSON(geog),mcc,net,radio,samples,changeable FROM mozilla_test_slice" +
                        " WHERE mcc=? AND net=? AND cell=? AND area=?" );
                statement.setInt(3,cid);
                statement.setInt(4,lac);
                statement.setInt(1,mcc);
                statement.setInt(2,net);
                rs = statement.executeQuery();

                JsonFactory f = new JsonFactory();
                JsonGenerator g = f.createGenerator(resp.getWriter());

                g.writeStartArray();
                g.writeStartObject();
                if (rs2!=null && rs2.next()) {
                    String geog = rs2.getString(1);
                    if (geog!=null) {
                        g.writeStringField("type", "Feature");
                        g.writeRaw(",\"geometry\":");
                        g.writeRaw(geog);
                        g.writeObjectFieldStart("properties");
                        g.writeStringField("estimatedLon", (rs2.getString(2)));
                        g.writeStringField("estimatedLat", (rs2.getString(3)));
                        g.writeEndObject();
                    }
                }

                g.writeEndObject();
                g.writeStartObject();
                if (rs.next()) {
                    g.writeStringField("type", "Feature");
                    g.writeRaw(",\"geometry\":");
                    g.writeRaw(rs.getString(1));
                    g.writeObjectFieldStart("properties");
                    g.writeStringField("cid", String.valueOf(cid));
                    g.writeStringField("lac", String.valueOf(lac));
                    g.writeStringField("mcc", String.valueOf(rs.getInt(2))); //mcc in db
                    g.writeStringField("mnc", String.valueOf(rs.getInt(3))); //net in db
                    g.writeStringField("radio", (rs.getString(4)));
                    g.writeStringField("samples", String.valueOf(rs.getInt(5)));
                    g.writeStringField("changeable", String.valueOf(rs.getBoolean(6)));
                    g.writeEndObject();
                }
                g.writeEndObject();

                g.writeEndArray();
                g.close();
                if (rs2!=null)
                    rs2.close();
                db.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
