import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { DashboardPage } from './modules/dashboard/DashboardPage';
import { VisitsPage } from './modules/visits/VisitsPage';
import { CompaniesPage } from './modules/companies/CompaniesPage';
import { SamplesPage } from './modules/samples/SamplesPage';
import { OffersPage } from './modules/offers/OffersPage';
import { ExcelReportsPage } from './modules/excel/ExcelReportsPage';
import { CalendarPage } from './modules/calendar/CalendarPage';
import { AuthView } from './modules/auth/AuthView';
import { authApi } from './services/api';

export const App: React.FC = () => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => {
    return !!authApi.getToken();
  });

  useEffect(() => {
    const handleLogout = () => setIsAuthenticated(false);
    const handleUnauthorized = () => setIsAuthenticated(false);
    window.addEventListener('auth:logout', handleLogout);
    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => {
      window.removeEventListener('auth:logout', handleLogout);
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
    };
  }, []);

  if (!isAuthenticated) {
    return <AuthView onLoginSuccess={() => setIsAuthenticated(true)} />;
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="ziyaretler" element={<VisitsPage />} />
          <Route path="takvim" element={<CalendarPage />} />
          <Route path="musteriler" element={<CompaniesPage />} />
          <Route path="numuneler" element={<SamplesPage />} />
          <Route path="teklifler" element={<OffersPage />} />
          <Route path="raporlar" element={<ExcelReportsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

export default App;
