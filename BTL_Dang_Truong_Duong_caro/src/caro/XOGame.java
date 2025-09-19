package caro;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class XOGame extends JFrame {
    private JButton[][] buttons = new JButton[3][3];
    private boolean myTurn;
    private char myMark, opponentMark;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String myName, opponentName;
    private JLabel lblPlayers;
    private int winRow = -1, winCol = -1;
    private boolean winDiagonal = false;
    private boolean winAntiDiagonal = false;
    private int myScore = 0;
    private int opponentScore = 0;
    private JPanel overlay;
    private boolean isServer;
    private volatile boolean isExiting = false; // Biến kiểm soát trạng thái thoát

    public XOGame(Socket socket, boolean isServer, String myName, String opponentName) {
        this.socket = socket;
        this.isServer = isServer;
        this.myName = myName;
        this.opponentName = opponentName;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("NAME:" + myName);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        myMark = isServer ? 'X' : 'O';
        opponentMark = isServer ? 'O' : 'X';
        myTurn = isServer;

        setTitle("Game XO - " + myName + " vs " + opponentName);
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(230, 240, 250));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        lblPlayers = new JLabel();
        updatePlayersLabel();
        lblPlayers.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPlayers.setForeground(Color.WHITE);
        headerPanel.add(lblPlayers, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(230, 240, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(360, 360));
        layeredPane.setMinimumSize(new Dimension(360, 360));
        layeredPane.setMaximumSize(new Dimension(360, 360));

        JPanel board = new JPanel(new GridLayout(3, 3, 8, 8));
        board.setBounds(0, 0, 360, 360);
        board.setBackground(new Color(180, 200, 230));
        layeredPane.add(board, Integer.valueOf(0));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                JButton btn = new JButton("");
                btn.setFont(new Font("Segoe UI", Font.BOLD, 60));
                btn.setBackground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.setBorder(new LineBorder(new Color(120, 140, 180), 2, true));

                final int row = i, col = j;
                btn.addActionListener(e -> handleMove(btn, row, col));

                btn.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        if (btn.getText().equals("")) btn.setBackground(new Color(230, 240, 250));
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        if (btn.getText().equals("")) btn.setBackground(Color.WHITE);
                    }
                });

                buttons[i][j] = btn;
                board.add(btn);
            }
        }

        overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (winRow == -1 && winCol == -1 && !winDiagonal && !winAntiDiagonal) return;
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(5));
                g2.setColor(Color.RED);

                int w = getWidth() / 3;
                int h = getHeight() / 3;

                if (winRow != -1) g2.drawLine(0, winRow * h + h / 2, getWidth(), winRow * h + h / 2);
                else if (winCol != -1) g2.drawLine(winCol * w + w / 2, 0, winCol * w + w / 2, getHeight());
                else if (winDiagonal) g2.drawLine(0, 0, getWidth(), getHeight());
                else if (winAntiDiagonal) g2.drawLine(getWidth(), 0, 0, getHeight());
            }
        };
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, 360, 360);
        layeredPane.add(overlay, Integer.valueOf(1));

        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(layeredPane, gbc);
        add(centerPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(new Color(230, 240, 250));

        JButton btnReset = new JButton("Chơi lại");
        JButton btnExit = new JButton("Thoát");
        styleButton(btnReset, new Color(0, 153, 76));
        styleButton(btnExit, new Color(204, 0, 0));

        btnReset.addActionListener(e -> {
            if (getWinner() != ' ' || isBoardFull()) {
                // Game đã kết thúc, reset ngay mà không cần xác nhận
                resetBoard();
                try {
                    out.writeUTF("RESET:" + myName);
                    out.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Game chưa kết thúc, gửi yêu cầu xác nhận
                try {
                    out.writeUTF("RESET_REQUEST");
                    out.flush();
                    JOptionPane.showMessageDialog(this, "Đã gửi yêu cầu chơi lại đến " + opponentName + ". Đang chờ phản hồi...");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnExit.addActionListener(e -> {
            if (!isExiting) {
                isExiting = true;
                closeGame();
                SwingUtilities.invokeLater(() -> {
                    if (GameMenu.getActiveGameMenu() == null || !GameMenu.getActiveGameMenu().isVisible()) {
                        GameMenu newMenu = new GameMenu(myName);
                        newMenu.setVisible(true);
                        GameMenu.setActiveGameMenu(newMenu);
                    }
                });
            }
        });

        controlPanel.add(btnReset);
        controlPanel.add(btnExit);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);

        new Thread(this::listenOpponent).start();
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void handleMove(JButton btn, int row, int col) {
        if (!myTurn || !btn.getText().equals("") || getWinner() != ' ' || isBoardFull()) return;

        btn.setText(String.valueOf(myMark));
        btn.setForeground(myMark == 'X' ? new Color(220, 20, 60) : new Color(0, 102, 204));

        myTurn = false;
        updatePlayersLabel();

        try {
            out.writeUTF("MOVE:" + row + "," + col);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        checkGameOver();
    }

    private void listenOpponent() {
        try {
            while (!isExiting) {
                String message = in.readUTF();
                System.out.println("Received: " + message);
                
                if (message.startsWith("NAME:")) {
                    opponentName = message.substring(5);
                    setTitle("Game XO - " + myName + " vs " + opponentName);
                    updatePlayersLabel();
                } else if (message.startsWith("MOVE:")) {
                    String[] parts = message.substring(5).split(",");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    
                    SwingUtilities.invokeLater(() -> {
                        JButton btn = buttons[row][col];
                        if (btn.getText().equals("")) {
                            btn.setText(String.valueOf(opponentMark));
                            btn.setForeground(opponentMark == 'X' ? new Color(220, 20, 60) : new Color(0, 102, 204));
                            myTurn = true;
                            updatePlayersLabel();
                            checkGameOver();
                        }
                    });
                } else if (message.equals("RESET_REQUEST")) {
                    SwingUtilities.invokeLater(() -> {
                        int response = JOptionPane.showConfirmDialog(this, 
                            opponentName + " muốn chơi lại. Bạn có đồng ý không?", 
                            "Yêu cầu chơi lại", 
                            JOptionPane.YES_NO_OPTION);
                        try {
                            if (response == JOptionPane.YES_OPTION) {
                                out.writeUTF("RESET_ACCEPT");
                                resetBoard();
                            } else {
                                out.writeUTF("RESET_REJECT");
                            }
                            out.flush();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                } else if (message.equals("RESET_ACCEPT")) {
                    SwingUtilities.invokeLater(() -> {
                        resetBoard();
                        JOptionPane.showMessageDialog(this, opponentName + " đã đồng ý chơi lại!");
                    });
                } else if (message.equals("RESET_REJECT")) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, opponentName + " không đồng ý chơi lại.");
                    });
                } else if (message.equals("EXIT")) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, opponentName + " đã thoát khỏi trò chơi.");
                        closeGame();
                        SwingUtilities.invokeLater(() -> {
                            if (GameMenu.getActiveGameMenu() == null || !GameMenu.getActiveGameMenu().isVisible()) {
                                GameMenu newMenu = new GameMenu(myName);
                                newMenu.setVisible(true);
                                GameMenu.setActiveGameMenu(newMenu);
                            }
                        });
                    });
                } else if (message.startsWith("RESET:")) {
                    SwingUtilities.invokeLater(() -> {
                        resetBoard();
                    });
                }
            }
        } catch (IOException e) {
            if (!isExiting) {
                JOptionPane.showMessageDialog(this, "Kết nối bị mất. Trò chơi kết thúc.");
                closeGame();
                SwingUtilities.invokeLater(() -> {
                    if (GameMenu.getActiveGameMenu() == null || !GameMenu.getActiveGameMenu().isVisible()) {
                        GameMenu newMenu = new GameMenu(myName);
                        newMenu.setVisible(true);
                        GameMenu.setActiveGameMenu(newMenu);
                    }
                });
            }
        }
    }

    private void updatePlayersLabel() {
        lblPlayers.setText("Lượt chơi: " + (myTurn ? myName : opponentName) + " | Tỉ số: " + myName + ": " + myScore + " - " + opponentName + ": " + opponentScore);
        lblPlayers.repaint();
    }

    private void checkGameOver() {
        char winner = getWinner();
        if (winner != ' ') {
            String winnerName = (winner == myMark) ? myName : opponentName;
            String result = (winner == myMark) ? "win" : "loss";
            int winnerScoreIncrement = (winner == myMark) ? 1 : 0;
            int loserScoreIncrement = (winner == myMark) ? 0 : 1;
            if (winner == myMark) {
                myScore += 1;
                opponentScore += 0;
                updateUserStats(myName, 1, 0, 0);
                updateUserStats(opponentName, 0, 1, 0);
            } else {
                myScore += 0;
                opponentScore += 1;
                updateUserStats(myName, 0, 1, 0);
                updateUserStats(opponentName, 1, 0, 0);
            }
            if (isServer) {
                updateMatchHistory(winnerName, (winner == myMark) ? opponentName : myName, result, winnerScoreIncrement, loserScoreIncrement);
            }
            updatePlayersLabel();
            overlay.repaint();
            JOptionPane.showMessageDialog(this, "🎉 " + winnerName + " thắng! Tỉ số: " + myName + ": " + myScore + " - " + opponentName + ": " + opponentScore);
        } else if (isBoardFull()) {
            myScore += 0;
            opponentScore += 0;
            updateUserStats(myName, 0, 0, 1);
            updateUserStats(opponentName, 0, 0, 1);
            // Không ghi lịch sử trận đấu khi hòa
            updatePlayersLabel();
            JOptionPane.showMessageDialog(this, "🤝 Hòa cờ! " + myName + " vs " + opponentName + " | Tỉ số: " + myName + ": " + myScore + " - " + opponentName + ": " + opponentScore);
        }
    }

    private void updateUserStats(String username, int wins, int losses, int draws) {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equals(username)) {
                    int currentWins = Integer.parseInt(parts[2]);
                    int currentLosses = Integer.parseInt(parts[3]);
                    int currentDraws = Integer.parseInt(parts[4]);
                    lines.add(parts[0] + "," + parts[1] + "," + (currentWins + wins) + "," + (currentLosses + losses) + "," + (currentDraws + draws));
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (!found) {
            lines.add(username + ",password," + wins + "," + losses + "," + draws);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.csv"))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMatchHistory(String player1, String player2, String result, int player1ScoreIncrement, int player2ScoreIncrement) {
        List<String> lines = new ArrayList<>();
        boolean found = false;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String matchKey1 = player1 + "," + player2;
        String matchKey2 = player2 + "," + player1;

        try (BufferedReader reader = new BufferedReader(new FileReader("match_history.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String currentKey = parts[0] + "," + parts[1];
                    if (currentKey.equals(matchKey1) || currentKey.equals(matchKey2)) {
                        int currentPlayer1Score = Integer.parseInt(parts[4]);
                        int currentPlayer2Score = Integer.parseInt(parts[5]);
                        int newPlayer1Score = currentPlayer1Score + player1ScoreIncrement;
                        int newPlayer2Score = currentPlayer2Score + player2ScoreIncrement;
                        String newLine = parts[0] + "," + parts[1] + "," + result + "," + timestamp + "," + newPlayer1Score + "," + newPlayer2Score;
                        lines.add(newLine);
                        found = true;
                    } else {
                        lines.add(line);
                    }
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!found) {
            lines.add(player1 + "," + player2 + "," + result + "," + timestamp + "," + player1ScoreIncrement + "," + player2ScoreIncrement);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("match_history.csv"))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private char getWinner() {
        for (int i = 0; i < 3; i++) {
            if (!buttons[i][0].getText().equals("") &&
                buttons[i][0].getText().equals(buttons[i][1].getText()) &&
                buttons[i][1].getText().equals(buttons[i][2].getText())) {
                winRow = i;
                winCol = -1;
                winDiagonal = false;
                winAntiDiagonal = false;
                return buttons[i][0].getText().charAt(0);
            }
        }
        for (int i = 0; i < 3; i++) {
            if (!buttons[0][i].getText().equals("") &&
                buttons[0][i].getText().equals(buttons[1][i].getText()) &&
                buttons[1][i].getText().equals(buttons[2][i].getText())) {
                winCol = i;
                winRow = -1;
                winDiagonal = false;
                winAntiDiagonal = false;
                return buttons[0][i].getText().charAt(0);
            }
        }
        if (!buttons[0][0].getText().equals("") &&
            buttons[0][0].getText().equals(buttons[1][1].getText()) &&
            buttons[1][1].getText().equals(buttons[2][2].getText())) {
            winDiagonal = true;
            winRow = winCol = -1;
            winAntiDiagonal = false;
            return buttons[0][0].getText().charAt(0);
        }
        if (!buttons[0][2].getText().equals("") &&
            buttons[0][2].getText().equals(buttons[1][1].getText()) &&
            buttons[1][1].getText().equals(buttons[2][0].getText())) {
            winAntiDiagonal = true;
            winRow = winCol = -1;
            winDiagonal = false;
            return buttons[0][2].getText().charAt(0);
        }
        return ' ';
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (buttons[i][j].getText().equals("")) return false;
        return true;
    }

    private void resetBoard() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setBackground(Color.WHITE);
                buttons[i][j].setForeground(Color.BLACK);
            }
        myTurn = (myMark == 'X');
        winRow = -1;
        winCol = -1;
        winDiagonal = false;
        winAntiDiagonal = false;
        overlay.repaint();
        updatePlayersLabel();
    }

    private void closeGame() {
        try {
            if (socket != null && !socket.isClosed()) {
                out.writeUTF("EXIT");
                out.flush();
                socket.close();
            }
        } catch (IOException e) {
            // Không cần xử lý lỗi socket vì đang thoát
        }
        dispose();
    }
}