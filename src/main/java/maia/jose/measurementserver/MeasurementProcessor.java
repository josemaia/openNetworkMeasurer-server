package maia.jose.measurementserver;

import maia.jose.measurementserver.jsonbeans.TemporaryParse;
import maia.jose.measurementserver.jsonbeans.TemporaryParse.CellData;
import maia.jose.measurementserver.jsonbeans.TemporaryParse.ScanResultObject;

import java.sql.*;

public class MeasurementProcessor {
    public final int TYPE_CELLULAR = 0;
    public final int TYPE_WIFI = 1;

    private TemporaryParse json;
    private Integer type = -1;
    private PreparedStatement statement;

    MeasurementProcessor(TemporaryParse parse) {
        json = parse;
        if (json.getCellDatas() != null) {
            type = TYPE_CELLULAR;
        } else type = TYPE_WIFI;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getTypeString() {
        switch (type) {
            case 0:
                return "Cellular";
            case 1:
                return "Wifi";
        }
        return "";
    }

    public void process() throws Exception {
        Connection db = null;
        try {
            db = DriverManager.getConnection("jdbc:postgresql://" + myPrivateValues.sqlServer + ":" + myPrivateValues.sqlPort + "/" + myPrivateValues.sqlDatabase, myPrivateValues.sqlUser, myPrivateValues.sqlPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (db != null) {
            if (type == TYPE_WIFI) {
                statement = db.prepareStatement("INSERT INTO raw_wifi_measurements (userid,manufacturer,model,hardware,sdkVersion," +
                        "osVersion,geog,accuracy,SeaLevelPressure,PhonePressure,Altitude,timestamp) VALUES (?,?,?,?,?,?,ST_GeogFromText(?),?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                genericProcess();
                wifiProcess(db);
            } else {
                statement = db.prepareStatement("INSERT INTO raw_cell_measurements (userid,manufacturer,model,hardware,sdkVersion," +
                        "osVersion,geog,accuracy,SeaLevelPressure,PhonePressure,Altitude,timestamp,networkType,networkClass,phoneType," +
                        "asu,signal,operatorName,operatorID) VALUES (?,?,?,?,?,?,ST_GeogFromText(?),?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                genericProcess();
                cellProcess(db);
            }
        }
        if (db != null) db.close();
    }

    private void genericProcess() throws Exception {
        //verify values
        if (json.getUserID() != null && json.getUserID().length() > 16
                || json.getManufacturer() != null && json.getManufacturer().length() > 50
                || json.getModel() != null && json.getModel().length() > 50
                || json.getHardware() != null && json.getHardware().length() > 50
                || json.getOsVersion() != null && json.getOsVersion().length() > 10)
            throw new Exception("String length too large");
        if (json.getLatitude() != null && json.getLongitude() != null)
            statement.setString(7, "SRID=4326;POINT(" + json.getLongitude() + " " +
                    json.getLatitude() + ")");
        else
            statement.setNull(7, Types.NULL);
        if (json.getSeaLevelPressure() != null && Float.parseFloat(json.getSeaLevelPressure()) < 0 ||
                json.getPhonePressure() != null && Float.parseFloat(json.getPhonePressure()) < 0)
            throw new Exception("Invalid pressure values");

        //prepare statement
        statement.setString(1, json.getUserID());
        statement.setString(2, json.getManufacturer());
        statement.setString(3, json.getModel());
        statement.setString(4, json.getHardware());
        statement.setInt(5, json.getSdkVersion());
        statement.setString(6, json.getOsVersion());
        //7 already done
        float parsed = 0.0f;
        if (json.getAccuracy() != null) parsed = json.getAccuracy();
        statement.setFloat(8, parsed);

        if (json.getSeaLevelPressure() != null)
            parsed = Float.parseFloat(json.getSeaLevelPressure());
        else parsed = 0;
        statement.setFloat(9, parsed);

        if (json.getPhonePressure() != null)
            parsed = Float.parseFloat(json.getPhonePressure());
        else parsed = 0;
        statement.setFloat(10, parsed);
        statement.setFloat(11, json.getAltitude());
        statement.setTimestamp(12, Timestamp.valueOf(json.getTimestamp()));
    }

    private void wifiProcess(Connection db) throws Exception {
        //wifi part complete, execute and begin a cycle
        statement.executeUpdate();
        ResultSet rs = statement.getGeneratedKeys();
        if (!rs.next()) throw new Exception("Couldn't insert wifi measurement to database");
        Integer foreignKey = rs.getInt("wifi_measurement_key");

        rs.close();

        //start of cycle
        for (ScanResultObject s : json.getWifiScanResults()) {
            statement = db.prepareStatement("INSERT INTO scan_results (ssid,bssid,signalStrength,security,keyManagement," +
                    "encryption,serviceSet,hasWPS,isPublicWifi,channel,hasHiddenSSID,linkSpeed,measurement) VALUES" +
                    "(?,?,?,?,?,?,?,?,FALSE,?,CAST(? AS ssid_type),?,?)", Statement.RETURN_GENERATED_KEYS);
            //verify values
            if (s.getBSSID() != null && s.getBSSID().length() > 50
                    || s.getSSID() != null && s.getSSID().length() > 50
                    || s.getSecurity() != null && s.getSecurity().length() > 10
                    || s.getKeyManagement() != null && s.getKeyManagement().length() > 10
                    || s.getEncryption() != null && s.getEncryption().length() > 10
                    || s.getServiceSet() != null && s.getServiceSet().length() > 10)
                throw new Exception("String length too large");
            String hiddenSSIDtype;
            if (s.hasHiddenSSID == null)
                hiddenSSIDtype = "unknown";
            else if (s.hasHiddenSSID) hiddenSSIDtype = "hidden";
            else hiddenSSIDtype = "visible";

            //prepare statement
            statement.setString(1, s.getSSID());
            statement.setString(2, s.getBSSID());
            statement.setInt(3, Integer.parseInt(s.getSignalStrength()));
            statement.setString(4, s.getSecurity());
            statement.setString(5, s.getKeyManagement());
            statement.setString(6, s.getEncryption());
            statement.setString(7, s.getServiceSet());
            statement.setBoolean(8, s.getHasWPS());
            statement.setInt(9, s.getChannel());
            statement.setString(10, hiddenSSIDtype);
            int parsed = 0;
            if (s.getLinkSpeed() != null) parsed = s.getLinkSpeed();
            statement.setInt(11, parsed);
            statement.setInt(12, foreignKey);

            statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            if (!rs.next()) throw new Exception("Couldn't insert scan result to database");
        }
        statement.close();
        rs.close();
    }

    private void cellProcess(Connection db) throws Exception {
        String operatorName;
        Integer operatorID;

        //verify remaining values
        if (json.getOperatorName().isEmpty() || json.getOperatorName().equals("")) { //fallback on SIM operator
            operatorName = json.getSimOperatorName();
            operatorID = Integer.parseInt(json.getSimOperatorID());
        } else {
            operatorName = json.getOperatorName();
            operatorID = Integer.parseInt(json.getOperatorID());
        }

        if (json.getNetworkTypeString() != null && json.getNetworkTypeString().length() > 10
                || json.getNetworkClass() != null && json.getNetworkClass().length() > 3
                || json.getPhoneTypeString() != null && json.getPhoneTypeString().length() > 50
                || json.getOperatorName() != null && json.getOperatorName().length() > 10
                || operatorName != null && operatorName.length() > 10)
            throw new Exception("String length too large");

        statement.setString(13, json.getNetworkTypeString());
        statement.setString(14, json.getNetworkClass());
        statement.setString(15, json.getPhoneTypeString());
        String[] signals = json.getSignalStrengthString().split(" ");
        statement.setInt(16, Integer.parseInt(signals[0])); //asu
        if (Integer.parseInt(signals[0]) == 99) statement.setNull(17, Types.BIGINT); //dBm
        else {
            statement.setInt(17, Integer.parseInt(signals[2]));
        }
        statement.setString(18, operatorName);
        statement.setInt(19, operatorID);
        //cell part complete, execute and begin a cycle
        statement.executeUpdate();
        ResultSet rs = statement.getGeneratedKeys();
        if (!rs.next()) throw new Exception("Couldn't insert cell measurement to database");
        Integer foreignKey = rs.getInt("cell_measurement_key");
        rs.close();

        for (CellData s : json.getCellDatas()) {
            cellDataProcess(db, s, foreignKey);
        }
        statement.close();
    }

    private void cellDataProcess(Connection db, CellData s, Integer foreignKey) throws Exception {

        if (s.getRadio() != null && s.getRadio().length() > 5) throw new Exception("String length too large");

        statement = db.prepareStatement("INSERT INTO cell_data (radio,mcc,mnc,lac,cid," +
                "signal,asu,ta,psc,wasProcessed,measurement) VALUES" +
                "(?,?,?,?,?,?,?,?,?,FALSE,?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, s.getRadio());
        Integer sync = -1;
        if (s.getMcc() != null) sync = s.getMcc();
        statement.setInt(2, sync);
        if (s.getMnc() != null) sync = s.getMnc();
        else sync = -1;
        statement.setInt(3, sync);
        if (s.getLac() != null) sync = s.getLac();
        else sync = -1;
        statement.setInt(4, sync);
        if (s.getCid() != null) sync = s.getCid();
        else sync = -1;
        statement.setInt(5, sync);
        if (s.getSignal() != null) sync = s.getSignal();
        else sync = -1;
        statement.setInt(6, sync);
        if (s.getAsu() != null) sync = s.getAsu();
        else sync = -1;
        statement.setInt(7, sync);
        if (s.getTa() != null) sync = s.getTa();
        else sync = -1;
        statement.setInt(8, sync);
        if (s.getPsc() != null) sync = s.getPsc();
        else sync = -1;
        statement.setInt(9, sync);
        statement.setInt(10, foreignKey);

        statement.executeUpdate();
        ResultSet rs = statement.getGeneratedKeys();
        if (!rs.next()) throw new Exception("Couldn't insert scan result to database");
        rs.close();
    }
}
