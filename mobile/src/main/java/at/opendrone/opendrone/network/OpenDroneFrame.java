package at.opendrone.opendrone.network;

import android.util.Log;

import java.nio.file.attribute.FileAttribute;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class OpenDroneFrame {
    private byte slaveID;

    private static final String TAG = "OpenDroneFramy";

    private String[] data;
    private int[] functionCodes;

    public OpenDroneFrame(byte slaveID, String[] data, int[] functionCodes) throws Exception {
        requireNonNull(data);
        requireNonNull(functionCodes);

        if (data.length <= 0 || functionCodes.length <= 0 || data.length != functionCodes.length) {
            throw new Exception("Length of the arrays must not be 0 and have to be equal!");
        }


        this.slaveID = slaveID;
        this.data = data;
        this.functionCodes = functionCodes;
    }

    private List<Byte> generateStart() {
        List<Byte> bytes = new LinkedList<>();
        for (int i = 0; i < 16; i++) {
            if (i >= 16 - 1 - 3) {
                bytes.add((byte) 0);
                continue;
            }
            if (i % 2 == 0) {
                bytes.add((byte) 1);
            } else {
                bytes.add((byte) 0);
            }
        }
        return bytes;
    }

    private List<Byte> generateEnd() {
        List<Byte> bytes = generateStart();
        Collections.reverse(bytes);
        return bytes;
    }

    private int getParity(double data) {
        if (data % 2 == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        char terminator = ';';
        String str = "";
        str += "1010101010101000" + terminator;
        str += (int) slaveID + "" + terminator;
        str += data.length + "" + terminator;

        for (int i = 0; i < data.length; i++) {
            int curCode = functionCodes[i];
            String curData = data[i];

            str += (int) curCode + "" + terminator;
            str += curData + "" + terminator;
            str += getParity(Double.parseDouble(curData)) + "" + terminator;
        }

        str += "0001010101010101" + terminator;
        return str;
    }
}
