package caro;

import java.io.*;
import java.net.*;

import javax.swing.JOptionPane;

public class Client {
    public Client(String serverIp, int port, String clientName) {
        Socket socket = null; 
        try {
            socket = new Socket(serverIp, port); //khởi tọa server
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            
            // Đọc handshake message từ server
            String initialMessage = in.readUTF();
            if ("ROOM_FULL".equals(initialMessage)) {
                JOptionPane.showMessageDialog(null, "Phòng đã đầy (2/2)! Không thể tham gia.", "Phòng đầy", JOptionPane.INFORMATION_MESSAGE);
                socket.close();
                new GameMenu(clientName).setVisible(true);
                return;
            }
            
            if (!"WELCOME".equals(initialMessage)) {
                JOptionPane.showMessageDialog(null, "Lỗi kết nối server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                socket.close();
                return;
            }
            
            // Tạo XOGame với tên tạm thời
            XOGame game = new XOGame(socket, false, clientName, "Đang kết nối...");
            
            // Gửi tên client
            out.writeUTF("NAME:" + clientName);
            out.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Không thể kết nối đến server!");
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
            // Quay về menu chính khi có lỗi kết nối
            new GameMenu(clientName).setVisible(true);
        }
    }
}