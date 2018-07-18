package au.com.mithril.rpgtalker;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class HfpMonitor implements Runnable {
    BluetoothDevice bd;
    BluetoothSocket socket;
    MainActivity main;
    String bluetoothName;
    BufferedReader input;
    OutputStreamWriter output;

    private boolean isrunning = false;

    public HfpMonitor(MainActivity main, BluetoothDevice device) {
        this.bd = device;
        this.main = main;
        if (bd != null) {
            if (main != null) bluetoothName = main.friendlyName(bd);
            else bluetoothName = bd.getName();
        }
    }

    synchronized public boolean isRunning() {
        return isrunning;
    }

    @Override
    public void run() {
        isrunning = true;
        try {
            handleConnection();
        } finally {
            closeSocket();
            isrunning = false;
        }
    }

    synchronized public void closeSocket() {
        if (socket == null) return;
        if (socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                // Just ignore.
            }
        }
        socket = null;
    }

    private void addln(Object msg) {
        if (main != null) {
            main.addln(msg);
        }
    }

    private UUID findHfpService() {
        UUID service = null;
        if (bd == null) return service;
        for (ParcelUuid uuid : bd.getUuids()) {
            if (uuid.toString().toUpperCase().startsWith("0000111E")) {
                service = uuid.getUuid();
                break;
            }
        }
        return service;
    }

    private void handleConnection() {
        addln("Connecting to " + bluetoothName);
        UUID service = findHfpService();
        if (service == null) {
            addln("HFP not supported.");
            return;
        }
        BluetoothSocket socket = null;
        try {
            socket = bd.createRfcommSocketToServiceRecord(service);
            socket.connect();
            addln("Connected.");
            input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            output = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            for (; ; ) {
                String s = input.readLine();
                if (s == null) break;
                s = s.trim();
                if (!s.isEmpty()) {
                    processCmd(s);
                }
            }
        } catch (IOException e) {
            addln("Connection failed. " + e.getMessage());
            return;
        }
    }

    void sendat(String cmd) throws IOException {
        output.write("\r\n" + cmd + "\r\n");
        output.flush();
        addln("Response: " + cmd);
    }

    private void sendok() throws IOException {
        sendat("OK");
    }

    private String getArg(String s) {
        int i = s.indexOf("=");
        if (i < 0) return s;
        return s.substring(i + 1);
    }

    private int atol(String s) {
        int result = 0;
        try {
            result = Integer.parseInt(s, 10);
        } catch (NumberFormatException e) {
            // Ignore error.
        }
        return result;
    }

    private void processCmd(String s) throws IOException {
        String arg;
        int ret;
        addln("Request: " + s);
        if (s.startsWith("AT+BRSF=")) {
            arg = getArg(s);
            ret = atol(arg);
            addln("Options: " + parseBRSF(ret));
            sendat("+BRSF: 11");
            sendok();
        } else if (s.startsWith("AT+CIND=?")) {
//            sendat("+CIND: (\"service\",(0,1)),(\"call\",(0,1))");
            sendat("+CIND: (\"call\",(0,1)),(\"callsetup\",(0-3)),(\"service\",(0-1)),(\"signal\",(0-5)),(\"roam\",(0,1)),(\"battchg\",(0-5)),(\"callheld\",(0-2))");
            sendok();
        } else if (s.startsWith("AT+CIND?")) {
//            sendat("+CIND: 1,0");
            sendat("+CIND: 0,0,0,4,0,1,0");
            sendok();
        } else if (s.startsWith("AT+CMER=")) {
            sendat("OK");
            startAudioConnection();
        } else if (s.startsWith("AT+CHLD=?")) {
            sendat("+CHLD: 0");
            sendok();
        } else if (s.startsWith("AT+VGS")) {
            ret = atol(getArg(s));
            sendok();
            addln("Set vol=" + ret);
        } else if (s.startsWith("AT+BTRH?")) {
            sendat("+BTRH: 0");
            addln("Query Bt Hold");
            sendok();
        } else if (s.startsWith("AT+CLIP")) {
            addln("CallerID=" + getArg(s));
            sendok();
        } else if (s.startsWith("AT+CCWA")) {
            addln("Call Waiting=" + getArg(s));
            sendok();
        } else if (s.startsWith("AT+NREC")) {
            addln("Noise Red/Echo Can=" + getArg(s));
            sendok();
        } else if (s.startsWith("AT+VGM")) {
            addln("Gain=" + getArg(s));
            sendok();
        } else if (s.startsWith("AT+XAPL")) {
            addln("Apple Vendor=" + getArg(s));
            sendok();
        } else if (s.startsWith("AT+IPHONEACCEV")) {
            addln("Apple Battery Level" + getArg(s));
            sendok();
        } else if (s.startsWith("AT+CMEE")) {
            addln("Extended Gateway Codes=" + getArg(s));
            sendok();
        } else sendat("ERROR");
    }

    private void startAudioConnection() {
        addln("Audio connection would go here.");
        Timer tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                addln("Ringing.");
                if (isRunning() && socket!=null && socket.isConnected()) {
                    try {
                        sendat("+CLIP: \"98612345\",128");
                    } catch (IOException e) {
                        addln("write error "+e.getMessage());
                    }
                }
            }
        }, 3000);
    }

    private String parseBRSF(int ret) {
        String result = "";
        if ((ret & 0x01) != 0) result += "EC/NR ";
        if ((ret & 0x02) != 0) result += "3way ";
        if ((ret & 0x04) != 0) result += "CLI ";
        if ((ret & 0x08) != 0) result += "VR ";
        if ((ret & 0x10) != 0) result += "RemVol ";
        if ((ret & 0x20) != 0) result += "EnhCallStatus ";
        if ((ret & 0x40) != 0) result += "EnhCallCont ";
        if ((ret & 0x80) != 0) result += "CodecNeg ";
        if ((ret & 0x100) != 0) result += "HfInd ";
        if ((ret & 0x200) != 0) result += "S4 ";
        return result.trim();
    }
}
