import React from 'react';
import {
  FileText,
  Plus,
  TrendingUp,
  Building2,
  Calendar,
  CheckCircle2,
  Clock,
  ArrowRight,
} from 'lucide-react';

export const OffersPage: React.FC = () => {
  const teklifler = [
    {
      id: 1,
      no: 'TEK-2026-104',
      firma: 'Balküpü Şeker ve Gıda Sanayi',
      tutar: '₺320.000',
      paraBirimi: 'TL',
      tarih: '10 Ekim 2026',
      gecerlilik: '24 Ekim 2026',
      durum: 'MUSTERIDE_BEKLIYOR',
      urunler: 'Doğal Karamel Aroması (500 Kg), Vanilya Ekstratı (100 Kg)',
    },
    {
      id: 2,
      no: 'TEK-2026-098',
      firma: 'Tat Unlu Mamulleri',
      tutar: '₺165.000',
      paraBirimi: 'TL',
      tarih: '04 Ekim 2026',
      gecerlilik: '18 Ekim 2026',
      durum: 'ONAYLANDI',
      urunler: 'Madagaskar Vanilya Ekstratı (25 Kg Deneme Partisi)',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Üst Başlık */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2.5">
            <FileText className="w-6 h-6 text-primary-400" />
            <span>Teklif Yönetimi</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Gıda aroması satış teklifleri, revizyonlar ve kapanış hacimleri
          </p>
        </div>

        <button className="inline-flex items-center gap-2 bg-primary-600 hover:bg-primary-500 active:scale-95 text-white text-xs sm:text-sm font-semibold px-4 py-2.5 rounded-xl shadow-md shadow-primary-950/60 transition-all">
          <Plus className="w-4 h-4" />
          <span>Yeni Teklif Hazırla</span>
        </button>
      </div>

      {/* Teklif Kartları */}
      <div className="space-y-3">
        {teklifler.map((teklif) => (
          <div
            key={teklif.id}
            className="p-5 rounded-2xl bg-forest-850/90 border border-forest-850 hover:border-primary-600/40 transition-all space-y-3"
          >
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-mono text-primary-400 font-bold">
                    {teklif.no}
                  </span>
                  <span className="text-slate-500">•</span>
                  <span className="text-xs text-slate-400 flex items-center gap-1">
                    <Calendar className="w-3 h-3 text-slate-500" />
                    {teklif.tarih}
                  </span>
                </div>
                <h3 className="text-base font-bold text-white mt-1">
                  {teklif.firma}
                </h3>
              </div>

              <div className="text-right">
                <div className="text-xl font-black text-white font-mono">
                  {teklif.tutar}
                </div>
                {teklif.durum === 'ONAYLANDI' ? (
                  <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-primary-500/20 text-primary-300 border border-primary-500/30">
                    <CheckCircle2 className="w-3 h-3" /> Onaylandı
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-gold-500/20 text-gold-500 border border-gold-500/30">
                    <Clock className="w-3 h-3" /> Değerlendirmede
                  </span>
                )}
              </div>
            </div>

            <div className="p-3 bg-forest-900/80 rounded-xl border border-forest-850 text-xs text-slate-300">
              <span className="font-semibold text-slate-400">Ürün Kalemleri: </span>
              {teklif.urunler}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default OffersPage;
