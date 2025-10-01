package caro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame() {
        // Cleanup sessions khi khởi động
        SessionManager.getInstance().cleanupOnStartup();
        
        setTitle("Game XO - Đăng Nhập");
        setSize(550, 530);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // Background trắng với gradient nhẹ
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền trắng
                g2d.setColor(new Color(255, 255, 255));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Thêm một chút gradient nhẹ từ trắng sang trắng kem
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255), 
                                                   0, getHeight(), new Color(250, 250, 252));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));
        setContentPane(mainPanel);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP GAME XO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(51, 51, 51)); // Màu xám đậm
        headerPanel.add(lblTitle);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));

        // Username
        JPanel usernamePanel = createInputPanel("Tên người dùng:");
        txtUsername = createStyledTextField();
        usernamePanel.add(txtUsername, BorderLayout.CENTER);

        // Password
        JPanel passwordPanel = createInputPanel("Mật khẩu:");
        txtPassword = createStyledPasswordField();
        passwordPanel.add(txtPassword, BorderLayout.CENTER);

        inputPanel.add(usernamePanel);
        inputPanel.add(Box.createVerticalStrut(25));
        inputPanel.add(passwordPanel);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JButton btnLogin = createStyledButton("Đăng nhập", new Color(52, 152, 219), new Color(41, 128, 185));
        JButton btnRegister = createStyledButton("Đăng ký", new Color(46, 204, 113), new Color(39, 174, 96));

        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> {
            setVisible(false);
            new RegisterFrame(this);
        });

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createInputPanel(String labelText) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(85, 85, 85)); // Màu xám trung bình
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 3, 0));
        
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        // Màu nền trắng kem
        textField.setBackground(new Color(248, 249, 250));
        // Border mềm mại
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.setBackground(new Color(255, 255, 255));
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                textField.setBackground(new Color(248, 249, 250));
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ));
            }
        });
        
        return textField;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setBackground(new Color(248, 249, 250));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setBackground(new Color(255, 255, 255));
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                passwordField.setBackground(new Color(248, 249, 250));
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1, true),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ));
            }
        });
        
        return passwordField;
    }

    // Phương pháp sửa lỗi hoàn toàn - tạo button với rounded corners
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        // Tạo button với paintComponent tùy chỉnh từ đầu
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ background với góc bo tròn
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Vẽ border
                g2.setColor(getForeground().equals(Color.WHITE) ? 
                    new Color(bgColor.getRed() - 20, bgColor.getGreen() - 20, bgColor.getBlue() - 20) : 
                    new Color(206, 212, 218));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        // Cấu hình button
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Tạo final variables để sử dụng trong MouseAdapter
        final Color finalBgColor = bgColor;
        final Color finalHoverColor = hoverColor;
        final JButton finalBtn = btn;
        
        finalBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                finalBtn.setBackground(finalHoverColor);
                finalBtn.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent evt) {
                finalBtn.setBackground(finalBgColor);
                finalBtn.repaint();
            }
        });
        
        return finalBtn;
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorDialog("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!Pattern.matches("^[a-zA-Z0-9_]{3,20}$", username)) {
            showErrorDialog("Username không hợp lệ: 3-20 ký tự, chỉ chữ cái, số, underscore!");
            return;
        }

        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    found = true;
                    break;
                }
            }
        } catch (IOException e) {
            showErrorDialog("Lỗi khi đọc file users.csv!");
            e.printStackTrace();
            return;
        }

        if (found) {
            // Kiểm tra xem user đã đăng nhập chưa
            if (SessionManager.getInstance().isUserLoggedIn(username)) {
                showErrorDialog("Tài khoản này đang được sử dụng ở nơi khác!\nVui lòng đăng nhập bằng tài khoản khác hoặc đợi phiên đăng nhập kết thúc.");
                return;
            }
            
            // Đăng nhập thành công
            SessionManager.getInstance().loginUser(username);
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!", "Thành Công", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
            SwingUtilities.invokeLater(() -> new GameMenu(username).setVisible(true));
        } else {
            showErrorDialog("Tên người dùng hoặc mật khẩu không đúng!");
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}