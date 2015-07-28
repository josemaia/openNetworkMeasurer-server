package maia.jose.measurementserver;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;


public class LeaderboardServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
                PreparedStatement statement = db.prepareStatement("SELECT userid, COUNT(*) FROM raw_cell_measurements WHERE userid IS NOT NULL GROUP BY userid ORDER BY 2 DESC");
                ResultSet rs = statement.executeQuery();

                JsonFactory f = new JsonFactory();
                JsonGenerator g = f.createGenerator(response.getWriter());

                g.writeStartArray();
                while (rs.next()){
                    String id = rs.getString(1);
                    int measurements = rs.getInt(2);

                    g.writeStartObject();
                    g.writeStringField("id", id);
                    g.writeStringField("measurements", String.valueOf(measurements));
                    g.writeEndObject();
                }
                g.writeEndArray();
                g.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
