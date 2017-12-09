package com.honeywell.iaq.net;

import com.honeywell.iaq.utils.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class TcpClient {

    public static Socket socket;

    private OutputStream outputStream;

    public void createTcpCient() {

        TcpClientThread tcpClientThread = new TcpClientThread(Constants.DEFAULT_AP_IP, Constants.DEFAULT_IAQ_PORT);

        tcpClientThread.start();
    }

    public class TcpClientThread extends Thread {

        private String ip = Constants.DEFAULT_IAQ_IP;

        private int port = Constants.DEFAULT_IAQ_PORT;

        public TcpClientThread(String Ip, int Port) {
            ip = Ip;
            port = Port;
        }

        @Override
        public void run() {
            super.run();
            try {
                socket = new Socket();

                SocketAddress socketAddress = new InetSocketAddress(ip, port);

                socket.connect(socketAddress);

                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean sendBuffer(String buf) {
        byte buffer[] = null;
        try {

            buffer = buf.getBytes("UTF-8");
            if (outputStream != null) {
                outputStream.write(buffer, 0, buffer.length);
                outputStream.flush();
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
