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

Hệ thống Game Tic Tac Toe (Caro 3x3) được phát triển theo mô hình Client-Server, hỗ trợ chơi multiplayer trực tuyến qua mạng LAN. Người chơi có thể đăng nhập/đăng ký tài khoản, chọn chế độ tạo phòng (làm server) hoặc tham gia phòng (làm client), chơi game với đối thủ, và xem lịch sử đấu cũng như thống kê thắng/thua/hòa. Game sử dụng bàn cờ 3x3 với luật chơi chuẩn: X/O luân phiên, thắng khi có 3 ký tự thẳng hàng (ngang/dọc/chéo), hỗ trợ đầu hàng, chơi lại, và thoát game. Kết quả trận đấu được lưu trữ và cập nhật thống kê người dùng.

**Chức năng chính**
- Đăng nhập/Đăng ký tài khoản (lưu trữ trong file `users.csv` với định dạng username,password,wins,losses,draws).
- Menu chính: Chọn tên hiển thị, tạo phòng chờ (server) hoặc tham gia phòng bằng IP/Port, xem lịch sử đấu.
- Ghép cặp người chơi: Server chờ client kết nối, trao đổi tên hiển thị, sau đó bắt đầu game.
- Chơi Tic Tac Toe: Hiển thị lượt chơi, điểm số, vẽ đường thắng, xử lý thắng/thua/hòa, đầu hàng, chơi lại.
- Xử lý sự cố: Nếu một bên thoát, bên kia thắng; hỗ trợ chơi lại hoặc đầu hàng.
- Lịch sử đấu: Lưu kết quả trận đấu trong `match_history.csv` (player1,player2,result,timestamp,player1Score,player2Score), hiển thị thống kê thắng/thua/hòa và bảng lịch sử.
- Giao diện đồ họa thân thiện với hiệu ứng hover, gradient background, và cập nhật realtime.

Dự án tập trung vào lập trình mạng (socket), giao diện Swing, quản lý trạng thái game, và lưu trữ dữ liệu CSV.

## 2. Công nghệ sử dụng
- Ngôn ngữ lập trình: Java (JDK 8+).
- Giao diện người dùng: Java Swing (JFrame, JButton, JLabel, JPanel, JPasswordField, JTable cho lịch sử).
- Truyền thông mạng: TCP Socket (ServerSocket cho server, Socket cho client).
- Lưu trữ dữ liệu: File CSV (`users.csv` cho tài khoản và thống kê, `match_history.csv` cho lịch sử trận đấu).
- Kiến trúc:
    - Client: `LoginFrame.java` (đăng nhập), `RegisterFrame.java` (đăng ký), `GameMenu.java` (menu chính với WaitingRoomFrame và HistoryFrame), `Client.java` (kết nối server), `XOGame.java` (giao diện game).
    - Server: `Server.java` (khởi tạo server và phòng chờ), `XOGame.java` (quản lý game phía server).
    - Các tính năng phụ: Thread cho lắng nghe đối thủ, DataInputStream/DataOutputStream cho trao đổi dữ liệu (move, name, reset, surrender, exit); BufferedReader/Writer cho xử lý CSV.

## 3. Hình ảnh các chức năng
- Màn hình đăng nhập/đăng ký
    - Nhập username/password, kiểm tra regex (username: 3-20 ký tự chữ cái/số/underscore; password: ít nhất 6 ký tự với chữ hoa/thường/số/ký tự đặc biệt).

<p align="center">
  <img src="assets/dangnhap.png" alt="Màn hình đăng nhập" width="400"/>
</p>
<p align="center">
  <em> Hình 1: Màn hình đăng nhập (LoginFrame) </em>
</p>

<p align="center">
  <img src="assets/dangky.png" alt="Màn hình đăng ký" width="400"/>
</p>
<p align="center">
  <em> Hình 1.1: Màn hình đăng ký (RegisterFrame) </em>
</p>

- Màn hình menu chính
    - Chọn tên hiển thị, nút Tạo Phòng (server), Tham Gia Phòng (client với IP/Port), Xem Lịch Sử Đấu.

<p align="center">
  <img src="assets/menu.png" alt="Màn hình menu" width="400"/>
</p>
<p align="center">
  <em> Hình 2: Menu chính (GameMenu) </em>
</p>

- Màn hình phòng chờ (khi tạo phòng)
    - Hiển thị thông tin IP/Port để chia sẻ, chờ client kết nối.

