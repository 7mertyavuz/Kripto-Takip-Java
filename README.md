# 🪙 Kripto Para Takip Aracı (Java Konsol Uygulaması)

Java öğrenirken pratik yapmak amacıyla geliştirdiğim, API üzerinden canlı kripto para verilerini çekip txt dosyasına kaydeden basit bir konsol uygulaması. 

Projenin asıl amacı, **harici bir JSON ayrıştırıcı kütüphane (GSON vs.) kullanmadan**, saf Java String metotlarıyla JSON verilerini işlemeyi ve dosya (File I/O) yönetimini kavramaktı.

## Neler Yapabiliyor?
- **Veri Çekme:** CoinCap API'sini kullanarak anlık fiyat, sıralama ve 24 saatlik değişim verilerini çekip `kripto_verilerim.txt` dosyasına yazıyor.
- **Filtreli Listeleme:** Çekilen veriler arasından örneğin 1000$'dan pahalı olanları veya o gün yükselişte olanları filtreleyebiliyor.
- **CRUD İşlemleri:** Dosya üzerinden coine göre arama yapma, fiyatını manuel olarak güncelleme veya listeden silme özelliklerine sahip.
- **Basit İstatistik:** Dosyayı tarayarak en pahalı coini, günün yıldızını ve piyasa ortalamasını hesaplıyor.
- **İşlem Günlüğü (Log):** Programın çalışırken yaptığı hataları veya başarıları tarih/saat damgasıyla `islemler_log.txt` dosyasına kaydediyor.

## Nasıl Çalıştırılır?

Projeyi bilgisayarınıza indirdikten sonra terminalde klasörün içine girip şu komutlarla çalıştırabilirsiniz:

```bash
# Derlemek için:
javac KriptoTakip.java

# Çalıştırmak için:
java KriptoTakip
