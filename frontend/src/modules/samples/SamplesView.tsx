import React, { useState, useEffect } from 'react';
import { numuneApi } from '../../services/api';
import { NumuneYaniti, NumuneDurumu } from '../../types/crm.types';
import {
  FlaskConical, CheckCircle2, XCircle, Clock, ShoppingCart,
  Layers, RefreshCw, Edit3, Trash2, ArrowUpRight
} from 'lucide-react';

export const SamplesView: React.FC = () => {
  const [samples, setSamples] = useState<NumuneYaniti[]>([]);
  const [stats, setStats] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(false);
  const [selectedSample, setSelectedSample] = useState<NumuneYaniti | null>(null);
  const [newStatus, setNewStatus] = useState<NumuneDurumu>('BEGENDI');
  const [resultNotes, setResultNotes] = useState('');
  const [updating, setUpdating] = useState(false);

  const loadSamplesAndStats = async () => {
    setLoading(true);
    try {
      const [samplesRes, statsRes] = await Promise.all([
        numuneApi.numuneleriGetir(0, 100),
        numuneApi.numuneIstatistikleriGetir().catch(() => ({})),
      ]);
      setSamples(samplesRes.content || []);
      setStats(statsRes || {});
    } catch (err) {
      console.error('Numuneler yüklenirken hata:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSamplesAndStats();
  }, []);

  const handleUpdateStatus = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSample) return;
    setUpdating(true);
    try {
      await numuneApi.numuneDurumuGuncelle(selectedSample.id, newStatus, resultNotes);
      setSelectedSample(null);
      setResultNotes('');
      loadSamplesAndStats();
    } catch (err: any) {
      alert('Durum güncellenirken hata: ' + (err.response?.data?.mesaj || err.message));
    } finally {
      setUpdating(false);
    }
  };

  const handleDeleteSample = async (id: number) => {
    if (!window.confirm('Bu numune kaydını silmek istediğinize emin misiniz?')) return;
    try {
      await numuneApi.numuneSil(id);
      loadSamplesAndStats();
    } catch (err: any) {
      alert('Numune silinemedi: ' + (err.response?.data?.mesaj || err.message));
    }
  };

  const getStatusBadge = (status: NumuneDurumu) => {
    switch (status) {
      case 'BEKLEMEDE':
        return <span className="badge badge-warning"><Clock size={11} /> Beklemede</span>;
      case 'TEST_ASAMASINDA':
        return <span className="badge badge-info"><FlaskConical size={11} /> Testte</span>;
      case 'BEGENDI':
        return <span className="badge badge-success"><CheckCircle2 size={11} /> Beğendi</span>;
      case 'SIPARISE_DONUSTU':
        return <span className="badge badge-purple"><ShoppingCart size={11} /> Sipariş Oldu 🎉</span>;
      case 'REDDETTI':
        return <span className="badge badge-danger"><XCircle size={11} /> Reddetti</span>;
      default:
        return <span className="badge badge-info">{status}</span>;
    }
  };

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <FlaskConical className="text-primary" size={24} />
            Numune Masası & Tadım Takibi (Kolon K & L)
          </h2>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            Müşteriye bırakılan ürünlerin test, beğeni ve siparişe dönüşüm hunisi
          </p>
        </div>

        <button onClick={loadSamplesAndStats} className="btn btn-secondary btn-sm">
          <RefreshCw size={14} className={loading ? 'animate-spin' : ''} /> Yenile
        </button>
      </div>

      {/* Stats Summary Cards */}
      <div className="grid-4" style={{ marginBottom: '1.25rem' }}>
        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(99, 102, 241, 0.15)', color: '#818cf8' }}>
            <Layers size={22} />
          </div>
          <div>
            <div className="stat-number">{samples.length}</div>
            <div className="stat-label">Toplam Bırakılan Numune</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(245, 158, 11, 0.15)', color: '#fbbf24' }}>
            <Clock size={22} />
          </div>
          <div>
            <div className="stat-number">
              {samples.filter((s) => s.durum === 'BEKLEMEDE' || s.durum === 'TEST_ASAMASINDA').length}
            </div>
            <div className="stat-label">Değerlendirme Aşamasında</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#34d399' }}>
            <CheckCircle2 size={22} />
          </div>
          <div>
            <div className="stat-number">
              {samples.filter((s) => s.durum === 'BEGENDI').length}
            </div>
            <div className="stat-label">Müşteri Beğendi</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(139, 92, 246, 0.15)', color: '#c084fc' }}>
            <ShoppingCart size={22} />
          </div>
          <div>
            <div className="stat-number">
              {samples.filter((s) => s.durum === 'SIPARISE_DONUSTU').length}
            </div>
            <div className="stat-label">Siparişe Dönüştü 🎯</div>
          </div>
        </div>
      </div>

      {/* Samples Table */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">
            <span>Numune Takip Listesi ({samples.length})</span>
          </div>
        </div>

        {samples.length === 0 ? (
          <div className="empty-state">
            <FlaskConical className="empty-state-icon" />
            <p>Kayıtlı numune bulunmuyor.</p>
            <p style={{ fontSize: '0.78rem', marginTop: '0.25rem' }}>
              Saha ziyareti oluştururken "Numune Bırakıldı mı?" kutucuğunu işaretleyerek ekleyebilirsiniz.
            </p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Firma Unvanı</th>
                  <th>Numune Ürün & Marka</th>
                  <th>Miktar</th>
                  <th>Durum</th>
                  <th>Tadım / Değerlendirme Notu (Kolon L)</th>
                  <th>Temsilci</th>
                  <th style={{ textAlign: 'right' }}>İşlemler</th>
                </tr>
              </thead>
              <tbody>
                {samples.map((s) => (
                  <tr key={s.id}>
                    <td style={{ fontWeight: 600 }}>{s.firmaAdi || s.firmaUnvani}</td>
                    <td>
                      <div>
                        <span style={{ fontWeight: 600 }}>{s.urunAdi}</span>
                        {s.marka && (
                          <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginLeft: '0.35rem' }}>
                            ({s.marka})
                          </span>
                        )}
                      </div>
                    </td>
                    <td>{s.miktar} Adet/Birim</td>
                    <td>{getStatusBadge(s.durum)}</td>
                    <td style={{ fontSize: '0.78rem', color: s.sonucNotlari ? 'var(--text-primary)' : 'var(--text-muted)' }}>
                      {s.sonucNotlari || 'Henüz sonuç notu girilmedi.'}
                    </td>
                    <td style={{ fontSize: '0.78rem' }}>{s.plasiyerAdi || s.plasiyerAdSoyad}</td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.35rem' }}>
                        <button
                          onClick={() => {
                            setSelectedSample(s);
                            setNewStatus(s.durum);
                            setResultNotes(s.sonucNotlari || '');
                          }}
                          className="btn btn-secondary btn-sm"
                          title="Durum Güncelle"
                        >
                          <Edit3 size={13} /> Durum
                        </button>
                        <button
                          onClick={() => handleDeleteSample(s.id)}
                          className="btn btn-danger btn-sm"
                          title="Numuneyi Sil"
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

      {/* Modal: Numune Durumu Güncelle */}
      {selectedSample && (
        <div className="modal-backdrop">
          <div className="modal-card" style={{ maxWidth: '480px' }}>
            <div className="card-header">
              <h3 className="card-title">
                <FlaskConical size={18} /> Numune Sonucu & Durumu Güncelle
              </h3>
              <button onClick={() => setSelectedSample(null)} className="btn btn-secondary btn-sm">X</button>
            </div>

            <div style={{ marginBottom: '1rem', fontSize: '0.82rem' }}>
              <div><strong>Firma:</strong> {selectedSample.firmaAdi || selectedSample.firmaUnvani}</div>
              <div><strong>Ürün:</strong> {selectedSample.urunAdi} ({selectedSample.marka || '-'})</div>
            </div>

            <form onSubmit={handleUpdateStatus}>
              <div className="form-group">
                <label className="form-label">Yeni Durum *</label>
                <select
                  className="form-control"
                  value={newStatus}
                  onChange={(e) => setNewStatus(e.target.value as NumuneDurumu)}
                >
                  <option value="BEKLEMEDE">⏳ Beklemede</option>
                  <option value="TEST_ASAMASINDA">🔬 Test Aşamasında / Mutfakta deneniyor</option>
                  <option value="BEGENDI">👍 Beğendi (Tadım Olumlu)</option>
                  <option value="REDDETTI">👎 Reddetti (Fiyat/Lezzet uymadı)</option>
                  <option value="SIPARISE_DONUSTU">🎯 Siparişe Dönüştü (Satış Kapandı)</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Tadım / Sonuç Notları (Kolon L)</label>
                <textarea
                  className="form-control"
                  rows={3}
                  placeholder="Müşteri aşçısının geri bildirimi, koku/tat yorumu veya ret sebebi..."
                  value={resultNotes}
                  onChange={(e) => setResultNotes(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
                <button type="button" onClick={() => setSelectedSample(null)} className="btn btn-secondary">
                  Vazgeç
                </button>
                <button type="submit" disabled={updating} className="btn btn-primary">
                  {updating ? 'Kaydediliyor...' : 'Güncelle ve Kaydet'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
