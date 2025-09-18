<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   GAME TIC TAC TOE (CARO 3x3) MULTIPLAYER
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="FIT DNU Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>


## 1. Giới thiệu hệ thống

Hệ thống Game Tic Tac Toe (Caro 3x3) được phát triển theo mô hình Client-Server, hỗ trợ chơi multiplayer trực tuyến qua mạng LAN. Người chơi có thể đăng nhập/đăng ký tài khoản, chọn chế độ tạo phòng (làm server) hoặc tham gia phòng (làm client), và chơi game với đối thủ. Game sử dụng bàn cờ 3x3 với luật chơi chuẩn: X/O luân phiên, thắng khi có 3 ký tự thẳng hàng, hỗ trợ đầu hàng, chơi lại, và thoát game.

**Chức năng chính**
- Đăng nhập/Đăng ký tài khoản (lưu trữ trong file `users.txt` với định dạng username:password).
- Menu chính: Chọn tên hiển thị, tạo phòng chờ (server) hoặc tham gia phòng bằng IP/Port.
- Ghép cặp người chơi: Server chờ client kết nối, sau đó bắt đầu game.
- Chơi Tic Tac Toe: Hiển thị lượt chơi, điểm số, vẽ đường thắng, xử lý thắng/thua/hòa.
- Xử lý sự cố: Nếu một bên thoát, bên kia thắng; hỗ trợ chơi lại hoặc đầu hàng.
- Giao diện đồ họa thân thiện với hiệu ứng hover, gradient background.

Dự án tập trung vào lập trình mạng (socket), giao diện Swing, và quản lý trạng thái game.

## 2. Công nghệ sử dụng
- Ngôn ngữ lập trình: Java (JDK 8+).
- Giao diện người dùng: Java Swing (JFrame, JButton, JLabel, JPanel, JPasswordField).
- Truyền thông mạng: TCP Socket (ServerSocket cho server, Socket cho client).
- Lưu trữ dữ liệu: File text (`users.txt` cho tài khoản người dùng).
- Kiến trúc:
    - Client: `LoginFrame.java` (đăng nhập), `RegisterFrame.java` (đăng ký), `GameMenu.java` (menu chính), `Client.java` (kết nối server), `XOGame.java` (giao diện game).
    - Server: `Server.java` (khởi tạo server và phòng chờ), `XOGame.java` (quản lý game phía server).
    - Các tính năng phụ: Thread cho lắng nghe đối thủ, DataInputStream/DataOutputStream cho trao đổi dữ liệu (move, name, reset, surrender).

## 3. Hình ảnh các chức năng
- Màn hình đăng nhập/đăng ký
    - Nhập username/password, kiểm tra regex (ít nhất 3-20 ký tự, mật khẩu có chữ hoa/thường/số/ký tự đặc biệt).
      
  <p align="center">
  <img src="docs/login_screen.png" alt="Màn hình đăng nhập" width="500"/>
</p>
<p align="center">
  <em> Hình 1: Màn hình đăng nhập (LoginFrame) </em>
</p>

- Màn hình menu chính
    - Chọn tên hiển thị, nút Tạo Phòng (server) hoặc Tham Gia Phòng (client với IP/Port).
      
<p align="center">
  <img src="docs/menu_screen.png" alt="Màn hình menu" width="500"/>
</p>
<p align="center">
  <em> Hình 2: Menu chính (GameMenu) </em>
</p>

- Màn hình phòng chờ (khi tạo phòng)
    - Hiển thị thông tin IP/Port để chia sẻ, chờ client kết nối.
      
<p align="center">
  <img src="docs/waiting_room.png" alt="Phòng chờ" width="500"/>
</p>
<p align="center">
  <em> Hình 3: Phòng chờ khi tạo server (WaitingRoomFrame) </em>
</p>

- Màn hình chơi game
    - Bàn cờ 3x3, hiển thị tên người chơi, lượt đi, điểm số, nút chơi lại/đầu hàng/thoát.
      
<p align="center">
  <img src="docs/game_screen.png" alt="Màn hình game" width="500"/>
</p>
<p align="center">
  <em> Hình 4: Giao diện chơi game (XOGame) với đường thắng </em>
</p>

- Thông báo kết quả
    - Popup hiển thị thắng/thua/hòa, cập nhật điểm số.
      
<p align="center">
  <img src="docs/result_popup.png" alt="Kết quả" width="500"/>
</p>
<p align="center">
  <em> Hình 5: Popup kết quả trận đấu </em>
</p>

- File lưu trữ 
    - `users.txt`: Lưu username:password (ví dụ: admin:Password123!).
      
<p align="center">
  <img src="docs/users_file.png" alt="File users" width="500"/>
</p>
<p align="center">
  <em> Hình 6: File lưu trữ tài khoản người dùng </em>
</p>

## 4. Cài đặt & chạy chương trình
- Bước 1: Chuẩn bị môi trường
    - Cài đặt Java JDK 8+.
    - Kiểm tra bằng lệnh: `java -version`.
- Bước 2: Biên dịch chương trình
    - Mở terminal ở thư mục gốc dự án.
    - Biên dịch: `javac caro/*.java` (hoặc sử dụng IDE như Eclipse/IntelliJ để build).
- Bước 3: Khởi chạy chương trình
    - Chạy login frame (làm client hoặc server): `java caro.LoginFrame`.
    - Đăng nhập/đăng ký tài khoản.
    - Vào menu: Chọn "Tạo Phòng" để làm server (port mặc định 12345, chia sẻ IP cho người khác).
    - Hoặc "Tham Gia Phòng": Nhập IP/Port của server để kết nối.
- Bước 4: Chơi game
    - Hai người chơi kết nối sẽ bắt đầu game tự động.
    - Luân phiên click vào ô bàn cờ để đánh X/O.
    - Khi kết thúc, kết quả hiển thị; có thể chơi lại hoặc thoát.
- Lưu ý: 
    - Chạy trên cùng mạng LAN để kết nối IP.
    - Nếu lỗi kết nối, kiểm tra firewall hoặc port 12345.
    - Không có lưu lịch sử trận đấu (chỉ lưu user); có thể mở rộng thêm.

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.

---