<p align="center">
  <img src="assets/chuanbitaophong.png" alt="Phòng chờ" width="400"/>
</p>
<p align="center">
  <img src="assets/chotaophong.png" alt="Phòng chờ" width="400"/>
</p>
<p align="center">
  <em> Hình 3: Phòng chờ khi tạo server (WaitingRoomFrame) </em>
</p>

- Màn hình chơi game
    - Bàn cờ 3x3, hiển thị tên người chơi, lượt đi, điểm số, nút chơi lại/đầu hàng/thoát.

<p align="center">
  <img src="assets/vogame.png" alt="Màn hình game" width="500"/>
</p>
<p align="center">
  <em> Hình 4: Giao diện chơi game (XOGame) với đường thắng </em>
</p>

- Màn hình lịch sử đấu
    - Hiển thị thống kê thắng/thua/hòa và bảng lịch sử các trận đấu của người chơi.

<p align="center">
  <img src="assets/lichsudau.png" alt="Lịch sử đấu" width="500"/>
</p>
<p align="center">
  <em> Hình 5: Màn hình lịch sử đấu (HistoryFrame) </em>
</p>

- Thông báo kết quả
    - Popup hiển thị thắng/thua/hòa, cập nhật điểm số.

<p align="center">
  <img src="assets/thongbaokeq.png" alt="Kết quả" width="500"/>
</p>
<p align="center">
  <img src="assets/Thongbaohoaco.png" alt="Kết quả" width="500"/>
</p>
<p align="center">
  <em> Hình 6: Popup kết quả trận đấu </em>
</p>

- File lưu trữ
    - `users.csv`: Lưu username,password,wins,losses,draws.
    - `match_history.csv`: Lưu player1,player2,result,timestamp,player1Score,player2Score.

<p align="center">
  <img src="assets/lichsudata.png" alt="File users" width="500"/>
</p>
<p align="center">
  <em> Hình 7: File lưu trữ tài khoản người dùng </em>
</p>

<p align="center">
  <img src="assets/lichsudata.png" alt="File match history" width="500"/>
</p>
<p align="center">
  <em> Hình 8: File lưu trữ lịch sử trận đấu </em>
</p>

## 4. Cài đặt & chạy chương trình
- Bước 1: Chuẩn bị môi trường
    - Cài đặt Eclipse IDE for Java Developers (hoặc phiên bản Eclipse hỗ trợ Java).
    - Cài đặt Java JDK 8+.
    - Kiểm tra JDK bằng lệnh trong terminal: `java -version`.
- Bước 2: Thiết lập dự án trong Eclipse
    - Mở Eclipse, chọn **File > New > Java Project**.
    - Đặt tên dự án (ví dụ: `TicTacToe`) và nhấn **Finish**.
    - Sao chép thư mục `caro` chứa các file mã nguồn (`LoginFrame.java`, `RegisterFrame.java`, `GameMenu.java`, `Server.java`, `Client.java`, `XOGame.java`) vào thư mục dự án trong Eclipse.
    - Nhấn chuột phải vào dự án trong **Package Explorer**, chọn **Refresh** để cập nhật các file.
- Bước 3: Chạy chương trình
    - Trong Eclipse, mở file `caro/LoginFrame.java`.
    - Nhấn chuột phải vào file, chọn **Run As > Java Application** để khởi động màn hình đăng nhập.
    - Đăng nhập hoặc đăng ký tài khoản.
    - Trong menu chính:
        - Chọn **Tạo Phòng** để làm server (port mặc định 12345, chia sẻ IP cho người khác).
        - Hoặc chọn **Tham Gia Phòng**, nhập IP/Port của server để kết nối.
        - Hoặc chọn **Xem Lịch Sử Đấu** để xem thống kê và lịch sử các trận đấu.
- Bước 4: Chơi game
    - Hai người chơi kết nối sẽ bắt đầu game tự động.
    - Luân phiên click vào ô bàn cờ để đánh X/O.
    - Khi kết thúc, kết quả hiển thị; có thể chơi lại, đầu hàng, hoặc thoát.
- Lưu ý:
    - Chạy trên cùng mạng LAN để kết nối IP.
    - Nếu lỗi kết nối, kiểm tra firewall hoặc port 12345.
    - File `users.csv` và `match_history.csv` được tạo tự động trong thư mục dự án nếu chưa tồn tại.
    - Đảm bảo Eclipse được cấu hình đúng JDK trong **Preferences > Java > Installed JREs**.

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.
