package caro;

import java.io.*;
import java.net.*;

import javax.swing.JOptionPane;

public class Client {
    public Client(String serverIp, int port, String clientName) {
        try {
            Socket socket = new Socket(serverIp, port);
            // Mở game XO, client là Player O, truyền tên
            new XOGame(socket, false, clientName, "Đối thủ");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Không thể kết nối đến server!");
        }
    }
}