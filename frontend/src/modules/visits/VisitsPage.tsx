import React, { useState } from 'react';
import {
  CalendarCheck,
  Plus,
  Search,
  Filter,
  Clock,
  MapPin,
  Building2,
  CheckCircle2,
  ChevronRight,
  Phone,
  FileEdit,
} from 'lucide-react';

export const VisitsPage: React.FC = () => {
  const [filterDurum, setFilterDurum] = useState<string>('HEPSI');

  const ziyaretler = [
    {
      id: 1,
      firma: 'Balküpü Şeker ve Gıda Sanayi',
      temsilci: 'Umut Arslan',
      tarih: 'Bugün, 11:00',
      durum: 'TAMAMLANDI',
      konu: 'Doğal Vanilya & Karamel Aroması Sunumu',
      not: 'Numuneler teslim edildi, 3 gün sonra Ar-Ge tadımı yapılacak.',
      yetkili: 'Mehmet Yılmaz',
      telefon: '0216 444 01 02',
      lokasyon: 'Kocaeli Dilovası OSB',
    },
    {
      id: 2,
      firma: 'Anadolu Bisküvi ve Çikolata A.Ş.',
      temsilci: 'Umut Arslan',
      tarih: 'Bugün, 14:30',
      durum: 'BEKLIYOR',
      konu: 'Fındık Püresi Numune Tadım Geri Bildirimi',
      not: 'Teklif taslağı hazırlandı, satınalma onayı görüşülecek.',
      yetkili: 'Ayşe Kaya',
      telefon: '0216 555 90 91',
      lokasyon: 'Tuzla Kimyacılar Sitesi',
    },
    {
      id: 3,
      firma: 'Marmara Unlu Mamulleri',
      temsilci: 'Umut Arslan',
      tarih: 'Bugün, 16:45',
      durum: 'BEKLIYOR',
      konu: 'Yıllık hammadde tedarik sözleşmesi',
      not: 'Fiyat opsiyonları ve vade koşulları masaya yatırılacak.',
      yetkili: 'Kemal Demir',
      telefon: '0216 333 12 12',
      lokasyon: 'Kartal Sanayi Çarşısı',
    },
    {
      id: 4,
      firma: 'Doğa Organik Meyve Suları',
      temsilci: 'Umut Arslan',
      tarih: '15 Ekim 2026, 10:00',
      durum: 'PLANLANDI',
      konu: 'Yeni Sezon Şeftali ve Elma Konsantresi',
      not: 'Yeni fabrika laboratuvarına ilk ziyaret.',
      yetkili: 'Canan Tunç',
      telefon: '0212 777 88 99',
      lokasyon: 'Hadımköy Fabrika',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Üst Başlık & Hızlı Ziyaret Girişi */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2.5">
            <CalendarCheck className="w-6 h-6 text-primary-400" />
            <span>Saha Ziyaretleri & Randevular</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Sahada 30 saniyede hızlı ziyaret girişi, rota ve aksiyon takibi
          </p>
        </div>

        <button className="inline-flex items-center gap-2 bg-primary-600 hover:bg-primary-500 active:scale-95 text-white text-xs sm:text-sm font-semibold px-4 py-2.5 rounded-xl shadow-md shadow-primary-950/60 transition-all">
          <Plus className="w-4 h-4" />
          <span>Yeni Ziyaret Başlat (30 sn)</span>
        </button>
      </div>

      {/* Arama ve Filtre Çubuğu */}
      <div className="p-3 bg-forest-850/80 rounded-2xl border border-forest-850 flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="flex items-center gap-2 w-full sm:w-auto flex-1 max-w-md">
          <div className="relative w-full">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5 pointer-events-none" />
            <input
              type="text"
              placeholder="Firma veya konu ara..."
              className="w-full bg-forest-900 border border-forest-850 rounded-xl pl-9 pr-4 py-2 text-xs text-white placeholder-slate-400 focus:outline-none focus:border-primary-500"
            />
          </div>
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto overflow-x-auto pb-1 sm:pb-0">
          <Filter className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
          {['HEPSI', 'BEKLIYOR', 'TAMAMLANDI', 'PLANLANDI'].map((durum) => (
            <button
              key={durum}
              onClick={() => setFilterDurum(durum)}
              className={`text-xs px-3 py-1.5 rounded-xl font-medium transition-all flex-shrink-0 ${
                filterDurum === durum
                  ? 'bg-primary-600 text-white font-semibold'
                  : 'bg-forest-900 text-slate-400 hover:text-white border border-forest-850'
              }`}
            >
              {durum === 'HEPSI' ? 'Tümü' : durum}
            </button>
          ))}
        </div>
      </div>

      {/* Ziyaret Listesi */}
      <div className="space-y-3">
        {ziyaretler
          .filter((z) => filterDurum === 'HEPSI' || z.durum === filterDurum)
          .map((ziyaret) => (
            <div
              key={ziyaret.id}
              className="p-5 rounded-2xl bg-forest-850/90 border border-forest-850 hover:border-primary-600/40 transition-all space-y-3"
            >
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-forest-900 border border-forest-850 flex items-center justify-center text-primary-400 flex-shrink-0">
                    <Building2 className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-white flex items-center gap-2">
                      {ziyaret.firma}
                      {ziyaret.durum === 'TAMAMLANDI' ? (
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-primary-500/20 text-primary-300 border border-primary-500/30">
                          Tamamlandı
                        </span>
                      ) : (
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-gold-500/20 text-gold-500 border border-gold-500/30">
                          {ziyaret.durum}
                        </span>
                      )}
                    </h3>
                    <div className="flex items-center gap-3 text-xs text-slate-400 mt-0.5">
                      <span className="flex items-center gap-1">
                        <Clock className="w-3.5 h-3.5 text-primary-400" />
                        {ziyaret.tarih}
                      </span>
                      <span>•</span>
                      <span className="flex items-center gap-1">
                        <MapPin className="w-3.5 h-3.5 text-slate-500" />
                        {ziyaret.lokasyon}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <a
                    href={`tel:${ziyaret.telefon}`}
                    className="p-2 rounded-xl bg-forest-900 hover:bg-forest-850 text-slate-300 hover:text-white border border-forest-850 transition-colors"
                  >
                    <Phone className="w-4 h-4" />
                  </a>
                  <button className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-forest-900 hover:bg-forest-850 text-slate-200 border border-forest-850 text-xs font-semibold transition-colors">
                    <FileEdit className="w-3.5 h-3.5" />
                    <span>Düzenle</span>
                  </button>
                </div>
              </div>

              <div className="p-3 bg-forest-900/80 rounded-xl border border-forest-850/80 text-xs">
                <span className="font-semibold text-primary-300">Konu: </span>
                <span className="text-slate-200">{ziyaret.konu}</span>
                <div className="mt-1 text-slate-400 text-[11px]">
                  <span className="font-semibold text-slate-400">Gözlem / Not: </span>
                  {ziyaret.not}
                </div>
              </div>
            </div>
          ))}
      </div>
    </div>
  );
};

export default VisitsPage;
