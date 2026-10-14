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
} from 'lucide-react';
import type { Rol, KullaniciOzetiYaniti } from './types/crm.types';

export const App: React.FC = () => {
  const [sayac, setSayac] = useState<number>(0);

  // Backend DTO tip uyumluluk testi (3. Kişi için tip güvenliği kanıtı)
  const ornekRol: Rol = 'YONETICI';
  const ornekKullanici: KullaniciOzetiYaniti = {
    id: 1,
    adSoyad: 'Umut Arslan',
    eposta: 'umut@akillisaha.com',
    rol: ornekRol,
    telefon: '0555 123 4567',
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 text-slate-100 flex flex-col justify-between p-4 sm:p-8">
      {/* Üst Başlık & Badge */}
      <header className="max-w-5xl mx-auto w-full pt-4">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-slate-800 pb-6">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-primary-600/20 border border-primary-500/30 flex items-center justify-center text-primary-400 shadow-glow-primary">
              <Sparkles className="w-6 h-6 animate-pulse" />
            </div>
            <div>
              <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
                Akıllı Saha Asistanı
                <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  Adım 1 Hazır
                </span>
              </h1>
              <p className="text-xs text-slate-400 mt-0.5">
                Modern CRM Platformu • 3. Kişi (Frontend Geliştirici) Çalışma Alanı
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 bg-slate-800/80 px-3 py-1.5 rounded-lg border border-slate-700/60 text-xs">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
            <span className="text-slate-300 font-medium">Vite + React 18 + TypeScript + Tailwind</span>
          </div>
        </div>
      </header>

      {/* Ana Gövde & Kartlar */}
      <main className="max-w-5xl mx-auto w-full my-8 space-y-6">
        {/* Hoş Geldin Kahraman Kartı */}
        <div className="bg-gradient-to-r from-primary-950/60 via-slate-900 to-slate-900 p-6 sm:p-8 rounded-2xl border border-primary-900/40 shadow-soft-lg relative overflow-hidden">
          <div className="absolute top-0 right-0 -mr-16 -mt-16 w-64 h-64 bg-primary-500/5 rounded-full blur-3xl pointer-events-none" />
          
          <div className="max-w-2xl space-y-3">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary-400 uppercase tracking-wider">
              <Layers className="w-4 h-4" /> Temel Altyapı Doğrulandı
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
              Saha Satışını 30 Saniyeye İndiren Modern Arayüz Doğuyor
            </h2>
            <p className="text-sm text-slate-300 leading-relaxed">
              Arka planda Java 21, Spring Boot 3 ve PostgreSQL motoru hazır. 
              Frontend ortamımız; katı tip güvenliği, sıfır derleme hatası ve yüksek performanslı Tailwind CSS mimarisiyle başarıyla kuruldu.
            </p>
          </div>

          <div className="mt-6 flex flex-wrap gap-3">
            <button
              onClick={() => setSayac((prev) => prev + 1)}
              className="inline-flex items-center gap-2 bg-primary-600 hover:bg-primary-500 active:scale-95 text-white text-sm font-semibold px-4 py-2.5 rounded-xl transition-all shadow-md shadow-primary-900/30"
            >
              <span>Reaktivite Testi: {sayac} Tık</span>
              <ChevronRight className="w-4 h-4" />
            </button>

            <div className="inline-flex items-center gap-2 bg-slate-800/80 text-slate-300 text-xs px-3.5 py-2 rounded-xl border border-slate-700/50">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              <span>JWT & Axios Hazır</span>
            </div>
          </div>
        </div>

        {/* 3 Sütunlu Durum Matrisi */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Kart 1: Tip Güvenliği */}
          <div className="bg-slate-850/80 p-5 rounded-xl border border-slate-800 hover:border-slate-700 transition-colors">
            <div className="w-9 h-9 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center mb-3">
              <CheckCircle2 className="w-5 h-5" />
            </div>
            <h3 className="text-sm font-semibold text-white">DTO Tip Uyumu (Type Safety)</h3>
            <p className="text-xs text-slate-400 mt-1">
              Backend ile frontend tipleri tek noktadan bağlanır. Örnek Kullanıcı:
            </p>
            <div className="mt-3 p-2.5 bg-slate-900 rounded-lg border border-slate-800 text-xs font-mono text-slate-300">
              <div><span className="text-slate-500">Ad:</span> {ornekKullanici.adSoyad}</div>
              <div><span className="text-slate-500">Rol:</span> <span className="text-primary-400 font-semibold">{ornekKullanici.rol}</span></div>
            </div>
          </div>

          {/* Kart 2: 17 Kolonluk Excel Entegrasyonu */}
          <div className="bg-slate-850/80 p-5 rounded-xl border border-slate-800 hover:border-slate-700 transition-colors">
            <div className="w-9 h-9 rounded-lg bg-primary-500/10 text-primary-400 flex items-center justify-center mb-3">
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <h3 className="text-sm font-semibold text-white">17 Kolonluk Excel Köprüsü</h3>
            <p className="text-xs text-slate-400 mt-1">
              Sahadaki Excel karmaşasını bitiren Apache POI entegrasyonu hazırlandı.
            </p>
            <div className="mt-3 flex items-center gap-2 text-xs text-emerald-400 font-medium">
              <Database className="w-3.5 h-3.5" />
              <span>Streaming SXSSFWorkbook Aktif</span>
            </div>
          </div>

          {/* Kart 3: Mobil Öncelikli PWA */}
          <div className="bg-slate-850/80 p-5 rounded-xl border border-slate-800 hover:border-slate-700 transition-colors">
            <div className="w-9 h-9 rounded-lg bg-amber-500/10 text-amber-400 flex items-center justify-center mb-3">
              <Smartphone className="w-5 h-5" />
            </div>
            <h3 className="text-sm font-semibold text-white">Mobil Öncelikli PWA</h3>
            <p className="text-xs text-slate-400 mt-1">
              Dokunmatik ekran ergonomisi, tek elle 30 saniyede hızlı ziyaret girişi.
            </p>
            <div className="mt-3 flex items-center gap-1.5 text-xs text-amber-300 font-medium">
              <Users className="w-3.5 h-3.5" />
              <span>Plasiyer Dostu Ergonomi</span>
            </div>
          </div>
        </div>

        {/* Sıradaki Adım Rehberi */}
        <div className="bg-slate-900 p-4 rounded-xl border border-slate-800 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-xs">
          <div className="flex items-center gap-2 text-slate-300">
            <span className="font-semibold text-primary-400">Sıradaki Adım:</span>
            <span>Adım 2: Tasarım Sistemi, Sidebar, Mobil Alt Menü ve Çekirdek Bileşenler</span>
          </div>
          <span className="text-slate-500 flex items-center gap-1 font-mono">
            Adım 1/9 Tamamlanıyor <ArrowRight className="w-3.5 h-3.5" />
          </span>
        </div>
      </main>

      {/* Alt Bilgi */}
      <footer className="max-w-5xl mx-auto w-full pt-4 border-t border-slate-800 text-center text-xs text-slate-500">
        CRM Akıllı Saha Asistanı • Frontend Geliştirme Ortamı © 2026
      </footer>
    </div>
  );
};

export default App;
