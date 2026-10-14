import React, { useState } from 'react';
import {
  Search,
  Bell,
  Plus,
  CalendarPlus,
  FlaskConical,
  X,
  CheckCircle2,
} from 'lucide-react';

interface HeaderProps {
  onQuickAction?: (actionType: 'ziyaret' | 'numune') => void;
}

export const Header: React.FC<HeaderProps> = ({ onQuickAction }) => {
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [showNotifications, setShowNotifications] = useState(false);
  const [showQuickModal, setShowQuickModal] = useState(false);

  return (
    <header className="h-16 sticky top-0 z-20 bg-forest-900/90 backdrop-blur-md border-b border-forest-850 px-4 sm:px-6 flex items-center justify-between gap-4">
      {/* Sol: Mobilde Logo, Masaüstünde Arama */}
      <div className="flex items-center gap-3 flex-1 max-w-md">
        {/* Mobilde Logo */}
        <div className="md:hidden flex items-center gap-2.5">
          <div className="bg-white/95 px-2 py-1 rounded-lg border border-primary-500/20 shadow-sm">
            <img
              src="/logo.png"
              alt="Yeşiller Gıda"
              className="h-6 w-auto object-contain"
            />
          </div>
          <span className="text-sm font-bold text-white tracking-tight truncate">
            Akıllı Saha
          </span>
        </div>

        {/* Masaüstü Arama Çubuğu */}
        <div className="hidden md:flex items-center w-full relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Müşteri, yetkili veya numune ara... (Ctrl + K)"
            className="w-full bg-forest-850 border border-forest-850 focus:border-primary-500/60 rounded-xl pl-9 pr-12 py-2 text-xs text-slate-200 placeholder-slate-400 focus:outline-none focus:ring-1 focus:ring-primary-500/50 transition-all"
          />
          <kbd className="hidden lg:inline-flex items-center gap-0.5 absolute right-2.5 px-1.5 py-0.5 text-[10px] font-mono text-slate-400 bg-forest-900 border border-forest-850 rounded">
            ⌘K
          </kbd>
        </div>
      </div>

      {/* Sağ: Hızlı Aksiyon, Bildirim ve Profil */}
      <div className="flex items-center gap-2 sm:gap-3">
        {/* Mobilde Arama Butonu */}
        <button
          onClick={() => setSearchOpen(!searchOpen)}
          aria-label="Arama Yap"
          className="md:hidden p-2 rounded-xl text-slate-300 hover:text-white bg-forest-850 border border-forest-850"
        >
          <Search className="w-4 h-4" />
        </button>

        {/* Sahada Hızlı Kayıt Butonu */}
        <div className="relative">
          <button
            onClick={() => setShowQuickModal(!showQuickModal)}
            className="inline-flex items-center gap-1.5 bg-primary-600 hover:bg-primary-500 active:scale-95 text-white text-xs font-semibold px-3 py-2 rounded-xl shadow-md shadow-primary-950/60 transition-all"
          >
            <Plus className="w-4 h-4" />
            <span className="hidden sm:inline">Hızlı Giriş</span>
          </button>

          {/* Hızlı Giriş Açılır Menüsü */}
          {showQuickModal && (
            <div className="absolute right-0 mt-2 w-56 bg-forest-850 border border-primary-900/50 rounded-xl shadow-xl p-2 z-50">
              <div className="text-[11px] font-bold text-slate-400 px-2 py-1 uppercase tracking-wider">
                Saha Hızlı Aksiyon
              </div>
              <button
                onClick={() => {
                  setShowQuickModal(false);
                  onQuickAction?.('ziyaret');
                }}
                className="w-full flex items-center gap-2.5 px-2.5 py-2 text-xs text-slate-200 hover:text-white hover:bg-forest-900 rounded-lg transition-colors text-left"
              >
                <div className="p-1 rounded-md bg-primary-500/20 text-primary-300">
                  <CalendarPlus className="w-4 h-4" />
                </div>
                <div>
                  <div className="font-medium">Ziyaret Kaydı Yap</div>
                  <div className="text-[10px] text-slate-400">30 saniyede hızlı form</div>
                </div>
              </button>
              <button
                onClick={() => {
                  setShowQuickModal(false);
                  onQuickAction?.('numune');
                }}
                className="w-full flex items-center gap-2.5 px-2.5 py-2 text-xs text-slate-200 hover:text-white hover:bg-forest-900 rounded-lg transition-colors text-left"
              >
                <div className="p-1 rounded-md bg-gold-500/20 text-gold-500">
                  <FlaskConical className="w-4 h-4" />
                </div>
                <div>
                  <div className="font-medium">Numune Teslim Et</div>
                  <div className="text-[10px] text-slate-400">Tadım & deneme takibi</div>
                </div>
              </button>
            </div>
          )}
        </div>

        {/* Bildirim Çanı */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            aria-label="Bildirimler"
            className="p-2 rounded-xl text-slate-300 hover:text-white bg-forest-850 border border-forest-850 hover:border-primary-900/60 relative transition-colors"
          >
            <Bell className="w-4 h-4" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-gold-500 animate-ping" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-gold-500" />
          </button>

          {/* Bildirim Paneli */}
          {showNotifications && (
            <div className="absolute right-0 mt-2 w-72 sm:w-80 bg-forest-850 border border-primary-900/50 rounded-2xl shadow-2xl p-3 z-50">
              <div className="flex items-center justify-between pb-2 border-b border-forest-900">
                <span className="text-xs font-bold text-white">Saha Bildirimleri</span>
                <span className="text-[10px] text-primary-400 font-semibold bg-primary-500/15 px-2 py-0.5 rounded-full">
                  2 Yeni
                </span>
              </div>
              <div className="mt-2 space-y-2">
                <div className="p-2 rounded-xl bg-forest-900/80 border border-forest-850 text-xs">
                  <div className="flex items-center gap-1.5 text-primary-300 font-semibold">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>Ziyaret Saati Yaklaştı</span>
                  </div>
                  <p className="text-slate-300 text-[11px] mt-1">
                    Saat 14:30'da <strong>Balküpü Şeker Fabrikası</strong> randevunuz var.
                  </p>
                </div>
                <div className="p-2 rounded-xl bg-forest-900/80 border border-forest-850 text-xs">
                  <div className="flex items-center gap-1.5 text-gold-500 font-semibold">
                    <FlaskConical className="w-3.5 h-3.5" />
                    <span>Numune Tadım Günü</span>
                  </div>
                  <p className="text-slate-300 text-[11px] mt-1">
                    <strong>Anadolu Bisküvi</strong> fındık aroması tadım sonucu bekleniyor.
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Plasiyer / Kullanıcı Rozeti */}
        <div className="hidden sm:flex items-center gap-2 pl-2 border-l border-forest-850">
          <div className="w-8 h-8 rounded-xl bg-primary-600/30 border border-primary-500/40 text-primary-300 flex items-center justify-center font-bold text-xs">
            UA
          </div>
          <div className="hidden lg:block text-left">
            <div className="text-xs font-semibold text-white leading-tight">Umut Arslan</div>
            <div className="text-[10px] text-slate-400">Yeşiller Gıda Saha</div>
          </div>
        </div>
      </div>

      {/* Mobilde Açılan Arama Çubuğu */}
      {searchOpen && (
        <div className="md:hidden absolute top-16 left-0 right-0 p-3 bg-forest-900 border-b border-forest-850 shadow-xl flex items-center gap-2">
          <Search className="w-4 h-4 text-slate-400" />
          <input
            type="text"
            autoFocus
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Müşteri, telefon veya numune ara..."
            className="flex-1 bg-forest-850 border border-forest-850 rounded-lg px-3 py-1.5 text-xs text-white placeholder-slate-400 focus:outline-none"
          />
          <button
            onClick={() => setSearchOpen(false)}
            className="p-1.5 text-slate-400 hover:text-white"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}
    </header>
  );
};
