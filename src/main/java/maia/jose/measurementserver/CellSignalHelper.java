package maia.jose.measurementserver;

public class CellSignalHelper {
    public final static int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public final static int SIGNAL_STRENGTH_POOR = 1;
    public final static int SIGNAL_STRENGTH_MODERATE = 2;
    public final static int SIGNAL_STRENGTH_GOOD = 3;
    public final static int SIGNAL_STRENGTH_GREAT = 4;

    public final static double LAMBDA = 0.6;

    public static int getGsmLevel(int asu) {
        int level;
        if (asu <= 2 || asu == 99) level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        else if (asu >= 12) level = SIGNAL_STRENGTH_GREAT;
        else if (asu >= 8) level = SIGNAL_STRENGTH_GOOD;
        else if (asu >= 5) level = SIGNAL_STRENGTH_MODERATE;
        else level = SIGNAL_STRENGTH_POOR;
        return level;
    }


    public static int getLteLevel(int mLteRsrp) {
        int rsrpIconLevel = -1;
        if (mLteRsrp > -44) rsrpIconLevel = -1;
        else if (mLteRsrp >= -85) rsrpIconLevel = SIGNAL_STRENGTH_GREAT;
        else if (mLteRsrp >= -95) rsrpIconLevel = SIGNAL_STRENGTH_GOOD;
        else if (mLteRsrp >= -105) rsrpIconLevel = SIGNAL_STRENGTH_MODERATE;
        else if (mLteRsrp >= -115) rsrpIconLevel = SIGNAL_STRENGTH_POOR;
        else if (mLteRsrp >= -140) rsrpIconLevel = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        return rsrpIconLevel;
    }

    public static int getCdmaLevel(int cdmaDbm) {
        int levelDbm;

        if (cdmaDbm >= -75) levelDbm = SIGNAL_STRENGTH_GREAT;
        else if (cdmaDbm >= -85) levelDbm = SIGNAL_STRENGTH_GOOD;
        else if (cdmaDbm >= -95) levelDbm = SIGNAL_STRENGTH_MODERATE;
        else if (cdmaDbm >= -100) levelDbm = SIGNAL_STRENGTH_POOR;
        else levelDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        return levelDbm;
    }

    public static int getSignalLevel(int mSignalDbm, int mSignalAsu, String mNetworkClass, String mPhoneType) {
            if (mPhoneType.equalsIgnoreCase("CDMA"))
                return getCdmaLevel(mSignalDbm);
            else if (mNetworkClass.equalsIgnoreCase("4G")) // LTE
                return getLteLevel(mSignalDbm);
            else if (mNetworkClass.equalsIgnoreCase("2G") || // GSM/UMTS
                    mNetworkClass.equalsIgnoreCase("3G"))
                return getGsmLevel(mSignalAsu);
            else {
                System.err.println("THIS SHOULD NEVER HAPPEN!");
                return -1;
            }
        }

    public static int calcAvg(int oldAvg, int newVal) {
        return (int) (LAMBDA*newVal+(1-LAMBDA)*oldAvg);
    }
}
