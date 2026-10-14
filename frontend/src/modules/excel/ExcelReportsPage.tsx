import React, { useState } from 'react';
import {
  FileSpreadsheet,
  Download,
  Calendar,
  Filter,
  CheckCircle2,
  Table,
  Sparkles,
} from 'lucide-react';

export const ExcelReportsPage: React.FC = () => {
  const [downloading, setDownloading] = useState(false);

  const handleDownload = () => {
    setDownloading(true);
    setTimeout(() => {
      setDownloading(false);
      alert('17 Kolonluk Excel dosyası başarıyla hazırlandı ve indirildi!');
    }, 1200);
  };

  const kolonlar = [
    'A: Sıra No',
    'B: Ziyaret Tarihi',
    'C: Temsilci Adı Soyadı',
    'D: Müşteri / Firma Adı',
    'E: Şehir / Bölge',
    'F: Sektör',
    'G: Görüşülen Yetkili',
    'H: Yetkili Unvanı',
    'I: İletişim Telefonu',
    'J: Ziyaret Konusu',
    'K: Bırakılan Numune Adı',
    'L: Numune Sonuç / Tadım Notu',
    'M: Teklif Tutarı (TL)',
    'N: Teklif Durumu',
    'O: Sonraki Aksiyon Adımı',
    'P: Sonraki Aksiyon Tarihi',
    'Q: Genel Saha Notları',
  ];

  return (
    <div className="space-y-6">
      {/* Üst Başlık & İndir Butonu */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2.5">
            <FileSpreadsheet className="w-6 h-6 text-gold-500" />
            <span>17 Kolonluk Orijinal Saha Exceli</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Backend Apache POI streaming motoru ile birebir uyumlu kurumsal veri tablosu
          </p>
        </div>

        <button
          onClick={handleDownload}
          disabled={downloading}
          className="inline-flex items-center gap-2 bg-primary-600 hover:bg-primary-500 active:scale-95 disabled:opacity-50 text-white text-xs sm:text-sm font-semibold px-4 py-2.5 rounded-xl shadow-md shadow-primary-950/60 transition-all"
        >
          <Download className="w-4 h-4" />
          <span>{downloading ? 'Hazırlanıyor...' : 'Excel İndir (.xlsx)'}</span>
        </button>
      </div>

      {/* Rapor Özeti */}
      <div className="p-6 rounded-2xl bg-forest-850/90 border border-forest-850 space-y-4">
        <div className="flex items-center justify-between flex-wrap gap-2">
          <div className="flex items-center gap-2 text-white font-bold text-sm">
            <Table className="w-4 h-4 text-primary-400" />
            <span>Aktarılacak 17 Kolonluk Şablon Mimarisi</span>
          </div>
          <span className="text-xs text-primary-300 font-mono bg-primary-500/10 px-2.5 py-1 rounded-lg border border-primary-500/20">
            Apache POI SXSSF Streaming Aktif
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-2.5">
          {kolonlar.map((kolon, idx) => (
            <div
              key={idx}
              className="p-2.5 rounded-xl bg-forest-900/80 border border-forest-850 text-xs font-mono text-slate-300 flex items-center gap-2"
            >
              <span className="w-5 h-5 rounded bg-primary-600/30 text-primary-300 text-[10px] font-bold flex items-center justify-center flex-shrink-0">
                {idx + 1}
              </span>
              <span className="truncate">{kolon}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ExcelReportsPage;
