package caro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GameMenu extends JFrame {
    private static String currentUsername = "";
    private static GameMenu activeGameMenu = null; // Biến static để theo dõi cửa sổ GameMenu đang hoạt động
    private JTextField txtDisplayName;

    public GameMenu(String username) {
        currentUsername = username;
        setTitle("Game XO - Menu - Chào mừng " + username);
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(230, 240, 250));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblWelcome = new JLabel("Game XO - Chào mừng " + username + "!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblWelcome.setForeground(Color.WHITE);
        headerPanel.add(lblWelcome);

        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        centerPanel.setBackground(new Color(230, 240, 250));

        JPanel namePanel = new JPanel(new BorderLayout(10, 10));
        namePanel.setBackground(new Color(230, 240, 250));
        namePanel.setBorder(BorderFactory.createTitledBorder("Tên hiển thị trong game:"));

        JLabel lblName = new JLabel("Tên của bạn:");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblName.setForeground(Color.DARK_GRAY);

        txtDisplayName = new JTextField(username);
        txtDisplayName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDisplayName.setPreferredSize(new Dimension(200, 30));
        txtDisplayName.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230), 1));

        namePanel.add(lblName, BorderLayout.NORTH);
        namePanel.add(txtDisplayName, BorderLayout.CENTER);

        centerPanel.add(namePanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(230, 240, 250));

        JButton btnCreate = createStyledButton("Tạo Phòng", new Color(34, 139, 34), 18, new Dimension(220, 50));
        JButton btnJoin = createStyledButton("Tham Gia Phòng", new Color(0, 123, 255), 18, new Dimension(220, 50));
        JButton btnViewHistory = createStyledButton("Xem Lịch Sử Đấu", new Color(255, 165, 0), 18, new Dimension(220, 50));

        buttonPanel.add(btnCreate);
        buttonPanel.add(btnJoin);
        buttonPanel.add(btnViewHistory);

        centerPanel.add(buttonPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(230, 240, 250));
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBorder(new EmptyBorder(10, 10, 20, 10));

        JLabel lblInfo = new JLabel("Chọn chế độ chơi hoặc xem lịch sử đấu!");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInfo.setForeground(new Color(100, 100, 100));
        footerPanel.add(lblInfo);

        add(footerPanel, BorderLayout.SOUTH);

        btnCreate.addActionListener(e -> {
            String displayName = txtDisplayName.getText().trim();
            if (displayName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên hiển thị!");
                return;
            }
            new WaitingRoomFrame(displayName, this);
            dispose();
            activeGameMenu = null; // Xóa tham chiếu khi đóng menu
        });

        btnJoin.addActionListener(e -> {
            String displayName = txtDisplayName.getText().trim();
            if (displayName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên hiển thị!");
                return;
            }
            String ip = JOptionPane.showInputDialog(this, "Nhập IP máy chủ:");
            if (ip == null || ip.trim().isEmpty()) return;
            String portStr = JOptionPane.showInputDialog(this, "Nhập cổng (port):");
            if (portStr == null || portStr.trim().isEmpty()) return;
            try {
                int port = Integer.parseInt(portStr);
                new Client(ip.trim(), port, displayName);
                dispose();
                activeGameMenu = null; // Xóa tham chiếu khi đóng menu
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Port không hợp lệ! Vui lòng nhập số.");
            }
        });

        btnViewHistory.addActionListener(e -> {
            new HistoryFrame(username);
        });

        // Xử lý khi đóng cửa sổ
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                activeGameMenu = null; // Xóa tham chiếu khi đóng
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor, int fontSize, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(15, 30, 15, 30));
        btn.setPreferredSize(size);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static GameMenu getActiveGameMenu() {
        return activeGameMenu;
    }

    public static void setActiveGameMenu(GameMenu menu) {
        activeGameMenu = menu;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameMenu menu = new GameMenu("Guest");
            menu.setVisible(true);
            activeGameMenu = menu;
        });
    }
}

