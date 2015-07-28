package maia.jose.measurementserver;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

public class OperatorServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
                PreparedStatement statement = db.prepareStatement("SELECT DISTINCT g.operatorid,string_agg(DISTINCT g.operatorname,', ') FROM grid_cells g " +
                        "WHERE ST_Intersects(ST_GeomFromText(?,4326),g.geog::GEOMETRY) GROUP BY 1 ORDER BY 1");
                double left = Double.parseDouble(req.getParameter("left"));
                double bottom = Double.parseDouble(req.getParameter("bottom"));
                double right = Double.parseDouble(req.getParameter("right"));
                double top = Double.parseDouble(req.getParameter("top"));
                statement.setString(1, getSQLPolygon(left, bottom, right, top));

                ResultSet rs = statement.executeQuery();

                JsonFactory f = new JsonFactory();
                JsonGenerator g = f.createGenerator(resp.getWriter());

                g.writeStartArray();
                while (rs.next()){
                    int operatorId = rs.getInt(1);
                    String operatorName = rs.getString(2);

                    g.writeStartObject();
                    g.writeStringField("operatorId", Integer.toString(operatorId));
                    g.writeStringField("operatorName", operatorName);
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

    private String getSQLPolygon(double left, double bottom, double right, double top) {
        return ("POLYGON(("+left+" "+bottom+","+
                left+" "+top+","+
                right+" "+top+","+
                right+" "+bottom+","+
                left+" "+bottom+"))"
        );
    }
}
