package caro;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private static boolean hasClient = false;

    public static void startServer(int port, String hostName) {
        final ServerSocket[] serverSocketHolder = new ServerSocket[1];
        final JFrame[] waitingRoomHolder = new JFrame[1];
        final AtomicBoolean isCancelled = new AtomicBoolean(false);
        
        try {
            serverSocketHolder[0] = new ServerSocket(port); // khởi tạo sever khi ấn nút tạo phòng 

            waitingRoomHolder[0] = new JFrame("Phòng chờ - " + hostName);
            JFrame waitingRoom = waitingRoomHolder[0];
            waitingRoom.setSize(500, 400);
            waitingRoom.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            waitingRoom.setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(240, 248, 255));
            waitingRoom.setContentPane(mainPanel);

            // Simple centered message
            JLabel lblWaiting = new JLabel("Chờ đối thủ kết nối");
            lblWaiting.setFont(new Font("Segoe UI", Font.BOLD, 28));
            lblWaiting.setForeground(new Color(70, 130, 180));
            lblWaiting.setHorizontalAlignment(SwingConstants.CENTER);
            lblWaiting.setVerticalAlignment(SwingConstants.CENTER);
            mainPanel.add(lblWaiting, BorderLayout.CENTER);

            // Simple cancel button
            JButton btnCancel = new JButton("Hủy");
            btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            btnCancel.setBackground(new Color(220, 20, 60));
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setFocusPainted(false);
            btnCancel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
            btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnCancel.addActionListener(e -> {
                isCancelled.set(true);
                hasClient = false;
                try {
                    if (serverSocketHolder[0] != null && !serverSocketHolder[0].isClosed()) {
                        serverSocketHolder[0].close();
                    }
                } catch (IOException ex) {
                    // Ignore
                }
                if (waitingRoom != null && waitingRoom.isDisplayable()) {
                    waitingRoom.dispose();
                }
                SwingUtilities.invokeLater(() -> {
                    if (GameMenu.getActiveGameMenu() == null || !GameMenu.getActiveGameMenu().isVisible()) {
                        GameMenu newMenu = new GameMenu(hostName);
                        newMenu.setVisible(true);
                        GameMenu.setActiveGameMenu(newMenu);
                    }
                });
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(new Color(240, 248, 255));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20));
            buttonPanel.add(btnCancel);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            waitingRoom.setVisible(true);

            new Thread(() -> {
                try {
                    while (!isCancelled.get() && !serverSocketHolder[0].isClosed()) {
                        Socket clientSocket = serverSocketHolder[0].accept();
                        if (hasClient) {
                            DataOutputStream rejectOut = new DataOutputStream(clientSocket.getOutputStream());
                            rejectOut.writeUTF("ROOM_FULL");// gửi phòng đã đầy
                            rejectOut.flush();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            clientSocket.close();
                            continue;
                        }
                        
                        hasClient = true;

                        // SỬA: Gửi handshake message trước
                        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                        out.writeUTF("WELCOME");//gửi chữ welcome
                        out.flush();
                
                        
                        // Tạo XOGame ngay lập tức với tên tạm thời
                        final String finalHostName = hostName;
                        final Socket finalClientSocket = clientSocket;
                        final DataOutputStream finalOut = out;
                        
                        SwingUtilities.invokeLater(() -> {
                            if (waitingRoomHolder[0] != null && waitingRoomHolder[0].isDisplayable()) {
                                waitingRoomHolder[0].dispose();
                            }
                            // Tạo game với tên "Đang kết nối..." - sẽ được cập nhật sau
                            new XOGame(finalClientSocket, true, finalHostName, "Đang kết nối...");
                        });
                        
                        // Bắt đầu thread riêng để lắng nghe messages từ client và trao đổi tên
                        new Thread(() -> {
                            try {
                                DataInputStream clientIn = new DataInputStream(clientSocket.getInputStream());
                                
                                // Đọc tên client
                                String clientNameMessage = clientIn.readUTF();
                                
                                if (clientNameMessage.startsWith("NAME:")) {//gửi têntên
                                }

                                finalOut.writeUTF("NAME:" + finalHostName);
                                finalOut.flush();
                                
                                // Bây giờ XOGame của server sẽ nhận được tên từ client thông qua listenForMessages()
                                
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                        
                        break;
                    }
                } catch (IOException e) {
                    if (!isCancelled.get()) {
                        String msg = e.getMessage();
                        if (msg != null && (!msg.contains("Socket closed") && !msg.contains("Socket is closed") && !msg.contains("Connection reset"))) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(null, "Lỗi khi chờ kết nối: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    }
                } finally {
                    hasClient = false;
                    try {
                        if (serverSocketHolder[0] != null && !serverSocketHolder[0].isClosed()) {
                            serverSocketHolder[0].close();
                        }
                    } catch (IOException ignored) {}
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi khởi tạo server!");
            if (waitingRoomHolder[0] != null && waitingRoomHolder[0].isDisplayable()) {
                waitingRoomHolder[0].dispose();
            }
        }
    }


}