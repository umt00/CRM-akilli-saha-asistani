import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  CalendarCheck,
  CalendarDays,
  Building2,
  FlaskConical,
  FileText,
  FileSpreadsheet,
  ChevronLeft,
  ChevronRight,
  LogOut,
  ShieldCheck,
  ExternalLink,
} from 'lucide-react';
import type { NavItem } from '../../types/navigation.types';

interface SidebarProps {
  collapsed: boolean;
  onToggleCollapse: () => void;
}

export const NAV_ITEMS: NavItem[] = [
  {
    id: 'dashboard',
    label: 'Genel Bakış',
    path: '/',
    icon: LayoutDashboard,
  },
  {
    id: 'ziyaretler',
    label: 'Ziyaretler & Randevular',
    path: '/ziyaretler',
    icon: CalendarCheck,
    badge: '3 Bugün',
    badgeColor: 'primary',
    description: '30 saniyede saha ziyareti kaydı',
  },
  {
    id: 'takvim',
    label: 'Akıllı Takvim',
    path: '/takvim',
    icon: CalendarDays,
    badge: 'Bitrix24',
    badgeColor: 'primary',
    description: 'Ay, hafta ve randevu ajandası',
  },
  {
    id: 'musteriler',
    label: 'Müşteriler & Portföy',
    path: '/musteriler',
    icon: Building2,
    badge: '142',
    description: 'Bayi, fabrika ve müşteri yönetimi',
  },
  {
    id: 'numuneler',
    label: 'Numune & Tadım',
    path: '/numuneler',
    icon: FlaskConical,
    badge: '2 Bekleyen',
    badgeColor: 'gold',
    description: 'Gıda numuneleri ve tadım onayları',
  },
  {
    id: 'teklifler',
    label: 'Teklif Yönetimi',
    path: '/teklifler',
    icon: FileText,
  },
  {
    id: 'raporlar',
    label: 'Excel & Raporlar',
    path: '/raporlar',
    icon: FileSpreadsheet,
    description: '17 Kolonluk canlı aktarım',
  },
];

export const Sidebar: React.FC<SidebarProps> = ({ collapsed, onToggleCollapse }) => {
  return (
    <aside
      className={`hidden md:flex flex-col justify-between h-screen sticky top-0 bg-forest-900 border-r border-forest-850/90 transition-all duration-300 z-30 select-none ${
        collapsed ? 'w-20' : 'w-64'
      }`}
    >
      {/* Üst Kısım: Logo ve Başlık */}
      <div>
        <div className="h-16 px-4 flex items-center justify-between border-b border-forest-850/80">
          {!collapsed ? (
            <div className="flex items-center gap-3 overflow-hidden">
              <div className="bg-white/95 px-2 py-1 rounded-lg border border-primary-500/20 shadow-sm flex-shrink-0">
                <img
                  src="/logo.png"
                  alt="Yeşiller Gıda"
                  className="h-7 w-auto object-contain"
                />
              </div>
              <div className="flex flex-col min-w-0">
                <span className="text-sm font-bold text-white tracking-tight truncate">
                  Akıllı Saha
                </span>
                <span className="text-[10px] text-primary-400 font-medium tracking-wide uppercase">
                  Yeşiller Gıda CRM
                </span>
              </div>
            </div>
          ) : (
            <div className="mx-auto bg-white/95 p-1 rounded-lg border border-primary-500/20 shadow-sm">
              <img
                src="/logo.png"
                alt="Yeşiller Gıda"
                className="h-6 w-auto object-contain"
              />
            </div>
          )}

          <button
            onClick={onToggleCollapse}
            aria-label={collapsed ? 'Menüyü Genişlet' : 'Menüyü Daralt'}
            className="hidden lg:flex items-center justify-center w-7 h-7 rounded-lg bg-forest-850 hover:bg-forest-850/80 text-slate-400 hover:text-white border border-forest-850 transition-colors"
          >
            {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
          </button>
        </div>

        {/* Menü Linkleri */}
        <nav className="p-3 space-y-1.5 overflow-y-auto max-h-[calc(100vh-160px)]">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.id}
                to={item.path}
                end={item.path === '/'}
                className={({ isActive }) =>
                  `group flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-primary-600 text-white shadow-md shadow-primary-950/60 font-semibold'
                      : 'text-slate-300 hover:text-white hover:bg-forest-850/80'
                  } ${collapsed ? 'justify-center px-2' : ''}`
                }
                title={collapsed ? item.label : undefined}
              >
                {({ isActive }) => (
                  <>
                    <Icon
                      className={`w-5 h-5 flex-shrink-0 transition-transform group-hover:scale-110 ${
                        isActive ? 'text-white' : 'text-slate-400 group-hover:text-primary-300'
                      }`}
                    />
                    {!collapsed && (
                      <div className="flex items-center justify-between flex-1 min-w-0">
                        <span className="truncate">{item.label}</span>
                        {item.badge && (
                          <span
                            className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider ${
                              isActive
                                ? 'bg-white/20 text-white'
                                : item.badgeColor === 'gold'
                                ? 'bg-gold-500/20 text-gold-500 border border-gold-500/30'
                                : 'bg-primary-500/20 text-primary-300 border border-primary-500/30'
                            }`}
                          >
                            {item.badge}
                          </span>
                        )}
                      </div>
                    )}
                  </>
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Alt Kısım: Kullanıcı ve Çıkış */}
      <div className="p-3 border-t border-forest-850/80 bg-forest-900/60">
        {!collapsed ? (
          <div className="flex items-center justify-between gap-2 p-2 rounded-xl bg-forest-850/80 border border-forest-850">
            <div className="flex items-center gap-2.5 min-w-0">
              <div className="w-8 h-8 rounded-lg bg-primary-600/30 border border-primary-500/40 text-primary-300 flex items-center justify-center font-bold text-xs flex-shrink-0">
                UA
              </div>
              <div className="flex flex-col min-w-0">
                <span className="text-xs font-semibold text-white truncate">Umut Arslan</span>
                <span className="text-[10px] text-primary-400 font-mono flex items-center gap-1 truncate">
                  <ShieldCheck className="w-3 h-3 text-primary-400 inline" />
                  YÖNETİCİ
                </span>
              </div>
            </div>
            <button
              title="Çıkış Yap"
              className="p-1.5 rounded-lg text-slate-400 hover:text-red-400 hover:bg-forest-900 transition-colors"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-2">
            <div
              title="Umut Arslan (YÖNETİCİ)"
              className="w-8 h-8 rounded-lg bg-primary-600/30 border border-primary-500/40 text-primary-300 flex items-center justify-center font-bold text-xs cursor-pointer"
            >
              UA
            </div>
            <button
              title="Çıkış Yap"
              className="p-1.5 rounded-lg text-slate-400 hover:text-red-400 hover:bg-forest-850 transition-colors"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        )}

        {!collapsed && (
          <div className="mt-2 text-center">
            <a
              href="https://www.yesillergida.com.tr"
              target="_blank"
              rel="noreferrer"
              className="text-[10px] text-slate-500 hover:text-primary-400 inline-flex items-center gap-1 transition-colors"
            >
              yesillergida.com.tr <ExternalLink className="w-2.5 h-2.5" />
            </a>
          </div>
        )}
      </div>
    </aside>
  );
};
