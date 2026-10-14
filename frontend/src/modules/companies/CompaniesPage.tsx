import React, { useState } from 'react';
import {
  Building2,
  Plus,
  Search,
  MapPin,
  Phone,
  Mail,
  User,
  ExternalLink,
  ChevronRight,
  ShieldCheck,
} from 'lucide-react';

export const CompaniesPage: React.FC = () => {
  const [arama, setArama] = useState('');

  const firmalar = [
    {
      id: 1,
      ad: 'Balküpü Şeker ve Gıda Sanayi',
      sektor: 'Şeker & Tatlandırıcı',
      il: 'Kocaeli / Dilovası',
      yetkili: 'Mehmet Yılmaz',
      unvan: 'Satınalma Müdürü',
      telefon: '0216 444 01 02',
      eposta: 'mehmet@balkupu.com.tr',
      durum: 'AKTIF_MUSTERI',
      toplamTeklif: '₺320.000',
    },
    {
      id: 2,
      ad: 'Anadolu Bisküvi ve Çikolata A.Ş.',
      sektor: 'Unlu Mamuller & Bisküvi',
      il: 'İstanbul / Tuzla',
      yetkili: 'Ayşe Kaya',
      unvan: 'Ar-Ge Şefi',
      telefon: '0216 555 90 91',
      eposta: 'ayse.kaya@anadolubiskuvi.com',
      durum: 'NUMUNE_ASAMASINDA',
      toplamTeklif: '₺165.000',
    },
    {
      id: 3,
      ad: 'Marmara Unlu Mamulleri Ltd. Şti.',
      sektor: 'Pastacılık & Ekmekçilik',
      il: 'İstanbul / Kartal',
      yetkili: 'Kemal Demir',
      unvan: 'İşletme Sahibi',
      telefon: '0216 333 12 12',
      eposta: 'kemal@marmaraun.com',
      durum: 'POTANSIYEL',
      toplamTeklif: '₺80.000',
    },
    {
      id: 4,
      ad: 'Doğa Organik Meyve Suları Sanayi',
      sektor: 'İçecek & Konsantre',
      il: 'İstanbul / Hadımköy',
      yetkili: 'Canan Tunç',
      unvan: 'Fabrika Müdürü',
      telefon: '0212 777 88 99',
      eposta: 'canan@dogaorganik.com',
      durum: 'AKTIF_MUSTERI',
      toplamTeklif: '₺540.000',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Başlık ve Buton */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2.5">
            <Building2 className="w-6 h-6 text-primary-400" />
            <span>Müşteriler & Firma Portföyü</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Yetkili temsilci izolasyonlu müşteri ağı ve fabrika bağlantıları
          </p>
        </div>

        <button className="inline-flex items-center gap-2 bg-primary-600 hover:bg-primary-500 active:scale-95 text-white text-xs sm:text-sm font-semibold px-4 py-2.5 rounded-xl shadow-md shadow-primary-950/60 transition-all">
          <Plus className="w-4 h-4" />
          <span>Yeni Firma Ekle</span>
        </button>
      </div>

      {/* Arama */}
      <div className="p-3 bg-forest-850/80 rounded-2xl border border-forest-850 flex items-center gap-2">
        <Search className="w-4 h-4 text-slate-400 ml-2" />
        <input
          type="text"
          value={arama}
          onChange={(e) => setArama(e.target.value)}
          placeholder="Firma adı, il veya yetkili ara..."
          className="w-full bg-transparent text-xs text-white placeholder-slate-400 focus:outline-none"
        />
      </div>

      {/* Firma Kartları Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {firmalar
          .filter((f) => f.ad.toLowerCase().includes(arama.toLowerCase()))
          .map((firma) => (
            <div
              key={firma.id}
              className="p-5 rounded-2xl bg-forest-850/90 border border-forest-850 hover:border-primary-600/40 transition-all space-y-4"
            >
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h3 className="text-base font-bold text-white hover:text-primary-300 transition-colors">
                    {firma.ad}
                  </h3>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="text-[11px] font-medium text-primary-400 bg-primary-500/10 px-2 py-0.5 rounded-md border border-primary-500/20">
                      {firma.sektor}
                    </span>
                    <span className="text-xs text-slate-400 flex items-center gap-1">
                      <MapPin className="w-3 h-3 text-slate-500" />
                      {firma.il}
                    </span>
                  </div>
                </div>

                <span
                  className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                    firma.durum === 'AKTIF_MUSTERI'
                      ? 'bg-primary-500/20 text-primary-300 border border-primary-500/30'
                      : firma.durum === 'NUMUNE_ASAMASINDA'
                      ? 'bg-gold-500/20 text-gold-500 border border-gold-500/30'
                      : 'bg-slate-800 text-slate-300 border border-slate-700'
                  }`}
                >
                  {firma.durum === 'AKTIF_MUSTERI'
                    ? 'Aktif Müşteri'
                    : firma.durum === 'NUMUNE_ASAMASINDA'
                    ? 'Numune Sürecinde'
                    : 'Potansiyel'}
                </span>
              </div>

              {/* Yetkili & İletişim Bilgileri */}
              <div className="p-3 bg-forest-900/80 rounded-xl border border-forest-850 text-xs space-y-1.5">
                <div className="flex items-center justify-between text-slate-200">
                  <span className="flex items-center gap-1.5 font-medium">
                    <User className="w-3.5 h-3.5 text-primary-400" />
                    {firma.yetkili}
                  </span>
                  <span className="text-[11px] text-slate-400 font-mono">
                    {firma.unvan}
                  </span>
                </div>
                <div className="flex items-center justify-between text-slate-400 text-[11px] pt-1 border-t border-forest-850/60">
                  <a
                    href={`tel:${firma.telefon}`}
                    className="hover:text-primary-300 flex items-center gap-1"
                  >
                    <Phone className="w-3 h-3" />
                    {firma.telefon}
                  </a>
                  <a
                    href={`mailto:${firma.eposta}`}
                    className="hover:text-primary-300 flex items-center gap-1"
                  >
                    <Mail className="w-3 h-3" />
                    {firma.eposta}
                  </a>
                </div>
              </div>

              {/* Alt Butonlar */}
              <div className="flex items-center justify-between pt-1">
                <span className="text-xs font-semibold text-slate-300">
                  Teklif Hacmi: <strong className="text-white">{firma.toplamTeklif}</strong>
                </span>
                <button className="text-xs font-semibold text-primary-400 hover:text-primary-300 inline-flex items-center gap-1 transition-colors">
                  Firma Detayı <ChevronRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))}
      </div>
    </div>
  );
};

export default CompaniesPage;
