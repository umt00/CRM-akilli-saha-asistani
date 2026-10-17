import React, { useState, useEffect } from 'react';
import { ziyaretApi, firmaApi } from '../../services/api';
import { ZiyaretYaniti, ZiyaretIstegi, FirmaYaniti } from '../../types/crm.types';
import {
  MapPin, Clock, Calendar, CheckCircle2, FlaskConical, DollarSign,
  ArrowRight, Plus, Trash2, Sparkles, AlertCircle, FileText
} from 'lucide-react';

export const VisitsView: React.FC = () => {
  const [visits, setVisits] = useState<ZiyaretYaniti[]>([]);
  const [companies, setCompanies] = useState<FirmaYaniti[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // 30-Second Rapid Visit Form State
  const [formData, setFormData] = useState<ZiyaretIstegi>({
    firmaId: 0,
    yetkiliId: undefined,
    ziyaretKonusu: '',
    tedarikEttigiUrunler: '',
    aylikKullanimMiktari: '',
    bizdenAldigiUrunler: '',
    mevcutTedarikciRakip: '',
    numuneVerildiMi: false,
    numuneUrunAdi: '',
    numuneMarka: 'Kerry',
    numuneMiktar: 1,
    teklifVerildiMi: false,
    teklifBaslik: '',
    teklifTutari: 0,
    teklifParaBirimi: 'TRY',
    sonrakiAksiyon: '',
    sonrakiZiyaretTarihi: '',
    notlar: '',
  });

  const loadData = async () => {
    setLoading(true);
    try {
      const [visitRes, compRes] = await Promise.all([
        ziyaretApi.ziyaretleriGetir(0, 50),
        firmaApi.firmalariGetir('', 0, 100),
      ]);
      setVisits(visitRes.content || []);
      setCompanies(compRes.content || []);
      if (compRes.content && compRes.content.length > 0 && formData.firmaId === 0) {
        setFormData((prev) => ({ ...prev, firmaId: compRes.content[0].id }));
      }
    } catch (err) {
      console.error('Veriler yüklenirken hata:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const selectedCompany = companies.find((c) => c.id === Number(formData.firmaId));

  const handleCompanyChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const cid = Number(e.target.value);
    const comp = companies.find((c) => c.id === cid);
    setFormData({
      ...formData,
      firmaId: cid,
      yetkiliId: comp?.yetkililer?.[0]?.id,
      tedarikEttigiUrunler: comp?.tedarikEttigiUrunler || '',
      aylikKullanimMiktari: comp?.aylikKullanimMiktari || '',
      bizdenAldigiUrunler: comp?.bizdenAldigiUrunler || '',
      mevcutTedarikciRakip: comp?.mevcutTedarikciRakip || '',
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.firmaId || !formData.ziyaretKonusu) {
      alert('Lütfen firma ve ziyaret konusunu doldurunuz.');
      return;
    }

    setSubmitting(true);
    setSuccessMsg(null);
    try {
      await ziyaretApi.ziyaretOlustur({
        ...formData,
        firmaId: Number(formData.firmaId),
        yetkiliId: formData.yetkiliId ? Number(formData.yetkiliId) : undefined,
        ziyaretTarihi: new Date().toISOString(),
      });
      setSuccessMsg('Saha ziyareti, bağlı numune ve teklif başarıyla kaydedildi!');
      // Reset form
      setFormData({
        firmaId: companies[0]?.id || 0,
        yetkiliId: undefined,
        ziyaretKonusu: '',
        tedarikEttigiUrunler: '',
        aylikKullanimMiktari: '',
        bizdenAldigiUrunler: '',
        mevcutTedarikciRakip: '',
        numuneVerildiMi: false,
        numuneUrunAdi: '',
        numuneMarka: 'Kerry',
        numuneMiktar: 1,
        teklifVerildiMi: false,
        teklifBaslik: '',
        teklifTutari: 0,
        teklifParaBirimi: 'TRY',
        sonrakiAksiyon: '',
        sonrakiZiyaretTarihi: '',
        notlar: '',
      });
      loadData();
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err: any) {
      alert('Ziyaret kaydedilemedi: ' + (err.response?.data?.mesaj || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteVisit = async (id: number) => {
    if (!window.confirm('Bu ziyareti silmek istediğinize emin misiniz?')) return;
    try {
      await ziyaretApi.ziyaretSil(id);
      loadData();
    } catch (err: any) {
      alert('Ziyaret silinemedi: ' + (err.response?.data?.mesaj || err.message));
    }
  };

  return (
    <div className="fade-in">
      <div style={{ marginBottom: '1.25rem' }}>
        <h2 style={{ fontSize: '1.3rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Clock className="text-primary" size={24} />
          30 Saniyelik Hızlı Saha Ziyareti Formu
        </h2>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          Plasiyerin sahada araç içindeyken tek ekranda doldurup otomatik Numune & Teklif masalarına bağladığı form
        </p>
      </div>

      {successMsg && (
        <div style={{
          background: 'rgba(16, 185, 129, 0.15)',
          border: '1px solid rgba(16, 185, 129, 0.3)',
          borderRadius: 'var(--radius-md)',
          padding: '0.85rem 1.25rem',
          color: '#34d399',
          fontSize: '0.85rem',
          marginBottom: '1.25rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          <CheckCircle2 size={18} />
          {successMsg}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '1.5rem' }}>
        {/* Form Card */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <Sparkles size={18} color="var(--primary)" />
              <span>Yeni Saha Görüşmesi Girişi</span>
            </div>
            <span className="badge badge-purple">⚡ Hızlı Form</span>
          </div>

          {companies.length === 0 ? (
            <div className="empty-state">
              <AlertCircle className="empty-state-icon" />
              <p>Ziyaret girebilmek için önce en az bir müşteri firma eklemelisiniz.</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="grid-2">
                <div className="form-group">
                  <label className="form-label">Ziyaret Edilen Firma *</label>
                  <select
                    className="form-control"
                    value={formData.firmaId}
                    onChange={handleCompanyChange}
                  >
                    {companies.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.unvan} ({c.sehirBolge || 'Bölge yok'})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Görüşülen Yetkili Kişi (Kolon B)</label>
                  <select
                    className="form-control"
                    value={formData.yetkiliId || ''}
                    onChange={(e) => setFormData({ ...formData, yetkiliId: e.target.value ? Number(e.target.value) : undefined })}
                  >
                    <option value="">Firma Genel / Belirtilmedi</option>
                    {selectedCompany?.yetkililer?.map((y) => (
                      <option key={y.id} value={y.id}>
                        {y.adSoyad} - {y.unvanGorev || 'Yetkili'}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Ziyaret Konusu & Görüşme Özeti (Kolon J) *</label>
                <input
                  type="text"
                  required
                  className="form-control"
                  placeholder="Örn: Yeni nacho aroması tadımı yapıldı, mevcut rakip fiyata itiraz etti"
                  value={formData.ziyaretKonusu}
                  onChange={(e) => setFormData({ ...formData, ziyaretKonusu: e.target.value })}
                />
              </div>

              <div className="grid-2">
                <div className="form-group">
                  <label className="form-label">Mevcut Rakip Tedarikçi (Kolon I)</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Örn: Rakip Kimya Ltd."
                    value={formData.mevcutTedarikciRakip || ''}
                    onChange={(e) => setFormData({ ...formData, mevcutTedarikciRakip: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Aylık Tüketim Miktarı (Kolon G)</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Örn: 250 KG / Ay"
                    value={formData.aylikKullanimMiktari || ''}
                    onChange={(e) => setFormData({ ...formData, aylikKullanimMiktari: e.target.value })}
                  />
                </div>
              </div>

              {/* Numune Tetikleyici */}
              <div style={{
                background: formData.numuneVerildiMi ? 'rgba(6, 182, 212, 0.08)' : 'rgba(255, 255, 255, 0.02)',
                border: `1px solid ${formData.numuneVerildiMi ? 'rgba(6, 182, 212, 0.4)' : 'var(--border-subtle)'}`,
                borderRadius: 'var(--radius-md)',
                padding: '0.85rem',
                marginBottom: '1rem',
                transition: 'all 0.2s ease'
              }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', cursor: 'pointer', fontSize: '0.85rem', fontWeight: 600 }}>
                  <input
                    type="checkbox"
                    checked={formData.numuneVerildiMi}
                    onChange={(e) => setFormData({ ...formData, numuneVerildiMi: e.target.checked })}
                    style={{ width: '16px', height: '16px', accentColor: '#06b6d4' }}
                  />
                  <FlaskConical size={18} color="#06b6d4" />
                  <span>🧪 Numune Bırakıldı mı? (Kolon K - Otomatik Numune Masasına Ekler)</span>
                </label>

                {formData.numuneVerildiMi && (
                  <div className="grid-3" style={{ marginTop: '0.75rem' }}>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label">Numune Ürün Adı</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="Örn: Nacho Peynir Aroması"
                        value={formData.numuneUrunAdi || ''}
                        onChange={(e) => setFormData({ ...formData, numuneUrunAdi: e.target.value })}
                      />
                    </div>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label">Marka</label>
                      <select
                        className="form-control"
                        value={formData.numuneMarka || 'Kerry'}
                        onChange={(e) => setFormData({ ...formData, numuneMarka: e.target.value })}
                      >
                        <option value="Kerry">Kerry</option>
                        <option value="Cargill">Cargill</option>
                        <option value="Orkide">Orkide</option>
                        <option value="Diğer">Diğer</option>
                      </select>
                    </div>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label">Miktar</label>
                      <input
                        type="number"
                        className="form-control"
                        placeholder="Örn: 1"
                        value={formData.numuneMiktar || 1}
                        onChange={(e) => setFormData({ ...formData, numuneMiktar: Number(e.target.value) })}
                      />
                    </div>
                  </div>
                )}
              </div>

              {/* Teklif Tetikleyici */}
              <div style={{
                background: formData.teklifVerildiMi ? 'rgba(16, 185, 129, 0.08)' : 'rgba(255, 255, 255, 0.02)',
                border: `1px solid ${formData.teklifVerildiMi ? 'rgba(16, 185, 129, 0.4)' : 'var(--border-subtle)'}`,
                borderRadius: 'var(--radius-md)',
                padding: '0.85rem',
                marginBottom: '1rem',
                transition: 'all 0.2s ease'
              }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', cursor: 'pointer', fontSize: '0.85rem', fontWeight: 600 }}>
                  <input
                    type="checkbox"
                    checked={formData.teklifVerildiMi}
                    onChange={(e) => setFormData({ ...formData, teklifVerildiMi: e.target.checked })}
                    style={{ width: '16px', height: '16px', accentColor: '#10b981' }}
                  />
                  <DollarSign size={18} color="#10b981" />
                  <span>💼 Fiyat Teklifi Verildi mi? (Kolon M - Otomatik Teklif Masasına Ekler)</span>
                </label>

                {formData.teklifVerildiMi && (
                  <div className="grid-3" style={{ marginTop: '0.75rem' }}>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label">Teklif Başlığı</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="Örn: 2026 Yıllık Sözleşme"
                        value={formData.teklifBaslik || ''}
                        onChange={(e) => setFormData({ ...formData, teklifBaslik: e.target.value })}
                      />
                    </div>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label">Tutar (Kolon N)</label>
                      <input
                        type="number"
                        className="form-control"
                        placeholder="Örn: 45000"
                        value={formData.teklifTutari || 0}
                        onChange={(e) => setFormData({ ...formData, teklifTutari: Number(e.target.value) })}
                      />
                    </div>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label">Para Birimi</label>
                      <select
                        className="form-control"
                        value={formData.teklifParaBirimi || 'TRY'}
                        onChange={(e) => setFormData({ ...formData, teklifParaBirimi: e.target.value })}
                      >
                        <option value="TRY">TRY (₺)</option>
                        <option value="USD">USD ($)</option>
                        <option value="EUR">EUR (€)</option>
                      </select>
                    </div>
                  </div>
                )}
              </div>

              {/* Ajanda & Sonraki Aksiyon */}
              <div className="grid-2">
                <div className="form-group">
                  <label className="form-label">Sonraki Aksiyon (Kolon O)</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Örn: Numune sonucu için haftaya aranacak"
                    value={formData.sonrakiAksiyon || ''}
                    onChange={(e) => setFormData({ ...formData, sonrakiAksiyon: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Sonraki Ziyaret / Randevu Tarihi (Kolon P - Ajanda)</label>
                  <input
                    type="datetime-local"
                    className="form-control"
                    value={formData.sonrakiZiyaretTarihi || ''}
                    onChange={(e) => setFormData({ ...formData, sonrakiZiyaretTarihi: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Saha Notları (Kolon Q)</label>
                <textarea
                  className="form-control"
                  rows={2}
                  placeholder="Görüşmeye dair ek kritik notlar..."
                  value={formData.notlar || ''}
                  onChange={(e) => setFormData({ ...formData, notlar: e.target.value })}
                />
              </div>

              <button
                type="submit"
                disabled={submitting}
                className="btn btn-primary"
                style={{ width: '100%', padding: '0.8rem', fontSize: '0.9rem' }}
              >
                {submitting ? 'Kaydediliyor...' : '✓ 30 Saniyede Ziyareti Kaydet'}
              </button>
            </form>
          )}
        </div>

        {/* Recent Visits Feed */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <FileText size={18} />
              <span>Son Kaydedilen Ziyaretler ({visits.length})</span>
            </div>
          </div>

          {visits.length === 0 ? (
            <div className="empty-state">
              <Calendar className="empty-state-icon" />
              <p>Henüz gerçekleşmiş saha ziyareti bulunmuyor.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', maxHeight: '680px', overflowY: 'auto' }}>
              {visits.map((v) => (
                <div
                  key={v.id}
                  style={{
                    background: 'var(--bg-input)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-md)',
                    padding: '0.9rem',
                    fontSize: '0.82rem'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.4rem' }}>
                    <div>
                      <span style={{ fontWeight: 700, fontSize: '0.9rem' }}>{v.firmaAdi || v.firmaUnvani}</span>
                      {v.sehirBolge && <span style={{ color: 'var(--text-muted)', marginLeft: '0.5rem' }}>({v.sehirBolge})</span>}
                    </div>
                    <button
                      onClick={() => handleDeleteVisit(v.id)}
                      className="btn btn-danger btn-sm"
                      style={{ padding: '0.2rem 0.4rem' }}
                      title="Ziyareti Sil"
                    >
                      <Trash2 size={12} />
                    </button>
                  </div>

                  <div style={{ color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>
                    <strong>Görüşme:</strong> {v.ziyaretKonusu}
                  </div>

                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem', marginBottom: '0.5rem' }}>
                    {v.numuneVerildiMi && (
                      <span className="badge badge-info">
                        🧪 Numune ({v.numuneler?.length || 1})
                      </span>
                    )}
                    {v.teklifVerildiMi && (
                      <span className="badge badge-success">
                        💼 Teklif ({v.teklifler?.length || 1})
                      </span>
                    )}
                    <span className="badge badge-purple">
                      👤 {v.plasiyerAdi || v.plasiyerAdSoyad}
                    </span>
                  </div>

                  {v.sonrakiAksiyon && (
                    <div style={{
                      background: 'rgba(99, 102, 241, 0.08)',
                      padding: '0.4rem 0.6rem',
                      borderRadius: 'var(--radius-sm)',
                      fontSize: '0.75rem',
                      color: '#a5b4fc',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.4rem'
                    }}>
                      <ArrowRight size={13} />
                      <span><strong>Sonraki Aksiyon:</strong> {v.sonrakiAksiyon}</span>
                    </div>
                  )}

                  <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.5rem', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                    <span>Tarih: {new Date(v.ziyaretTarihi).toLocaleDateString('tr-TR')}</span>
                    {(v.yetkiliAdi || v.yetkiliAdSoyad) && <span>Yetkili: {v.yetkiliAdi || v.yetkiliAdSoyad}</span>}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
