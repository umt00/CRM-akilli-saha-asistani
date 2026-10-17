import React, { useState, useEffect } from 'react';
import { firmaApi } from '../../services/api';
import { FirmaYaniti, FirmaIstegi, YetkiliIstegi } from '../../types/crm.types';
import {
  Building2, Plus, Search, Trash2, UserPlus, Phone, Mail, MapPin,
  ExternalLink, Layers, ShieldAlert, Sparkles, RefreshCw
} from 'lucide-react';
import { CompanyHoverCard } from '../../components/common/CustomerCard';

export const CompaniesView: React.FC = () => {
  const [companies, setCompanies] = useState<FirmaYaniti[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCompany, setSelectedCompany] = useState<FirmaYaniti | null>(null);

  // Modals
  const [showAddCompanyModal, setShowAddCompanyModal] = useState(false);
  const [showAddContactModal, setShowAddContactModal] = useState(false);

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

  // New Contact Form State
  const [newContact, setNewContact] = useState<YetkiliIstegi>({
    firmaId: 0,
    adSoyad: '',
    unvanGorev: '',
    telefon: '',
    eposta: '',
    notlar: '',
  });

  const loadCompanies = async () => {
    setLoading(true);
    try {
      const data = await firmaApi.firmalariGetir(searchTerm, 0, 100);
      setCompanies(data.content || []);
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

  const handleAddContact = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCompany || !newContact.adSoyad) return;
    try {
      await firmaApi.yetkiliEkle(selectedCompany.id, {
        ...newContact,
        firmaId: selectedCompany.id,
      });
      setShowAddContactModal(false);
      setNewContact({
        firmaId: 0,
        adSoyad: '',
        unvanGorev: '',
        telefon: '',
        eposta: '',
        notlar: '',
      });
      // Refresh selected company detail
      const refreshed = await firmaApi.firmaDetayiGetir(selectedCompany.id);
      setSelectedCompany(refreshed);
      loadCompanies();
    } catch (err: any) {
      alert('Yetkili eklenirken hata: ' + (err.response?.data?.mesaj || err.message));
    }
  };

  const handleDeleteCompany = async (id: number) => {
    if (!window.confirm('Bu firmayı ve bağlı tüm kayıtlarını silmek istediğinize emin misiniz?')) return;
    try {
      await firmaApi.firmaSil(id);
      if (selectedCompany?.id === id) setSelectedCompany(null);
      loadCompanies();
    } catch (err: any) {
      alert('Firma silinemedi: ' + (err.response?.data?.mesaj || err.message));
    }
  };

  const handleDeleteContact = async (companyId: number, contactId: number) => {
    if (!window.confirm('Bu yetkiliyi silmek istediğinize emin misiniz?')) return;
    try {
      await firmaApi.yetkiliSil(companyId, contactId);
      const refreshed = await firmaApi.firmaDetayiGetir(companyId);
      setSelectedCompany(refreshed);
      loadCompanies();
    } catch (err: any) {
      alert('Yetkili silinemedi: ' + (err.response?.data?.mesaj || err.message));
    }
  };

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.75rem' }}>
        <div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Building2 className="text-primary" size={24} />
            Müşteri Firmalar Portföyü
          </h2>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            17 Kolonluk Excel formatı, rakip analizleri ve yetkili kişi yönetimi
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

      {/* Search Input */}
      <div className="card" style={{ marginBottom: '1.25rem', padding: '0.75rem 1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Search size={18} color="var(--text-muted)" />
          <input
            type="text"
            className="form-control"
            style={{ width: '100%', border: 'none', background: 'transparent' }}
            placeholder="Firma unvanı veya bölgeye göre ara..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: selectedCompany ? '1fr 1fr' : '1fr', gap: '1.25rem' }}>
        {/* Companies List Table */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <span>Portföydeki Firmalar ({companies.length})</span>
            </div>
          </div>

          {companies.length === 0 ? (
            <div className="empty-state">
              <Building2 className="empty-state-icon" />
              <p>Henüz kayıtlı müşteri firma bulunmuyor.</p>
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
                    <th>Firma Unvanı</th>
                    <th>Bölge / Şehir</th>
                    <th>Rakip Tedarikçi</th>
                    <th>Temsilci</th>
                    <th>Yetkililer</th>
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
                            <span style={{ borderBottom: '1px dashed #64748b' }}>{c.unvan}</span>
                          </CompanyHoverCard>
                        </div>
                      </td>
                      <td>
                        <span className="badge badge-info">{c.sehirBolge || 'Belirtilmedi'}</span>
                      </td>
                      <td style={{ color: '#f87171', fontSize: '0.78rem' }}>
                        {c.mevcutTedarikciRakip || '-'}
                      </td>
                      <td style={{ fontSize: '0.78rem' }}>{c.atananPlasiyerAdi || c.atananPlasiyerAdSoyad || '-'}</td>
                      <td>
                        <span className="badge badge-purple">
                          {c.yetkililer?.length || 0} Yetkili
                        </span>
                      </td>
                      <td style={{ textAlign: 'right' }} onClick={(e) => e.stopPropagation()}>
                        <button
                          onClick={() => handleDeleteCompany(c.id)}
                          className="btn btn-danger btn-sm"
                          title="Firmayı Sil"
                          style={{ padding: '0.25rem 0.5rem' }}
                        >
                          <Trash2 size={13} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Selected Company 360° Detail Card */}
        {selectedCompany && (
          <div className="card fade-in">
            <div className="card-header">
              <div className="card-title">
                <Building2 size={18} />
                <span>{selectedCompany.unvan} - 360° Kart</span>
              </div>
              <button
                onClick={() => setSelectedCompany(null)}
                className="btn btn-secondary btn-sm"
              >
                Kapat
              </button>
            </div>

            <div className="grid-2" style={{ marginBottom: '1rem' }}>
              <div>
                <span className="form-label">Şehir / Bölge</span>
                <div style={{ fontSize: '0.88rem', fontWeight: 600 }}>{selectedCompany.sehirBolge || '-'}</div>
              </div>
              <div>
                <span className="form-label">Sorumlu Plasiyer</span>
                <div style={{ fontSize: '0.88rem', fontWeight: 600 }}>{selectedCompany.atananPlasiyerAdi || selectedCompany.atananPlasiyerAdSoyad || '-'}</div>
              </div>
              <div>
                <span className="form-label">Telefon</span>
                <div style={{ fontSize: '0.85rem' }}>{selectedCompany.telefon || '-'}</div>
              </div>
              <div>
                <span className="form-label">E-Posta</span>
                <div style={{ fontSize: '0.85rem' }}>{selectedCompany.eposta || '-'}</div>
              </div>
            </div>

            <div style={{ marginBottom: '1rem', background: 'var(--bg-input)', padding: '0.75rem', borderRadius: 'var(--radius-md)' }}>
              <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginBottom: '0.25rem' }}>
                📍 Açık Adres:
              </div>
              <div style={{ fontSize: '0.82rem' }}>{selectedCompany.adres || 'Adres bilgisi girilmemiş.'}</div>
            </div>

            {/* 17-Kolon Excel Detayları */}
            <div style={{
              background: 'rgba(99, 102, 241, 0.05)',
              border: '1px solid rgba(99, 102, 241, 0.2)',
              borderRadius: 'var(--radius-md)',
              padding: '0.85rem',
              marginBottom: '1.25rem'
            }}>
              <div style={{ fontWeight: 700, fontSize: '0.82rem', marginBottom: '0.5rem', color: '#818cf8' }}>
                📋 17 Kolonluk Saha Pazar Verileri
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', fontSize: '0.78rem' }}>
                <div><strong>Mevcut Rakip Tedarikçi (Kolon I):</strong> {selectedCompany.mevcutTedarikciRakip || '-'}</div>
                <div><strong>Tedarik Ettiği Ürünler (Kolon F):</strong> {selectedCompany.tedarikEttigiUrunler || '-'}</div>
                <div><strong>Aylık Tüketim Miktarı (Kolon G):</strong> {selectedCompany.aylikKullanimMiktari || '-'}</div>
                <div><strong>Bizden Satın Aldığı Ürünler (Kolon H):</strong> {selectedCompany.bizdenAldigiUrunler || '-'}</div>
              </div>
            </div>

            {/* Contacts Section */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontWeight: 700, fontSize: '0.85rem' }}>
                  👥 İlgili Kişiler / Şefler ({selectedCompany.yetkililer?.length || 0})
                </span>
                <button
                  onClick={() => {
                    setNewContact({ ...newContact, firmaId: selectedCompany.id });
                    setShowAddContactModal(true);
                  }}
                  className="btn btn-secondary btn-sm"
                  style={{ fontSize: '0.72rem' }}
                >
                  <UserPlus size={13} /> Yetkili Ekle
                </button>
              </div>

              {(!selectedCompany.yetkililer || selectedCompany.yetkililer.length === 0) ? (
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', padding: '0.5rem 0' }}>
                  Henüz yetkili kişi kaydedilmemiş.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {selectedCompany.yetkililer.map((c) => (
                    <div
                      key={c.id}
                      style={{
                        background: 'var(--bg-input)',
                        padding: '0.6rem 0.8rem',
                        borderRadius: 'var(--radius-sm)',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        fontSize: '0.8rem'
                      }}
                    >
                      <div>
                        <div style={{ fontWeight: 600 }}>{c.adSoyad}</div>
                        <div style={{ fontSize: '0.72rem', color: 'var(--text-secondary)' }}>
                          {c.unvanGorev || 'Görevi belirtilmedi'} {c.telefon && `• 📞 ${c.telefon}`}
                        </div>
                      </div>
                      <button
                        onClick={() => handleDeleteContact(selectedCompany.id, c.id)}
                        className="btn btn-danger btn-sm"
                        style={{ padding: '0.2rem 0.4rem' }}
                      >
                        <Trash2 size={12} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </div>

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
                  17 Kolonluk Excel Alanları
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

      {/* Modal: Yetkili Ekle */}
      {showAddContactModal && selectedCompany && (
        <div className="modal-backdrop">
          <div className="modal-card" style={{ maxWidth: '480px' }}>
            <div className="card-header">
              <h3 className="card-title">
                <UserPlus size={18} /> {selectedCompany.unvan} İçin Yetkili Ekle
              </h3>
              <button onClick={() => setShowAddContactModal(false)} className="btn btn-secondary btn-sm">X</button>
            </div>

            <form onSubmit={handleAddContact}>
              <div className="form-group">
                <label className="form-label">Yetkili Adı Soyadı *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  placeholder="Örn: Mehmet Usta"
                  value={newContact.adSoyad}
                  onChange={(e) => setNewContact({ ...newContact, adSoyad: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Görevi / Departmanı (Kolon C)</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Örn: Mutfak Şefi, Satın Alma Müdürü"
                  value={newContact.unvanGorev}
                  onChange={(e) => setNewContact({ ...newContact, unvanGorev: e.target.value })}
                />
              </div>

              <div className="grid-2">
                <div className="form-group">
                  <label className="form-label">Telefon</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="05XX XXX XX XX"
                    value={newContact.telefon}
                    onChange={(e) => setNewContact({ ...newContact, telefon: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">E-Posta</label>
                  <input
                    type="email"
                    className="form-control"
                    placeholder="ornek@firma.com"
                    value={newContact.eposta}
                    onChange={(e) => setNewContact({ ...newContact, eposta: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Notlar</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Örn: Salı günleri 14:00'dan sonra müsait"
                  value={newContact.notlar}
                  onChange={(e) => setNewContact({ ...newContact, notlar: e.target.value })}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowAddContactModal(false)} className="btn btn-secondary">
                  Vazgeç
                </button>
                <button type="submit" className="btn btn-primary">
                  Yetkiliyi Kaydet
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
