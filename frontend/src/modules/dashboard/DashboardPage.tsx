import React from 'react';
import {
  Sparkles,
  Calendar,
  Building2,
  FlaskConical,
  FileSpreadsheet,
  TrendingUp,
  MapPin,
  Clock,
  ArrowRight,
  Phone,
  CheckCircle2,
  AlertCircle,
  ExternalLink,
} from 'lucide-react';
import { Link } from 'react-router-dom';

export const DashboardPage: React.FC = () => {
  // Saha Temsilcisi KPI Verileri (Yeşiller Gıda)
  const kpiListesi = [
    {
      id: 'ziyaret',
      baslik: 'Bugünkü Ziyaretler',
      deger: '3 / 5',
      aciklama: '2 Ziyaret tamamlandı',
      ikon: Calendar,
      renk: 'primary',
      trend: '+12% bu hafta',
    },
    {
      id: 'numune',
      baslik: 'Açık Numune Tadımları',
      deger: '8 Adet',
      aciklama: '2 Fabrika onay bekliyor',
      ikon: FlaskConical,
      renk: 'gold',
      trend: 'Tadım aşamasında',
    },
    {
      id: 'musteri',
      baslik: 'Aktif Portföy',
      deger: '142',
      aciklama: 'Yetkili bayi & gıda üreticisi',
      ikon: Building2,
      renk: 'primary',
      trend: '+4 Yeni bu ay',
    },
    {
      id: 'teklif',
      baslik: 'Açık Teklif Hacmi',
      deger: '₺485.000',
      aciklama: '6 Teklif değerlendirmede',
      ikon: TrendingUp,
      renk: 'gold',
      trend: '%68 kapanma oranı',
    },
  ];

  // Bugünkü Saha Randevuları
  const bugunkuRandevular = [
    {
      id: 1,
      firma: 'Balküpü Şeker ve Gıda Sanayi',
      yetkili: 'Mehmet Yılmaz (Satınalma Müdürü)',
      saat: '11:00',
      durum: 'TAMAMLANDI',
      konu: 'Doğal Vanilya & Karamel Aroması Sunumu',
      adres: 'Organize Sanayi Bölgesi 4. Cadde No: 12',
      telefon: '0216 444 01 02',
    },
    {
      id: 2,
      firma: 'Anadolu Bisküvi ve Çikolata A.Ş.',
      yetkili: 'Ayşe Kaya (Ar-Ge Şefi)',
      saat: '14:30',
      durum: 'YAKLASIYOR',
      konu: 'Fındık Püresi Numune Tadım Geri Bildirimi',
      adres: 'Tuzla Kimyacılar Sanayi Sitesi',
      telefon: '0216 555 90 91',
    },
    {
      id: 3,
      firma: 'Marmara Unlu Mamulleri',
      yetkili: 'Kemal Demir (İşletme Sahibi)',
      saat: '16:45',
      durum: 'PLANLANDI',
      konu: 'Yıllık hammadde tedarik sözleşmesi',
      adres: 'Kartal Sanayi Çarşısı No: 45',
      telefon: '0216 333 12 12',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Kurumsal Karşılama Banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-primary-950/80 via-forest-850 to-forest-850 p-6 sm:p-8 border border-primary-600/30 shadow-glow-primary">
        <div className="absolute top-0 right-0 -mr-16 -mt-16 w-64 h-64 bg-primary-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-6 relative z-10">
          <div className="space-y-2 max-w-2xl">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary-300 uppercase tracking-wider">
              <Sparkles className="w-4 h-4 text-gold-500" />
              <span>Yeşiller Gıda Akıllı Saha Asistanı</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Hoş Geldin, Umut Bey 👋
            </h1>
            <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
              Bugün için planlanmış <strong>3 saha ziyaretiniz</strong> ve sonuç beklenen{' '}
              <strong>2 numune tadımınız</strong> bulunmaktadır. Tek tıkla ziyaret başlatabilir veya 17 kolonluk Excel raporunu aktarabilirsiniz.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <Link
              to="/ziyaretler"
              className="inline-flex items-center gap-2 bg-primary-600 hover:bg-primary-500 active:scale-95 text-white text-xs sm:text-sm font-semibold px-4 py-2.5 rounded-xl shadow-lg shadow-primary-950/60 transition-all"
            >
              <Calendar className="w-4 h-4" />
              <span>Ziyaret Rotasını Başlat</span>
            </Link>
            <Link
              to="/raporlar"
              className="inline-flex items-center gap-2 bg-forest-900/90 hover:bg-forest-850 text-slate-200 text-xs sm:text-sm font-medium px-4 py-2.5 rounded-xl border border-forest-850 transition-colors"
            >
              <FileSpreadsheet className="w-4 h-4 text-gold-500" />
              <span>Excel Raporu Al</span>
            </Link>
          </div>
        </div>
      </div>

      {/* KPI Kartları Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {kpiListesi.map((kpi) => {
          const Icon = kpi.ikon;
          const isGold = kpi.renk === 'gold';
          return (
            <div
              key={kpi.id}
              className="p-5 rounded-2xl bg-forest-850/80 border border-forest-850 hover:border-primary-600/40 transition-all duration-200 shadow-sm group"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-slate-400">{kpi.baslik}</span>
                <div
                  className={`w-9 h-9 rounded-xl flex items-center justify-center transition-transform group-hover:scale-110 ${
                    isGold
                      ? 'bg-gold-500/15 text-gold-500'
                      : 'bg-primary-500/15 text-primary-300'
                  }`}
                >
                  <Icon className="w-5 h-5" />
                </div>
              </div>
              <div className="mt-3">
                <div className="text-2xl font-black text-white tracking-tight">{kpi.deger}</div>
                <div className="flex items-center justify-between text-xs mt-1">
                  <span className="text-slate-400">{kpi.aciklama}</span>
                  <span
                    className={`font-semibold ${
                      isGold ? 'text-gold-500' : 'text-primary-400'
                    }`}
                  >
                    {kpi.trend}
                  </span>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* İki Sütunlu Bölüm: Ziyaret Rotası & Numuneler */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Sol 2 Sütun: Bugünkü Saha Rotası */}
        <div className="lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                <Calendar className="w-4 h-4 text-primary-400" />
                <span>Bugünkü Saha Rotası</span>
              </h2>
              <p className="text-xs text-slate-400">
                Günün randevu sırası ve ziyaret aksiyonları
              </p>
            </div>
            <Link
              to="/ziyaretler"
              className="text-xs font-semibold text-primary-400 hover:text-primary-300 inline-flex items-center gap-1 transition-colors"
            >
              Tümünü Gör <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="space-y-3">
            {bugunkuRandevular.map((randevu) => (
              <div
                key={randevu.id}
                className="p-4 rounded-xl bg-forest-850/90 border border-forest-850 hover:border-primary-700/50 transition-colors flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4"
              >
                <div className="flex items-start gap-3.5">
                  <div className="flex flex-col items-center justify-center w-12 h-12 rounded-xl bg-forest-900 border border-forest-850 text-slate-200 flex-shrink-0">
                    <Clock className="w-3.5 h-3.5 text-primary-400 mb-0.5" />
                    <span className="text-xs font-bold font-mono">{randevu.saat}</span>
                  </div>

                  <div className="space-y-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <h3 className="text-sm font-bold text-white truncate">
                        {randevu.firma}
                      </h3>
                      {randevu.durum === 'TAMAMLANDI' && (
                        <span className="inline-flex items-center gap-1 text-[10px] font-bold px-2 py-0.5 rounded-full bg-primary-500/20 text-primary-300 border border-primary-500/30">
                          <CheckCircle2 className="w-3 h-3" /> Tamamlandı
                        </span>
                      )}
                      {randevu.durum === 'YAKLASIYOR' && (
                        <span className="inline-flex items-center gap-1 text-[10px] font-bold px-2 py-0.5 rounded-full bg-gold-500/20 text-gold-500 border border-gold-500/30 animate-pulse">
                          <AlertCircle className="w-3 h-3" /> Sıradaki
                        </span>
                      )}
                      {randevu.durum === 'PLANLANDI' && (
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-300 border border-slate-700">
                          Planlandı
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-slate-300 font-medium">
                      {randevu.konu}
                    </p>
                    <div className="flex items-center gap-3 text-[11px] text-slate-400">
                      <span className="truncate">{randevu.yetkili}</span>
                      <span>•</span>
                      <span className="flex items-center gap-1 truncate">
                        <MapPin className="w-3 h-3 text-slate-500" />
                        {randevu.adres}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 w-full sm:w-auto justify-end pt-2 sm:pt-0 border-t sm:border-t-0 border-forest-900">
                  <a
                    href={`tel:${randevu.telefon}`}
                    className="p-2 rounded-xl bg-forest-900 hover:bg-forest-850 text-slate-300 hover:text-white border border-forest-850 transition-colors"
                    title="Ara"
                  >
                    <Phone className="w-4 h-4" />
                  </a>
                  <Link
                    to={`/ziyaretler?id=${randevu.id}`}
                    className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl bg-primary-600/30 hover:bg-primary-600 text-primary-300 hover:text-white border border-primary-500/40 text-xs font-semibold transition-all"
                  >
                    <span>Forma Git</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Sağ 1 Sütun: Numune & Tadım Hatırlatıcıları */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-white flex items-center gap-2">
              <FlaskConical className="w-4 h-4 text-gold-500" />
              <span>Numune Tadım Takibi</span>
            </h2>
            <Link
              to="/numuneler"
              className="text-xs font-semibold text-gold-500 hover:underline"
            >
              Tümü
            </Link>
          </div>

          <div className="space-y-3">
            <div className="p-4 rounded-xl bg-forest-850/90 border border-gold-500/30 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-white">Anadolu Bisküvi</span>
                <span className="text-[10px] font-bold text-gold-500 bg-gold-500/10 px-2 py-0.5 rounded-md border border-gold-500/20">
                  Tadım Bekleniyor
                </span>
              </div>
              <p className="text-xs text-slate-300">
                Fındık Aroması Konsantresi (Kod: AR-204)
              </p>
              <div className="text-[11px] text-slate-400 flex items-center justify-between pt-2 border-t border-forest-900">
                <span>Teslim: 3 gün önce</span>
                <span className="text-gold-500 font-semibold">Tadım Günü: Bugün</span>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-forest-850/90 border border-forest-850 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-white">Tat Unlu Mamulleri</span>
                <span className="text-[10px] font-bold text-primary-400 bg-primary-500/10 px-2 py-0.5 rounded-md border border-primary-500/20">
                  Onaylandı
                </span>
              </div>
              <p className="text-xs text-slate-300">
                Vanilya Ekstratı (25 Kg Deneme Partisi)
              </p>
              <div className="text-[11px] text-slate-400 flex items-center justify-between pt-2 border-t border-forest-900">
                <span>Teklife dönüştürülmeye hazır</span>
                <Link
                  to="/teklifler"
                  className="text-primary-300 hover:text-white font-semibold inline-flex items-center gap-1"
                >
                  Teklif Ver <ExternalLink className="w-3 h-3" />
                </Link>
              </div>
            </div>
          </div>

          {/* 17 Kolonluk Excel Bilgilendirme Kutusu */}
          <div className="p-4 rounded-xl bg-forest-900 border border-primary-900/60 space-y-2">
            <div className="flex items-center gap-2 text-primary-300 font-semibold text-xs">
              <FileSpreadsheet className="w-4 h-4 text-gold-500" />
              <span>17 Kolonluk Orijinal Saha Exceli</span>
            </div>
            <p className="text-[11px] text-slate-400 leading-relaxed">
              Tüm ziyaret, numune ve teklif kayıtlarınız otomatik olarak Yeşiller Gıda'nın kurumsal Excel şablonuyla senkronizedir.
            </p>
            <Link
              to="/raporlar"
              className="block w-full text-center py-2 rounded-lg bg-forest-850 hover:bg-forest-800 text-xs font-semibold text-slate-200 border border-forest-850 transition-colors"
            >
              Excel Dışa Aktarımı Başlat
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
