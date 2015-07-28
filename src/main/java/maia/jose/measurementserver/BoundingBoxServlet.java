package maia.jose.measurementserver;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

public class BoundingBoxServlet extends HttpServlet {

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
                PreparedStatement statement = db.prepareStatement("SELECT ST_AsGeoJson(g.geog),g.signallevel,g.operatorid,g.operatorname,g.signalasu,g.signaldbm,g.networkclass,g.numsamples FROM grid_cells g " +
                        "WHERE ST_Intersects(ST_GeomFromText(?,4326),g.geog::GEOMETRY)");
                double left = Double.parseDouble(req.getParameter("left"));
                double bottom = Double.parseDouble(req.getParameter("bottom"));
                double right = Double.parseDouble(req.getParameter("right"));
                double top = Double.parseDouble(req.getParameter("top"));
                statement.setString(1, AuxiliaryMethods.getSQLPolygon(left, bottom, right, top));

                ResultSet rs = statement.executeQuery();

                JsonFactory f = new JsonFactory();
                JsonGenerator g = f.createGenerator(resp.getWriter());

                g.writeStartArray();
                while (rs.next()){
                    String geog = rs.getString(1);
                    int level = rs.getInt(2);
                    int operatorId = rs.getInt(3);
                    String operatorName = rs.getString(4);
                    int signalAsu = rs.getInt(5);
                    int signalDbm = rs.getInt(6);
                    String networkClass = rs.getString(7);
                    int numsamples = rs.getInt(8);

                    g.writeStartObject();
                    g.writeStringField("type", "Feature");
                    g.writeRaw(",\"geometry\":");
                    g.writeRaw(geog);
                    g.writeObjectFieldStart("properties");
                    g.writeStringField("level", Integer.toString(level));
                    g.writeStringField("operatorId", Integer.toString(operatorId));
                    g.writeStringField("operatorName", operatorName);
                    g.writeStringField("signalAsu",Integer.toString(signalAsu));
                    g.writeStringField("signalDbm",Integer.toString(signalDbm));
                    g.writeStringField("networkClass",networkClass);
                    g.writeStringField("numsamples",Integer.toString(numsamples));
                    g.writeEndObject();
                    g.writeEndObject();
                }
                g.writeEndArray();
                g.close();

                db.close();
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
