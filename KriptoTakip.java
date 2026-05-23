import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class odev_2516501021 {

    static String dosyaAdi = "kripto_verilerim.txt";
    static String logDosyasi = "islemler_log.txt";
    static Scanner oku = new Scanner(System.in);

    public static void main(String[] args) {

        int secim = 0;
        logYaz("Sistem baslatildi.");

        while (secim != 9) {
            System.out.println("\n========== KRIPTO PARA TAKIP SISTEMI ==========");
            System.out.println("1- API'den Guncel Veri Cek");
            System.out.println("2- Listeleme ve Filtreleme");
            System.out.println("3- Coin Ara (Sembol ile)");
            System.out.println("4- Fiyat Guncelle");
            System.out.println("5- Coin Sil");
            System.out.println("6- Piyasa Istatistikleri");
            System.out.println("7- Gecmis Islemleri (Log) Goster");
            System.out.println("8- Veritabanini (Txt) Temizle");
            System.out.println("9- Cikis");
            System.out.print("Yapmak istediginiz islem: ");

            // Sonsuz donguye girmemesi icin Scanneri guvenli aliyoruz
            if (!oku.hasNextLine()) break;
            String giris = oku.nextLine().trim();
            if (giris.isEmpty()) continue;

            try {
                secim = Integer.parseInt(giris);

                switch (secim) {
                    case 1: apiVeriCek(); break;
                    case 2: listelemeMenusu(); break;
                    case 3: coinAra(); break;
                    case 4: veriGuncelle(); break;
                    case 5: veriSil(); break;
                    case 6: istatistikler(); break;
                    case 7: loglariGoster(); break;
                    case 8: dosyayiTemizle(); break;
                    case 9:
                        System.out.println("Program kapatiliyor. Iyi gunler!");
                        logYaz("Sistemden cikis yapildi.");
                        break;
                    default:
                        System.out.println("Lutfen menudeki numaralardan birini secin.");
                }
            } catch (Exception e) {
                System.out.println("HATA: Lutfen harf degil rakam giriniz!");
            }
        }
    }

    public static void apiVeriCek() {
        try {
            // Ustune yazsin diye false yapiyorum, yoksa ayni veriler alt alta cogaliyor
            FileWriter fw = new FileWriter(dosyaAdi, false);
            
            // Java 8 SSL hatasi vermesin diye
            System.setProperty("https.protocols", "TLSv1.2");
            URL url = new URL("https://api.coincap.io/v2/assets");
            HttpURLConnection baglanti = (HttpURLConnection) url.openConnection();
            baglanti.setRequestMethod("GET");

            // API bizi bot sanip 403 Forbidden vermesin diye tarayici taklidi yapiyoruz
            baglanti.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            BufferedReader br = new BufferedReader(new InputStreamReader(baglanti.getInputStream()));
            String satir;
            StringBuilder tumVeri = new StringBuilder();

            while ((satir = br.readLine()) != null) {
                tumVeri.append(satir);
            }
            br.close();
            baglanti.disconnect();

            // JSON kutuphanesi yasak oldugu icin veriyi id key'ine gore manuel boluyorum
            String jsonFormat = tumVeri.toString();
            String[] coinler = jsonFormat.split("\\{\"id\":");
            int kaydedilenAdet = 0;

            // 0. index genelde bos veya baslangic tagi oluyor, o yuzden 1'den baslattim
            for (int i = 1; i < coinler.length; i++) {
                if (kaydedilenAdet >= 35) break; // Hocanin istedigi min 30 sarti
                
                String tekCoin = coinler[i];
                String sira = parcaBul(tekCoin, "rank");
                String sembol = parcaBul(tekCoin, "symbol");
                String isim = parcaBul(tekCoin, "name");
                String fiyat = parcaBul(tekCoin, "priceUsd");
                String degisim = parcaBul(tekCoin, "changePercent24Hr");

                // Virgullerden sonrasi cok uzun olmasin diye kirpiyorum
                if (fiyat.length() > 10) fiyat = fiyat.substring(0, 10);
                if (degisim.length() > 7) degisim = degisim.substring(0, 7);

                String yazilacakFormat = sira + ";" + sembol + ";" + isim + ";" + fiyat + ";" + degisim;
                fw.write(yazilacakFormat + "\n");
                kaydedilenAdet++;
            }
            fw.close();

            System.out.println("Islem basarili! Toplam " + kaydedilenAdet + " coin dosyaya islendi.");
            logYaz("API uzerinden " + kaydedilenAdet + " adet veri guncellendi.");

        } catch (Exception e) {
            System.out.println("API Baglanti Hatasi. Gecici olarak internetinizi degistirip (ornegin telefonun internetiyle) deneyin.");
            logYaz("HATA: API'den veri cekilemedi.");
        }
    }

    // Yardimci metot: indexOf ve substring kullanarak istenen veriyi cekiyor
    public static String parcaBul(String metin, String arananKelime) {
        try {
            String aranan = "\"" + arananKelime + "\":\"";
            int baslangic = metin.indexOf(aranan);
            if (baslangic == -1) return "0";
            
            baslangic += aranan.length();
            int bitis = metin.indexOf("\"", baslangic);
            return metin.substring(baslangic, bitis);
        } catch (Exception e) {
            return "0";
        }
    }

    public static void listelemeMenusu() {
        while (true) {
            System.out.println("\n--- LISTELEME FILTRELERI ---");
            System.out.println("1- Degeri 1000$ uzerinde olanlari listele");
            System.out.println("2- Son 24 saatte yukseliste olanlari listele");
            System.out.println("3- Dususte olanlari listele");
            System.out.println("4- Dosyadaki TUM coinleri listele");
            System.out.println("5- Ana menuye don");
            System.out.print("Seciminiz: ");

            try {
                if (!oku.hasNextLine()) break;
                String giris = oku.nextLine().trim();
                if (giris.isEmpty()) continue;
                
                int tercih = Integer.parseInt(giris);

                if (tercih >= 1 && tercih <= 4) {
                    ekranaYazdir(tercih);
                } else if (tercih == 5) {
                    break;
                } else {
                    System.out.println("Hatali secim.");
                }
            } catch (Exception e) {
                System.out.println("Lutfen rakam girin.");
            }
        }
    }

    public static void ekranaYazdir(int filtreTipi) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(dosyaAdi));
            String okunanSatir;

            System.out.printf("\n%-5s | %-8s | %-20s | %-12s | %-10s\n", "Sira", "Sembol", "Coin Adi", "Fiyat($)", "Degisim(%)");
            System.out.println("-----------------------------------------------------------------------");

            while ((okunanSatir = br.readLine()) != null) {
                String[] ayrilmis = okunanSatir.split(";");
                if (ayrilmis.length < 5) continue;

                double dFiyat = 0, dDegisim = 0;
                try { dFiyat = Double.parseDouble(ayrilmis[3]); } catch (Exception e) {}
                try { dDegisim = Double.parseDouble(ayrilmis[4]); } catch (Exception e) {}

                boolean gosterilecekMi = false;

                if (filtreTipi == 1 && dFiyat > 1000.0) gosterilecekMi = true;
                else if (filtreTipi == 2 && dDegisim > 0.0) gosterilecekMi = true;
                else if (filtreTipi == 3 && dDegisim < 0.0) gosterilecekMi = true;
                else if (filtreTipi == 4) gosterilecekMi = true;

                if (gosterilecekMi) {
                    System.out.printf("%-5s | %-8s | %-20s | %-12s | %-10s\n", ayrilmis[0], ayrilmis[1], ayrilmis[2], ayrilmis[3], ayrilmis[4]);
                }
            }
            br.close();
            logYaz("Filtreli listeleme yapildi. Filtre No: " + filtreTipi);
        } catch (Exception e) {
            System.out.println("Txt dosyasi bulunamadi. Lutfen once veri cekin.");
        }
    }

    public static void coinAra() {
        System.out.print("Aramak istediginiz coinin sembolu (Orn: SOL): ");
        String aranan = oku.nextLine().toUpperCase();
        boolean bulunduMu = false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(dosyaAdi));
            String okunanSatir;

            while ((okunanSatir = br.readLine()) != null) {
                String[] ayrilmis = okunanSatir.split(";");
                if (ayrilmis.length < 5) continue;

                if (ayrilmis[1].equalsIgnoreCase(aranan)) {
                    System.out.println("\n*** Eslesme Bulundu ***");
                    System.out.println("Piyasa Sirasi : " + ayrilmis[0]);
                    System.out.println("Sembol        : " + ayrilmis[1]);
                    System.out.println("Coin          : " + ayrilmis[2]);
                    System.out.println("Guncel Fiyat  : $" + ayrilmis[3]);
                    System.out.println("24s Degisim   : %" + ayrilmis[4]);
                    bulunduMu = true;
                    logYaz(aranan + " sembolu dosyada aratildi.");
                }
            }
            br.close();
            if (!bulunduMu) System.out.println("Dosyada boyle bir coin bulunamadi.");

        } catch (Exception e) {
            System.out.println("Hata! Dosya okunamiyor.");
        }
    }

    public static void veriGuncelle() {
        System.out.print("Fiyatini degistirmek istediginiz coinin sembolunu girin (Orn: BTC): ");
        String arananSembol = oku.nextLine().toUpperCase();
        
        String[] geciciHafiza = new String[500]; // Dosyayi tutmak icin dizi
        int satirSayaci = 0;
        boolean degisimOlduMu = false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(dosyaAdi));
            String okunanSatir;

            while ((okunanSatir = br.readLine()) != null) {
                String[] ayrilmis = okunanSatir.split(";");

                if (ayrilmis.length >= 5 && ayrilmis[1].equalsIgnoreCase(arananSembol) && !degisimOlduMu) {
                    System.out.println("\nEski Kayit: " + okunanSatir);
                    System.out.print("Degistirmek istediginiz veri bu mu? (e/h): ");
                    
                    if (oku.nextLine().equalsIgnoreCase("e")) {
                        System.out.print("Lutfen yeni fiyati yazin: ");
                        String yeniDeger = oku.nextLine();
                        // Satiri yeni fiyatla tekrar olusturuyoruz
                        okunanSatir = ayrilmis[0] + ";" + ayrilmis[1] + ";" + ayrilmis[2] + ";" + yeniDeger + ";" + ayrilmis[4];
                        degisimOlduMu = true;
                        System.out.println("Basarili! Yeni fiyat dosyaya islendi.");
                        logYaz(arananSembol + " adli coinin fiyati manuel guncellendi.");
                    }
                }
                geciciHafiza[satirSayaci++] = okunanSatir;
            }
            br.close();

            // Eger bir degisiklik yapildiysa diziyi tekrar txt dosyasina ustune yazarak kaydediyoruz
            if (degisimOlduMu) {
                FileWriter fw = new FileWriter(dosyaAdi, false);
                for (int i = 0; i < satirSayaci; i++) {
                    fw.write(geciciHafiza[i] + "\n");
                }
                fw.close();
            } else {
                System.out.println("Islem iptal edildi veya kayit bulunamadi.");
            }
        } catch (Exception e) {
            System.out.println("Guncelleme sirasinda bir hata olustu.");
        }
    }

    public static void veriSil() {
        System.out.print("Silmek istediginiz coinin sembolunu girin (Orn: DOGE): ");
        String arananSembol = oku.nextLine().toUpperCase();
        
        String[] geciciHafiza = new String[500];
        int satirSayaci = 0;
        boolean silindiMi = false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(dosyaAdi));
            String okunanSatir;

            while ((okunanSatir = br.readLine()) != null) {
                String[] ayrilmis = okunanSatir.split(";");

                if (ayrilmis.length >= 5 && ayrilmis[1].equalsIgnoreCase(arananSembol) && !silindiMi) {
                    System.out.println("\nBulunan Coin: " + ayrilmis[2] + " | Fiyat: " + ayrilmis[3]);
                    System.out.print("Bu veriyi tamamen silmek istiyor musunuz? (e/h): ");
                    
                    if (oku.nextLine().equalsIgnoreCase("e")) {
                        silindiMi = true;
                        System.out.println("Coin dosyadan kalici olarak silindi.");
                        logYaz(arananSembol + " sembolu txt dosyasindan silindi.");
                        continue; // Ekledigim bu continue sayesinde silinen veriyi diziye aktarmiyor
                    }
                }
                geciciHafiza[satirSayaci++] = okunanSatir;
            }
            br.close();

            if (silindiMi) {
                FileWriter fw = new FileWriter(dosyaAdi, false);
                for (int i = 0; i < satirSayaci; i++) {
                    fw.write(geciciHafiza[i] + "\n");
                }
                fw.close();
            } else {
                System.out.println("Silme islemi iptal edildi.");
            }
        } catch (Exception e) {
            System.out.println("Silme islemi basarisiz oldu.");
        }
    }

    // YARIM KALAN İSTATİSTİK METODU TAMAMLANDI
    public static void istatistikler() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(dosyaAdi));
            String okunanSatir;

            double maksFiyat = 0, maksDegisim = -9999, toplamHacim = 0;
            String enPahali = "", enCokYukselen = "";
            int toplamAdet = 0;

            while ((okunanSatir = br.readLine()) != null) {
                String[] ayrilmis = okunanSatir.split(";");
                if (ayrilmis.length < 5) continue;

                double f = 0, d = 0;
                try { f = Double.parseDouble(ayrilmis[3]); } catch (Exception e) {}
                try { d = Double.parseDouble(ayrilmis[4]); } catch (Exception e) {}

                toplamHacim += f;
                toplamAdet++;

                if (f > maksFiyat) {
                    maksFiyat = f;
                    enPahali = ayrilmis[2];
                }
                if (d > maksDegisim) {
                    maksDegisim = d;
                    enCokYukselen = ayrilmis[2];
                }
            }
            br.close();

            if (toplamAdet > 0) {
                System.out.println("\n--- PİYASA İSTATİSTİKLERİ ---");
                System.out.println("En Pahalı Coin    : " + enPahali + " ($" + maksFiyat + ")");
                System.out.println("En Çok Yükselen   : " + enCokYukselen + " (%" + maksDegisim + ")");
                System.out.println("Ortalama Fiyat    : $" + (toplamHacim / toplamAdet));
                logYaz("Piyasa istatistikleri goruntulendi.");
            } else {
                System.out.println("Dosyada veri bulunamadı. Lütfen önce API'den veri çekin.");
            }

        } catch (Exception e) {
            System.out.println("Istatistikler hesaplanirken bir hata olustu.");
        }
    }

    // EKSİK OLAN LOG YAZMA METODU EKLENDİ
    public static void logYaz(String mesaj) {
        try {
            FileWriter fw = new FileWriter(logDosyasi, true); // true parametresi veriyi silmeden altina ekler
            String zaman = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            fw.write("[" + zaman + "] " + mesaj + "\n");
            fw.close();
        } catch (Exception e) {
            System.out.println("Log kaydi olusturulamadi.");
        }
    }

    // EKSİK OLAN LOG GÖSTERME METODU EKLENDİ
    public static void loglariGoster() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(logDosyasi));
            String satir;
            System.out.println("\n--- SİSTEM LOGLARI ---");
            while ((satir = br.readLine()) != null) {
                System.out.println(satir);
            }
            br.close();
            System.out.println("-----------------------");
        } catch (Exception e) {
            System.out.println("Gosterilecek log kaydi bulunamadi.");
        }
    }

    // EKSİK OLAN DOSYA TEMİZLEME METODU EKLENDİ
    public static void dosyayiTemizle() {
        try {
            FileWriter fw = new FileWriter(dosyaAdi, false);
            fw.write(""); // Icerigini bosaltir
            fw.close();
            System.out.println("Veritabani (Txt dosyasi) basariyla temizlendi.");
            logYaz("Veritabani manuel olarak temizlendi.");
        } catch (Exception e) {
            System.out.println("Dosya temizlenirken hata olustu.");
        }
    }
}
