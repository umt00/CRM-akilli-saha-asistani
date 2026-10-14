import React, { useState } from 'react';
import {
  Sparkles,
  Layers,
  ShieldCheck,
  CheckCircle2,
  FileSpreadsheet,
  Users,
  Smartphone,
  ChevronRight,
  Database,
  ArrowRight,
  ExternalLink,
} from 'lucide-react';
import type { Rol, KullaniciOzetiYaniti } from './types/crm.types';

export const App: React.FC = () => {
  const [sayac, setSayac] = useState<number>(0);

  // Backend DTO tip uyumluluk testi (Yeşiller Gıda Saha Temsilcisi Profili)
  const ornekRol: Rol = 'YONETICI';
  const ornekKullanici: KullaniciOzetiYaniti = {
    id: 1,
    adSoyad: 'Umut Arslan',
    eposta: 'umut@yesillergida.com.tr',
    rol: ornekRol,
    telefon: '0532 000 0000',
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-forest-950 via-forest-900 to-forest-950 text-slate-100 flex flex-col justify-between p-4 sm:p-8">
      {/* Üst Başlık & Logo */}
      <header className="max-w-5xl mx-auto w-full pt-2">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-forest-850/80 pb-6">
          <div className="flex items-center gap-4">
            {/* Yeşiller Gıda Orijinal Logosu */}
            <div className="bg-white/95 px-3 py-1.5 rounded-xl border border-primary-500/20 shadow-md">
              <img
                src="/logo.png"
                alt="Yeşiller Gıda"
                className="h-9 w-auto object-contain"
              />
            </div>
            <div>
              <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-white flex items-center gap-2">
                Akıllı Saha Asistanı
                <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-primary-500/15 text-primary-300 border border-primary-500/30">
                  Yeşiller Gıda CRM
                </span>
              </h1>
              <p className="text-xs text-slate-400 mt-0.5 flex items-center gap-2">
                <span>Dağıtım & Saha Satış Ağı Portalı</span>
                <span>•</span>
                <a
                  href="https://www.yesillergida.com.tr"
                  target="_blank"
                  rel="noreferrer"
                  className="text-primary-400 hover:text-primary-300 inline-flex items-center gap-1 transition-colors"
                >
                  yesillergida.com.tr <ExternalLink className="w-3 h-3" />
                </a>
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 bg-forest-850 px-3 py-1.5 rounded-lg border border-primary-900/60 text-xs">
            <span className="w-2 h-2 rounded-full bg-primary-400 animate-ping" />
            <span className="text-slate-200 font-medium font-mono">#1d6e3c Kurumsal Yeşil</span>
          </div>
        </div>
      </header>

      {/* Ana Gövde & Kartlar */}
      <main className="max-w-5xl mx-auto w-full my-8 space-y-6">
        {/* Kahraman Banner */}
        <div className="bg-gradient-to-r from-primary-950/70 via-forest-850 to-forest-850 p-6 sm:p-8 rounded-2xl border border-primary-600/30 shadow-glow-primary relative overflow-hidden">
          <div className="absolute top-0 right-0 -mr-20 -mt-20 w-72 h-72 bg-primary-500/10 rounded-full blur-3xl pointer-events-none" />

          <div className="max-w-2xl space-y-3">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary-300 uppercase tracking-wider">
              <Sparkles className="w-4 h-4 text-gold-500" /> Yeşiller Gıda Kurumsal Kimliği Aktif
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Saha Satış & Numune Süreçlerinde Dijital Dönüşüm
            </h2>
            <p className="text-sm text-slate-300 leading-relaxed">
              Yeşiller Gıda'nın resmi kurumsal renk paleti (<span className="text-primary-300 font-semibold font-mono">#1d6e3c</span>) ve 
              tipografisi arayüze başarıyla giydirildi. Saha ekibinin 17 kolonluk Excel formunu 30 saniyede dolduracağı altyapı hazır.
            </p>
          </div>

          <div className="mt-6 flex flex-wrap gap-3">
            <button
              onClick={() => setSayac((prev) => prev + 1)}
              className="inline-flex items-center gap-2 bg-primary-600 hover:bg-primary-500 active:scale-95 text-white text-sm font-semibold px-4 py-2.5 rounded-xl transition-all shadow-md shadow-primary-950/50"
            >
              <span>Etkileşim Testi: {sayac} Tık</span>
              <ChevronRight className="w-4 h-4" />
            </button>

            <div className="inline-flex items-center gap-2 bg-forest-900/90 text-slate-300 text-xs px-3.5 py-2 rounded-xl border border-forest-850">
              <ShieldCheck className="w-4 h-4 text-primary-400" />
              <span>JWT & PostgreSQL Hazır</span>
            </div>
          </div>
        </div>

        {/* 3 Sütunlu Kartlar */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Kart 1 */}
          <div className="bg-forest-850/80 p-5 rounded-xl border border-forest-850 hover:border-primary-700/50 transition-colors">
            <div className="w-9 h-9 rounded-lg bg-primary-500/10 text-primary-400 flex items-center justify-center mb-3">
              <CheckCircle2 className="w-5 h-5" />
            </div>
            <h3 className="text-sm font-semibold text-white">Kurumsal Kullanıcı Profili</h3>
            <p className="text-xs text-slate-400 mt-1">
              Yeşiller Gıda satış ekibi veri izolasyonu:
            </p>
            <div className="mt-3 p-2.5 bg-forest-900 rounded-lg border border-forest-850 text-xs font-mono text-slate-300">
              <div><span className="text-slate-500">Temsilci:</span> {ornekKullanici.adSoyad}</div>
              <div><span className="text-slate-500">Yetki:</span> <span className="text-primary-300 font-semibold">{ornekKullanici.rol}</span></div>
            </div>
          </div>

          {/* Kart 2 */}
          <div className="bg-forest-850/80 p-5 rounded-xl border border-forest-850 hover:border-primary-700/50 transition-colors">
            <div className="w-9 h-9 rounded-lg bg-gold-500/10 text-gold-500 flex items-center justify-center mb-3">
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <h3 className="text-sm font-semibold text-white">17 Kolonluk Excel Entegrasyonu</h3>
            <p className="text-xs text-slate-400 mt-1">
              Gıda ürünleri, numune ve teklif döngüsünün Apache POI aktarımı.
            </p>
            <div className="mt-3 flex items-center gap-2 text-xs text-primary-300 font-medium">
              <Database className="w-3.5 h-3.5" />
              <span>Streaming Excel Aktarımı Aktif</span>
            </div>
          </div>

          {/* Kart 3 */}
          <div className="bg-forest-850/80 p-5 rounded-xl border border-forest-850 hover:border-primary-700/50 transition-colors">
            <div className="w-9 h-9 rounded-lg bg-primary-500/10 text-primary-400 flex items-center justify-center mb-3">
              <Smartphone className="w-5 h-5" />
            </div>
            <h3 className="text-sm font-semibold text-white">Sahada 30 Saniye Ergonomisi</h3>
            <p className="text-xs text-slate-400 mt-1">
              Plasiyer arabadayken tek tıkla ziyaret, numune ve teklif girişi.
            </p>
            <div className="mt-3 flex items-center gap-1.5 text-xs text-primary-300 font-medium">
              <Users className="w-3.5 h-3.5" />
              <span>Yeşiller Gıda Satış Gücü</span>
            </div>
          </div>
        </div>

        {/* Sıradaki Adım Rehberi */}
        <div className="bg-forest-850/90 p-4 rounded-xl border border-primary-900/50 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-xs">
          <div className="flex items-center gap-2 text-slate-300">
            <span className="font-semibold text-primary-300">Sıradaki Adım:</span>
            <span>Adım 2: Yeşiller Gıda Sidebar Menüsü, Mobil Alt Menü ve Ana İskelet</span>
          </div>
          <span className="text-slate-400 flex items-center gap-1 font-mono">
            Renk Güncellemesi Tamamlandı <ArrowRight className="w-3.5 h-3.5" />
          </span>
        </div>
      </main>

      {/* Alt Bilgi */}
      <footer className="max-w-5xl mx-auto w-full pt-4 border-t border-forest-850/80 text-center text-xs text-slate-500">
        Yeşiller Gıda San. ve Tic. Ltd. Şti. • Akıllı Saha Asistanı CRM © 2026
      </footer>
    </div>
  );
};

export default App;
