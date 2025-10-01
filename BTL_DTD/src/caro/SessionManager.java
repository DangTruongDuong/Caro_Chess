package caro;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class SessionManager {
    private static SessionManager instance;
    private Set<String> activeSessions;
    private static final String SESSION_FILE = "active_sessions.txt";
    private static final String LOCK_FILE = "session_lock.txt";
    
    private SessionManager() {
        activeSessions = new HashSet<>();
        loadSessions();
        createLockFile();
        addShutdownHook();
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    private void loadSessions() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SESSION_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String username = line.trim();
                if (!username.isEmpty()) {
                    activeSessions.add(username);
                }
            }
        } catch (IOException e) {
            // File không tồn tại hoặc lỗi đọc - tạo file mới
            saveSessions();
        }
    }
    
    private void saveSessions() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SESSION_FILE))) {
            for (String username : activeSessions) {
                writer.write(username);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isUserLoggedIn(String username) {
        // Reload sessions từ file để đảm bảo dữ liệu mới nhất
        loadSessions();
        return activeSessions.contains(username);
    }
    
    public boolean loginUser(String username) {
        if (isUserLoggedIn(username)) {
            return false; // User already logged in
        }
        activeSessions.add(username);
        saveSessions();
        return true;
    }
    
    public void logoutUser(String username) {
        activeSessions.remove(username);
        saveSessions();
    }
    
    public int getActiveSessionCount() {
        loadSessions();
        return activeSessions.size();
    }
    
    private void createLockFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOCK_FILE))) {
            writer.write("LOCK");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Xóa lock file khi chương trình thoát
                File lockFile = new File(LOCK_FILE);
                if (lockFile.exists()) {
                    lockFile.delete();
                }
                
                // Nếu không còn lock file nào khác, xóa session file
                if (!isAnyInstanceRunning()) {
                    File sessionFile = new File(SESSION_FILE);
                    if (sessionFile.exists()) {
                        sessionFile.delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
    
    private boolean isAnyInstanceRunning() {
        // Kiểm tra xem có instance nào khác đang chạy không
        // Bằng cách kiểm tra lock file
        File lockFile = new File(LOCK_FILE);
        return lockFile.exists();
    }
    
    public void cleanupOnStartup() {
        // Kiểm tra khi khởi động - nếu không có lock file, có nghĩa là tất cả instance đã thoát
        if (!isAnyInstanceRunning()) {
            File sessionFile = new File(SESSION_FILE);
            if (sessionFile.exists()) {
                sessionFile.delete();
            }
        }
    }
}
