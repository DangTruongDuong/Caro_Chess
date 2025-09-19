package caro;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Server {
    public static void startServer(int port, String hostName) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            // Tạo phòng chờ với tên host
            JFrame waitingRoom = new JFrame("Phòng chờ - " + hostName);
            waitingRoom.setSize(450, 250);
            waitingRoom.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            waitingRoom.setLocationRelativeTo(null);

            JLabel lblInfo = new JLabel("<html><center><h2>🎮 " + hostName + " đang chờ đối thủ...</h2>" +
                    "<p>Chia sẻ thông tin sau cho bạn chơi:</p>" +
                    "<p><strong>IP:</strong> " + getLocalIPv4() + "<br>" +
                    "<strong>Port:</strong> " + port + "</p>" +
                    "<p>Đang chờ người chơi...</p></center></html>", SwingConstants.CENTER);
            lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblInfo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            waitingRoom.add(lblInfo, BorderLayout.CENTER);
            waitingRoom.setVisible(true);

            // Chờ client kết nối
            Socket clientSocket = serverSocket.accept();

            waitingRoom.dispose(); // đóng phòng chờ

            // Khi có client → mở game XO
            new XOGame(clientSocket, true, hostName, "Đối thủ"); // true = server là Player X

            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi khởi tạo server!");
        }
    }

    private static String getLocalIPv4() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Không thể lấy IP";
        }
    }
}