import React, { useState, useEffect } from 'react';
import {
  FirmaYaniti, ZiyaretYaniti, NumuneYaniti, TeklifYaniti, YetkiliIstegi
} from '../../types/crm.types';
import { ziyaretApi, numuneApi, teklifApi, firmaApi } from '../../services/api';
import {
  X, Building2, MapPin, Phone, Mail, User, Clock,
  Car, FlaskConical, DollarSign, Calendar, ShieldCheck,
  CheckCircle2, XCircle, AlertCircle, Plus, Sparkles,
  RefreshCw, FileText, ChevronRight, MessageSquare
} from 'lucide-react';
import './CustomerDrawer.css';

interface CustomerDrawerProps {
  company: FirmaYaniti | null;
  isOpen: boolean;
  onClose: () => void;
  onCompanyUpdated?: () => void;
}

type TabType = 'timeline' | 'contacts' | 'intelligence';
type TimelineFilter = 'all' | 'visit' | 'sample' | 'offer';

interface TimelineEvent {
  id: string;
  type: 'visit' | 'sample' | 'offer';
  date: string;
  title: string;
  badge: React.ReactNode;
  repName?: string;
  contactName?: string;
  details?: string;
  rawDate: number;
}

export const CustomerDrawer: React.FC<CustomerDrawerProps> = ({
  company,
  isOpen,
  onClose,
  onCompanyUpdated,
}) => {
  const [activeTab, setActiveTab] = useState<TabType>('timeline');
  const [timelineFilter, setTimelineFilter] = useState<TimelineFilter>('all');
  const [loading, setLoading] = useState(false);

  // Loaded child activities
  const [visits, setVisits] = useState<ZiyaretYaniti[]>([]);
  const [samples, setSamples] = useState<NumuneYaniti[]>([]);
  const [offers, setOffers] = useState<TeklifYaniti[]>([]);

  // Add Contact Form State
  const [showAddContact, setShowAddContact] = useState(false);
  const [newContact, setNewContact] = useState<YetkiliIstegi>({
    firmaId: 0,
    adSoyad: '',
    unvanGorev: '',
    telefon: '',
    eposta: '',
    notlar: '',
  });

  const loadActivities = async () => {
    if (!company) return;
    setLoading(true);
    try {
      const [visitRes, sampleRes, offerRes] = await Promise.all([
        ziyaretApi.ziyaretleriGetir(0, 100, company.id),
        numuneApi.numuneleriGetir(0, 100, undefined, company.id),
        teklifApi.teklifleriGetir(0, 100, undefined, company.id),
      ]);
      setVisits(visitRes.content || []);
      setSamples(sampleRes.content || []);
      setOffers(offerRes.content || []);
    } catch (err) {
      console.error('Müşteri aktiviteleri yüklenirken hata:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen && company) {
      loadActivities();
      setActiveTab('timeline');
      setShowAddContact(false);
    }
  }, [isOpen, company?.id]);

  if (!isOpen || !company) return null;

  // Build Unified Timeline Stream
  const timelineEvents: TimelineEvent[] = [];

  // Add visits
  visits.forEach((v) => {
    const d = new Date(v.ziyaretTarihi || v.olusturulmaTarihi);
    timelineEvents.push({
      id: `visit-${v.id}`,
      type: 'visit',
      date: v.ziyaretTarihi ? new Date(v.ziyaretTarihi).toLocaleDateString('tr-TR') : '-',
      rawDate: d.getTime() || 0,
      title: v.ziyaretKonusu || 'Saha Ziyareti',
      badge: <span className="badge badge-info"><Car size={11} /> Ziyaret</span>,
      repName: v.plasiyerAdi || v.plasiyerAdSoyad,
      contactName: v.yetkiliAdi || v.yetkiliAdSoyad,
      details: v.notlar || (v.sonrakiAksiyon ? `Sonraki Aksiyon: ${v.sonrakiAksiyon}` : undefined),
    });
  });

  // Add samples
  samples.forEach((s) => {
    const d = new Date(s.gonderimTarihi || s.olusturulmaTarihi);
    let sampleBadge = <span className="badge badge-purple"><FlaskConical size={11} /> Numune</span>;
    if (s.durum === 'BEGENDI' || s.durum === 'SIPARISE_DONUSTU') {
      sampleBadge = <span className="badge badge-success"><CheckCircle2 size={11} /> {s.durum}</span>;
    } else if (s.durum === 'REDDETTI') {
      sampleBadge = <span className="badge badge-danger"><XCircle size={11} /> Reddedildi</span>;
    }

    timelineEvents.push({
      id: `sample-${s.id}`,
      type: 'sample',
      date: s.gonderimTarihi ? new Date(s.gonderimTarihi).toLocaleDateString('tr-TR') : '-',
      rawDate: d.getTime() || 0,
      title: `${s.urunAdi} (${s.miktar || '1 Adet'})`,
      badge: sampleBadge,
      repName: s.plasiyerAdi || s.plasiyerAdSoyad,
      details: s.sonucNotlari || undefined,
    });
  });

  // Add offers
  offers.forEach((o) => {
    const d = new Date(o.olusturulmaTarihi);
    let offerBadge = <span className="badge badge-info"><Clock size={11} /> Teklif (Açık)</span>;
    if (o.durum === 'KAZANILDI') {
      offerBadge = <span className="badge badge-success"><CheckCircle2 size={11} /> Teklif Kazanıldı</span>;
    } else if (o.durum === 'KAYBEDILDI') {
      offerBadge = <span className="badge badge-danger"><XCircle size={11} /> Kaybedildi</span>;
    }

    timelineEvents.push({
      id: `offer-${o.id}`,
      type: 'offer',
      date: o.olusturulmaTarihi ? new Date(o.olusturulmaTarihi).toLocaleDateString('tr-TR') : '-',
      rawDate: d.getTime() || 0,
      title: `${o.baslik} • ${Number(o.tutar).toLocaleString('tr-TR')} ${o.paraBirimi || 'TRY'}`,
      badge: offerBadge,
      repName: o.plasiyerAdi || o.plasiyerAdSoyad,
      details: o.notlar || undefined,
    });
  });

  // Sort chronologically (latest first)
  timelineEvents.sort((a, b) => b.rawDate - a.rawDate);

  // Filter
  const filteredEvents = timelineEvents.filter((item) => {
    if (timelineFilter === 'all') return true;
    return item.type === timelineFilter;
  });

  const totalOfferVolume = offers.reduce((sum, o) => sum + (Number(o.tutar) || 0), 0);

  const getInitials = (name: string) => {
    if (!name) return 'CRM';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[1][0]).toUpperCase();
  };

  const handleCreateContact = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newContact.adSoyad.trim()) return;
    try {
      await firmaApi.yetkiliEkle(company.id, {
        ...newContact,
        firmaId: company.id,
      });
      setShowAddContact(false);
      setNewContact({
        firmaId: company.id,
        adSoyad: '',
        unvanGorev: '',
        telefon: '',
        eposta: '',
        notlar: '',
      });
      if (onCompanyUpdated) onCompanyUpdated();
    } catch (err: any) {
      alert('Yetkili eklenemedi: ' + (err.response?.data?.mesaj || err.message));
    }
  };

  const handleDeleteContact = async (yetkiliId: number) => {
    if (!window.confirm('Bu yetkiliyi silmek istediğinize emin misiniz?')) return;
    try {
      await firmaApi.yetkiliSil(company.id, yetkiliId);
      if (onCompanyUpdated) onCompanyUpdated();
    } catch (err: any) {
      alert('Yetkili silinemedi: ' + (err.response?.data?.mesaj || err.message));
    }
  };

  return (
    <div className="customer-drawer-backdrop" onClick={onClose}>
      <div className="customer-drawer-panel" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="customer-drawer-header">
          <div className="customer-drawer-company-info">
            <div className="customer-drawer-avatar">
              {getInitials(company.unvan)}
            </div>
            <div className="customer-drawer-title-box">
              <div className="customer-drawer-unvan" title={company.unvan}>
                {company.unvan}
              </div>
              <div className="customer-drawer-meta">
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                  <MapPin size={13} className="text-primary" />
                  {company.sehirBolge || 'Bölge Belirtilmemiş'}
                </span>
                <span>•</span>
                <span style={{ color: '#cbd5e1' }}>
                  Temsilci: {company.atananPlasiyerAdi || company.atananPlasiyerAdSoyad || 'Atanmamış'}
                </span>
              </div>
            </div>
          </div>

          <button onClick={onClose} className="customer-drawer-close-btn" title="Kapat (ESC)">
            <X size={18} />
          </button>
        </div>

        {/* Quick KPI Row */}
        <div className="customer-drawer-kpis">
          <div className="customer-kpi-item">
            <span className="customer-kpi-label">Toplam Ziyaret</span>
            <span className="customer-kpi-value" style={{ color: '#38bdf8' }}>
              {visits.length} Ziyaret
            </span>
          </div>
          <div className="customer-kpi-item">
            <span className="customer-kpi-label">Numune Çıkışı</span>
            <span className="customer-kpi-value" style={{ color: '#a855f7' }}>
              {samples.length} Numune
            </span>
          </div>
          <div className="customer-kpi-item">
            <span className="customer-kpi-label">Teklif Hacmi</span>
            <span className="customer-kpi-value" style={{ color: '#34d399' }}>
              {totalOfferVolume.toLocaleString('tr-TR')} ₺
            </span>
          </div>
        </div>

        {/* Tabs Bar */}
        <div className="customer-drawer-tabs">
          <button
            onClick={() => setActiveTab('timeline')}
            className={`customer-drawer-tab ${activeTab === 'timeline' ? 'active' : ''}`}
          >
            <Clock size={15} />
            <span>Zaman Çizelgesi ({timelineEvents.length})</span>
          </button>
          <button
            onClick={() => setActiveTab('contacts')}
            className={`customer-drawer-tab ${activeTab === 'contacts' ? 'active' : ''}`}
          >
            <User size={15} />
            <span>Yetkililer ({company.yetkililer?.length || 0})</span>
          </button>
          <button
            onClick={() => setActiveTab('intelligence')}
            className={`customer-drawer-tab ${activeTab === 'intelligence' ? 'active' : ''}`}
          >
            <FileText size={15} />
            <span>Pazar İstihbaratı</span>
          </button>
        </div>

        {/* Drawer Body */}
        <div className="customer-drawer-body">
          {/* TAB 1: 360° TIMELINE FEED */}
          {activeTab === 'timeline' && (
            <div className="fade-in">
              <div className="timeline-filter-bar">
                <button
                  onClick={() => setTimelineFilter('all')}
                  className={`timeline-filter-btn ${timelineFilter === 'all' ? 'active' : ''}`}
                >
                  Tümü ({timelineEvents.length})
                </button>
                <button
                  onClick={() => setTimelineFilter('visit')}
                  className={`timeline-filter-btn ${timelineFilter === 'visit' ? 'active' : ''}`}
                >
                  🚗 Ziyaretler ({visits.length})
                </button>
                <button
                  onClick={() => setTimelineFilter('sample')}
                  className={`timeline-filter-btn ${timelineFilter === 'sample' ? 'active' : ''}`}
                >
                  🧪 Numuneler ({samples.length})
                </button>
                <button
                  onClick={() => setTimelineFilter('offer')}
                  className={`timeline-filter-btn ${timelineFilter === 'offer' ? 'active' : ''}`}
                >
                  💰 Teklifler ({offers.length})
                </button>
              </div>

              {loading ? (
                <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  <RefreshCw className="animate-spin" size={24} style={{ margin: '0 auto 0.5rem' }} />
                  <span>Aktivite geçmişi yükleniyor...</span>
                </div>
              ) : filteredEvents.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--text-muted)' }}>
                  <Clock size={32} style={{ opacity: 0.4, margin: '0 auto 0.5rem' }} />
                  <p style={{ fontSize: '0.88rem' }}>Bu filtreye uygun geçmiş aktivite bulunamadı.</p>
                </div>
              ) : (
                <div className="timeline-stream">
                  {filteredEvents.map((ev) => {
                    let dotBg = '#38bdf8';
                    let dotIcon = <Car size={10} color="#ffffff" />;
                    if (ev.type === 'sample') {
                      dotBg = '#a855f7';
                      dotIcon = <FlaskConical size={10} color="#ffffff" />;
                    } else if (ev.type === 'offer') {
                      dotBg = '#10b981';
                      dotIcon = <DollarSign size={10} color="#ffffff" />;
                    }

                    return (
                      <div key={ev.id} className="timeline-item">
                        <div className="timeline-item-dot" style={{ background: dotBg }}>
                          {dotIcon}
                        </div>

                        <div className="timeline-card">
                          <div className="timeline-card-header">
                            <div className="timeline-type-tag">
                              {ev.badge}
                              {ev.contactName && (
                                <span style={{ color: '#a5b4fc', fontSize: '0.72rem' }}>
                                  Muhatap: {ev.contactName}
                                </span>
                              )}
                            </div>
                            <span className="timeline-date">{ev.date}</span>
                          </div>

                          <div className="timeline-title">{ev.title}</div>

                          {ev.details && (
                            <div className="timeline-details">
                              {ev.details}
                            </div>
                          )}

                          {ev.repName && (
                            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.3rem', marginTop: '0.2rem' }}>
                              <User size={11} className="text-primary" />
                              <span>Kaydeden Plasiyer: {ev.repName}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

          {/* TAB 2: CONTACT PERSONS & CHEFS */}
          {activeTab === 'contacts' && (
            <div className="fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 700 }}>
                  Kayıtlı Yetkili ve Karar Vericiler
                </span>
                <button
                  onClick={() => setShowAddContact(!showAddContact)}
                  className="btn btn-primary btn-sm"
                  style={{ fontSize: '0.75rem' }}
                >
                  <Plus size={14} /> {showAddContact ? 'Formu Kapat' : 'Yeni Yetkili Ekle'}
                </button>
              </div>

              {/* Add Contact Inline Form */}
              {showAddContact && (
                <form
                  onSubmit={handleCreateContact}
                  style={{
                    background: 'rgba(99, 102, 241, 0.08)',
                    border: '1px solid rgba(99, 102, 241, 0.25)',
                    borderRadius: '10px',
                    padding: '1rem',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.75rem',
                  }}
                >
                  <div style={{ fontWeight: 700, fontSize: '0.85rem', color: '#a5b4fc' }}>
                    Yeni İlgili Kişi / Şef Bilgileri
                  </div>
                  <div className="grid-2">
                    <div className="form-group">
                      <label className="form-label">Adı Soyadı *</label>
                      <input
                        type="text"
                        required
                        className="form-control"
                        placeholder="Örn: Ahmet Yılmaz"
                        value={newContact.adSoyad}
                        onChange={(e) => setNewContact({ ...newContact, adSoyad: e.target.value })}
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Görevi / Departmanı</label>
                      <input
                        type="text"
                        className="form-control"
                        placeholder="Örn: Satın Alma Müdürü / Baş Aşçı"
                        value={newContact.unvanGorev || ''}
                        onChange={(e) => setNewContact({ ...newContact, unvanGorev: e.target.value })}
                      />
                    </div>
                  </div>
                  <div className="grid-2">
                    <div className="form-group">
                      <label className="form-label">Telefon Numarası</label>
                      <input
                        type="tel"
                        className="form-control"
                        placeholder="0532 ..."
                        value={newContact.telefon || ''}
                        onChange={(e) => setNewContact({ ...newContact, telefon: e.target.value })}
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">E-Posta Adresi</label>
                      <input
                        type="email"
                        className="form-control"
                        placeholder="ahmet@firma.com"
                        value={newContact.eposta || ''}
                        onChange={(e) => setNewContact({ ...newContact, eposta: e.target.value })}
                      />
                    </div>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                    <button
                      type="button"
                      onClick={() => setShowAddContact(false)}
                      className="btn btn-secondary btn-sm"
                    >
                      Vazgeç
                    </button>
                    <button type="submit" className="btn btn-primary btn-sm">
                      Yetkiliyi Kaydet
                    </button>
                  </div>
                </form>
              )}

              {/* Contacts List */}
              {(!company.yetkililer || company.yetkililer.length === 0) ? (
                <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  <User size={30} style={{ opacity: 0.35, margin: '0 auto 0.5rem' }} />
                  <p style={{ fontSize: '0.85rem' }}>Henüz kayıtlı yetkili kişi bulunmuyor.</p>
                </div>
              ) : (
                company.yetkililer.map((c) => (
                  <div key={c.id} className="drawer-contact-card">
                    <div>
                      <div style={{ fontWeight: 700, fontSize: '0.92rem', color: '#f8fafc' }}>
                        {c.adSoyad}
                      </div>
                      <div style={{ fontSize: '0.78rem', color: '#a5b4fc', marginTop: '0.1rem' }}>
                        {c.unvanGorev || 'Görevi Belirtilmemiş'}
                      </div>
                      <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.35rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        {c.telefon && <span>📞 {c.telefon}</span>}
                        {c.eposta && <span>✉️ {c.eposta}</span>}
                      </div>
                    </div>

                    <div className="drawer-contact-actions">
                      {c.telefon && (
                        <a
                          href={`tel:${c.telefon}`}
                          className="drawer-action-circle-btn"
                          style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#34d399', border: '1px solid rgba(16, 185, 129, 0.3)' }}
                          title="Hemen Ara"
                        >
                          <Phone size={14} />
                        </a>
                      )}
                      {c.eposta && (
                        <a
                          href={`mailto:${c.eposta}`}
                          className="drawer-action-circle-btn"
                          style={{ background: 'rgba(59, 130, 246, 0.15)', color: '#60a5fa', border: '1px solid rgba(59, 130, 246, 0.3)' }}
                          title="E-posta Gönder"
                        >
                          <Mail size={14} />
                        </a>
                      )}
                      <button
                        onClick={() => handleDeleteContact(c.id)}
                        className="drawer-action-circle-btn"
                        style={{ background: 'rgba(244, 63, 94, 0.12)', color: '#fb7185', border: '1px solid rgba(244, 63, 94, 0.25)', cursor: 'pointer' }}
                        title="Yetkiliyi Sil"
                      >
                        <X size={14} />
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}

          {/* TAB 3: 17-COLUMN FIELD INTELLIGENCE */}
          {activeTab === 'intelligence' && (
            <div className="fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="intel-card-section">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#818cf8', fontWeight: 700, fontSize: '0.88rem' }}>
                  <Sparkles size={16} />
                  <span>Saha & Pazar Rekabet Verileri</span>
                </div>

                <div className="intel-row">
                  <span className="intel-label">Mevcut Rakip Tedarikçi (Excel Kolon I)</span>
                  <span className="intel-value" style={{ color: company.mevcutTedarikciRakip ? '#fb7185' : 'var(--text-muted)' }}>
                    {company.mevcutTedarikciRakip || 'Rakip bilgisi girilmemiş'}
                  </span>
                </div>

                <div className="intel-row">
                  <span className="intel-label">Tedarik Ettiği Ürünler (Excel Kolon F)</span>
                  <span className="intel-value">
                    {company.tedarikEttigiUrunler || 'Belirtilmemiş'}
                  </span>
                </div>

                <div className="intel-row">
                  <span className="intel-label">Aylık Tüketim Kapasitesi (Excel Kolon G)</span>
                  <span className="intel-value" style={{ color: '#38bdf8' }}>
                    {company.aylikKullanimMiktari || 'Tüketim miktarı bilinmiyor'}
                  </span>
                </div>

                <div className="intel-row">
                  <span className="intel-label">Bizden Satın Aldığı Portföy (Excel Kolon H)</span>
                  <span className="intel-value" style={{ color: '#34d399' }}>
                    {company.bizdenAldigiUrunler || 'Henüz düzenli alım yok'}
                  </span>
                </div>
              </div>

              <div className="intel-card-section">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#cbd5e1', fontWeight: 700, fontSize: '0.88rem' }}>
                  <Building2 size={16} />
                  <span>Şirket ve Lokasyon Künyesi</span>
                </div>

                <div className="intel-row">
                  <span className="intel-label">Açık Adres</span>
                  <span className="intel-value">
                    {company.adres || 'Adres bilgisi mevcut değil'}
                  </span>
                </div>

                <div className="intel-row">
                  <span className="intel-label">Genel Santral / Telefon</span>
                  <span className="intel-value">
                    {company.telefon || '-'}
                  </span>
                </div>

                <div className="intel-row">
                  <span className="intel-label">Genel E-Posta</span>
                  <span className="intel-value">
                    {company.eposta || '-'}
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
