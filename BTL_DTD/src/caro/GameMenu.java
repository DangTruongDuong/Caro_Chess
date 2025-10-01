package caro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingConstants;

public class GameMenu extends JFrame {
    private static String currentUsername = "";
    private static GameMenu activeGameMenu = null;
    private JTextField txtDisplayName;

    public GameMenu(String username) {
        currentUsername = username;
        setTitle("Game XO - Menu - Chào mừng " + username);
        setSize(650, 550);
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

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 10));
        buttonPanel.setBackground(new Color(230, 240, 250));

        JButton btnCreate = createStyledButton("Tạo Phòng", new Color(34, 139, 34), 18, new Dimension(150, 50));
        JButton btnJoin = createStyledButton("Tham Gia Phòng", new Color(0, 123, 255), 18, new Dimension(150, 50));
        JButton btnViewHistory = createStyledButton("Xem Lịch Sử Đấu", new Color(255, 165, 0), 18, new Dimension(150, 50));
        JButton btnLeaderboard = createStyledButton("Bảng Xếp Hạng", new Color(128, 0, 128), 18, new Dimension(150, 50));

        buttonPanel.add(btnCreate);
        buttonPanel.add(btnJoin);
        buttonPanel.add(btnViewHistory);
        buttonPanel.add(btnLeaderboard);

        centerPanel.add(buttonPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(230, 240, 250));
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBorder(new EmptyBorder(15, 10, 25, 10));
        footerPanel.setPreferredSize(new Dimension(650, 60));

        JButton btnLogout = createStyledButton("Đăng xuất", new Color(220, 20, 60), 14, new Dimension(150, 40));
        footerPanel.add(btnLogout);

        add(footerPanel, BorderLayout.SOUTH);

        btnCreate.addActionListener(e -> {
            String displayName = txtDisplayName.getText().trim();
            if (displayName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên hiển thị!");
                return;
            }
            new WaitingRoomFrame(displayName, this);
            dispose();
            activeGameMenu = null;
        });

        btnJoin.addActionListener(e -> {
            String displayName = txtDisplayName.getText().trim();
            if (displayName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên hiển thị!");
                return;
            }
            String ip = JOptionPane.showInputDialog(this, "Nhập IP máy chủ:");
            if (ip == null || ip.trim().isEmpty()) return;
            
            // Sử dụng port mặc định 12345
            int port = 12345;
            new Client(ip.trim(), port, displayName);
            dispose();
            activeGameMenu = null;
        });

        btnViewHistory.addActionListener(e -> {
            new HistoryFrame(username);
        });

        btnLeaderboard.addActionListener(e -> {
            new LeaderboardFrame();
        });

        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // Đăng xuất khỏi session
                SessionManager.getInstance().logoutUser(username);
                dispose();
                activeGameMenu = null;
                new LoginFrame().setVisible(true);
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // Đăng xuất khỏi session khi đóng cửa sổ
                SessionManager.getInstance().logoutUser(username);
                activeGameMenu = null;
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

        JLabel lblStatus = new JLabel("Nhấn 'Xác nhận' để tạo phòng hoặc 'Đóng' để hủy");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(lblStatus);

        add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footerPanel.setBackground(new Color(230, 240, 250));
        footerPanel.setBorder(new EmptyBorder(10, 10, 20, 10));

        JButton btnConfirm = createStyledButton("Xác nhận", new Color(34, 139, 34), 14, new Dimension(120, 40));
        JButton btnClose = createStyledButton("Đóng", new Color(220, 20, 60), 14, new Dimension(120, 40));

        btnConfirm.addActionListener(e -> {
            // Sử dụng port mặc định 12345
            int port = 12345;
            Server.startServer(port, displayName);
            dispose();
        });

        btnClose.addActionListener(e -> {
            dispose();
            if (parentFrame != null && !parentFrame.isVisible()) {
                parentFrame.setVisible(true);
                GameMenu.setActiveGameMenu(parentFrame);
            }
        });

        footerPanel.add(btnConfirm);
        footerPanel.add(btnClose);
        add(footerPanel, BorderLayout.SOUTH);

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
        setSize(700, 500);
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

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBackground(new Color(230, 240, 250));
        statsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lblWins = createStatsLabel("Thắng: 0", new Color(34, 139, 34));
        JLabel lblLosses = createStatsLabel("Thua: 0", new Color(220, 20, 60));
        JLabel lblDraws = createStatsLabel("Hòa: 0", new Color(255, 165, 0));
        statsPanel.add(lblWins);
        statsPanel.add(lblLosses);
        statsPanel.add(lblDraws);

        centerPanel.add(statsPanel, BorderLayout.NORTH);

        String[] columnNames = {"Đối thủ", "Kết quả gần nhất", "Thời gian", "Tổng trận", "Thắng", "Thua", "Hòa"};
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

        for (int i = 1; i < columnNames.length; i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(new CenterAlignedRenderer());
        }

        JTableHeader header = historyTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230), 1));

        Map<String, List<MatchRecord>> opponentMatches = new HashMap<>();
        int totalWins = 0, totalLosses = 0, totalDraws = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader("match_history.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String p1 = parts[0];
                    String p2 = parts[1];
                    String w = parts[2];
                    String ts = parts[3];
                    if (p1.equals(username) || p2.equals(username)) {
                        String opp = p1.equals(username) ? p2 : p1;
                        String resForMe;
                        if (w.equals("draw")) {
                            resForMe = "draw";
                        } else {
                            boolean isP1 = p1.equals(username);
                            if (isP1) {
                                resForMe = w.equals("p1") ? "win" : "loss";
                            } else {
                                resForMe = w.equals("p2") ? "win" : "loss";
                            }
                        }
                        opponentMatches.computeIfAbsent(opp, k -> new ArrayList<>()).add(new MatchRecord(resForMe, ts));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<MatchSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<MatchRecord>> entry : opponentMatches.entrySet()) {
            String opp = entry.getKey();
            List<MatchRecord> matches = entry.getValue();
            int wins = 0, losses = 0, draws = 0;
            String lastTime = "";
            String lastResult = "";
            String maxTs = "";
            for (MatchRecord mr : matches) {
                if (mr.resForMe.equals("win")) wins++;
                else if (mr.resForMe.equals("loss")) losses++;
                else draws++;
                if (mr.timestamp.compareTo(maxTs) > 0) {
                    maxTs = mr.timestamp;
                    lastTime = formatDateTime(maxTs);
                    lastResult = mr.resForMe.equals("win") ? "Thắng" : (mr.resForMe.equals("loss") ? "Thua" : "Hòa");
                }
            }
            MatchSummary summary = new MatchSummary(opp, wins, losses, draws, lastTime);
            summary.result = lastResult;
            summaries.add(summary);
            totalWins += wins;
            totalLosses += losses;
            totalDraws += draws;
        }

        Collections.sort(summaries, new Comparator<MatchSummary>() {
            @Override
            public int compare(MatchSummary s1, MatchSummary s2) {
                return s2.lastMatchTime.compareTo(s1.lastMatchTime);
            }
        });

        for (MatchSummary summary : summaries) {
            tableModel.addRow(new Object[]{
                summary.opponent,
                summary.result,
                summary.lastMatchTime,
                summary.getTotalMatches(),
                summary.wins,
                summary.losses,
                summary.draws
            });
        }

        if (summaries.isEmpty()) {
            tableModel.addRow(new Object[]{"", "Chưa có lịch sử đấu.", "", "", "", "", ""});
        }

        lblWins.setText("Thắng: " + totalWins);
        lblLosses.setText("Thua: " + totalLosses);
        lblDraws.setText("Hòa: " + totalDraws);

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

    private JLabel createStatsLabel(String text, Color bgColor) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setBackground(bgColor);
        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        return label;
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

    private static class CenterAlignedRenderer extends DefaultTableCellRenderer {
        public CenterAlignedRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private String formatDateTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }
        try {
            String[] parts = timestamp.split(" ");
            String datePart = parts[0];
            String timePart = parts[1].substring(0, 5);
            return datePart + " " + timePart;
        } catch (Exception e) {
            return timestamp;
        }
    }

    private static class MatchRecord {
        String resForMe;
        String timestamp;

        MatchRecord(String resForMe, String timestamp) {
            this.resForMe = resForMe;
            this.timestamp = timestamp;
        }
    }

    private static class MatchSummary {
        String opponent;
        int wins = 0;
        int losses = 0;
        int draws = 0;
        String lastMatchTime = "";
        String result = "";

        MatchSummary(String opponent, int wins, int losses, int draws, String lastMatchTime) {
            this.opponent = opponent;
            this.wins = wins;
            this.losses = losses;
            this.draws = draws;
            this.lastMatchTime = lastMatchTime;
        }


        int getTotalMatches() {
            return wins + losses + draws;
        }
    }
}

