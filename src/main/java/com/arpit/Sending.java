package com.arpit;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by arpit on 8/6/17.
 */
public class Sending
{
    SerialPort selectedPort;
    NewJFrame newJFrame;
    String message = "Done !";

    boolean executionFlag=false;
    Sending(NewJFrame newJFrame,SerialPort selectedPort)
    {
        this.newJFrame = newJFrame;
        this.selectedPort = selectedPort;
    }

    public void startSending()
    {
        selectedPort.openPort();
        for(int i=0;(i<newJFrame.jTable1.getRowCount()&& executionFlag );i++)
        {
            newJFrame.jLabel2.setText("Sending "+(i+1)+" of "+newJFrame.jTable1.getRowCount());
            newJFrame.jLabel2.repaint();
            String num = (String)newJFrame.jTable1.getValueAt(i,1);
            String message = (String)newJFrame.jTable1.getValueAt(i,2);
            if(num.equals("")||message.equals(""))
                continue;
            if(sendThis(num,message))
            {
                newJFrame.jTable1.setValueAt("Sent",i,3);
            }
            else
            {
                newJFrame.jTable1.setValueAt("Failed",i,3);
            }
        }
        newJFrame.jLabel2.setText("Finished");
        if(!executionFlag)
            newJFrame.jLabel2.setText("Stopped");

        newJFrame.jLabel2.repaint();
        selectedPort.closePort();

        JOptionPane.showMessageDialog(newJFrame,message);

        newJFrame.jButton1.setEnabled(true);
        newJFrame.jButton2.setEnabled(true);
        newJFrame.jButton3.setEnabled(false);
        newJFrame.jButton4.setEnabled(true);
        newJFrame.jButton5.setEnabled(true);

    }

    boolean sendThis(String num,String message)
    {
        try {
            InputStream inputStream = selectedPort.getInputStream();
            OutputStream outputStream = selectedPort.getOutputStream();
            String str = "AT+CSCS=\"GSM\"" + (char)13 ;
            outputStream.write(str.getBytes());
            //System.out.println(str);

            if(!waitForOK(inputStream))
                return false;

            str = "AT+CMGF=1" + (char) 13;
            outputStream.write(str.getBytes());
            //System.out.println(str);

            if (!waitForOK(inputStream))
                return false;

            str="AT+CMGS=\""+num+"\""+(char)13;
            outputStream.write(str.getBytes());
            //System.out.println(str);
            Thread.sleep(500);
            str=message+(char)26 +(char)26;
            outputStream.write(str.getBytes());
            //System.out.println(str);

            if(!waitForOK(inputStream))
                return false;

        } catch (IOException e) {
            newJFrame.jLabel1.setText("Error connecting to modem");
            newJFrame.jLabel1.repaint();
            executionFlag = false;
            this.message = "Error connecting to modem";
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    boolean waitForOK(InputStream inputStream) throws IOException {
        String str="";
        int time=20000;
        while(time>0 && (!str.contains("OK")))
        {
            //while (selectedPort.bytesAvailable() == 0)
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            //System.out.println("reached here");

            while(selectedPort.bytesAvailable()!=0) {
                char ch = (char)inputStream.read();
                str+=ch;

            }
            System.out.print(str);
            time -=500;
        }
        return str.contains("OK");
    }

}
