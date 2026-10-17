import React, { useState, useEffect } from 'react';
import { ziyaretApi } from '../../services/api';
import { ZiyaretYaniti } from '../../types/crm.types';
import { Calendar, Clock, MapPin, Building2, User, ArrowRight, RefreshCw, CheckCircle2 } from 'lucide-react';

export const AgendaView: React.FC = () => {
  const [agendaVisits, setAgendaVisits] = useState<ZiyaretYaniti[]>([]);
  const [loading, setLoading] = useState(false);

  const loadAgenda = async () => {
    setLoading(true);
    try {
      const data = await ziyaretApi.ajandaZiyaretleriniGetir();
      setAgendaVisits(data || []);
    } catch (err) {
      console.error('Ajanda yüklenirken hata:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAgenda();
  }, []);

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Calendar className="text-primary" size={24} />
            Ajanda & Yaklaşan Randevular (Kolon P)
          </h2>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            Vadesi gelen, bugün yapılması gereken veya planlanan saha aksiyonları
          </p>
        </div>

        <button onClick={loadAgenda} className="btn btn-secondary btn-sm">
          <RefreshCw size={14} className={loading ? 'animate-spin' : ''} /> Yenile
        </button>
      </div>

      <div className="card">
        <div className="card-header">
          <div className="card-title">
            <span>Planlanmış Aksiyonlar & Randevular ({agendaVisits.length})</span>
          </div>
        </div>

        {agendaVisits.length === 0 ? (
          <div className="empty-state">
            <CheckCircle2 className="empty-state-icon" style={{ color: 'var(--success)', opacity: 0.8 }} />
            <p style={{ fontWeight: 600 }}>Tebrikler! Vadesi geçmiş veya bekleyen randevu bulunmuyor.</p>
            <p style={{ fontSize: '0.78rem', marginTop: '0.25rem' }}>
              Yeni bir saha ziyareti oluştururken "Sonraki Ziyaret Tarihi" belirterek ajandanıza ekleyebilirsiniz.
            </p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Hedef Tarih (Kolon P)</th>
                  <th>Müşteri Firma</th>
                  <th>Bölge</th>
                  <th>Yetkili</th>
                  <th>Planlanan Sonraki Aksiyon (Kolon O)</th>
                  <th>Temsilci</th>
                </tr>
              </thead>
              <tbody>
                {agendaVisits.map((item) => {
                  const targetDate = item.sonrakiZiyaretTarihi
                    ? new Date(item.sonrakiZiyaretTarihi)
                    : null;
                  const isPast = targetDate && targetDate.getTime() < Date.now();

                  return (
                    <tr key={item.id}>
                      <td>
                        <span className={`badge ${isPast ? 'badge-danger' : 'badge-warning'}`}>
                          <Clock size={12} />
                          {targetDate
                            ? targetDate.toLocaleString('tr-TR', { dateStyle: 'short', timeStyle: 'short' })
                            : 'Tarih yok'}
                        </span>
                        {isPast && (
                          <div style={{ fontSize: '0.68rem', color: '#f87171', marginTop: '0.2rem' }}>
                            ⚠️ Vadesi Geçti
                          </div>
                        )}
                      </td>
                      <td style={{ fontWeight: 600 }}>{item.firmaAdi || item.firmaUnvani}</td>
                      <td>{item.sehirBolge || '-'}</td>
                      <td>{item.yetkiliAdi || item.yetkiliAdSoyad || '-'}</td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#c7d2fe' }}>
                          <ArrowRight size={14} />
                          <span>{item.sonrakiAksiyon || 'Aksiyon notu belirtilmemiş'}</span>
                        </div>
                      </td>
                      <td>{item.plasiyerAdi || item.plasiyerAdSoyad}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
