# Maven Setup Guide

## Vấn đề: Lỗi 401 khi download dependencies

Nếu bạn gặp lỗi `status code: 401` khi Maven download dependencies, hãy làm theo các bước sau:

### Bước 1: Kiểm tra IntelliJ Maven Settings

1. Mở **File → Settings** (hoặc **IntelliJ IDEA → Preferences** trên Mac)
2. Đi đến **Build, Execution, Deployment → Build Tools → Maven**
3. Kiểm tra:
   - **Maven home path**: Trỏ đến Maven installation của bạn
   - **User settings file**: Đảm bảo không có custom settings.xml với spring-milestones repository
   - **Local repository**: Đường dẫn đến local Maven repository

### Bước 2: Reload Maven Project

1. Mở **Maven** tool window (View → Tool Windows → Maven)
2. Click vào icon **Reload All Maven Projects** (🔄)
3. Hoặc right-click vào `pom.xml` → **Maven → Reload Project**

### Bước 3: Force Update Dependencies

Trong IntelliJ terminal hoặc command line:

```bash
cd portfolio-be
mvn clean install -U
```

Flag `-U` sẽ force update tất cả dependencies.

### Bước 4: Kiểm tra Maven Settings.xml

Nếu bạn có file `~/.m2/settings.xml`, kiểm tra xem có repository nào yêu cầu authentication không:

```xml
<settings>
    <servers>
        <!-- Nếu có server config với spring-milestones, có thể gây lỗi -->
    </servers>
</settings>
```

Nếu có, bạn có thể comment out hoặc remove.

### Bước 5: Clear Maven Cache (nếu cần)

```bash
# Xóa local repository cache (cẩn thận, sẽ xóa tất cả cached dependencies)
rm -rf ~/.m2/repository

# Hoặc chỉ xóa cache của project
cd portfolio-be
mvn dependency:purge-local-repository
```

### Bước 6: Verify Java Version

Đảm bảo IntelliJ đang dùng đúng Java version:

1. **File → Project Structure → Project**
   - **SDK**: Corretto-21
   - **Language level**: 21

2. **File → Settings → Build, Execution, Deployment → Compiler → Java Compiler**
   - **Project bytecode version**: 21
   - **Per-module bytecode version**: 21

### Bước 7: Invalidate Caches

1. **File → Invalidate Caches...**
2. Chọn **Invalidate and Restart**

## Các Dependencies Đã Được Fix

- ✅ `jakarta.cache-api:2.0.0` - Version hợp lệ từ Maven Central
- ✅ `caffeine:3.1.8` - Version hợp lệ
- ✅ `caffeine-jcache:3.1.8` - Version hợp lệ
- ✅ Đã thêm Maven Central repository explicitly

## Bước 8: Sử dụng .mvn/settings.xml (Đã được tạo tự động)

File `.mvn/settings.xml` đã được tạo để override repository configuration và chỉ sử dụng:
- Maven Central (public)
- Spring Releases (public)

File này sẽ được Maven tự động sử dụng khi chạy từ project directory.

## Bước 9: Cấu hình IntelliJ để sử dụng .mvn/settings.xml

1. **File → Settings → Build Tools → Maven**
2. Trong phần **User settings file**, có thể để trống hoặc trỏ đến:
   ```
   I:\portfolio\portfolio-be\.mvn\settings.xml
   ```
3. Hoặc để IntelliJ tự động detect (nó sẽ tìm `.mvn/settings.xml` trong project)

## Bước 10: Force Reload với Settings Override

1. **Maven** tool window → Click **Reload All Maven Projects** (🔄)
2. Hoặc trong terminal:
   ```bash
   cd portfolio-be
   mvn clean install -U -s .mvn/settings.xml
   ```

## Nếu Vẫn Gặp Lỗi

1. **Kiểm tra IntelliJ Maven Settings:**
   - **File → Settings → Build Tools → Maven**
   - **Maven home path**: Đảm bảo đúng
   - **User settings file**: Có thể để trống hoặc trỏ đến `.mvn/settings.xml`
   - **Local repository**: Đường dẫn hợp lệ

2. **Kiểm tra Global Maven Settings:**
   - Kiểm tra file `~/.m2/settings.xml` (hoặc `C:\Users\<username>\.m2\settings.xml` trên Windows)
   - Nếu có repository `spring-milestones` với authentication, comment out hoặc remove

3. **Clear IntelliJ Maven Cache:**
   - **File → Invalidate Caches... → Invalidate and Restart**
   - Sau đó reload Maven project

4. **Thử chạy Maven từ command line:**
   ```bash
   cd portfolio-be
   mvn clean install -U -s .mvn/settings.xml
   ```
   Nếu command line work nhưng IntelliJ không work, vấn đề là ở IntelliJ configuration.

5. **Kiểm tra Maven version:**
   ```bash
   mvn -version
   ```
   Nên là Maven 3.8+ và Java 21

6. **Kiểm tra internet connection và firewall/proxy settings**
