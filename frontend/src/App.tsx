import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { DashboardPage } from './modules/dashboard/DashboardPage';
import { VisitsPage } from './modules/visits/VisitsPage';
import { CompaniesPage } from './modules/companies/CompaniesPage';
import { SamplesPage } from './modules/samples/SamplesPage';
import { OffersPage } from './modules/offers/OffersPage';
import { ExcelReportsPage } from './modules/excel/ExcelReportsPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="ziyaretler" element={<VisitsPage />} />
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
