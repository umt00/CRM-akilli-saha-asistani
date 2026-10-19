import React, { useState } from 'react';
import { TeklifYaniti, TeklifDurumu, FirmaYaniti } from '../../types/crm.types';
import { CompanyHoverCard } from '../../components/common/CustomerCard';
import {
  Clock, CheckCircle2, XCircle, AlertCircle, Building2,
  DollarSign, User, Trash2, ArrowRightCircle, Sparkles, Inbox
} from 'lucide-react';
import './KanbanBoard.css';

interface KanbanBoardProps {
  offers: TeklifYaniti[];
  companies: FirmaYaniti[];
  onUpdateStatus: (id: number, newStatus: TeklifDurumu) => Promise<void>;
  onDeleteOffer: (id: number) => Promise<void>;
  onSelectCompany?: (comp: FirmaYaniti) => void;
}

interface ColumnConfig {
  id: TeklifDurumu;
  title: string;
  icon: React.ReactNode;
  headerBg: string;
  color: string;
  badgeBg: string;
}

const COLUMNS: ColumnConfig[] = [
  {
    id: 'ACIK',
    title: 'Müzakerede / Açık',
    icon: <Clock size={16} className="text-sky-400" />,
    headerBg: 'rgba(56, 189, 248, 0.1)',
    color: '#38bdf8',
    badgeBg: 'rgba(56, 189, 248, 0.2)',
  },
  {
    id: 'KAZANILDI',
    title: 'Kazanıldı (Sipariş)',
    icon: <CheckCircle2 size={16} className="text-emerald-400" />,
    headerBg: 'rgba(16, 185, 129, 0.1)',
    color: '#34d399',
    badgeBg: 'rgba(16, 185, 129, 0.2)',
  },
  {
    id: 'KAYBEDILDI',
    title: 'Kaybedildi',
    icon: <XCircle size={16} className="text-rose-400" />,
    headerBg: 'rgba(244, 63, 94, 0.1)',
    color: '#fb7185',
    badgeBg: 'rgba(244, 63, 94, 0.2)',
  },
  {
    id: 'IPTAL',
    title: 'İptal / Askıda',
    icon: <AlertCircle size={16} className="text-slate-400" />,
    headerBg: 'rgba(148, 163, 184, 0.1)',
    color: '#94a3b8',
    badgeBg: 'rgba(148, 163, 184, 0.2)',
  },
];

