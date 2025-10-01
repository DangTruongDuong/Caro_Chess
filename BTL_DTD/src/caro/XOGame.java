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
    private String myName, opponentName = "Đang kết nối...";
	private JLabel lblPlayers;
	private JLabel lblPlayer1;
	private JLabel lblPlayer2;
	private JLabel lblTurn;
    private int winRow = -1, winCol = -1;
    private boolean winDiagonal = false;
    private boolean winAntiDiagonal = false;
    private int myScore = 0;
    private int opponentScore = 0;
    private JPanel overlay;
    private volatile boolean isExiting = false;
    private boolean waitingForReset = false;
    private boolean roundOver = false;
    private boolean gameFinished = false;
    private boolean inResetDialog = false;
    private boolean waitingForExitResponse = false;
    private boolean nameSent = false;
    
    private JButton btnReset;
    private JButton btnExit;

    public XOGame(Socket socket, boolean isServer, String myName, String initialOpponentName) {
        this.socket = socket;
        this.myName = myName;
        this.opponentName = initialOpponentName;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            
            if (!nameSent) {
                out.writeUTF("NAME:" + myName);
                out.flush();
                nameSent = true;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        myMark = isServer ? 'X' : 'O';
        opponentMark = isServer ? 'O' : 'X';
        myTurn = isServer;

        setTitle("Game XO - " + myName + " vs " + opponentName);
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(230, 240, 250));

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(new Color(245, 245, 245));
		headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

		// Scoreboard panel with horizontal layout
		JPanel scoreboardPanel = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				// Gradient background
				GradientPaint gradient = new GradientPaint(
					0, 0, new Color(74, 144, 226), 
					getWidth(), 0, new Color(80, 200, 120)
				);
				g2d.setPaint(gradient);
				g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
				
				// Subtle inner shadow
				g2d.setColor(new Color(0, 0, 0, 20));
				g2d.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 10, 10);
				
				g2d.dispose();
			}
		};
		scoreboardPanel.setOpaque(false);
		scoreboardPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

		// Player 1 (left)
		JLabel lblPlayer1 = new JLabel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				// Text shadow
				g2d.setColor(new Color(0, 0, 0, 30));
				g2d.drawString(getText(), 1, getHeight() - 3);
				
				// Main text
				g2d.setColor(getForeground());
				g2d.drawString(getText(), 0, getHeight() - 4);
				
				g2d.dispose();
			}
		};
		lblPlayer1.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lblPlayer1.setForeground(Color.WHITE);
		lblPlayer1.setHorizontalAlignment(SwingConstants.LEFT);
		scoreboardPanel.add(lblPlayer1, BorderLayout.WEST);

		// VS (center)
		JLabel lblVS = new JLabel("VS") {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				// Glow effect
				g2d.setColor(new Color(255, 255, 255, 100));
				g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
				FontMetrics fm = g2d.getFontMetrics();
				int x = (getWidth() - fm.stringWidth("VS")) / 2;
				int y = (getHeight() + fm.getAscent()) / 2;
				g2d.drawString("VS", x, y);
				
				// Main text
				g2d.setColor(Color.WHITE);
				g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
				fm = g2d.getFontMetrics();
				x = (getWidth() - fm.stringWidth("VS")) / 2;
				y = (getHeight() + fm.getAscent()) / 2;
				g2d.drawString("VS", x, y);
				
				g2d.dispose();
			}
		};
		lblVS.setHorizontalAlignment(SwingConstants.CENTER);
		scoreboardPanel.add(lblVS, BorderLayout.CENTER);

		// Player 2 and turn info (right)
		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
		rightPanel.setOpaque(false);
		
		JLabel lblPlayer2 = new JLabel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				// Text shadow
				g2d.setColor(new Color(0, 0, 0, 30));
				g2d.drawString(getText(), 1, getHeight() - 3);
				
				// Main text
				g2d.setColor(getForeground());
				g2d.drawString(getText(), 0, getHeight() - 4);
				
				g2d.dispose();
			}
		};
		lblPlayer2.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lblPlayer2.setForeground(Color.WHITE);
		lblPlayer2.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JLabel lblTurn = new JLabel() {
			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				// Glowing background for turn info
				g2d.setColor(new Color(255, 215, 0, 80));
				g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
				
				// Text shadow
				g2d.setColor(new Color(0, 0, 0, 40));
				g2d.drawString(getText(), 1, getHeight() - 3);
				
				// Main text
				g2d.setColor(getForeground());
				g2d.drawString(getText(), 0, getHeight() - 4);
				
				g2d.dispose();
			}
		};
		lblTurn.setFont(new Font("Segoe UI", Font.BOLD, 12));
		lblTurn.setForeground(new Color(255, 255, 255));
		lblTurn.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTurn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		
		rightPanel.add(lblPlayer2);
		rightPanel.add(lblTurn);
		scoreboardPanel.add(rightPanel, BorderLayout.EAST);

		// Store references for updates
		this.lblPlayer1 = lblPlayer1;
		this.lblPlayer2 = lblPlayer2;
		this.lblTurn = lblTurn;

		headerPanel.add(scoreboardPanel, BorderLayout.CENTER);

		// Message line (bottom) - hidden since we're using horizontal layout
		lblPlayers = new JLabel("");
		lblPlayers.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblPlayers.setForeground(new Color(80, 80, 80));
		lblPlayers.setHorizontalAlignment(SwingConstants.CENTER);
		lblPlayers.setVisible(false); // Hide this label

		updatePlayersLabel();

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
                        if (btn.getText().equals("") && !roundOver && !gameFinished && !waitingForReset && !waitingForExitResponse && !inResetDialog) {
                            btn.setBackground(new Color(230, 240, 250));
                        }
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        if (btn.getText().equals("") && !roundOver && !gameFinished && !waitingForReset && !waitingForExitResponse && !inResetDialog) {
                            btn.setBackground(Color.WHITE);
                        }
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

        btnReset = new JButton("Chơi lại");
        btnExit = new JButton("Thoát");
        styleButton(btnReset, new Color(0, 153, 76));
        styleButton(btnExit, new Color(204, 0, 0));

        btnReset.addActionListener(e -> {
            // Sửa: Cho phép gửi yêu cầu chơi lại bất kỳ lúc nào, trừ khi game đã kết thúc hoặc đang chờ thoát
            if (gameFinished || waitingForExitResponse) {
                return;
            }
            
            if (waitingForReset || inResetDialog) {
                int resendChoice = JOptionPane.showConfirmDialog(this, 
                    "Đang chờ phản hồi. Bạn muốn gửi lại yêu cầu chơi lại?", 
                    "Gửi lại yêu cầu", 
                    JOptionPane.YES_NO_OPTION);
                if (resendChoice != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            waitingForReset = true;
            inResetDialog = true;
            updateButtonStates();
            
            try {
                out.writeUTF("RESET_REQUEST:" + myName);
                out.flush();
                lblPlayers.repaint();
            } catch (IOException ex) {
                ex.printStackTrace();
                waitingForReset = false;
                inResetDialog = false;
                updateButtonStates();
                updatePlayersLabel();
            }
        });

        btnExit.addActionListener(e -> {
            if (!isExiting && !waitingForExitResponse && !inResetDialog) {
                int choice = JOptionPane.showOptionDialog(
                    this,
                    "Bạn có chắc muốn thoát không?\nNếu thoát bạn sẽ bị tính 1 lượt thua!",
                    "Xác nhận thoát",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[]{"OK (Thoát)", "Hủy (Ở lại)"},
                    "Hủy (Ở lại)"
                );
                
                if (choice == JOptionPane.YES_OPTION) {
                    waitingForExitResponse = true;
                    updateButtonStates();
                    try {
                        out.writeUTF("EXIT_REQUEST:" + myName);
                        out.flush();
                        lblPlayers.setText("Đang chờ xác nhận thoát...");
                        lblPlayers.repaint();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        waitingForExitResponse = false;
                        updateButtonStates();
                        updatePlayersLabel();
                    }
                }
            }
        });

        controlPanel.add(btnReset);
        controlPanel.add(btnExit);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);

        listenForMessages();
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 2, true),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 50, 50), 3, true),
                    BorderFactory.createEmptyBorder(10, 25, 10, 25)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 100), 2, true),
                    BorderFactory.createEmptyBorder(10, 25, 10, 25)
                ));
            }
        });
    }

    private void updatePlayersLabel() {
        String activePlayer = myTurn ? myName : opponentName;
        
        // Update individual labels
        lblPlayer1.setText(myName + " (" + myScore + ")");
        lblPlayer2.setText(opponentName + " (" + opponentScore + ")");
        lblTurn.setText("| Lượt chơi của: " + activePlayer);
        
        // Repaint all labels
        lblPlayer1.repaint();
        lblPlayer2.repaint();
        lblTurn.repaint();
    }

    private void updateButtonStates() {
        btnReset.setEnabled(!gameFinished && !waitingForExitResponse);
        btnExit.setEnabled(!gameFinished && !waitingForExitResponse && !inResetDialog);
    }

    private void handleMove(JButton btn, int row, int col) {
        if (!myTurn || !btn.getText().equals("") || roundOver || gameFinished || waitingForReset || waitingForExitResponse || inResetDialog) return;
        btn.setText(String.valueOf(myMark));
        btn.setForeground(myMark == 'X' ? Color.BLUE : Color.RED);
        myTurn = false;
        updatePlayersLabel();
        try {
            out.writeUTF("MOVE:" + row + "," + col); // gửi nước điđi
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkGameOver();
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                String message;// nhận dữ liệu
                while ((message = in.readUTF()) != null) {
                    if (message.startsWith("MOVE:")) {// nhận nước đi
                        String[] parts = message.substring(5).split(",");
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);
                        buttons[row][col].setText(String.valueOf(opponentMark));
                        buttons[row][col].setForeground(opponentMark == 'X' ? Color.BLUE : Color.RED);
                        myTurn = true;
                        updatePlayersLabel();
                        checkGameOver();
                    } else if (message.startsWith("NAME:")) {
                        opponentName = message.substring(5).trim();
                        setTitle("Game XO - " + myName  + " vs " +  opponentName);
                        updatePlayersLabel();
                    } else if (message.startsWith("RESET_REQUEST:")) {
                        String requester = message.substring(14).trim();
                        inResetDialog = true;
                        updateButtonStates();
                        int choice = JOptionPane.showOptionDialog(
                            this,
                            requester + " muốn chơi lại. Bạn đồng ý không?",
                            "Yêu cầu chơi lại",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"Đồng ý (Chơi lại)", "Từ chối (Tiếp tục)"},
                            "Từ chối (Tiếp tục)"
                        );
                        inResetDialog = false;
                        updateButtonStates();
                        try {
                            if (choice == JOptionPane.YES_OPTION) {
                                out.writeUTF("RESET_ACCEPT:" + myName);
                                out.flush();
                                resetBoard();
                            } else {
                                out.writeUTF("RESET_DECLINE:" + myName);
                                out.flush();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else if (message.startsWith("RESET_ACCEPT:")) {
                        String accepter = message.substring(13).trim();
                        waitingForReset = false;
                        inResetDialog = false;
                        updateButtonStates();
                        JOptionPane.showMessageDialog(this, accepter + " đã đồng ý chơi lại. Bắt đầu round mới!");
                        resetBoard();
                    } else if (message.startsWith("RESET_DECLINE:")) {
                        String decliner = message.substring(14).trim();
                        waitingForReset = false;
                        inResetDialog = false;
                        updateButtonStates();
                        updatePlayersLabel();
                        JOptionPane.showMessageDialog(this, decliner + " không đồng ý chơi lại. Hãy tiếp tục game!");
                    } else if (message.startsWith("EXIT_REQUEST:")) {
                        String requester = message.substring(13).trim();
                        inResetDialog = true;
                        updateButtonStates();
                        int choice = JOptionPane.showOptionDialog(
                            this,
                            requester + " muốn thoát game. Bạn đồng ý không?\nNếu đồng ý, cả hai thoát mà không tính thua.",
                            "Yêu cầu thoát",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"Đồng ý (Thoát)", "Từ chối (Tiếp tục)"},
                            "Từ chối (Tiếp tục)"
                        );
                        inResetDialog = false;
                        updateButtonStates();
                        try {
                            if (choice == JOptionPane.YES_OPTION) {
                                out.writeUTF("EXIT_ACCEPT:" + myName);
                                out.flush();
                                JOptionPane.showMessageDialog(this, "Bạn đã đồng ý cho " + requester + " thoát. Game kết thúc mà không tính thua.");
                                closeGame();
                                returnToMenu();
                            } else {
                                out.writeUTF("EXIT_DECLINE:" + myName);
                                out.flush();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else if (message.startsWith("EXIT_ACCEPT:")) {
                        String accepter = message.substring(12).trim();
                        waitingForExitResponse = false;
                        updateButtonStates();
                        JOptionPane.showMessageDialog(this, accepter + " đã đồng ý cho bạn thoát. Game kết thúc mà không tính thua.");
                        closeGame();
                        returnToMenu();
                    } else if (message.startsWith("EXIT_DECLINE:")) {
                        String decliner = message.substring(13).trim();
                        waitingForExitResponse = false;
                        updateButtonStates();
                        updatePlayersLabel();
                        JOptionPane.showMessageDialog(this, decliner + " không đồng ý cho bạn thoát.");
                        int forceChoice = JOptionPane.showConfirmDialog(
                            this,
                            decliner + " không đồng ý. Bạn vẫn muốn thoát? Nếu có, bạn sẽ bị tính thua.",
                            "Xác nhận thoát",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                        );
                        if (forceChoice == JOptionPane.YES_OPTION) {
                            try {
                                out.writeUTF("FORCE_EXIT");
                                out.flush();
                                myScore += 0;
                                opponentScore += 1;
                                updateUserStats(myName, 0, 1, 0);
                                updateUserStats(opponentName, 1, 0, 0);
                                updateMatchHistory(opponentName, myName, "win", 1, 0);
                                JOptionPane.showMessageDialog(this, "Bạn đã thoát và bị tính thua. Game kết thúc.");
                                closeGame();
                                returnToMenu();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    } else if (message.equals("FORCE_EXIT")) {
                        myScore += 1;
                        opponentScore += 0;
                        updateUserStats(myName, 1, 0, 0);
                        updateUserStats(opponentName, 0, 1, 0);
                        updateMatchHistory(myName, opponentName, "win", 1, 0);
                        JOptionPane.showMessageDialog(this, opponentName + " đã thoát dù bạn không đồng ý. Bạn thắng round này!");
                        closeGame();
                        returnToMenu();
                    }
                }
            } catch (IOException e) {
                if (!isExiting) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Kết nối bị mất! Game kết thúc.", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                        closeGame();
                        returnToMenu();
                    });
                }
            }
        }).start();
    }

    private void checkGameOver() {
        char winner = getWinner();
        if (winner != ' ') {
            roundOver = true;
            String winnerName = (winner == myMark) ? myName : opponentName;
            if (winner == myMark) {
                myScore += 1;
                opponentScore += 0;
                updateUserStats(myName, 1, 0, 0);
                updateUserStats(opponentName, 0, 1, 0);
                updateMatchHistory(winnerName, opponentName, "win", 1, 0);
            } else {
                myScore += 0;
                opponentScore += 1;
                updateUserStats(myName, 0, 1, 0);
                updateUserStats(opponentName, 1, 0, 0);
                updateMatchHistory(winnerName, myName, "win", 1, 0);
            }
            updatePlayersLabel();
            overlay.repaint();
            JOptionPane.showMessageDialog(this, "🎉 " + winnerName + " thắng round này! Tỉ số: " + myName + ": " + myScore + " - " + opponentName + ": " + opponentScore);
            updateButtonStates();
        } else if (isBoardFull()) {
            roundOver = true;
            myScore += 0;
            opponentScore += 0;
            updateUserStats(myName, 0, 0, 1);
            updateUserStats(opponentName, 0, 0, 1);
            updateMatchHistory(myName, opponentName, "draw", 0, 0);
            updatePlayersLabel();
            JOptionPane.showMessageDialog(this, "🤝 Hòa round này! " + myName + " vs " + opponentName + " | Tỉ số: " + myName + ": " + myScore + " - " + opponentName + ": " + opponentScore);
            updateButtonStates();
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

    private void updateMatchHistory(String playerA, String playerB, String result, int scoreA, int scoreB) {
        // Chuẩn hóa thứ tự: actualP1 luôn là tên nhỏ hơn theo alphabet
        String actualP1;
        String actualP2;
        String winnerField;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (result.equals("draw")) {
            actualP1 = playerA.compareTo(playerB) < 0 ? playerA : playerB;
            actualP2 = playerA.compareTo(playerB) < 0 ? playerB : playerA;
            winnerField = "draw";
        } else {
            // result = "win" (playerA là người thắng, playerB là người thua)
            actualP1 = playerA.compareTo(playerB) < 0 ? playerA : playerB;
            actualP2 = playerA.compareTo(playerB) < 0 ? playerB : playerA;
            // Nếu winner là actualP1 thì ghi "p1", ngược lại "p2"
            winnerField = actualP1.equals(playerA) ? "p1" : "p2";
        }

        // Chống ghi trùng 2 lần: chỉ client có tên bằng actualP1 mới ghi lịch sử
        if (!myName.equals(actualP1)) {
            return;
        }

        String newLine = actualP1 + "," + actualP2 + "," + winnerField + "," + timestamp;

        // Ghi nối (append) để tránh ghi đè lịch sử; đảm bảo tạo file nếu chưa tồn tại
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("match_history.csv", true))) {
            writer.write(newLine);
            writer.newLine();
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
                buttons[i][j].setEnabled(true);
            }
        myTurn = (myMark == 'X');
        winRow = -1;
        winCol = -1;
        winDiagonal = false;
        winAntiDiagonal = false;
        roundOver = false;
        waitingForReset = false;
        inResetDialog = false;
        waitingForExitResponse = false;
        overlay.repaint();
        updatePlayersLabel();
        updateButtonStates();
    }

    private void closeGame() {
        isExiting = true;
        gameFinished = true;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Không cần xử lý lỗi socket vì đang thoát
        }
        dispose();
    }

    private void returnToMenu() {
        SwingUtilities.invokeLater(() -> {
            if (GameMenu.getActiveGameMenu() == null || !GameMenu.getActiveGameMenu().isVisible()) {
                GameMenu newMenu = new GameMenu(myName);
                newMenu.setVisible(true);
                GameMenu.setActiveGameMenu(newMenu);
            }
        });
    }
}