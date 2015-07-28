package maia.jose.measurementserver.jsonbeans;

import java.util.LinkedList;

public class TemporaryParse {
    String userID;
    String manufacturer;
    String model;
    String hardware;
    Integer SdkVersion;
    String OsVersion;
    String Latitude;
    String Longitude;

    Float Accuracy = 0.0f;
    String SeaLevelPressure;
    String PhonePressure;
    float altitude = 0.0f;
    String timestamp;
    LinkedList<ScanResultObject> wifiScanResults;
    String networkTypeString;
    String networkClass;
    String phoneTypeString;
    String signalStrengthString;
    String operatorName;
    String operatorID;
    String SimOperatorName;
    String SimOperatorID;
    LinkedList<CellData> cellDatas;
    String measurementList;
    Double stdev;
    String maxSignal;
    String minSignal;

    public static class ScanResultObject{
        public String SSID;
        public String BSSID;
        public String signalStrength;
        public String maxSignal;
        public String minSignal;
        public Double stDev;
        public String security;
        public String keyManagement;
        public String encryption;
        public Boolean hasWPS;
        public Boolean isPublicWifi;
        public String serviceSet;
        public Integer channel;
        public Boolean hasHiddenSSID;
        public Integer linkSpeed;

        public Integer getChannel() {
            return channel;
        }

        public void setChannel(Integer channel) {
            this.channel = channel;
        }

        public Boolean getHasHiddenSSID() {
            return hasHiddenSSID;
        }

        public void setHasHiddenSSID(Boolean hasHiddenSSID) {
            this.hasHiddenSSID = hasHiddenSSID;
        }

        public Integer getLinkSpeed() {
            return linkSpeed;
        }

        public void setLinkSpeed(Integer linkSpeed) {
            this.linkSpeed = linkSpeed;
        }

        public String getSignalStrength() {
            return signalStrength;
        }

        public void setSignalStrength(String signalStrength) {
            this.signalStrength = signalStrength;
        }

        public String getSecurity() {
            return security;
        }

        public void setSecurity(String security) {
            this.security = security;
        }

        public String getKeyManagement() {
            return keyManagement;
        }

        public void setKeyManagement(String keyManagement) {
            this.keyManagement = keyManagement;
        }

        public String getEncryption() {
            return encryption;
        }

        public void setEncryption(String encryption) {
            this.encryption = encryption;
        }

        public Boolean getHasWPS() {
            return hasWPS;
        }

        public void setHasWPS(Boolean hasWPS) {
            this.hasWPS = hasWPS;
        }

        public Boolean getIsPublicWifi() {
            return isPublicWifi;
        }

        public void setIsPublicWifi(Boolean isPublicWifi) {
            this.isPublicWifi = isPublicWifi;
        }

        public String getSSID() {
            return SSID;
        }

        public void setSSID(String SSID) {
            this.SSID = SSID;
        }

        public String getBSSID() {
            return BSSID;
        }

        public void setBSSID(String BSSID) {
            this.BSSID = BSSID;
        }

        public String getServiceSet() {
            return serviceSet;
        }

        public void setServiceSet(String serviceSet) {
            this.serviceSet = serviceSet;
        }
    }

    public static class CellData { // mozilla ichnaea-style classification: https://mozilla-ichnaea.readthedocs.org/en/latest/cell.html
        String radio; // gsm, umts, lte, cdma
        Integer mcc; // mobile country code
        Integer mnc; // mobile network code; system identifier in CDMA
        Integer lac = -1; // local area code in GSM/UMTS; tracking area code in LTE; network ID in CDMA;
        Integer cid = -1; // Cell ID; base station ID in CDMA;
        Integer signal; // Cell signal strength (RSSI in GSM and CDMA / RSCP in UMTS / RSRP in LTE)
        Integer asu; // arbitrary signal strength
        Integer ta; //only valid for gsm/lte
        Integer psc; //only on UMTS and LTE (pci)

        public void setPsc(Integer psc) {
            this.psc = psc;
        }

        public String getRadio() {
            return radio;
        }

