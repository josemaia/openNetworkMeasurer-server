package maia.jose.measurementserver;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import maia.jose.measurementserver.beans.IchnaeaMeasurement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class MozillaCsvParser {
    static CsvSchema schema;
    static CsvMapper mapper;
    static Connection db = null;

    public static void main(String[] args){
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            db = DriverManager.getConnection("jdbc:postgresql://" + myPrivateValues.sqlServer + ":" + myPrivateValues.sqlPort + "/" + myPrivateValues.sqlDatabase, myPrivateValues.sqlUser, myPrivateValues.sqlPassword);

            mapper = new CsvMapper();
            schema = mapper.schemaFor(IchnaeaMeasurement.class);
            parseCsv(args[0]);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void pushToDb(IchnaeaMeasurement i) {
        try {
            if (db != null) {
                PreparedStatement statement = db.prepareStatement("INSERT INTO mozilla_import (radio,mcc,net,area,cell," +
                        "unit,geog,range,samples,changeable,created,updated,averageSignal) " +
                        "VALUES (?,?,?,?,?,?,ST_GeogFromText(?),?,?,?,to_timestamp(?),to_timestamp(?),?)", Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, i.radio);
                statement.setInt(2, i.mcc);
                statement.setInt(3,i.net);
                statement.setInt(4,i.area);
                statement.setInt(5,i.cell);
                statement.setInt(6,i.unit);
                if (i.lon!=0.0 && i.lat!=0.0)
                    statement.setString(7,"SRID=4326;POINT(" + i.lon + " " +i.lat + ")");
                else
                    statement.setNull(7, Types.NULL);
                statement.setInt(8,i.range);
                statement.setInt(9,i.samples);
                if (i.changeable == 1) statement.setBoolean(10,true);
                else statement.setBoolean(10,false);
                statement.setInt(11,i.created);
                statement.setInt(12,i.updated);
                statement.setInt(13,i.averageSignal);

                statement.executeUpdate();
                ResultSet rs = statement.getGeneratedKeys();
                if (!rs.next()) System.out.println("Error: could not insert measurement ("+i.radio+","+i.mcc+","+i.net+","+i.area+","+i.cell+")");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void parseCsv(String arg) {
        Path file = Paths.get(arg);
        try {
            InputStream in = Files.newInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            reader.readLine(); // skip first line, description
            while ((line = reader.readLine()) != null) {
                IchnaeaMeasurement i = serializeFromCsv(line);
                if (i!=null)
                    pushToDb(i);
            }
            in.close();
        } catch (IOException x) {
            System.out.println(x.getMessage());
        }
    }

    private static IchnaeaMeasurement serializeFromCsv(String line) {
        try {
            return mapper.reader(IchnaeaMeasurement.class).with(schema).readValue(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
