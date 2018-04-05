package jp.co.omron.healthcare.sampleapps.ble.blesampleomron;

import java.util.Locale;

public class Utils {
    public static String byteDataToHexString(byte[] data) {
        if (null == data) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (byte b : data) {
            sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }
}
