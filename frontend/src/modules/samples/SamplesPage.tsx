import React, { useState } from 'react';
import {
  FlaskConical,
  Plus,
  CheckCircle2,
  Clock,
  AlertCircle,
  Building2,
  FileSpreadsheet,
  ChevronRight,
  Sparkles,
} from 'lucide-react';

export const SamplesPage: React.FC = () => {
  const [filter, setFilter] = useState('HEPSI');

  const numuneler = [
    {
      id: 1,
      kod: 'NUM-2026-042',
      urunAdi: 'Doğal Sıvı Karamel Aroması',
      miktar: '500 ml',
      musteri: 'Balküpü Şeker ve Gıda Sanayi',
      temsilci: 'Umut Arslan',
      teslimTarihi: '12 Ekim 2026',
      tadimTarihi: '15 Ekim 2026',
      durum: 'TADIM_BEKLENIYOR',
      notlar: 'Üretim hattında kek harcında denenecek.',
    },
    {
      id: 2,
      kod: 'NUM-2026-039',
      urunAdi: 'Fındık Konsantresi (Ekstra Yağlı)',
      miktar: '2 kg',
      musteri: 'Anadolu Bisküvi ve Çikolata A.Ş.',
      temsilci: 'Umut Arslan',
      teslimTarihi: '08 Ekim 2026',
      tadimTarihi: '14 Ekim 2026',
      durum: 'TADIM_BEKLENIYOR',
      notlar: 'Bisküvi hamuru ve krema dolgusu tadımı yapılacak.',
    },
    {
      id: 3,
      kod: 'NUM-2026-031',
      urunAdi: 'Madagaskar Vanilya Ekstratı',
      miktar: '1 kg',
      musteri: 'Tat Unlu Mamulleri',
      temsilci: 'Umut Arslan',
      teslimTarihi: '01 Ekim 2026',
      tadimTarihi: '06 Ekim 2026',
      durum: 'ONAYLANDI',
      notlar: 'Ar-Ge ekibi aromadan çok memnun kaldı, 25 kg sipariş için teklif istendi.',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Üst Başlık */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2.5">
            <FlaskConical className="w-6 h-6 text-gold-500" />
            <span>Gıda Numune & Tadım Takibi</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            Gıda aroması numune teslimatı, Ar-Ge tadım günleri ve siparişe dönüşüm süreci
          </p>
        </div>

        <button className="inline-flex items-center gap-2 bg-primary-600 hover:bg-primary-500 active:scale-95 text-white text-xs sm:text-sm font-semibold px-4 py-2.5 rounded-xl shadow-md shadow-primary-950/60 transition-all">
          <Plus className="w-4 h-4" />
          <span>Yeni Numune Teslim Et</span>
        </button>
      </div>

      {/* Durum Filtreleri */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {['HEPSI', 'TADIM_BEKLENIYOR', 'ONAYLANDI'].map((d) => (
          <button
            key={d}
            onClick={() => setFilter(d)}
            className={`text-xs px-3.5 py-1.5 rounded-xl font-medium transition-all ${
              filter === d
                ? 'bg-gold-500 text-forest-950 font-bold'
                : 'bg-forest-850 text-slate-300 hover:text-white border border-forest-850'
            }`}
          >
            {d === 'HEPSI'
              ? 'Tüm Numuneler'
              : d === 'TADIM_BEKLENIYOR'
              ? 'Tadım Bekleyenler'
              : 'Onaylananlar'}
          </button>
        ))}
      </div>

      {/* Numune Kartları */}
      <div className="space-y-3">
        {numuneler
          .filter((n) => filter === 'HEPSI' || n.durum === filter)
          .map((numune) => (
            <div
              key={numune.id}
              className="p-5 rounded-2xl bg-forest-850/90 border border-forest-850 hover:border-gold-500/40 transition-all space-y-3"
            >
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-xl bg-forest-900 border border-gold-500/30 flex items-center justify-center text-gold-500 flex-shrink-0">
                    <FlaskConical className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-[11px] font-mono text-slate-400">
                        {numune.kod}
                      </span>
                      <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-forest-900 border border-forest-850 text-slate-300">
                        {numune.miktar}
                      </span>
                    </div>
                    <h3 className="text-sm font-bold text-white mt-0.5">
                      {numune.urunAdi}
                    </h3>
                  </div>
                </div>

                {numune.durum === 'ONAYLANDI' ? (
                  <span className="inline-flex items-center gap-1 text-xs font-bold px-3 py-1 rounded-full bg-primary-500/20 text-primary-300 border border-primary-500/30">
                    <CheckCircle2 className="w-3.5 h-3.5" /> Tadım Onaylandı
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 text-xs font-bold px-3 py-1 rounded-full bg-gold-500/20 text-gold-500 border border-gold-500/30">
                    <Clock className="w-3.5 h-3.5" /> Tadım Bekleniyor
                  </span>
                )}
              </div>

              {/* Müşteri ve Süreç Detayları */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
                <div className="p-2.5 rounded-xl bg-forest-900/80 border border-forest-850">
                  <div className="text-slate-400 text-[11px] flex items-center gap-1">
                    <Building2 className="w-3 h-3 text-slate-500" />
                    Müşteri Firma
                  </div>
                  <div className="text-slate-100 font-semibold mt-0.5">
                    {numune.musteri}
                  </div>
                </div>

                <div className="p-2.5 rounded-xl bg-forest-900/80 border border-forest-850">
                  <div className="text-slate-400 text-[11px] flex items-center gap-1">
                    <Clock className="w-3 h-3 text-slate-500" />
                    Planlanan Tadım Günü
                  </div>
                  <div className="text-gold-500 font-semibold mt-0.5">
                    {numune.tadimTarihi}
                  </div>
                </div>
              </div>

              {/* Not Alanı */}
              <div className="p-3 bg-forest-900/40 rounded-xl border border-forest-850/60 text-xs text-slate-300">
                <span className="font-semibold text-slate-400">Tadım/Deneme Notu: </span>
                {numune.notlar}
              </div>
            </div>
          ))}
      </div>
    </div>
  );
};

export default SamplesPage;
