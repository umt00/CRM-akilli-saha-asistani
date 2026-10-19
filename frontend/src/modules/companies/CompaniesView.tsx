import React, { useState, useEffect } from 'react';
import { firmaApi } from '../../services/api';
import { FirmaYaniti, FirmaIstegi } from '../../types/crm.types';
import {
  Building2, Plus, Search, Trash2, Phone, Mail, MapPin,
  ExternalLink, Layers, ShieldAlert, Sparkles, RefreshCw, ChevronRight,
  User
} from 'lucide-react';
import { CompanyHoverCard } from '../../components/common/CustomerCard';
import { CustomerDrawer } from '../../components/common/CustomerDrawer';

export const CompaniesView: React.FC = () => {
  const [companies, setCompanies] = useState<FirmaYaniti[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCompany, setSelectedCompany] = useState<FirmaYaniti | null>(null);

  // Modals
  const [showAddCompanyModal, setShowAddCompanyModal] = useState(false);

  // New Company Form State (17-column matching)
  const [newCompany, setNewCompany] = useState<FirmaIstegi>({
    unvan: '',
    sehirBolge: '',
    adres: '',
    telefon: '',
    eposta: '',
    mevcutTedarikciRakip: '',
    tedarikEttigiUrunler: '',
    aylikKullanimMiktari: '',
    bizdenAldigiUrunler: '',
  });

  const loadCompanies = async () => {
    setLoading(true);
    try {
      const data = await firmaApi.firmalariGetir(searchTerm, 0, 200);
      setCompanies(data.content || []);
      // If a company is already selected, update its reference
      if (selectedCompany) {
        const updated = data.content.find((c) => c.id === selectedCompany.id);
        if (updated) setSelectedCompany(updated);
      }
    } catch (err: any) {
      console.error('Firmalar getirilemedi:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCompanies();
  }, [searchTerm]);

  const handleCreateCompany = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCompany.unvan) return;
    try {
      await firmaApi.firmaOlustur(newCompany);
      setShowAddCompanyModal(false);
      setNewCompany({
        unvan: '',
        sehirBolge: '',
        adres: '',
        telefon: '',
        eposta: '',
        mevcutTedarikciRakip: '',
        tedarikEttigiUrunler: '',
        aylikKullanimMiktari: '',
        bizdenAldigiUrunler: '',
      });
      loadCompanies();
    } catch (err: any) {
      alert('Firma oluşturulurken hata: ' + (err.response?.data?.mesaj || err.message));
    }
  };

  const handleDeleteCompany = async (id: number) => {
    if (!window.confirm('Bu firmayı silmek istediğinize emin misiniz?')) return;
    try {
      await firmaApi.firmaSil(id);
      if (selectedCompany?.id === id) setSelectedCompany(null);
      loadCompanies();
    } catch (err: any) {
      alert('Firma silinemedi: ' + (err.response?.data?.mesaj || err.message));
    }
  };

  return (
    <div className="fade-in">
      {/* Top Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.75rem' }}>
        <div>
          <h2 style={{ fontSize: '1.35rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <Building2 className="text-primary" size={26} />
            Müşteri Firmalar Portföyü (360° Künye)
          </h2>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
            17 Kolonluk Excel formatı, rakip analizleri, yetkili kişiler ve kronolojik aktivite akışı
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button onClick={loadCompanies} className="btn btn-secondary btn-sm" title="Yenile">
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} /> Yenile
          </button>
          <button onClick={() => setShowAddCompanyModal(true)} className="btn btn-primary btn-sm">
            <Plus size={16} /> Yeni Firma Ekle
          </button>
        </div>
      </div>

      {/* Search Input Bar */}
      <div className="card" style={{ marginBottom: '1.25rem', padding: '0.75rem 1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <Search size={18} color="var(--text-muted)" />
          <input
            type="text"
            className="form-control"
            style={{ width: '100%', border: 'none', background: 'transparent' }}
            placeholder="Firma unvanı veya bölgeye göre hızlı ara..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          {searchTerm && (
            <button
              onClick={() => setSearchTerm('')}
              style={{ background: 'transparent', border: 'none', color: '#64748b', cursor: 'pointer', fontSize: '0.75rem' }}
            >
              Temizle
            </button>
          )}
        </div>
      </div>

      {/* Companies List Full Table */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">
            <span>Portföydeki Firmalar ({companies.length})</span>
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            Detaylı zaman çizelgesi ve yetkilileri görmek için firmaya tıklayın
          </span>
        </div>

        {companies.length === 0 ? (
          <div className="empty-state">
            <Building2 className="empty-state-icon" />
            <p>{searchTerm ? 'Aramanıza uygun firma bulunamadı.' : 'Henüz kayıtlı müşteri firma bulunmuyor.'}</p>
            <button
              onClick={() => setShowAddCompanyModal(true)}
              className="btn btn-primary btn-sm"
              style={{ marginTop: '0.75rem' }}
            >
              İlk Firmayı Ekle
            </button>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Firma Ticari Unvanı</th>
                  <th>Bölge / Şehir</th>
                  <th>Mevcut Rakip Tedarikçi</th>
                  <th>Sorumlu Temsilci</th>
                  <th>Yetkili Sayısı</th>
                  <th>İletişim</th>
                  <th style={{ textAlign: 'right' }}>İşlemler</th>
                </tr>
              </thead>
              <tbody>
                {companies.map((c) => (
                  <tr
                    key={c.id}
                    style={{
                      cursor: 'pointer',
                      background: selectedCompany?.id === c.id ? 'rgba(99, 102, 241, 0.08)' : undefined
                    }}
                    onClick={() => setSelectedCompany(c)}
                    title="360° Zaman Çizelgesini ve Detayları Aç"
                  >
                    <td style={{ fontWeight: 600 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                        <CompanyHoverCard
                          data={{
                            firmaId: c.id,
                            firmaUnvani: c.unvan,
                            sehirBolge: c.sehirBolge,
                            yetkiliAdSoyad: c.yetkililer && c.yetkililer.length > 0 ? c.yetkililer[0].adSoyad : undefined,
                            yetkiliUnvan: c.yetkililer && c.yetkililer.length > 0 ? c.yetkililer[0].unvanGorev : undefined,
                            yetkiliTelefon: c.yetkililer && c.yetkililer.length > 0 ? c.yetkililer[0].telefon : c.telefon,
                            yetkiliEposta: c.yetkililer && c.yetkililer.length > 0 ? c.yetkililer[0].eposta : c.eposta,
                            plasiyerAdSoyad: c.atananPlasiyerAdSoyad || c.atananPlasiyerAdi,
                          }}
                          onOpenDetails={() => setSelectedCompany(c)}
                        >
                          <span style={{ color: '#818cf8', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                            <Building2 size={15} />
                            {c.unvan}
                          </span>
                        </CompanyHoverCard>
                      </div>
                    </td>
                    <td>
                      <span className="badge badge-info">{c.sehirBolge || 'Belirtilmedi'}</span>
                    </td>
                    <td style={{ color: c.mevcutTedarikciRakip ? '#fb7185' : 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600 }}>
                      {c.mevcutTedarikciRakip || '-'}
                    </td>
                    <td style={{ fontSize: '0.8rem', color: '#cbd5e1' }}>
                      {c.atananPlasiyerAdi || c.atananPlasiyerAdSoyad || '-'}
                    </td>
                    <td>
                      <span className="badge badge-purple" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
                        <User size={11} /> {c.yetkililer?.length || 0} Yetkili
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.4rem' }}>
                        {c.telefon && (
                          <a
                            href={`tel:${c.telefon}`}
                            onClick={(e) => e.stopPropagation()}
                            className="badge badge-success"
                            style={{ textDecoration: 'none' }}
                            title="Telefonu Ara"
                          >
                            <Phone size={11} /> Ara
                          </a>
                        )}
                        {c.eposta && (
                          <a
                            href={`mailto:${c.eposta}`}
                            onClick={(e) => e.stopPropagation()}
                            className="badge badge-info"
                            style={{ textDecoration: 'none' }}
                            title="E-posta Gönder"
                          >
                            <Mail size={11} /> E-posta
                          </a>
                        )}
                      </div>
                    </td>
                    <td style={{ textAlign: 'right' }} onClick={(e) => e.stopPropagation()}>
                      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.35rem', alignItems: 'center' }}>
                        <button
                          onClick={() => setSelectedCompany(c)}
                          className="btn btn-secondary btn-sm"
                          style={{ padding: '0.2rem 0.5rem', fontSize: '0.72rem' }}
                          title="Detay Çekmecesini Aç"
                        >
                          <span>360° İncele</span>
                          <ChevronRight size={12} />
                        </button>
                        <button
                          onClick={() => handleDeleteCompany(c.id)}
                          className="btn btn-danger btn-sm"
                          title="Firmayı Sil"
                          style={{ padding: '0.25rem 0.45rem' }}
                        >
                          <Trash2 size={13} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Bitrix24 Customer 360° Sliding Drawer */}
      <CustomerDrawer
        company={selectedCompany}
        isOpen={!!selectedCompany}
        onClose={() => setSelectedCompany(null)}
        onCompanyUpdated={loadCompanies}
      />

      {/* Modal: Yeni Firma Ekle */}
      {showAddCompanyModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="card-header">
              <h3 className="card-title">
                <Building2 size={20} /> Yeni Müşteri Firma Tanımla
              </h3>
              <button onClick={() => setShowAddCompanyModal(false)} className="btn btn-secondary btn-sm">X</button>
            </div>

            <form onSubmit={handleCreateCompany}>
              <div className="form-group">
                <label className="form-label">Firma Ticari Unvanı *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  placeholder="Örn: Ege Gıda Sanayi Ltd."
                  value={newCompany.unvan}
                  onChange={(e) => setNewCompany({ ...newCompany, unvan: e.target.value })}
                />
              </div>

              <div className="grid-2">
                <div className="form-group">
                  <label className="form-label">Şehir / Bölge (Kolon D)</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Örn: İzmir / Bornova"
                    value={newCompany.sehirBolge}
                    onChange={(e) => setNewCompany({ ...newCompany, sehirBolge: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Telefon</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="0232 XXX XX XX"
                    value={newCompany.telefon}
                    onChange={(e) => setNewCompany({ ...newCompany, telefon: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Açık Adres</label>
                <textarea
                  className="form-control"
                  rows={2}
                  placeholder="Cadde, sokak, no..."
                  value={newCompany.adres}
                  onChange={(e) => setNewCompany({ ...newCompany, adres: e.target.value })}
                />
              </div>

              <div style={{ margin: '1rem 0', padding: '0.75rem', background: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-sm)' }}>
                <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#818cf8', marginBottom: '0.5rem' }}>
                  17 Kolonluk Excel Saha Pazar Alanları
                </div>

                <div className="grid-2">
                  <div className="form-group">
                    <label className="form-label">Mevcut Rakip Tedarikçi (Kolon I)</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Örn: Kerry Rakip A.Ş."
                      value={newCompany.mevcutTedarikciRakip}
                      onChange={(e) => setNewCompany({ ...newCompany, mevcutTedarikciRakip: e.target.value })}
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label">Aylık Kullanım Miktarı (Kolon G)</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Örn: 500 KG / Ay"
                      value={newCompany.aylikKullanimMiktari}
                      onChange={(e) => setNewCompany({ ...newCompany, aylikKullanimMiktari: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Tedarik Ettiği Ürünler (Kolon F)</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Örn: Çikolata aroması, Nacho sos, Yağ"
                    value={newCompany.tedarikEttigiUrunler}
                    onChange={(e) => setNewCompany({ ...newCompany, tedarikEttigiUrunler: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Bizden Satın Aldığı Ürünler (Kolon H)</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Örn: DB-82 Kakao, Cargill Yağ"
                    value={newCompany.bizdenAldigiUrunler}
                    onChange={(e) => setNewCompany({ ...newCompany, bizdenAldigiUrunler: e.target.value })}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                <button type="button" onClick={() => setShowAddCompanyModal(false)} className="btn btn-secondary">
                  Vazgeç
                </button>
                <button type="submit" className="btn btn-primary">
                  Kaydet
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
