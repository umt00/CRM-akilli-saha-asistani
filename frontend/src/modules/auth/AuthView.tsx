import React, { useState } from 'react';
import { authApi } from '../../services/api';
import { ShieldCheck, UserCheck, KeyRound, Mail, Phone, Lock, Sparkles } from 'lucide-react';
import { Rol } from '../../types/crm.types';

interface AuthViewProps {
  onLoginSuccess: (user: any) => void;
}

export const AuthView: React.FC<AuthViewProps> = ({ onLoginSuccess }) => {
  const [isRegister, setIsRegister] = useState(false);
  const [eposta, setEposta] = useState('admin@akillisaha.com');
  const [sifre, setSifre] = useState('saha1234');
  const [adSoyad, setAdSoyad] = useState('');
  const [telefon, setTelefon] = useState('');
  const [rol, setRol] = useState<Rol>('PLASIYER');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (isRegister) {
        const res = await authApi.kayitOl({
          eposta,
          sifre,
          adSoyad,
          telefon,
          rol,
        });
        onLoginSuccess(res.user || res);
      } else {
        const res = await authApi.girisYap({ eposta, sifre });
        onLoginSuccess(res.user || res);
      }
    } catch (err: any) {
      setError(
        err.response?.data?.mesaj ||
          err.message ||
          'İşlem başarısız oldu. Lütfen bilgileri kontrol edin.'
      );
    } finally {
      setLoading(false);
    }
  };

  const fillQuickAccount = (email: string, pass: string) => {
    setIsRegister(false);
    setEposta(email);
    setSifre(pass);
    setError(null);
  };

  return (
    <div style={{
      minHeight: '80vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2rem'
    }}>
      <div className="card fade-in" style={{ maxWidth: '440px', width: '100%', padding: '2rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
          <div className="brand-logo-icon" style={{ margin: '0 auto 1rem', width: '48px', height: '48px' }}>
            <Sparkles size={26} />
          </div>
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>Akıllı Saha CRM</h2>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
            {isRegister ? 'Yeni Saha Temsilcisi veya Yönetici Kaydı' : 'Saha Asistanı Giriş Portalı'}
          </p>
        </div>

        {error && (
          <div style={{
            background: 'rgba(239, 68, 68, 0.12)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            borderRadius: 'var(--radius-sm)',
            padding: '0.75rem',
            color: '#f87171',
            fontSize: '0.8rem',
            marginBottom: '1.25rem'
          }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {isRegister && (
            <>
              <div className="form-group">
                <label className="form-label">Ad Soyad</label>
                <div style={{ position: 'relative' }}>
                  <input
                    type="text"
                    required
                    className="form-control"
                    placeholder="Örn: Burak Kaya"
                    value={adSoyad}
                    onChange={(e) => setAdSoyad(e.target.value)}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Telefon</label>
                <div style={{ position: 'relative' }}>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="05XX XXX XX XX"
                    value={telefon}
                    onChange={(e) => setTelefon(e.target.value)}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Sistem Rolü</label>
                <select
                  className="form-control"
                  value={rol}
                  onChange={(e) => setRol(e.target.value as Rol)}
                >
                  <option value="PLASIYER">Saha Satış Temsilcisi (Plasiyer)</option>
                  <option value="YONETICI">Sistem Yöneticisi (Admin)</option>
                </select>
              </div>
            </>
          )}

          <div className="form-group">
            <label className="form-label">E-Posta Adresi</label>
            <input
              type="email"
              required
              className="form-control"
              placeholder="ornek@akillisaha.com"
              value={eposta}
              onChange={(e) => setEposta(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Şifre</label>
            <input
              type="password"
              required
              className="form-control"
              placeholder="••••••••"
              value={sifre}
              onChange={(e) => setSifre(e.target.value)}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '0.75rem', padding: '0.75rem' }}
          >
            {loading ? 'İşleniyor...' : isRegister ? 'Hesap Oluştur ve Giriş Yap' : 'Giriş Yap'}
          </button>
        </form>

        <div style={{ marginTop: '1.25rem', textAlign: 'center' }}>
          <button
            type="button"
            onClick={() => setIsRegister(!isRegister)}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--primary)',
              fontSize: '0.8rem',
              cursor: 'pointer',
              textDecoration: 'underline'
            }}
          >
            {isRegister ? 'Zaten hesabınız var mı? Giriş Yapın' : 'Yeni hesap oluşturmak için tıklayın'}
          </button>
        </div>

        {/* Quick Test Accounts Filler */}
        <div style={{
          marginTop: '1.5rem',
          paddingTop: '1rem',
          borderTop: '1px solid var(--border-subtle)',
          fontSize: '0.75rem',
          color: 'var(--text-muted)'
        }}>
          <div style={{ marginBottom: '0.5rem', fontWeight: 600 }}>⚡ Hızlı Test Hesapları:</div>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              style={{ flex: 1, fontSize: '0.72rem' }}
              onClick={() => fillQuickAccount('admin@akillisaha.com', 'saha1234')}
            >
              👑 Yönetici (admin)
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              style={{ flex: 1, fontSize: '0.72rem' }}
              onClick={() => fillQuickAccount('temsilci@akillisaha.com', 'saha1234')}
            >
              🚗 Plasiyer (temsilci)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
