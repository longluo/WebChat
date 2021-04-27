package com.longluo.webchat;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * 类名：ServerFrame
 * 描述：The Server UI. 服务器窗口
 *
 * @author longluo
 */
public class ServerFrame extends JFrame implements ActionListener {
    protected JTabbedPane tpServer;

    // 服务器信息面板
    protected JPanel pnlServer;
    protected JPanel pnlServerInfo;

    protected JLabel lblNumber;
    protected JLabel lblServerName;
    protected JLabel lblIP;
    protected JLabel lblPort;
    protected JLabel lblLog;

    protected JTextField txtNumber;
    protected JTextField txtServerName;
    protected JTextField txtIP;
    protected JTextField txtPort;
    protected JButton btnStop;
    protected JButton btnSaveLog;
    protected TextArea taLog;

    // 用户信息面板
    protected JPanel pnlUser;
    protected JLabel lblUser;
    protected JList lstUser;
    protected JScrollPane spUser;

    // 关于本软件
    protected JPanel pnlAbout;
    protected JLabel lblVersionNo;
    protected JLabel lblAbout;

    public ServerFrame() {
        super(Constants.APP_SERVER_NAME);
        setSize(550, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();// 在屏幕居中显示
        Dimension fra = this.getSize();
        if (fra.width > scr.width) {
            fra.width = scr.width;
        }
        if (fra.height > scr.height) {
            fra.height = scr.height;
        }
        this.setLocation((scr.width - fra.width) / 2,
                (scr.height - fra.height) / 2);

        // 服务器信息
        pnlServerInfo = new JPanel(new GridLayout(14, 1));
        pnlServerInfo.setBackground(new Color(52, 130, 203));
        pnlServerInfo.setFont(new Font(Constants.FONT_SONG, 0, 12));
        pnlServerInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(""),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));

        lblNumber = new JLabel(Constants.ONLINE_USERS_NUM);
        lblNumber.setForeground(Color.YELLOW);
        lblNumber.setFont(new Font(Constants.FONT_SONG, 0, 12));
        txtNumber = new JTextField(Constants.ZERO_NUM, 10);
        txtNumber.setBackground(Color.decode("#d6f4f2"));
        txtNumber.setFont(new Font(Constants.FONT_SONG, 0, 12));
        txtNumber.setEditable(false);

        lblServerName = new JLabel(Constants.SERVER_NAME);
        lblServerName.setForeground(Color.YELLOW);
        lblServerName.setFont(new Font(Constants.FONT_SONG, 0, 12));
        txtServerName = new JTextField(10);
        txtServerName.setBackground(Color.decode("#d6f4f2"));
        txtServerName.setFont(new Font(Constants.FONT_SONG, 0, 12));
        txtServerName.setEditable(false);

        lblIP = new JLabel(Constants.SERVER_IP);
        lblIP.setForeground(Color.YELLOW);
        lblIP.setFont(new Font(Constants.FONT_SONG, 0, 12));
        txtIP = new JTextField(10);
        txtIP.setBackground(Color.decode("#d6f4f2"));
        txtIP.setFont(new Font(Constants.FONT_SONG, 0, 12));
        txtIP.setEditable(false);

        lblPort = new JLabel(Constants.SERVER_PORT_DESC);
        lblPort.setForeground(Color.YELLOW);
        lblPort.setFont(new Font(Constants.FONT_SONG, 0, 12));
        txtPort = new JTextField(String.valueOf(Constants.SERVER_PORT), 10);
        txtPort.setBackground(Color.decode("#d6f4f2"));
        txtPort.setFont(new Font(Constants.FONT_SONG, 0, 12));
        txtPort.setEditable(false);

