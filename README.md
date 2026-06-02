# FitOl - Fitness & Diyet Takip Uygulaması (Java Spring Boot)

Kişisel fitness ve diyet takip web u ygulaması.

## Teknolojiler
- **Java 17** + **Spring Boot 3.2**
- **Thymeleaf** (HTML template engine)
- **Spring Security** (kullanıcı yetkilendirme)
- **Spring Data JPA** (veritabanı ORM)
- **H2 Database** (gömülü veritabanı - kurulum gerektirmez)
- **Bootstrap 5** + **Chart.js** (frontend)
- **Maven** (build aracı)

## Özellikler
- 🔐 Kullanıcı kayıt/giriş sistemi (BCrypt şifreleme)
- 📊 Dashboard - günlük özet ve grafikler
- 🍽️ Yemek/kalori/makro takibi (öğün bazlı)
- 💪 Egzersiz kaydı (120+ hazır egzersiz, 8 kategori)
- 🏋️ Hazır antrenman programları (başlangıç, orta, ileri)
- 💧 Su tüketimi takibi
- 📏 Vücut ölçümleri
- 🍽️ Diyet planı oluşturma
- 🧮 BMI / BMR / TDEE / İdeal kilo hesaplayıcı
- 📈 Detaylı raporlar ve grafikler
- ⏰ Hatırlatıcı yönetimi
- 👤 Profil yönetimi

## Kurulum

### 1. Java 17 Kur
[Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) veya
[OpenJDK 17](https://adoptium.net/) indir ve kur.

### 2. Maven Kur
[Maven İndir](https://maven.apache.org/download.cgi) ve PATH'e ekle.

### 3. Projeyi Çalıştır
```bash
cd fitol
mvn spring-boot:run
```

### 4. Tarayıcıdan Aç
```
http://localhost:8080
```

## Proje Yapısı
```
src/main/java/com/fitol/
├── FitolApplication.java          # Ana uygulama
├── config/SecurityConfig.java     # Güvenlik ayarları
├── model/                         # Entity sınıfları (7 tablo)
├── repository/                    # Veritabanı erişim (JPA Repository)
├── service/                       # İş mantığı
└── controller/                    # Web controller'ları (11 adet)

src/main/resources/
├── application.properties         # Uygulama ayarları
├── templates/                     # Thymeleaf HTML şablonları
└── static/                        # CSS, JS dosyaları
```

## Veritabanı
H2 gömülü veritabanı kullanılır. Uygulama ilk çalıştırıldığında otomatik olarak tablo oluşturulur.
H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/fitoldb`)

## Ekran Görüntüleri
Uygulama koyu tema (dark mode) tasarımına sahiptir.
