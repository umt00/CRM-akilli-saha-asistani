import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  CalendarCheck,
  Building2,
  FlaskConical,
  FileSpreadsheet,
} from 'lucide-react';
import type { BottomNavItem } from '../../types/navigation.types';

export const BOTTOM_NAV_ITEMS: BottomNavItem[] = [
  {
    id: 'dashboard',
    label: 'Özet',
    path: '/',
    icon: LayoutDashboard,
  },
  {
    id: 'ziyaretler',
    label: 'Ziyaretler',
    path: '/ziyaretler',
    icon: CalendarCheck,
    badge: 3,
  },
  {
    id: 'musteriler',
    label: 'Müşteriler',
    path: '/musteriler',
    icon: Building2,
  },
  {
    id: 'numuneler',
    label: 'Numune',
    path: '/numuneler',
    icon: FlaskConical,
    badge: 2,
  },
  {
    id: 'raporlar',
    label: 'Raporlar',
    path: '/raporlar',
    icon: FileSpreadsheet,
  },
];

export const BottomNav: React.FC = () => {
  return (
    <nav
      aria-label="Mobil Alt Menü"
      className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-forest-900/95 backdrop-blur-lg border-t border-forest-850 px-2 py-1.5 shadow-2xl safe-area-pb"
    >
      <div className="flex items-center justify-around max-w-lg mx-auto">
        {BOTTOM_NAV_ITEMS.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.id}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) =>
                `relative flex flex-col items-center justify-center py-1 px-2.5 rounded-xl transition-all duration-200 select-none ${
                  isActive
                    ? 'text-primary-300 font-semibold'
                    : 'text-slate-400 hover:text-slate-200 active:scale-95'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <div
                    className={`relative p-1 rounded-lg transition-colors ${
                      isActive ? 'bg-primary-600/25 text-primary-300' : 'text-slate-400'
                    }`}
                  >
                    <Icon className="w-5 h-5" />
                    {typeof item.badge === 'number' && item.badge > 0 && (
                      <span className="absolute -top-1 -right-1.5 min-w-[16px] h-4 px-1 rounded-full bg-primary-500 text-white text-[10px] font-bold flex items-center justify-center border border-forest-900 shadow-sm">
                        {item.badge}
                      </span>
                    )}
                  </div>
                  <span className="text-[11px] mt-0.5 tracking-tight truncate">
                    {item.label}
                  </span>
                  {isActive && (
                    <span className="w-1.5 h-1.5 rounded-full bg-primary-400 mt-0.5 animate-pulse" />
                  )}
                </>
              )}
            </NavLink>
          );
        })}
      </div>
    </nav>
  );
};
