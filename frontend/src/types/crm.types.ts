/**
 * Akıllı Saha CRM - TypeScript Tip Tanımları (Frontend Entegrasyon Sözleşmesi)
 * Backend Java modelleri, DTO'ları ve standart yanıt zarfı (IslemSonucu) ile %100 uyumludur.
 * 
 * @author Akıllı Saha CRM Ekibi
 */

// =============================================================================
// 1. Enum Tipleri
// =============================================================================

export type Rol = 'YONETICI' | 'PLASIYER';

export type NumuneDurumu =
  | 'BEKLEMEDE'
  | 'TEST_ASAMASINDA'
  | 'BEGENDI'
  | 'REDDETTI'
  | 'SIPARISE_DONUSTU';

export type TeklifDurumu = 'ACIK' | 'KAZANILDI' | 'KAYBEDILDI' | 'IPTAL';

// =============================================================================
// 2. Standart API Yanıt Zarfı (ApiResponse) & Sayfalama
// =============================================================================

export interface IslemSonucu<T> {
  basarili: boolean;
  mesaj: string;
  veri: T;
  zamanDamgasi: string;
}

export interface SayfalanmisYanit<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// =============================================================================
// 3. Kimlik Doğrulama & Kullanıcı Modelleri
// =============================================================================

export interface GirisIstegi {
  eposta: string;
  sifre: string;
}

export interface KayitIstegi {
  eposta: string;
  sifre: string;
  adSoyad: string;
  telefon?: string;
  rol: Rol;
}

export interface GirisYaniti {
  token?: string;
  tokenTipi?: string;
  erisimBelirteci?: string;
  belirtecTuru?: string;
  kullaniciId?: number;
  eposta?: string;
  adSoyad?: string;
  rol?: Rol;
  gecerlilikSuresiMs?: number;
  kullanici?: KullaniciOzetiYaniti;
}

export interface KimlikDogrulamaYaniti {
  token: string;
  tokenTipi: string;
  kullaniciId: number;
  eposta: string;
  adSoyad: string;
  rol: Rol;
}

export interface KullaniciOzetiYaniti {
  id: number;
  eposta: string;
  adSoyad: string;
  rol: Rol;
  telefon?: string;
  aktifMi?: boolean;
}

export interface Urun {
  id: number;
  code: string;
  name: string;
  brand: string;
  category: string;
  unit: string;
}

// =============================================================================
// 4. Müşteri & Firma Modelleri (17 Kolonluk Excel Entegrasyonu)
// =============================================================================

export interface FirmaIstegi {
  unvan: string;
  sehirBolge?: string;
  adres?: string;
  telefon?: string;
  eposta?: string;
  mevcutTedarikciRakip?: string; // Excel Kolon I
  tedarikEttigiUrunler?: string; // Excel Kolon F
  aylikKullanimMiktari?: string; // Excel Kolon G
  bizdenAldigiUrunler?: string;  // Excel Kolon H
  atananPlasiyerId?: number;
}

export interface YetkiliIstegi {
  firmaId: number;
  adSoyad: string;
  unvanGorev?: string;
  telefon?: string;
  eposta?: string;
  notlar?: string;
}

export interface YetkiliYaniti {
  id: number;
  firmaId: number;
  adSoyad: string;
  unvanGorev?: string;
  telefon?: string;
  eposta?: string;
  notlar?: string;
  olusturulmaTarihi: string;
}

export interface FirmaYaniti {
  id: number;
  unvan: string;
  sehirBolge?: string;
  adres?: string;
  telefon?: string;
  eposta?: string;
  mevcutTedarikciRakip?: string;
  tedarikEttigiUrunler?: string;
  aylikKullanimMiktari?: string;
  bizdenAldigiUrunler?: string;
  atananPlasiyerId?: number;
  atananPlasiyerAdi?: string;
  atananPlasiyerAdSoyad?: string;
  yetkililer?: YetkiliYaniti[];
  olusturulmaTarihi?: string;
  guncellenmeTarihi?: string;
}

// =============================================================================
// 5. Saha Ziyareti Modelleri (30 Saniyelik Hızlı Form)
// =============================================================================

export interface ZiyaretIstegi {
  firmaId: number;
  yetkiliId?: number;
  ziyaretTarihi?: string; // yyyy-MM-dd HH:mm:ss
  ziyaretKonusu: string;
  tedarikEttigiUrunler?: string;
  aylikKullanimMiktari?: string;
  bizdenAldigiUrunler?: string;
  mevcutTedarikciRakip?: string;
  numuneVerildiMi?: boolean;
  numuneUrunId?: number;
  numuneUrunAdi?: string;
  numuneMarka?: string;
  numuneMiktar?: number | string;
  teklifVerildiMi?: boolean;
  teklifBaslik?: string;
  teklifTutari?: number;
  teklifParaBirimi?: string;
  sonrakiAksiyon?: string;
  sonrakiZiyaretTarihi?: string; // yyyy-MM-dd HH:mm:ss
  notlar?: string;
}

