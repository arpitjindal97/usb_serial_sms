package com.arpit;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by arpit on 9/6/17.
 */
public class Testing extends TimerTask {
    SerialPort selectedPort;
    boolean result = false;

    @Override
    public void run() {
        try {
            InputStream inputStream = selectedPort.getInputStream();
            OutputStream outputStream = selectedPort.getOutputStream();
            String str = "AT+CSCS=\"GSM\"" + (char) 13;
            outputStream.write(str.getBytes());

            Sending sending = new Sending(null, selectedPort);

            if (!sending.waitForOK(inputStream))
                result = false;
            str = "AT+CMGF=1" + (char) 13;
            outputStream.write(str.getBytes());

            if (!sending.waitForOK(inputStream))
                result = false;
        } catch (Exception e) {
            result = false;
        }
        result = true;
    }

    static boolean test(SerialPort selectedPort) {
        selectedPort.openPort();
        Testing timerTask = new Testing();
        timerTask.selectedPort = selectedPort;
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 20 * 1000);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.cancel();
        timer.purge();
        selectedPort.closePort();
        return timerTask.result;
    }
}
