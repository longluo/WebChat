package com.longluo.webchat;

/*
 * 类名：ServerProcess
 * 描述：接收到客户端socket发来的信息后进行解析、处理、转发。
 */
import java.io.*;
import java.net.*;
import java.util.*;

class ServerProcess extends Thread {
	private Socket socket = null;// 定义客户端套接字

	private BufferedReader in;// 定义输入流
	private PrintWriter out;// 定义输出流

	private static Vector onlineUser = new Vector(10, 5);
	private static Vector socketUser = new Vector(10, 5);

	private String strReceive, strKey;
	private StringTokenizer st;

	private final String USERLIST_FILE = "F:\\Android\\Work\\user.txt"; // 设定存放用户信息的文件
	private ServerFrame sFrame = null;

	//
	public ServerProcess(Socket client, ServerFrame frame) throws IOException {
		socket = client;
		sFrame = frame;

		in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 客户端接收
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream())), true);// 客户端输出
		this.start();
	}

	public void run() {
		try {
			while (true) {
				strReceive = in.readLine();// 从服务器端接收一条信息后拆分、解析，并执行相应操作
				st = new StringTokenizer(strReceive, "|");
				strKey = st.nextToken();
				if (strKey.equals("login")) {
					login();
				} else if (strKey.equals("talk")) {
					talk();
				} else if (strKey.equals("init")) {
					freshClientsOnline();
				} else if (strKey.equals("reg")) {
					register();
				}
			}
		} catch (IOException e) { // 用户关闭客户端造成此异常，关闭该用户套接字。
			String leaveUser = closeSocket();
			Date t = new Date();
			log("用户" + leaveUser + "已经退出, " + "退出时间:" + t.toLocaleString());
			try {
				freshClientsOnline();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("[SYSTEM] " + leaveUser + " leave chatroom!");
			sendAll("talk|>>>" + leaveUser + " 恋恋不舍的离开了聊天室。");
		}
	}

	// 判断是否有该注册用户
	private boolean isExistUser(String name) {
		String strRead;
		try {
			FileInputStream inputfile = new FileInputStream(USERLIST_FILE);
			DataInputStream inputdata = new DataInputStream(inputfile);
			while ((strRead = inputdata.readLine()) != null) {
				StringTokenizer stUser = new StringTokenizer(strRead, "|");
				if (stUser.nextToken().equals(name)) {
					return true;
				}
			}
		} catch (FileNotFoundException fn) {
			System.out.println("[ERROR] User File has not exist!" + fn);
			out.println("warning|读写文件时出错!");
		} catch (IOException ie) {
			System.out.println("[ERROR] " + ie);
			out.println("warning|读写文件时出错!");
		}
		return false;
	}

	// 判断用户的用户名密码是否正确
	private boolean isUserLogin(String name, String password) {
		String strRead;
		try {
			FileInputStream inputfile = new FileInputStream(USERLIST_FILE);
			DataInputStream inputdata = new DataInputStream(inputfile);
			while ((strRead = inputdata.readLine()) != null) {
				if (strRead.equals(name + "|" + password)) {
					return true;
				}
			}
		} catch (FileNotFoundException fn) {
			System.out.println("[ERROR] User File has not exist!" + fn);
			out.println("warning|读写文件时出错!");
		} catch (IOException ie) {
			System.out.println("[ERROR] " + ie);
			out.println("warning|读写文件时出错!");
		}
		return false;
	}

	// 用户注册
	private void register() throws IOException {
		String name = st.nextToken(); // 得到用户名称
		String password = st.nextToken().trim();// 得到用户密码
		Date t = new Date();

		if (isExistUser(name)) {
			System.out.println("[ERROR] " + name + " Register fail!");
			out.println("warning|该用户已存在，请改名!");
		} else {
			RandomAccessFile userFile = new RandomAccessFile(USERLIST_FILE,
					"rw");
			userFile.seek(userFile.length()); // 在文件尾部加入新用户信息
			userFile.writeBytes(name + "|" + password + "\r\n");
			log("用户" + name + "注册成功, " + "注册时间:" + t.toLocaleString());
			userLoginSuccess(name); // 自动登陆聊天室
		}
	}

	// 用户登陆(从登陆框直接登陆)
	private void login() throws IOException {
		String name = st.nextToken(); // 得到用户名称
		String password = st.nextToken().trim();// 得到用户密码
		boolean succeed = false;
		Date t = new Date();

		log("用户" + name + "正在登陆..." + "\n" + "密码 :" + password + "\n" + "端口 "
				+ socket + t.toLocaleString());
		System.out.println("[USER LOGIN] " + name + ":" + password + ":"
				+ socket);

		for (int i = 0; i < onlineUser.size(); i++) {
			if (onlineUser.elementAt(i).equals(name)) {
				System.out.println("[ERROR] " + name + " is logined!");
				out.println("warning|" + name + "已经登陆聊天室");
			}
		}
		if (isUserLogin(name, password)) { // 判断用户名和密码
			userLoginSuccess(name);
			succeed = true;
		}
		if (!succeed) {
			out.println("warning|" + name + "登陆失败，请检查您的输入!");
			log("用户" + name + "登陆失败！" + t.toLocaleString());
			System.out.println("[SYSTEM] " + name + " login fail!");
		}
	}

	// 用户登陆
	private void userLoginSuccess(String name) throws IOException {
		Date t = new Date();
		out.println("login|succeed");
		sendAll("online|" + name);

		onlineUser.addElement(name);
		socketUser.addElement(socket);

		log("用户" + name + "登录成功，" + "登录时间:" + t.toLocaleString());

		freshClientsOnline();
		sendAll("talk|>>>欢迎 " + name + " 进来与我们一起交谈!");
		System.out.println("[SYSTEM] " + name + " login succeed!");
	}

	// 聊天信息处理
	private void talk() throws IOException {
		String strTalkInfo = st.nextToken(); // 得到聊天内容;
		String strSender = st.nextToken(); // 得到发消息人
		String strReceiver = st.nextToken(); // 得到接收人
		System.out.println("[TALK_" + strReceiver + "] " + strTalkInfo);
		Socket socketSend;
		PrintWriter outSend;
		Date t = new Date();

		// 得到当前时间
		GregorianCalendar calendar = new GregorianCalendar();
		String strTime = "(" + calendar.get(Calendar.HOUR) + ":"
				+ calendar.get(Calendar.MINUTE) + ":"
				+ calendar.get(Calendar.SECOND) + ")";
		strTalkInfo += strTime;

		log("用户" + strSender + "对 " + strReceiver + "说:" + strTalkInfo
				+ t.toLocaleString());

		if (strReceiver.equals("All")) {
			sendAll("talk|" + strSender + " 对所有人说：" + strTalkInfo);
		} else {
			if (strSender.equals(strReceiver)) {
				out.println("talk|>>>不能自言自语哦!");
			} else {
				for (int i = 0; i < onlineUser.size(); i++) {
					if (strReceiver.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(new BufferedWriter(
								new OutputStreamWriter(
										socketSend.getOutputStream())), true);
						outSend.println("talk|" + strSender + " 对你说："
								+ strTalkInfo);
					} else if (strSender.equals(onlineUser.elementAt(i))) {
						socketSend = (Socket) socketUser.elementAt(i);
						outSend = new PrintWriter(new BufferedWriter(
								new OutputStreamWriter(
										socketSend.getOutputStream())), true);
						outSend.println("talk|你对 " + strReceiver + "说："
								+ strTalkInfo);
					}
				}
			}
		}
	}

	// 在线用户列表
	private void freshClientsOnline() throws IOException {
		String strOnline = "online";
		String[] userList = new String[20];
		String useName = null;

		for (int i = 0; i < onlineUser.size(); i++) {
			strOnline += "|" + onlineUser.elementAt(i);
			useName = " " + onlineUser.elementAt(i);
			userList[i] = useName;
		}

		sFrame.txtNumber.setText("" + onlineUser.size());
		sFrame.lstUser.setListData(userList);
		System.out.println(strOnline);
		out.println(strOnline);
	}

	// 信息群发
	private void sendAll(String strSend) {
		Socket socketSend;
		PrintWriter outSend;
		try {
			for (int i = 0; i < socketUser.size(); i++) {
				socketSend = (Socket) socketUser.elementAt(i);
				outSend = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(socketSend.getOutputStream())),
						true);
				outSend.println(strSend);
			}
		} catch (IOException e) {
			System.out.println("[ERROR] send all fail!");
		}
	}

	public void log(String log) {
		String newlog = sFrame.taLog.getText() + "\n" + log;
		sFrame.taLog.setText(newlog);
	}

	// 关闭套接字，并将用户信息从在线列表中删除
	private String closeSocket() {
		String strUser = "";
		for (int i = 0; i < socketUser.size(); i++) {
			if (socket.equals((Socket) socketUser.elementAt(i))) {
				strUser = onlineUser.elementAt(i).toString();
				socketUser.removeElementAt(i);
				onlineUser.removeElementAt(i);
				try {
					freshClientsOnline();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sendAll("remove|" + strUser);
			}
		}
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("[ERROR] " + e);
		}

		return strUser;
	}
}