        btnStop = new JButton(Constants.CLOSE_SERVER);
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                closeServer();
            }
        });
        btnStop.setBackground(Color.ORANGE);
        btnStop.setFont(new Font(Constants.FONT_SONG, 0, 12));

        pnlServerInfo.setBounds(5, 5, 100, 400);
        pnlServerInfo.add(lblNumber);
        pnlServerInfo.add(txtNumber);
        pnlServerInfo.add(lblServerName);
        pnlServerInfo.add(txtServerName);
        pnlServerInfo.add(lblIP);
        pnlServerInfo.add(txtIP);
        pnlServerInfo.add(lblPort);
        pnlServerInfo.add(txtPort);

        // 服务器面板
        pnlServer = new JPanel();
        pnlServer.setLayout(null);
        pnlServer.setBackground(new Color(52, 130, 203));

        lblLog = new JLabel(Constants.SERVER_LOG);
        lblLog.setForeground(Color.YELLOW);
        lblLog.setFont(new Font(Constants.FONT_SONG, 0, 12));
        taLog = new TextArea(20, 50);
        taLog.setFont(new Font(Constants.FONT_SONG, 0, 12));

        btnSaveLog = new JButton(Constants.SAVE_LOG);
        btnSaveLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                saveLog();
            }
        });
        btnSaveLog.setBackground(Color.ORANGE);
        btnSaveLog.setFont(new Font(Constants.FONT_SONG, 0, 12));

        lblLog.setBounds(110, 5, 100, 30);
        taLog.setBounds(110, 35, 300, 370);
        btnStop.setBounds(200, 410, 120, 30);
        btnSaveLog.setBounds(320, 410, 120, 30);

        //
        pnlServer.add(pnlServerInfo);
        pnlServer.add(lblLog);
        pnlServer.add(taLog);
        pnlServer.add(btnStop);
        pnlServer.add(btnSaveLog);

        // 用户面板
        pnlUser = new JPanel();
        pnlUser.setLayout(null);
        pnlUser.setBackground(new Color(52, 130, 203));
        pnlUser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(""),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));

        lblUser = new JLabel(Constants.ONLINE_USERS_LIST);
        lblUser.setFont(new Font(Constants.FONT_SONG, 0, 12));
        lblUser.setForeground(Color.YELLOW);

        lstUser = new JList();
        lstUser.setFont(new Font(Constants.FONT_SONG, 0, 12));
        lstUser.setVisibleRowCount(17);
        lstUser.setFixedCellWidth(180);
        lstUser.setFixedCellHeight(18);

        spUser = new JScrollPane();
        spUser.setBackground(Color.cyan);
        spUser.setFont(new Font(Constants.FONT_SONG, 0, 12));
        spUser.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        spUser.getViewport().setView(lstUser);

        pnlUser.setBounds(50, 5, 300, 400);
        lblUser.setBounds(50, 10, 100, 30);
        spUser.setBounds(50, 35, 200, 360);

        pnlUser.add(lblUser);
        pnlUser.add(spUser);

        // 软件信息
        pnlAbout = new JPanel();
        pnlAbout.setLayout(null);
        pnlAbout.setBackground(new Color(52, 130, 203));
        pnlAbout.setFont(new Font(Constants.FONT_SONG, 0, 14));

        lblVersionNo = new JLabel(Constants.VERSION_INFO);
        lblVersionNo.setFont(new Font(Constants.FONT_SONG, 0, 14));
        lblVersionNo.setForeground(Color.YELLOW);

        lblAbout = new JLabel();
        lblAbout.setFont(new Font(Constants.FONT_SONG, 0, 14));
        lblAbout.setText(Constants.APP_ABOUT_INFO);
        lblAbout.setForeground(Color.YELLOW);

        lblVersionNo.setBounds(110, 5, 100, 30);
        lblAbout.setBounds(110, 35, 400, 50);

        pnlAbout.add(lblVersionNo);
        pnlAbout.add(lblAbout);

        // 主标签面板
        tpServer = new JTabbedPane(JTabbedPane.TOP);
        tpServer.setBackground(Color.CYAN);
        tpServer.setFont(new Font(Constants.FONT_SONG, 0, 14));

        tpServer.add(Constants.SERVER_ADMIN, pnlServer);
        tpServer.add(Constants.ONLINE_USERS, pnlUser);
        tpServer.add(Constants.ABOUT_APP, pnlAbout);

        this.getContentPane().add(tpServer);
        setVisible(true);
    }

    protected void closeServer() {
        this.dispose();
    }

    protected void saveLog() {
        try {
            FileOutputStream fileoutput = new FileOutputStream("log.txt", true);
            String temp = taLog.getText();
            fileoutput.write(temp.getBytes());
            fileoutput.close();
            JOptionPane.showMessageDialog(null, Constants.SAVE_LOG_FILE);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void actionPerformed(ActionEvent evt) {
    }

    /**
     * 服务器窗口
     *
     * @param args
     */
    public static void main(String[] args) {
        new ServerFrame();
    }
}