        public void setRadio(String radio) {
            this.radio = radio;
        }

        public Integer getMcc() {
            return mcc;
        }

        public void setMcc(Integer mcc) {
            this.mcc = mcc;
        }

        public Integer getMnc() {
            return mnc;
        }

        public void setMnc(Integer mnc) {
            this.mnc = mnc;
        }

        public Integer getLac() {
            return lac;
        }

        public void setLac(Integer lac) {
            this.lac = lac;
        }

        public Integer getCid() {
            return cid;
        }

        public void setCid(Integer cid) {
            this.cid = cid;
        }

        public Integer getSignal() {
            return signal;
        }

        public void setSignal(Integer signal) {
            this.signal = signal;
        }

        public Integer getAsu() {
            return asu;
        }

        public void setAsu(Integer asu) {
            this.asu = asu;
        }

        public Integer getTa() {
            return ta;
        }

        public void setTa(Integer ta) {
            this.ta = ta;
        }

        public Integer getPsc() {
            return psc;
        }
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public Integer getSdkVersion() {
        return SdkVersion;
    }

    public void setSdkVersion(Integer sdkVersion) {
        SdkVersion = sdkVersion;
    }

    public String getOsVersion() {
        return OsVersion;
    }

    public void setOsVersion(String osVersion) {
        OsVersion = osVersion;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public Float getAccuracy() {
        return Accuracy;
    }

    public void setAccuracy(Float accuracy) {
        Accuracy = accuracy;
    }

    public String getSeaLevelPressure() {
        return SeaLevelPressure;
    }

    public void setSeaLevelPressure(String seaLevelPressure) {
        SeaLevelPressure = seaLevelPressure;
    }

    public String getPhonePressure() {
        return PhonePressure;
    }

    public void setPhonePressure(String phonePressure) {
        PhonePressure = phonePressure;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public LinkedList<ScanResultObject> getWifiScanResults() {
        return wifiScanResults;
    }

    public void setWifiScanResults(LinkedList<ScanResultObject> wifiScanResults) {
        this.wifiScanResults = wifiScanResults;
    }

    public String getNetworkTypeString() {
        return networkTypeString;
    }

    public void setNetworkTypeString(String networkTypeString) {
        this.networkTypeString = networkTypeString;
    }

    public String getPhoneTypeString() {
        return phoneTypeString;
    }

    public void setPhoneTypeString(String phoneTypeString) {
        this.phoneTypeString = phoneTypeString;
    }

    public String getSignalStrengthString() {
        return signalStrengthString;
    }

    public void setSignalStrengthString(String signalStrengthString) {
        this.signalStrengthString = signalStrengthString;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(String operatorID) {
        this.operatorID = operatorID;
    }

    public String getSimOperatorName() {
        return SimOperatorName;
    }

    public void setSimOperatorName(String simOperatorName) {
        SimOperatorName = simOperatorName;
    }

    public String getSimOperatorID() {
        return SimOperatorID;
    }

    public void setSimOperatorID(String simOperatorID) {
        SimOperatorID = simOperatorID;
    }

    public LinkedList<CellData> getCellDatas() {
        return cellDatas;
    }

    public void setCellDatas(LinkedList<CellData> cellDatas) {
        this.cellDatas = cellDatas;
    }

    public String getMeasurementList() {
        return measurementList;
    }

    public void setMeasurementList(String measurementList) {
        this.measurementList = measurementList;
    }

    public Double getStdev() {
        return stdev;
    }

    public void setStdev(Double stdev) {
        this.stdev = stdev;
    }

    public String getNetworkClass() {
        return networkClass;
    }

    public void setNetworkClass(String networkClass) {
        this.networkClass = networkClass;
    }

    public String getMaxSignal() {
        return maxSignal;
    }

    public void setMaxSignal(String maxSignal) {
        this.maxSignal = maxSignal;
    }

    public String getMinSignal() {
        return minSignal;
    }

    public void setMinSignal(String minSignal) {
        this.minSignal = minSignal;
    }
}