export const KanbanBoard: React.FC<KanbanBoardProps> = ({
  offers,
  companies,
  onUpdateStatus,
  onDeleteOffer,
  onSelectCompany,
}) => {
  const [draggedOfferId, setDraggedOfferId] = useState<number | null>(null);
  const [activeDropColumn, setActiveDropColumn] = useState<TeklifDurumu | null>(null);

  // Group offers by status
  const offersByStatus: Record<TeklifDurumu, TeklifYaniti[]> = {
    ACIK: [],
    KAZANILDI: [],
    KAYBEDILDI: [],
    IPTAL: [],
  };

  offers.forEach((o) => {
    if (offersByStatus[o.durum]) {
      offersByStatus[o.durum].push(o);
    } else {
      offersByStatus.ACIK.push(o);
    }
  });

  // Drag handlers
  const handleDragStart = (e: React.DragEvent, offerId: number) => {
    e.dataTransfer.setData('text/plain', String(offerId));
    e.dataTransfer.effectAllowed = 'move';
    setDraggedOfferId(offerId);
  };

  const handleDragEnd = () => {
    setDraggedOfferId(null);
    setActiveDropColumn(null);
  };

  const handleDragOver = (e: React.DragEvent, columnId: TeklifDurumu) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    if (activeDropColumn !== columnId) {
      setActiveDropColumn(columnId);
    }
  };

  const handleDragLeave = (e: React.DragEvent, columnId: TeklifDurumu) => {
    // Only clear if leaving the column element itself
    if (e.currentTarget === e.target) {
      setActiveDropColumn(null);
    }
  };

  const handleDrop = async (e: React.DragEvent, columnId: TeklifDurumu) => {
    e.preventDefault();
    setActiveDropColumn(null);
    const offerIdStr = e.dataTransfer.getData('text/plain');
    const offerId = Number(offerIdStr) || draggedOfferId;
    if (!offerId) return;

    const offer = offers.find((o) => o.id === offerId);
    if (!offer || offer.durum === columnId) return;

    await onUpdateStatus(offerId, columnId);
  };

  // Helper to lookup company details for hover card
  const getCompanyData = (offer: TeklifYaniti) => {
    const matched = companies.find((c) => c.id === offer.firmaId);
    return {
      firmaId: offer.firmaId,
      firmaUnvani: offer.firmaAdi || offer.firmaUnvani || matched?.unvan || 'Müşteri Firma',
      sehirBolge: matched?.sehirBolge,
      yetkiliAdSoyad: matched?.yetkililer?.[0]?.adSoyad,
      yetkiliUnvan: matched?.yetkililer?.[0]?.unvanGorev,
      yetkiliTelefon: matched?.yetkililer?.[0]?.telefon || matched?.telefon,
      yetkiliEposta: matched?.yetkililer?.[0]?.eposta || matched?.eposta,
      plasiyerAdSoyad: offer.plasiyerAdi || offer.plasiyerAdSoyad,
      durum: offer.durum,
      notlar: offer.notlar,
    };
  };

  return (
    <div className="kanban-columns-container">
      {COLUMNS.map((col) => {
        const columnOffers = offersByStatus[col.id] || [];
        const totalAmount = columnOffers.reduce((sum, o) => sum + (Number(o.tutar) || 0), 0);
        const isDropTarget = activeDropColumn === col.id;

        return (
          <div
            key={col.id}
            className={`kanban-column ${isDropTarget ? 'drag-over' : ''}`}
            onDragOver={(e) => handleDragOver(e, col.id)}
            onDragLeave={(e) => handleDragLeave(e, col.id)}
            onDrop={(e) => handleDrop(e, col.id)}
          >
            {/* Column Header */}
            <div
              className="kanban-column-header"
              style={{ background: col.headerBg, borderTop: `3px solid ${col.color}` }}
            >
              <div className="kanban-header-top">
                <div className="kanban-column-title">
                  {col.icon}
                  <span>{col.title}</span>
                </div>
                <span
                  className="kanban-count-badge"
                  style={{ background: col.badgeBg, color: col.color }}
                >
                  {columnOffers.length} Teklif
                </span>
              </div>

              <div className="kanban-column-sum">
                <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Toplam Hacim</span>
                <span style={{ color: col.color, fontWeight: 800 }}>
                  {totalAmount.toLocaleString('tr-TR')} ₺
                </span>
              </div>
            </div>

            {/* Column Cards */}
            <div className="kanban-cards-list">
              {columnOffers.length === 0 ? (
                <div className="kanban-empty-col">
                  <Inbox size={22} style={{ opacity: 0.5, marginBottom: '0.2rem' }} />
                  <span>Bu aşamada teklif yok</span>
                  <span style={{ fontSize: '0.7rem', opacity: 0.7 }}>Kart sürükleyip bırakabilirsiniz</span>
                </div>
              ) : (
                columnOffers.map((offer) => {
                  const matchedComp = companies.find((c) => c.id === offer.firmaId);
                  const companyData = getCompanyData(offer);
                  const isDragging = draggedOfferId === offer.id;

                  return (
                    <div
                      key={offer.id}
                      draggable
                      onDragStart={(e) => handleDragStart(e, offer.id)}
                      onDragEnd={handleDragEnd}
                      className={`kanban-card ${isDragging ? 'is-dragging' : ''}`}
                    >
                      {/* Card Top: Company Hover Card & Delete */}
                      <div className="kanban-card-top">
                        <CompanyHoverCard
                          data={companyData}
                          onOpenDetails={matchedComp && onSelectCompany ? () => onSelectCompany(matchedComp) : undefined}
                        >
                          <div
                            className="kanban-card-company"
                            title="Müşteri kartını incelemek için üzerine gelin, zaman çizelgesini açmak için tıklayın"
                            onClick={() => matchedComp && onSelectCompany && onSelectCompany(matchedComp)}
                          >
                            <Building2 size={14} className="text-primary flex-shrink-0" />
                            <span className="truncate max-w-[190px]">{companyData.firmaUnvani}</span>
                          </div>
                        </CompanyHoverCard>

                        <button
                          onClick={() => onDeleteOffer(offer.id)}
                          className="kanban-card-delete-btn"
                          title="Teklifi Sil"
                        >
                          <Trash2 size={13} />
                        </button>
                      </div>

                      {/* Offer Subject / Title */}
                      <div className="kanban-card-title" title={offer.baslik}>
                        {offer.baslik}
                      </div>

                      {/* Big Money Pill */}
                      <div className="kanban-card-amount">
                        <span>{Number(offer.tutar).toLocaleString('tr-TR')}</span>
                        <span className="kanban-card-currency">{offer.paraBirimi || 'TRY'}</span>
                      </div>

                      {/* Notes excerpt if any */}
                      {offer.notlar && (
                        <div
                          style={{
                            fontSize: '0.72rem',
                            color: 'var(--text-muted)',
                            background: 'rgba(255, 255, 255, 0.03)',
                            padding: '0.35rem 0.5rem',
                            borderRadius: '6px',
                            borderLeft: '2px solid rgba(99, 102, 241, 0.5)',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap',
                          }}
                          title={offer.notlar}
                        >
                          {offer.notlar}
                        </div>
                      )}

                      {/* Card Footer: Representative & Fast Status Change Buttons */}
                      <div className="kanban-card-footer">
                        <div className="kanban-card-rep" title="Sorumlu Plasiyer">
                          <User size={12} className="text-primary" />
                          <span>{offer.plasiyerAdi || offer.plasiyerAdSoyad || 'Temsilci'}</span>
                        </div>

                        {/* Quick Status Buttons (For one-click change without drag) */}
                        <div className="kanban-quick-actions">
                          {col.id === 'ACIK' && (
                            <>
                              <button
                                onClick={() => onUpdateStatus(offer.id, 'KAZANILDI')}
                                className="kanban-stage-btn win"
                                title="Kazanıldı (Sipariş Alındı)"
                              >
                                ✓ Kazan
                              </button>
                              <button
                                onClick={() => onUpdateStatus(offer.id, 'KAYBEDILDI')}
                                className="kanban-stage-btn lose"
                                title="Kaybedildi Olarak İşaretle"
                              >
                                ✗ Kaybet
                              </button>
                            </>
                          )}

                          {col.id !== 'ACIK' && (
                            <button
                              onClick={() => onUpdateStatus(offer.id, 'ACIK')}
                              className="kanban-stage-btn reopen"
                              title="Teklifi Yeniden Aç / Müzakereye Al"
                            >
                              ↩ Aç
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};
