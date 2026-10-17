import React, { useState } from 'react';
import {
  Building2, User, Phone, Mail, MapPin, ExternalLink, Calendar,
  Sparkles, CheckCircle, ShieldCheck, Clock, FileText, ChevronRight
} from 'lucide-react';

export interface CustomerCardData {
  firmaId?: number;
  firmaUnvani: string;
  sehirBolge?: string;
  yetkiliAdSoyad?: string;
  yetkiliUnvan?: string;
  yetkiliTelefon?: string;
  yetkiliEposta?: string;
  plasiyerAdSoyad?: string;
  durum?: string;
  notlar?: string;
}

interface CompanyHoverCardProps {
  data: CustomerCardData;
  children: React.ReactNode;
  onOpenDetails?: () => void;
}

/**
 * Bitrix24 tarzı etkileşimli Müşteri ve Kişi Önizleme Kartı (Hover Popover).
 * Müşteri adının üzerine gelindiğinde zengin görsel kart açar.
 */
export const CompanyHoverCard: React.FC<CompanyHoverCardProps> = ({
  data,
  children,
  onOpenDetails,
}) => {
  const [isVisible, setIsVisible] = useState(false);
  const [timeoutId, setTimeoutId] = useState<any>(null);

  const handleMouseEnter = () => {
    if (timeoutId) clearTimeout(timeoutId);
    const id = setTimeout(() => setIsVisible(true), 200);
    setTimeoutId(id);
  };

  const handleMouseLeave = () => {
    if (timeoutId) clearTimeout(timeoutId);
    const id = setTimeout(() => setIsVisible(false), 250);
    setTimeoutId(id);
  };

  const getInitials = (name: string) => {
    if (!name) return 'CRM';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[1][0]).toUpperCase();
  };

  return (
    <div
      className="company-hover-wrapper"
      style={{ position: 'relative', display: 'inline-block' }}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      {children}

      {isVisible && (
        <div
          className="customer-hover-card fade-in"
          style={{
            position: 'absolute',
            top: 'calc(100% + 8px)',
            left: '0',
            zIndex: 1000,
            width: '320px',
            background: 'var(--bg-card, #1e293b)',
            border: '1px solid var(--border-color, #334155)',
            borderRadius: '12px',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.5)',
            padding: '1rem',
            backdropFilter: 'blur(16px)',
            color: 'var(--text-primary, #f8fafc)',
            pointerEvents: 'auto',
          }}
        >
          {/* Header with Avatar & Company Name */}
          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', marginBottom: '0.75rem' }}>
            <div
              style={{
                width: '42px',
                height: '42px',
                borderRadius: '10px',
                background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 700,
                fontSize: '0.9rem',
                color: '#ffffff',
                boxShadow: '0 4px 10px rgba(99, 102, 241, 0.3)',
                flexShrink: 0,
              }}
            >
              {getInitials(data.firmaUnvani)}
            </div>

            <div style={{ flex: 1, minWidth: 0 }}>
              <div
                style={{
                  fontWeight: 700,
                  fontSize: '0.95rem',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.35rem',
                }}
              >
                <span>{data.firmaUnvani}</span>
                <ShieldCheck size={14} style={{ color: '#38bdf8', flexShrink: 0 }} />
              </div>
              <div style={{ fontSize: '0.75rem', color: '#94a3b8', display: 'flex', alignItems: 'center', gap: '0.3rem', marginTop: '0.1rem' }}>
                <MapPin size={12} />
                <span>{data.sehirBolge || 'Bölge Belirtilmemiş'}</span>
              </div>
            </div>
          </div>

          {/* Contact Person Card Box */}
          <div
            style={{
              background: 'rgba(255, 255, 255, 0.04)',
              border: '1px solid rgba(255, 255, 255, 0.08)',
              borderRadius: '8px',
              padding: '0.65rem 0.75rem',
              marginBottom: '0.75rem',
            }}
          >
            <div style={{ fontSize: '0.7rem', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '0.3rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
              <User size={12} />
              <span>Görüşülen Yetkili Muhatap</span>
            </div>
            <div style={{ fontWeight: 600, fontSize: '0.88rem', color: '#e2e8f0' }}>
              {data.yetkiliAdSoyad || 'Doğrudan Tabela / Firma Sahibi'}
            </div>
            {data.yetkiliUnvan && (
              <div style={{ fontSize: '0.75rem', color: '#a5b4fc', marginTop: '0.1rem' }}>
                {data.yetkiliUnvan}
              </div>
            )}
          </div>

          {/* Quick Actions (Call, Mail) */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', marginBottom: '0.75rem' }}>
            {data.yetkiliTelefon ? (
              <a
                href={`tel:${data.yetkiliTelefon}`}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.4rem',
                  background: 'rgba(16, 185, 129, 0.15)',
                  border: '1px solid rgba(16, 185, 129, 0.3)',
                  color: '#34d399',
                  padding: '0.45rem 0.5rem',
                  borderRadius: '6px',
                  fontSize: '0.78rem',
                  fontWeight: 600,
                  textDecoration: 'none',
                  transition: 'all 0.15s ease',
                }}
              >
                <Phone size={13} />
                <span>Hemen Ara</span>
              </a>
            ) : (
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.4rem',
                  background: 'rgba(255, 255, 255, 0.03)',
                  border: '1px solid rgba(255, 255, 255, 0.05)',
                  color: '#64748b',
                  padding: '0.45rem 0.5rem',
                  borderRadius: '6px',
                  fontSize: '0.75rem',
                }}
              >
                <Phone size={13} />
                <span>Tel Yok</span>
              </div>
            )}

            {data.yetkiliEposta ? (
              <a
                href={`mailto:${data.yetkiliEposta}`}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.4rem',
                  background: 'rgba(59, 130, 246, 0.15)',
                  border: '1px solid rgba(59, 130, 246, 0.3)',
                  color: '#60a5fa',
                  padding: '0.45rem 0.5rem',
                  borderRadius: '6px',
                  fontSize: '0.78rem',
                  fontWeight: 600,
                  textDecoration: 'none',
                }}
              >
                <Mail size={13} />
                <span>E-posta</span>
              </a>
            ) : (
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.4rem',
                  background: 'rgba(255, 255, 255, 0.03)',
                  border: '1px solid rgba(255, 255, 255, 0.05)',
                  color: '#64748b',
                  padding: '0.45rem 0.5rem',
                  borderRadius: '6px',
                  fontSize: '0.75rem',
                }}
              >
                <Mail size={13} />
                <span>E-posta Yok</span>
              </div>
            )}
          </div>

          {/* Footer Plasiyer info & More button */}
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              borderTop: '1px solid rgba(255, 255, 255, 0.08)',
              paddingTop: '0.6rem',
              fontSize: '0.72rem',
              color: '#94a3b8',
            }}
          >
            <span>Temsilci: <strong style={{ color: '#cbd5e1' }}>{data.plasiyerAdSoyad || 'Atanmamış'}</strong></span>

            {onOpenDetails && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onOpenDetails();
                }}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: '#818cf8',
                  cursor: 'pointer',
                  fontWeight: 600,
                  fontSize: '0.75rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.2rem',
                  padding: '2px 4px',
                }}
              >
                <span>Detay Gör</span>
                <ChevronRight size={13} />
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
