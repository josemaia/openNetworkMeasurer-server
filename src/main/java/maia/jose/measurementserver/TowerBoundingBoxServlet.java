package maia.jose.measurementserver;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

public class TowerBoundingBoxServlet extends HttpServlet {

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
                PreparedStatement statement = db.prepareStatement("SELECT ST_AsGeoJson(m.geog),m.radio,m.mcc,m.net,m.area,m.cell,o.color,o.name FROM mozilla_test_slice m,operator_table o " +
                        "WHERE ST_Intersects(ST_GeomFromText(?,4326),m.geog::GEOMETRY) AND (m.mcc*100+m.net = o.id)");
                double left = Double.parseDouble(req.getParameter("left"));
                double bottom = Double.parseDouble(req.getParameter("bottom"));
                double right = Double.parseDouble(req.getParameter("right"));
                double top = Double.parseDouble(req.getParameter("top"));
                statement.setString(1, AuxiliaryMethods.getSQLPolygon(left, bottom, right, top));
                ResultSet rs = statement.executeQuery();

                JsonFactory f = new JsonFactory();
                JsonGenerator g = f.createGenerator(resp.getWriter());

                g.writeStartArray();
                while (rs.next()) {
                    g.writeStartObject();
                    String geog = rs.getString(1);
                    if (geog!=null) {
                        g.writeStringField("type", "Feature");
                        g.writeRaw(",\"geometry\":");
                        g.writeRaw(geog);
                        g.writeObjectFieldStart("properties");
                        g.writeStringField("radio", (rs.getString(2)));
                        g.writeStringField("mcc", String.valueOf(rs.getInt(3)));
                        g.writeStringField("net", String.valueOf(rs.getInt(4)));
                        g.writeStringField("area", String.valueOf(rs.getInt(5)));
                        g.writeStringField("cell", String.valueOf(rs.getInt(6)));
                        g.writeStringField("color", rs.getString(7));
                        g.writeStringField("name",rs.getString(8));
                        g.writeEndObject();
                        g.writeEndObject();
                    }
                }
                g.writeEndArray();
                g.close();
                rs.close();
                db.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
