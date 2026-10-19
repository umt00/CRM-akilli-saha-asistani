import React, { useState, useEffect, useMemo } from 'react';
import { teklifApi, firmaApi } from '../../services/api';
import { TeklifYaniti, TeklifDurumu, TeklifIstegi, FirmaYaniti } from '../../types/crm.types';
import { KanbanBoard } from './KanbanBoard';
import { CompanyHoverCard } from '../../components/common/CustomerCard';
import { CustomerDrawer } from '../../components/common/CustomerDrawer';
import {
  DollarSign, CheckCircle2, XCircle, Clock, Plus, RefreshCw,
  Trash2, TrendingUp, Briefcase, LayoutGrid, List, Search, Filter,
  Building2
} from 'lucide-react';

export const OffersView: React.FC = () => {
  const [offers, setOffers] = useState<TeklifYaniti[]>([]);
  const [companies, setCompanies] = useState<FirmaYaniti[]>([]);
  const [loading, setLoading] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [viewMode, setViewMode] = useState<'kanban' | 'table'>('kanban');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCompany, setSelectedCompany] = useState<FirmaYaniti | null>(null);

  const [newOffer, setNewOffer] = useState<TeklifIstegi>({
    firmaId: 0,
    baslik: '',
    tutar: 0,
    paraBirimi: 'TRY',
    notlar: '',
  });

  const loadData = async () => {
    setLoading(true);
    try {
      const [offerRes, compRes] = await Promise.all([
        teklifApi.teklifleriGetir(0, 200),
        firmaApi.firmalariGetir('', 0, 200),
      ]);
      setOffers(offerRes.content || []);
      setCompanies(compRes.content || []);
      if (compRes.content && compRes.content.length > 0 && newOffer.firmaId === 0) {
        setNewOffer((prev) => ({ ...prev, firmaId: compRes.content[0].id }));
      }
    } catch (err) {
      console.error('Teklifler yüklenirken hata:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreateOffer = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newOffer.firmaId || !newOffer.baslik || !newOffer.tutar) return;
    try {
      await teklifApi.teklifOlustur({
        ...newOffer,
        firmaId: Number(newOffer.firmaId),
        tutar: Number(newOffer.tutar),
      });
      setShowAddModal(false);
      setNewOffer({
        firmaId: companies[0]?.id || 0,
        baslik: '',
        tutar: 0,
        paraBirimi: 'TRY',
        notlar: '',
      });
      loadData();
    } catch (err: any) {
      alert('Teklif oluşturulamadı: ' + (err.response?.data?.mesaj || err.message));
    }
  };

  const handleUpdateStatus = async (id: number, status: TeklifDurumu) => {
    // Optimistic UI update for instantaneous feel
    setOffers((prev) =>
      prev.map((o) => (o.id === id ? { ...o, durum: status } : o))
    );
    try {
      await teklifApi.teklifDurumuGuncelle(id, status);
    } catch (err: any) {
      alert('Durum güncellenemedi: ' + (err.response?.data?.mesaj || err.message));
      loadData(); // rollback
    }
  };

  const handleDeleteOffer = async (id: number) => {
    if (!window.confirm('Bu teklifi silmek istediğinize emin misiniz?')) return;
    setOffers((prev) => prev.filter((o) => o.id !== id));
    try {
      await teklifApi.teklifSil(id);
    } catch (err: any) {
      alert('Teklif silinemedi: ' + (err.response?.data?.mesaj || err.message));
      loadData();
    }
  };

  // Filtered offers based on search query
  const filteredOffers = useMemo(() => {
    if (!searchQuery.trim()) return offers;
    const q = searchQuery.toLowerCase();
    return offers.filter(
      (o) =>
        (o.firmaAdi && o.firmaAdi.toLowerCase().includes(q)) ||
        (o.firmaUnvani && o.firmaUnvani.toLowerCase().includes(q)) ||
        (o.baslik && o.baslik.toLowerCase().includes(q)) ||
        (o.plasiyerAdi && o.plasiyerAdi.toLowerCase().includes(q)) ||
        (o.plasiyerAdSoyad && o.plasiyerAdSoyad.toLowerCase().includes(q)) ||
        (o.notlar && o.notlar.toLowerCase().includes(q))
    );
  }, [offers, searchQuery]);

  // Pipeline stats
  const totalAmount = offers.reduce((acc, o) => acc + (Number(o.tutar) || 0), 0);
  const wonOffers = offers.filter((o) => o.durum === 'KAZANILDI');
  const wonAmount = wonOffers.reduce((acc, o) => acc + (Number(o.tutar) || 0), 0);
  const openOffers = offers.filter((o) => o.durum === 'ACIK');
  const openAmount = openOffers.reduce((acc, o) => acc + (Number(o.tutar) || 0), 0);

  const getStatusBadge = (status: TeklifDurumu) => {
    switch (status) {
      case 'ACIK':
        return <span className="badge badge-info"><Clock size={11} /> Müzakerede</span>;
      case 'KAZANILDI':
        return <span className="badge badge-success"><CheckCircle2 size={11} /> Kazanıldı 🎉</span>;
      case 'KAYBEDILDI':
        return <span className="badge badge-danger"><XCircle size={11} /> Kaybedildi</span>;
      case 'IPTAL':
        return <span className="badge badge-warning">İptal Edildi</span>;
      default:
        return <span className="badge badge-info">{status}</span>;
    }
  };

  return (
    <div className="fade-in">
      {/* Top Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.75rem' }}>
        <div>
          <h2 style={{ fontSize: '1.35rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <DollarSign className="text-primary" size={26} />
            Satış Hunisi & Teklif Masası (Bitrix24 Pipeline)
          </h2>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
            Sahadaki fiyat tekliflerini, ticari fırsatları ve aşamaları interaktif yönetin
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <button onClick={loadData} className="btn btn-secondary btn-sm" title="Verileri Yenile">
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} /> Yenile
          </button>
          <button onClick={() => setShowAddModal(true)} className="btn btn-primary btn-sm">
            <Plus size={16} /> Yeni Fırsat / Teklif Aç
          </button>
        </div>
      </div>

      {/* Summary KPI Cards */}
      <div className="grid-3" style={{ marginBottom: '1.25rem' }}>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(99, 102, 241, 0.15)', color: '#818cf8' }}>
            <Briefcase size={22} />
          </div>
          <div>
            <div className="stat-number">{offers.length} Teklif</div>
            <div className="stat-label">Toplam Satış Boru Hattı: {totalAmount.toLocaleString('tr-TR')} ₺</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#34d399' }}>
            <TrendingUp size={22} />
          </div>
          <div>
            <div className="stat-number" style={{ color: '#34d399' }}>
              {wonAmount.toLocaleString('tr-TR')} ₺
            </div>
            <div className="stat-label">Kazanılan Ciro ({wonOffers.length} Teklif)</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(6, 182, 212, 0.15)', color: '#38bdf8' }}>
            <Clock size={22} />
          </div>
          <div>
            <div className="stat-number" style={{ color: '#38bdf8' }}>
              {openAmount.toLocaleString('tr-TR')} ₺
            </div>
            <div className="stat-label">Müzakerede Bekleyen ({openOffers.length} Teklif)</div>
          </div>
        </div>
      </div>

      {/* Bitrix24 Toolbar: Search Filter & View Mode Switcher */}
      <div className="kanban-toolbar" style={{ marginBottom: '1.25rem' }}>
        <div className="kanban-search-box">
          <Search size={15} className="text-secondary" />
          <input
            type="text"
            placeholder="Firma, teklif konusu veya temsilci ara..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              style={{ background: 'transparent', border: 'none', color: '#64748b', cursor: 'pointer', fontSize: '0.75rem' }}
            >
              Temizle
            </button>
          )}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
            Gösterilen: <strong>{filteredOffers.length}</strong> / {offers.length}
          </span>

          <div className="kanban-view-toggle">
            <button
              onClick={() => setViewMode('kanban')}
              className={`kanban-toggle-btn ${viewMode === 'kanban' ? 'active' : ''}`}
              title="Kanban Satış Hunisi Görünümü"
            >
              <LayoutGrid size={14} />
              <span>Kanban Hunisi</span>
            </button>
            <button
              onClick={() => setViewMode('table')}
              className={`kanban-toggle-btn ${viewMode === 'table' ? 'active' : ''}`}
              title="Klasik Liste Tablo Görünümü"
            >
              <List size={14} />
              <span>Tablo</span>
            </button>
          </div>
        </div>
      </div>

      {/* Main View: Kanban vs Table */}
      {viewMode === 'kanban' ? (
        <KanbanBoard
          offers={filteredOffers}
          companies={companies}
          onUpdateStatus={handleUpdateStatus}
          onDeleteOffer={handleDeleteOffer}
          onSelectCompany={(comp) => setSelectedCompany(comp)}
        />
      ) : (
        /* Classic Table View */
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>Teklif & Fırsat Listesi ({filteredOffers.length})</span>
            </div>
          </div>

          {filteredOffers.length === 0 ? (
            <div className="empty-state">
              <DollarSign className="empty-state-icon" />
              <p>{searchQuery ? 'Aramanıza uygun teklif bulunamadı.' : 'Kayıtlı fiyat teklifi bulunmuyor.'}</p>
              <button onClick={() => setShowAddModal(true)} className="btn btn-primary btn-sm" style={{ marginTop: '0.75rem' }}>
                İlk Teklifi Aç
              </button>
            </div>
          ) : (
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Firma Unvanı (Önizleme)</th>
                    <th>Teklif Başlığı</th>
                    <th>Tutar</th>
                    <th>Durum</th>
                    <th>Temsilci</th>
                    <th>Aşama İlerle</th>
                    <th style={{ textAlign: 'right' }}>İşlem</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredOffers.map((o) => {
                    const matchedComp = companies.find((c) => c.id === o.firmaId);
                    return (
                      <tr key={o.id}>
                        <td style={{ fontWeight: 600 }}>
                          <CompanyHoverCard
                            data={{
                              firmaId: o.firmaId,
                              firmaUnvani: o.firmaAdi || o.firmaUnvani || matchedComp?.unvan || 'Firma',
                              sehirBolge: matchedComp?.sehirBolge,
                              yetkiliAdSoyad: matchedComp?.yetkililer?.[0]?.adSoyad,
                              yetkiliUnvan: matchedComp?.yetkililer?.[0]?.unvanGorev,
                              yetkiliTelefon: matchedComp?.yetkililer?.[0]?.telefon || matchedComp?.telefon,
                              yetkiliEposta: matchedComp?.yetkililer?.[0]?.eposta || matchedComp?.eposta,
                              plasiyerAdSoyad: o.plasiyerAdi || o.plasiyerAdSoyad,
                              durum: o.durum,
                            }}
                          >
                            <span style={{ cursor: 'pointer', color: '#818cf8', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                              <Building2 size={14} />
                              {o.firmaAdi || o.firmaUnvani || matchedComp?.unvan}
                            </span>
                          </CompanyHoverCard>
                        </td>
                        <td>{o.baslik}</td>
                        <td style={{ fontWeight: 800, color: '#34d399', fontSize: '0.92rem' }}>
                          {Number(o.tutar).toLocaleString('tr-TR')} {o.paraBirimi || 'TRY'}
                        </td>
                        <td>{getStatusBadge(o.durum)}</td>
                        <td style={{ fontSize: '0.78rem', color: '#cbd5e1' }}>{o.plasiyerAdi || o.plasiyerAdSoyad || 'Atanmamış'}</td>
                        <td>
                          <div style={{ display: 'inline-flex', gap: '0.25rem' }}>
                            {o.durum === 'ACIK' && (
                              <>
                                <button
                                  onClick={() => handleUpdateStatus(o.id, 'KAZANILDI')}
                                  className="btn btn-success btn-sm"
                                  style={{ padding: '0.2rem 0.45rem', fontSize: '0.7rem' }}
                                  title="Kazanıldı Olarak İşaretle"
                                >
                                  ✓ Kazanıldı
                                </button>
                                <button
                                  onClick={() => handleUpdateStatus(o.id, 'KAYBEDILDI')}
                                  className="btn btn-danger btn-sm"
                                  style={{ padding: '0.2rem 0.45rem', fontSize: '0.7rem' }}
                                  title="Kaybedildi Olarak İşaretle"
                                >
                                  ✗ Kaybedildi
                                </button>
                              </>
                            )}
                            {o.durum !== 'ACIK' && (
                              <button
                                onClick={() => handleUpdateStatus(o.id, 'ACIK')}
                                className="btn btn-secondary btn-sm"
                                style={{ padding: '0.2rem 0.45rem', fontSize: '0.7rem' }}
                              >
                                Tekrar Aç
                              </button>
                            )}
                          </div>
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <button
                            onClick={() => handleDeleteOffer(o.id)}
                            className="btn btn-danger btn-sm"
                            style={{ padding: '0.2rem 0.4rem' }}
                            title="Teklifi Sil"
                          >
                            <Trash2 size={12} />
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Modal: Yeni Teklif Ekle */}
      {showAddModal && (
        <div className="modal-backdrop">
          <div className="modal-card" style={{ maxWidth: '480px' }}>
            <div className="card-header">
              <h3 className="card-title">
                <DollarSign size={18} /> Yeni Ticari Fırsat / Teklif Aç
              </h3>
              <button onClick={() => setShowAddModal(false)} className="btn btn-secondary btn-sm">X</button>
            </div>

            <form onSubmit={handleCreateOffer}>
              <div className="form-group">
                <label className="form-label">Müşteri Firma *</label>
                <select
                  className="form-control"
                  value={newOffer.firmaId}
                  onChange={(e) => setNewOffer({ ...newOffer, firmaId: Number(e.target.value) })}
                >
                  {companies.map((c) => (
                    <option key={c.id} value={c.id}>{c.unvan} ({c.sehirBolge || 'Bölge Yok'})</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Teklif Konusu / Başlık *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  placeholder="Örn: 2026 Kerry Aroma Yıllık Alım Sözleşmesi"
                  value={newOffer.baslik}
                  onChange={(e) => setNewOffer({ ...newOffer, baslik: e.target.value })}
                />
              </div>

              <div className="grid-2">
                <div className="form-group">
                  <label className="form-label">Tutar *</label>
                  <input
                    type="number"
                    required
                    min={1}
                    className="form-control"
                    placeholder="Örn: 150000"
                    value={newOffer.tutar || ''}
                    onChange={(e) => setNewOffer({ ...newOffer, tutar: Number(e.target.value) })}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Para Birimi</label>
                  <select
                    className="form-control"
                    value={newOffer.paraBirimi}
                    onChange={(e) => setNewOffer({ ...newOffer, paraBirimi: e.target.value })}
                  >
                    <option value="TRY">TRY (₺)</option>
                    <option value="USD">USD ($)</option>
                    <option value="EUR">EUR (€)</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Teklif Notları / Şartlar</label>
                <textarea
                  className="form-control"
                  rows={2}
                  placeholder="Vade, iskonto veya teslimat detayları..."
                  value={newOffer.notlar || ''}
                  onChange={(e) => setNewOffer({ ...newOffer, notlar: e.target.value })}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowAddModal(false)} className="btn btn-secondary">
                  Vazgeç
                </button>
                <button type="submit" className="btn btn-primary">
                  Teklifi Kaydet
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Bitrix24 Customer 360° Sliding Drawer */}
      <CustomerDrawer
        company={selectedCompany}
        isOpen={!!selectedCompany}
        onClose={() => setSelectedCompany(null)}
        onCompanyUpdated={loadData}
      />
    </div>
  );
};
