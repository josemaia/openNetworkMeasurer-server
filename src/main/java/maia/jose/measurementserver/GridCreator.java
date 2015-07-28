package maia.jose.measurementserver;

import maia.jose.measurementserver.beans.GridCell;

import java.sql.*;
import java.util.HashMap;

public class GridCreator {
    static String[] colors;

    public static void main(String[] args) {
        GridHelper gh = new GridHelper();
        colors = new String[] {"red", "blue", "green", "purple",
                "orange", "darkred", "lightred", "beige", "darkblue",
                "darkgreen", "cadetblue", "darkpurple", "black", "pink",
                "lightblue", "lightgreen", "gray", "white", "lightgray"};
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
                PreparedStatement statement = db.prepareStatement("DELETE FROM grid_cells");
                statement.executeUpdate();

                statement = db.prepareStatement("DELETE FROM operator_table");
                statement.executeUpdate();

                statement = db.prepareStatement("SELECT " +
                        "ST_X(c.geog::geometry),ST_Y(c.geog::geometry),c.signal,c.asu,c.operatorname,c.operatorid,c.networkclass,c.networktype,c.phonetype" +
                        " FROM raw_cell_measurements c WHERE c.geog IS NOT NULL");

                ResultSet rs = statement.executeQuery();
                GridCell cell;
                int mOperatorID;
                String mOperatorName;
                int mSignalLevel;
                int mSignalDbm;
                int mSignalAsu;
                String mNetworkClass;
                String mNetworkType;
                String mPhoneType;
                PreparedStatement statement2;

                while (rs.next()){
                    cell = gh.findGridCell(rs.getDouble(1), rs.getDouble(2));
                    mSignalDbm = rs.getInt(3);
                    mSignalAsu = rs.getInt(4);
                    mOperatorName = rs.getString(5);
                    mOperatorID = rs.getInt(6);
                    mNetworkClass = rs.getString(7);
                    mNetworkType = rs.getString(8);
                    mPhoneType = rs.getString(9);
                    mSignalLevel = CellSignalHelper.getSignalLevel(mSignalDbm, mSignalAsu, mNetworkClass, mPhoneType);

                    statement2 = db.prepareStatement("SELECT * FROM grid_cells g WHERE g.x_index=? AND g.y_index=? " +
                            "AND g.operatorid=? AND g.networkclass=?");
                    statement2.setInt(1,cell.indexX);
                    statement2.setInt(2, cell.indexY);
                    statement2.setInt(3, mOperatorID);
                    statement2.setString(4, mNetworkClass);

                    ResultSet rs2 = statement2.executeQuery();
                    if (rs2 == null || !rs2.next()) {
                    //create new entry
                        if (rs2!=null) rs2.close();
                        statement2 = db.prepareStatement("INSERT INTO grid_cells (" +
                                "geog,x_index,y_index,signalLevel,signalDbm,signalAsu," +
                                "operatorID,operatorName,networkClass,numsamples) VALUES " +
                                "(ST_GeogFromText(?),?,?,?,?,?,?,?,?,1)",Statement.RETURN_GENERATED_KEYS);

                        statement2.setString(1, gh.getSQLPolygon(cell));
                        statement2.setInt(2, cell.indexX);
                        statement2.setInt(3, cell.indexY);
                        statement2.setInt(4,mSignalLevel);
                        statement2.setInt(5,mSignalDbm);
                        statement2.setInt(6, mSignalAsu);
                        statement2.setInt(7, mOperatorID);
                        statement2.setString(8, mOperatorName);
                        statement2.setString(9, mNetworkClass);

                        statement2.executeUpdate();
                        statement2.close();
                    }
                    else {
                        System.out.println("REPEAT DETECTED:"+mOperatorName+"("+mOperatorID+") "+cell.indexX+","+cell.indexY);
                        //already exists; increment counter and perform avg
                        int pkey = rs2.getInt("grid_cell_key");
                        int oldDbm = rs2.getInt("signalDbm");
                        int oldAsu = rs2.getInt("signalAsu");
                        rs2.close();

                        PreparedStatement statement3 = db.prepareStatement(
                                "UPDATE grid_cells SET numsamples = numsamples+1, signalDbm = ?, signalAsu = ?, signalLevel = ?" +
                                        " WHERE grid_cell_key = ?"
                        );
                        int newDbm = CellSignalHelper.calcAvg(oldDbm, mSignalDbm);
                        int newAsu = CellSignalHelper.calcAvg(oldAsu,mSignalAsu);
                        statement3.setInt(1,newDbm);
                        statement3.setInt(2,newAsu);
                        statement3.setInt(3,CellSignalHelper.getSignalLevel(newDbm, newAsu, mNetworkClass, mPhoneType));
                        statement3.setInt(4,pkey);

                        statement3.executeUpdate();
                        statement3.close();
                    }
                }
                buildOperatorTable(db);
                //CLOSE STUFF
                statement.close();
                db.close();
            }
        }
        catch(SQLException e){
            System.out.println(e.toString());
        }
    }

    private static void buildOperatorTable(Connection db) throws SQLException {
        PreparedStatement statement = db.prepareStatement("SELECT DISTINCT g.operatorid,string_agg(DISTINCT g.operatorname,', ') FROM grid_cells g GROUP BY 1 ORDER BY 1;");
        ResultSet rs = statement.executeQuery();

        HashMap<String,Integer> map = new HashMap<>();
        while (rs.next()){
            String operator = String.valueOf(rs.getInt(1));
            String mcc = operator.substring(0,operator.length()-2);
            String mnc = operator.substring(operator.length()-2,operator.length());
            String color;
            if (map.containsKey(mcc)){
                int i = map.get(mcc);
                color = getColorString(i,mcc,mnc,map);
                map.replace(mcc,i,i+1);
            }
            else{
                map.put(mcc,0); //country has one operator (index 0 means 1 operator...)
                color = getColorString(0,mcc,mnc, map);
            }
            statement = db.prepareStatement("INSERT INTO operator_table(id,name,color) VALUES (?,?,?)");
            statement.setInt(1,rs.getInt(1));
            statement.setString(2,rs.getString(2));
            statement.setString(3,color);

            statement.executeUpdate();
            }
    }

    private static String getColorString(int i, String mcc, String mnc, HashMap<String, Integer> map) {
        if (getColorOverride(mcc,mnc)!=null){
            return getColorOverride(mcc,mnc);
        }
        else {
            return colors[i];
        }
    }

    private static String getColorOverride(String mcc, String mnc) { // if we override one of the operators, we must override all of them, or we can get duplicates
        if (mcc.equals("268") && mnc.equals("06")) {
            return "blue";
        }
        if (mcc.equals("268") && mnc.equals("03")) {
            return "orange";
        }
        if (mcc.equals("268") && mnc.equals("01")){
            return "red";
        }
        else return null;
    }
}