export interface ZiyaretYaniti {
  id: number;
  firmaId: number;
  firmaAdi?: string;
  firmaUnvani?: string;
  sehirBolge?: string;
  yetkiliId?: number;
  yetkiliAdi?: string;
  yetkiliAdSoyad?: string;
  yetkiliGorevi?: string;
  yetkiliUnvanGorev?: string;
  plasiyerId: number;
  plasiyerAdi?: string;
  plasiyerAdSoyad?: string;
  ziyaretTarihi: string;
  ziyaretKonusu: string;
  tedarikEttigiUrunler?: string;
  aylikKullanimMiktari?: string;
  bizdenAldigiUrunler?: string;
  mevcutTedarikciRakip?: string;
  numuneVerildiMi: boolean;
  teklifVerildiMi: boolean;
  sonrakiAksiyon?: string;
  sonrakiZiyaretTarihi?: string;
  notlar?: string;
  numuneler?: NumuneYaniti[];
  teklifler?: TeklifYaniti[];
  olusturulmaTarihi: string;
}

// =============================================================================
// 6. Numune & Teklif Takip Masası Modelleri
// =============================================================================

export interface NumuneIstegi {
  ziyaretId?: number;
  firmaId: number;
  urunId?: number;
  urunAdi: string;
  marka?: string;
  miktar: number | string;
  sonucNotlari?: string;
}

export interface NumuneYaniti {
  id: number;
  ziyaretId?: number;
  firmaId: number;
  firmaAdi?: string;
  firmaUnvani?: string;
  plasiyerId: number;
  plasiyerAdi?: string;
  plasiyerAdSoyad?: string;
  urunId?: number;
  urunAdi: string;
  marka?: string;
  miktar: any;
  durum: NumuneDurumu;
  sonucNotlari?: string;
  gonderimTarihi: string;
  degerlendirilmeTarihi?: string;
  olusturulmaTarihi: string;
}

export interface NumuneIstatistikYaniti {
  toplamNumune: number;
  bekleyenNumune: number;
  testAsamasindaNumune: number;
  begendiNumune: number;
  reddettiNumune: number;
  sipariseDonusenNumune: number;
  markaDagilimi: Record<string, number>;
}

export interface TeklifIstegi {
  firmaId: number;
  ziyaretId?: number;
  baslik: string;
  tutar: number;
  paraBirimi?: string;
  gecerlilikTarihi?: string;
  notlar?: string;
}

export interface TeklifYaniti {
  id: number;
  firmaId: number;
  firmaAdi?: string;
  firmaUnvani?: string;
  ziyaretId?: number;
  plasiyerId: number;
  plasiyerAdi?: string;
  plasiyerAdSoyad?: string;
  baslik: string;
  tutar: number;
  paraBirimi: string;
  durum: TeklifDurumu;
  notlar?: string;
  gecerlilikTarihi?: string;
  olusturulmaTarihi: string;
}

// =============================================================================
// 7. Excel İçe/Dışa Aktarım Modelleri
// =============================================================================

export interface ExcelIceAktarimSonucuYaniti {
  toplamSatirSayisi: number;
  basariliKayitSayisi: number;
  hataliSatirSayisi: number;
  hataDetaylari: string[];
}

// =============================================================================
// 8. Bitrix24 İnteraktif Takvim ve Etkinlik Modelleri
// =============================================================================

export interface TakvimEtkinlikYaniti {
  id: string; // e.g. "RANDEVU-1", "ZIYARET-1"
  baslik: string;
  tarih: string; // ISO date string
  bitisTarihi?: string;
  tur: 'RANDEVU' | 'ZIYARET' | 'NUMUNE' | 'TEKLIF';
  turEtiketi: string;
  renkKodu: string;
  firmaId?: number;
  firmaUnvani?: string;
  sehirBolge?: string;
  yetkiliAdSoyad?: string;
  yetkiliUnvan?: string;
  yetkiliTelefon?: string;
  yetkiliEposta?: string;
  plasiyerId?: number;
  plasiyerAdSoyad?: string;
  aciklama?: string;
  durum?: string;
  referansId?: number;
  oncelik?: 'NORMAL' | 'YUKSEK' | 'ACIL';
}