class LeaderboardFrame extends JFrame {
    public LeaderboardFrame() {
        setTitle("Bảng Xếp Hạng");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(230, 240, 250));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblTitle = new JLabel("Bảng Xếp Hạng - Top Người Chơi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(230, 240, 250));
        centerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Đồng bộ dữ liệu trước khi load ranking
        syncUserStatsFromHistory();

        String[] columnNames = {"STT", "Tên người chơi", "Số thắng", "Số thua", "Số hòa", "Tỉ lệ thắng (%)"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable leaderboardTable = new JTable(tableModel);
        leaderboardTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leaderboardTable.setRowHeight(30);
        leaderboardTable.setBackground(new Color(245, 245, 245));
        leaderboardTable.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230), 1));
        leaderboardTable.setGridColor(new Color(200, 200, 200));
        leaderboardTable.setShowGrid(true);

        JTableHeader header = leaderboardTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 230), 1));

        List<UserStats> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Remove whitespace
                if (line.isEmpty()) continue; // Skip empty lines
                
                String[] parts = line.split(",");
                if (parts.length >= 5 && !parts[0].isEmpty()) {
                    try {
                        String username = parts[0];
                        int wins = Integer.parseInt(parts[2]);
                        int losses = Integer.parseInt(parts[3]);
                        int draws = Integer.parseInt(parts[4]);
                        int total = wins + losses + draws;
                        double winRate = total > 0 ? (double) wins / total * 100 : 0;
                        users.add(new UserStats(username, wins, losses, draws, winRate));
                    } catch (NumberFormatException e) {
                        // Skip invalid lines
                        System.err.println("Invalid line in users.csv: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(users, new Comparator<UserStats>() {
            @Override
            public int compare(UserStats u1, UserStats u2) {
                if (u1.wins != u2.wins) {
                    return Integer.compare(u2.wins, u1.wins); // Descending wins
                }
                return Double.compare(u2.winRate, u1.winRate); // Then descending win rate
            }
        });

        for (int i = 0; i < users.size(); i++) {
            UserStats u = users.get(i);
            tableModel.addRow(new Object[]{i + 1, u.username, u.wins, u.losses, u.draws, String.format("%.2f", u.winRate)});
        }

        if (users.isEmpty()) {
            tableModel.addRow(new Object[]{"", "Chưa có dữ liệu.", "", "", "", ""});
        }

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footerPanel.setBackground(new Color(230, 240, 250));
        footerPanel.setBorder(new EmptyBorder(10, 10, 20, 10));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(new Color(220, 20, 60));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnClose.setPreferredSize(new Dimension(120, 40));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnClose.setBackground(new Color(139, 0, 0));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnClose.setBackground(new Color(220, 20, 60));
            }
        });
        footerPanel.add(btnClose);
        add(footerPanel, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dispose());

        setVisible(true);
    }

    // Phương thức mới: Đồng bộ users.csv từ match_history.csv
    private void syncUserStatsFromHistory() {
        Map<String, UserStats> statsMap = new HashMap<>();

        // Bước 1: Đọc match_history.csv và tính tổng stats cho từng user
        try (BufferedReader reader = new BufferedReader(new FileReader("match_history.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String p1 = parts[0];
                    String p2 = parts[1];
                    String result = parts[2];
                    // Bỏ qua timestamp parts[3]

                    UserStats p1Stats = statsMap.computeIfAbsent(p1, k -> new UserStats(k, 0, 0, 0, 0));
                    UserStats p2Stats = statsMap.computeIfAbsent(p2, k -> new UserStats(k, 0, 0, 0, 0));

                    if (result.equals("draw")) {
                        p1Stats.draws += 1;
                        p2Stats.draws += 1;
                    } else if (result.equals("p1")) {
                        p1Stats.wins += 1;
                        p2Stats.losses += 1;
                    } else if (result.equals("p2")) {
                        p2Stats.wins += 1;
                        p1Stats.losses += 1;
                    }
                }
            }
        } catch (IOException e) {
            // Nếu không có file match_history, bỏ qua (stats sẽ là 0)
        }

        // Bước 2: Đọc users.csv và cập nhật stats từ map (giữ nguyên user không có trong history)
        List<String> userLines = new ArrayList<>();
        Set<String> existingUsers = new HashSet<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // Skip empty lines
                
                String[] parts = line.split(",");
                if (parts.length >= 2) { // Ít nhất username và password
                    String username = parts[0];
                    String password = parts[1];
                    existingUsers.add(username);
                    
                    // Lấy stats từ history nếu có, nếu không thì giữ nguyên hoặc set 0
                    UserStats stats;
                    if (statsMap.containsKey(username)) {
                        stats = statsMap.get(username);
                    } else if (parts.length >= 5) {
                        // Giữ nguyên stats cũ nếu không có trong history
                        try {
                            int wins = Integer.parseInt(parts[2]);
                            int losses = Integer.parseInt(parts[3]);
                            int draws = Integer.parseInt(parts[4]);
                            stats = new UserStats(username, wins, losses, draws, 0);
                        } catch (NumberFormatException e) {
                            stats = new UserStats(username, 0, 0, 0, 0);
                        }
                    } else {
                        stats = new UserStats(username, 0, 0, 0, 0);
                    }
                    
                    // Cập nhật dòng: username,password,wins,losses,draws
                    userLines.add(username + "," + password + "," + stats.wins + "," + stats.losses + "," + stats.draws);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Bước 3: Thêm user mới từ history mà không có trong users.csv
        for (String username : statsMap.keySet()) {
            if (!existingUsers.contains(username)) {
                UserStats stats = statsMap.get(username);
                userLines.add(username + ",password," + stats.wins + "," + stats.losses + "," + stats.draws);
            }
        }

        // Bước 4: Viết lại users.csv với dữ liệu đồng bộ
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.csv"))) {
            for (String updatedLine : userLines) {
                writer.write(updatedLine);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class UserStats {
        String username;
        int wins, losses, draws;
        double winRate;

        UserStats(String username, int wins, int losses, int draws, double winRate) {
            this.username = username;
            this.wins = wins;
            this.losses = losses;
            this.draws = draws;
            this.winRate = winRate;
        }
    }
}