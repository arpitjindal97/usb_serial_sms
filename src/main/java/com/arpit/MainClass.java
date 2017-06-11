package com.arpit;

import com.fazecast.jSerialComm.SerialPort;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * Created by arpit on 7/6/17.
 */
public class MainClass {

    static public SerialPort selectedPort;
    static SerialPort[] ports = SerialPort.getCommPorts();
    static NewJFrame newJFrame = new NewJFrame();

    static Thread smsThread = null;
    static Sending sending = null;

    public static void main(String arg[]) {
        final PortSelection portSelection = new PortSelection();
        refreshPorts(portSelection.jComboBox1);

        final Webcam webcam = Webcam.getDefault();

        final WebcamPanel panel = (webcam !=null)? new WebcamPanel(webcam, WebcamResolution.QVGA.getSize(), false):null;

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                portSelection.setLocationRelativeTo(null);
                portSelection.jPanel3.setLayout(new BorderLayout());
                if(webcam!=null) {
                    portSelection.jPanel3.removeAll();
                    portSelection.jPanel3.add(panel, BorderLayout.CENTER);
                }
                portSelection.jPanel3.revalidate();
                portSelection.jPanel3.repaint();
                portSelection.setVisible(true);
            }
        });
        if(webcam!=null)
            panel.start();

        portSelection.jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                refreshPorts(portSelection.jComboBox1);
            }
        });

        portSelection.jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    selectedPort = ports[portSelection.jComboBox1.getSelectedIndex()];
                    if (!Testing.test(selectedPort)) {
                        throw new Exception("error connecting to modem");
                    }
                    portSelection.setVisible(false);
                    startMainGUI();
                    if(webcam!=null)
                        panel.stop();
                    portSelection.dispose();
                } catch (Exception e) {
                    portSelection.setVisible(true);
                    JOptionPane.showMessageDialog(portSelection, "Please select another port", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

    }

    public static void refreshPorts(JComboBox jComboBox) {
        ports = SerialPort.getCommPorts();
        jComboBox.removeAllItems();
        for (SerialPort port : ports)
            jComboBox.addItem(port.getDescriptivePortName() + "   ----   " + port.getSystemPortName() + "");
        jComboBox.revalidate();
        jComboBox.repaint();
    }

    public static void startMainGUI() {


        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                newJFrame.setLocationRelativeTo(null);
                newJFrame.setVisible(true);
            }
        });

        newJFrame.jButton3.setEnabled(false);
        newJFrame.jLabel1.setText("Connected at " + selectedPort.getSystemPortName());
        newJFrame.jLabel2.setText("Not started");
        newJFrame.jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {

                sending = new Sending(newJFrame, selectedPort);
                sending.executionFlag = true;

                smsThread = new Thread(new Runnable() {
                    public void run() {
                        sending.startSending();
                    }
                });
                smsThread.start();
                newJFrame.jLabel2.setText("Started");
                newJFrame.jButton1.setEnabled(false);
                newJFrame.jButton2.setEnabled(false);
                newJFrame.jButton3.setEnabled(true);
                newJFrame.jButton4.setEnabled(false);
                newJFrame.jButton5.setEnabled(false);


            }
        });

        newJFrame.jButton3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                sending.executionFlag = false;
            }
        });

        newJFrame.jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
                fileChooser.setFileFilter(filter);
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

                File choosedFile = null;
                int result = fileChooser.showOpenDialog(newJFrame.jButton1);
                if (result == JFileChooser.APPROVE_OPTION) {
                    choosedFile = fileChooser.getSelectedFile();
                    System.out.println("Selected file: " + choosedFile.getAbsolutePath());
                } else if (result == JFileChooser.CANCEL_OPTION) {
                    return;
                }
                FileReader fr = null;
                BufferedReader br = null;
                try {
                    fr = new FileReader(choosedFile);
                    br = new BufferedReader(fr);
                    String str = br.readLine();
                    if (str == null)
                        throw new Exception("Selected file is empty");

                    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                    centerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
                    newJFrame.jTable1.setDefaultRenderer(String.class, centerRenderer);

                    int total = (newJFrame.jTable1.getModel()).getRowCount();
                    while (total-- > 0) {
                        ((DefaultTableModel) newJFrame.jTable1.getModel()).removeRow(total);
                    }
                    do {
                        StringBuilder builder = new StringBuilder(str);
                        String num = builder.substring(0, builder.indexOf("\t")),
                                message = builder.substring(builder.indexOf("\t") + 1, builder.length());

                        Object object[] = new Object[]{(newJFrame.jTable1.getModel()).getRowCount() + 1, num, message, ""};

                        ((DefaultTableModel) newJFrame.jTable1.getModel()).addRow(object);


                    } while ((str = br.readLine()) != null);
                    br.close();

                } catch (FileNotFoundException e) {
                    JOptionPane.showMessageDialog(null, "File doesn't exists");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Cannot read file");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                    e.printStackTrace();
                } finally {
                    newJFrame.jTable1.revalidate();
                    newJFrame.jTable1.repaint();
                }
            }
        });

        newJFrame.jButton5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                for(int i=0;(i<newJFrame.jTable1.getRowCount() );i++)
                {
                    String status = (String)newJFrame.jTable1.getValueAt(i,3);

                    if(status.equals("Sent"))
                    {
                        ((DefaultTableModel) newJFrame.jTable1.getModel()).removeRow(i--);
                    }
                }
                newJFrame.revalidate();
                newJFrame.repaint();
            }
        });
        newJFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                selectedPort.closePort();
                System.exit(0);
            }
        });
    }

}
