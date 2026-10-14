import React, { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { BottomNav } from './BottomNav';

export const AppLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState<boolean>(false);
  const navigate = useNavigate();

  const handleQuickAction = (actionType: 'ziyaret' | 'numune') => {
    if (actionType === 'ziyaret') {
      navigate('/ziyaretler?yeni=1');
    } else if (actionType === 'numune') {
      navigate('/numuneler?yeni=1');
    }
  };

  return (
    <div className="min-h-screen flex bg-forest-950 text-slate-100 font-sans antialiased">
      {/* Masaüstü Sol Menü (Sidebar) */}
      <Sidebar
        collapsed={collapsed}
        onToggleCollapse={() => setCollapsed((prev) => !prev)}
      />

      {/* Sağ Ana Alan (Header + İçerik) */}
      <div className="flex-1 flex flex-col min-w-0 min-h-screen overflow-x-hidden">
        {/* Üst Çubuk */}
        <Header onQuickAction={handleQuickAction} />

        {/* Dinamik Sayfa İçeriği */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8 pb-24 md:pb-8 max-w-7xl w-full mx-auto animate-fadeIn">
          <Outlet />
        </main>

        {/* Mobil Alt Menü (Bottom Navigation) */}
        <BottomNav />
      </div>
    </div>
  );
};

export default AppLayout;
