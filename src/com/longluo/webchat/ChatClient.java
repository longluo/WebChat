package com.longluo.webchat;

/*
 * 类名：ChatClient
 * 描述：创建一个聊天室客户端,连接服务器并实现聊天功能
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ChatClient extends JFrame implements ActionListener {
	JFrame clientFrame = new JFrame("WebChat 网络聊天室");

	GridBagLayout gl;
	BorderLayout bdl;
	GridBagConstraints gbc;

	// 聊天界面
	JPanel pnlBack, pnlTalk;
	JButton btnTalk;
	JTextArea txtViewTalk;
	JLabel lblTalk, lblTo;
	JComboBox listOnline;

	// 登陆界面
	JPanel pnlLogin;
	JLabel lblServerIP, lblName, lblPassword;
	JTextField txtTalk, txtServerIP, txtName;
	JPasswordField txtPassword;
	JButton btnLogin, btnReg, btnExit;

	JDialog dialogLogin = new JDialog(this, "登陆", true);

	Socket socket = null;
	BufferedReader in = null;
	PrintWriter out = null;

	String strSend, strReceive, strKey, strStatus;
	private StringTokenizer st;

	public ChatClient() {
		// 初始化
		gl = new GridBagLayout();
		bdl = new BorderLayout();
		gbc = new GridBagConstraints();
		pnlBack = (JPanel) getContentPane();
		pnlBack.setLayout(bdl);

		// 初始化控件
		pnlLogin = new JPanel();
		pnlLogin.setLayout(gl);

		lblServerIP = new JLabel("服务器IP:");
		lblName = new JLabel("    用户名:");
		lblPassword = new JLabel("     密码:  ");
		txtServerIP = new JTextField(12);
		txtName = new JTextField(12);
		txtPassword = new JPasswordField(12);

		txtServerIP.setText("127.0.0.1");

		btnLogin = new JButton("登陆");
		btnReg = new JButton("注册");
		btnExit = new JButton("退出");

		btnTalk = new JButton("发送");
		lblTalk = new JLabel("发言:");

		lblTo = new JLabel(" To :");
		txtTalk = new JTextField(30);
		pnlTalk = new JPanel();
		txtViewTalk = new JTextArea(18, 40);
		listOnline = new JComboBox();
		txtViewTalk.setForeground(Color.blue);
		btnTalk.addActionListener(this);

		btnLogin.addActionListener(this);
		btnReg.addActionListener(this);
		btnExit.addActionListener(this);

		listOnline.addItem("All");

		pnlTalk.add(lblTalk);
		pnlTalk.add(txtTalk);
		pnlTalk.add(lblTo);
		pnlTalk.add(listOnline);
		pnlTalk.add(btnTalk);
		pnlBack.add("Center", txtViewTalk);
		pnlBack.add("South", pnlTalk);
		pnlTalk.setBackground(Color.cyan);
		btnTalk.setEnabled(false);

		clientFrame.getContentPane().add(pnlBack);
		clientFrame.setSize(600, 450);
		clientFrame.setVisible(true);
		clientFrame.setResizable(false);
		clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 登陆对话框初始化
		dialogLogin.getContentPane().setLayout(new FlowLayout());
		dialogLogin.getContentPane().add(lblServerIP);
		dialogLogin.getContentPane().add(txtServerIP);
		dialogLogin.getContentPane().add(lblName);
		dialogLogin.getContentPane().add(txtName);
		dialogLogin.getContentPane().add(lblPassword);
		dialogLogin.getContentPane().add(txtPassword);
		dialogLogin.getContentPane().add(btnLogin);
		dialogLogin.getContentPane().add(btnReg);
		dialogLogin.getContentPane().add(btnExit);

		dialogLogin.setBounds(300, 300, 250, 200);
		dialogLogin.getContentPane().setBackground(Color.gray);
		dialogLogin.show(true);
	}

	public static void main(String[] args) {
		new ChatClient();
	}

	// 建立与服务端通信的套接字
	void connectServer() {
		try {
			socket = new Socket(txtServerIP.getText(), 8888);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream())), true);
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(this, "连接服务器失败!", "ERROE",
					JOptionPane.INFORMATION_MESSAGE);
			txtServerIP.setText("");
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// 弹出窗口
	public void popWindows(String strWarning, String strTitle) {
		JOptionPane.showMessageDialog(this, strWarning, strTitle,
				JOptionPane.INFORMATION_MESSAGE);
	}

	//
	private void initLogin() throws IOException {
		strReceive = in.readLine();
		st = new StringTokenizer(strReceive, "|");
		strKey = st.nextToken();
		if (strKey.equals("login")) {
			strStatus = st.nextToken();
			if (strStatus.equals("succeed")) {
				btnLogin.setEnabled(false);
				btnTalk.setEnabled(true);
				pnlLogin.setVisible(false);
				dialogLogin.dispose();
				new ClientThread(socket);
				out.println("init|online");
			}
			popWindows(strKey + " " + strStatus + "!", "Login");
		}
		if (strKey.equals("warning")) {
			strStatus = st.nextToken();
			popWindows(strStatus, "Register");
		}
	}

	public void actionPerformed(ActionEvent evt) {
		Object obj = evt.getSource();

		try {
			if (obj.equals(btnLogin)) {
				if ((txtServerIP.getText().length() > 0)
						&& (txtName.getText().length() > 0)
						&& (txtPassword.getText().length() > 0)) {
					connectServer();
					strSend = "login|" + txtName.getText() + "|"
							+ String.valueOf(txtPassword.getPassword());
					out.println(strSend);
					initLogin();
				} else {
					popWindows("请输入完整信息", "ERROE");
				}
			} else if (obj.equals(btnReg)) {
				if ((txtName.getText().length() > 0)
						&& (txtPassword.getText().length() > 0)) {
					connectServer();
					strSend = "reg|" + txtName.getText() + "|"
							+ String.valueOf(txtPassword.getPassword());
					out.println(strSend);
					initLogin();
				}
			} else if (obj.equals(btnExit)) {
				System.exit(0);
			} else if (obj.equals(btnTalk)) {
				if (txtTalk.getText().length() > 0) {
					out.println("talk|" + txtTalk.getText() + "|"
							+ txtName.getText() + "|"
							+ listOnline.getSelectedItem().toString());//
					txtTalk.setText("");
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	class ClientThread implements Runnable {
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		private String strReceive, strKey;
		private Thread threadTalk;
		private StringTokenizer st;

		public ClientThread(Socket s) throws IOException {
			this.socket = s;
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			threadTalk = new Thread(this);
			threadTalk.start();
		}

		public void run() {
			while (true) {
				synchronized (this) {
					try {
						strReceive = in.readLine();
						st = new StringTokenizer(strReceive, "|");
						strKey = st.nextToken();
						if (strKey.equals("talk")) {
							String strTalk = st.nextToken();
							strTalk = txtViewTalk.getText() + "\r\n   "
									+ strTalk;
							txtViewTalk.setText(strTalk);
						} else if (strKey.equals("online")) {
							String strOnline;
							while (st.hasMoreTokens()) {
								strOnline = st.nextToken();
								listOnline.addItem(strOnline);
							}
						} else if (strKey.equals("remove")) {
							String strRemove;
							while (st.hasMoreTokens()) {
								strRemove = st.nextToken();
								listOnline.removeItem(strRemove);
							}
						} else if (strKey.equals("warning")) {
							String strWarning = st.nextToken();
							popWindows(strWarning, "Warning");
						}
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					} catch (IOException e) {
					}
				}
			}
		}
	}
}