class WaitingRoomFrame extends JFrame {
    public WaitingRoomFrame(String displayName, GameMenu parentFrame) {
        setTitle("Phòng Chờ - " + displayName);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(230, 240, 250));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblTitle = new JLabel("Đang chuẩn bị tạo phòng...");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBackground(new Color(230, 240, 250));
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblInfo = new JLabel("Tên hiển thị: " + displayName);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(lblInfo);

        JLabel lblStatus = new JLabel("Nhấn 'Xác nhận' để tạo phòng hoặc 'Hủy' để quay lại.");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(lblStatus);
        add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footerPanel.setBackground(new Color(230, 240, 250));
        footerPanel.setBorder(new EmptyBorder(10, 10, 20, 10));

        JButton btnConfirm = createStyledButton("Xác nhận", new Color(34, 139, 34), 14, new Dimension(120, 40));
        JButton btnCancel = createStyledButton("Hủy", new Color(220, 20, 60), 14, new Dimension(120, 40));

        footerPanel.add(btnConfirm);
        footerPanel.add(btnCancel);
        add(footerPanel, BorderLayout.SOUTH);

        btnConfirm.addActionListener(e -> {
            new Thread(() -> {
                Server.startServer(12345, displayName);
            }).start();
            dispose();
        });

        btnCancel.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                if (GameMenu.getActiveGameMenu() == null || !GameMenu.getActiveGameMenu().isVisible()) {
                    GameMenu newMenu = new GameMenu(parentFrame.getCurrentUsername());
                    newMenu.setVisible(true);
                    GameMenu.setActiveGameMenu(newMenu);
                }
            });
        });

        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor, int fontSize, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setPreferredSize(size);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }
}

class HistoryFrame extends JFrame {
    public HistoryFrame(String username) {
        setTitle("Lịch Sử Đấu - " + username);
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(230, 240, 250));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblTitle = new JLabel("Lịch Sử Đấu - " + username);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(230, 240, 250));
        centerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblStats = new JLabel();
        lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblStats.setForeground(Color.DARK_GRAY);
        int wins = 0, losses = 0, draws = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equals(username)) {
                    wins = Integer.parseInt(parts[2]);
                    losses = Integer.parseInt(parts[3]);
                    draws = Integer.parseInt(parts[4]);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        centerPanel.add(lblStats, BorderLayout.NORTH);

        String[] columnNames = {"Người chơi 1", "Người chơi 2", "Tỉ số", "Thời gian"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        historyTable.setRowHeight(30);
        historyTable.setBackground(new Color(245, 245, 245));
        historyTable.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230), 1));
        historyTable.setGridColor(new Color(200, 200, 200));
        historyTable.setShowGrid(true);

        JTableHeader header = historyTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230), 1));

        try (BufferedReader reader = new BufferedReader(new FileReader("match_history.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String player1 = parts[0];
                    String player2 = parts[1];
                    String result = parts[2];
                    String timestamp = parts[3];
                    int player1Score = Integer.parseInt(parts[4]);
                    int player2Score = Integer.parseInt(parts[5]);
                    if (player1.equals(username) || player2.equals(username)) {
                        String winner, loser, scoreText;
                        if (result.equals("draw")) {
                            winner = "";
                            loser = player1 + " vs " + player2;
                            scoreText = player1Score + "-" + player2Score;
                        } else {
                            winner = result.equals("win") ? player1 : player2;
                            loser = result.equals("win") ? player2 : player1;
                            scoreText = player1.equals(winner) ? player1Score + "-" + player2Score : player2Score + "-" + player1Score;
                        }
                        tableModel.addRow(new Object[]{winner, loser, scoreText, timestamp});
                    }
                }
            }
        } catch (IOException e) {
            tableModel.addRow(new Object[]{"", "Chưa có lịch sử đấu.", "", ""});
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footerPanel.setBackground(new Color(230, 240, 250));
        footerPanel.setBorder(new EmptyBorder(10, 10, 20, 10));

        JButton btnClose = createStyledButton("Đóng", new Color(220, 20, 60), 14, new Dimension(120, 40));
        footerPanel.add(btnClose);
        add(footerPanel, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dispose());

        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor, int fontSize, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setPreferredSize(size);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }
}