package caro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    public static void startServer(int port, String hostName) {
        // Biến final để có thể sử dụng trong lambda
        final ServerSocket[] serverSocketHolder = new ServerSocket[1];
        final JFrame[] waitingRoomHolder = new JFrame[1];
        final AtomicBoolean isCancelled = new AtomicBoolean(false);
        
        try {
            serverSocketHolder[0] = new ServerSocket(port);

            // Tạo phòng chờ với thiết kế đẹp hơn
            waitingRoomHolder[0] = new JFrame("Phòng chờ - " + hostName);
            JFrame waitingRoom = waitingRoomHolder[0];
            waitingRoom.setSize(500, 400);
            waitingRoom.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            waitingRoom.setLocationRelativeTo(null);

            // Main panel với gradient background
            JPanel mainPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, new Color(70, 130, 180), 0, getHeight(), new Color(230, 240, 250));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            mainPanel.setLayout(new BorderLayout(15, 15));
            waitingRoom.setContentPane(mainPanel);

            // Header panel
            JPanel headerPanel = new JPanel();
            headerPanel.setOpaque(false);
            headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel lblTitle = new JLabel("Phòng Chờ - " + hostName);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblTitle.setForeground(Color.WHITE);
            headerPanel.add(lblTitle);

            mainPanel.add(headerPanel, BorderLayout.NORTH);

            // Center panel với thông tin
            JPanel infoPanel = new JPanel();
            infoPanel.setOpaque(false);
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

            String ip = getLocalIPv4();
            JLabel lblInfo = new JLabel("<html><center>" +
                    "<h2>Đang chờ đối thủ kết nối...</h2>" +
                    "<p>Chia sẻ thông tin sau cho bạn chơi:</p>" +
                    "<b>IP:</b> " + ip + "<br>" +
                    "<b>Port:</b> " + port + "<br><br>" +
                    "<i>⏳ Đang chờ người chơi...</i>" +
                    "</center></html>", SwingConstants.CENTER);
            lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
            lblInfo.setBackground(new Color(245, 245, 245));
            lblInfo.setOpaque(true);

            infoPanel.add(lblInfo);
            mainPanel.add(infoPanel, BorderLayout.CENTER);

            // Footer panel với nút hủy
            JPanel buttonPanel = new JPanel();
            buttonPanel.setOpaque(false);
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JButton btnCancel = createStyledButton("Hủy & Quay Lại", new Color(204, 0, 0));

            btnCancel.addActionListener(e -> {
                isCancelled.set(true);
                try {
                    if (serverSocketHolder[0] != null && !serverSocketHolder[0].isClosed()) {
                        serverSocketHolder[0].close(); // Unblock accept()
                    }
                } catch (IOException ex) {
                    // Ignore
                }
                if (waitingRoom != null && waitingRoom.isDisplayable()) {
                    waitingRoom.dispose();
                }
                // Quay lại GameMenu
                SwingUtilities.invokeLater(() -> {
                    if (GameMenu.getActiveGameMenu() == null || !GameMenu.getActiveGameMenu().isVisible()) {
                        GameMenu newMenu = new GameMenu(hostName);
                        newMenu.setVisible(true);
                        GameMenu.setActiveGameMenu(newMenu);
                    }
                });
            });

            buttonPanel.add(btnCancel);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            waitingRoom.setVisible(true);

            // Chạy accept() trong thread riêng để không block UI
            new Thread(() -> {
                try {
                    if (!isCancelled.get()) {
                        Socket clientSocket = serverSocketHolder[0].accept();

                        // Kết nối thành công, đóng waiting room và mở game
                        SwingUtilities.invokeLater(() -> {
                            if (waitingRoomHolder[0] != null && waitingRoomHolder[0].isDisplayable()) {
                                waitingRoomHolder[0].dispose();
                            }
                            new XOGame(clientSocket, true, hostName, "Đối thủ"); // true = server là Player X
                        });
                    }
                } catch (IOException e) {
                    // Nếu bị hủy (socket closed), không hiển thị lỗi
                    if (!isCancelled.get()) {
                        String msg = e.getMessage();
                        if (msg != null && (!msg.contains("Socket closed") && !msg.contains("Socket is closed") && !msg.contains("Connection reset"))) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(null, "Lỗi khi chờ kết nối: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    }
                } finally {
                    // Đóng server socket sau khi accept hoặc lỗi
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

    private static JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 2, true),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 50, 50), 3, true),
                    BorderFactory.createEmptyBorder(10, 25, 10, 25)
                ));
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(bgColor);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 100), 2, true),
                    BorderFactory.createEmptyBorder(10, 25, 10, 25)
                ));
            }
        });
        return btn;
    }

    private static String getLocalIPv4() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Không thể lấy IP";
        }
    }
}