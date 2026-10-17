import React, { useState, useEffect } from 'react';
import { systemApi, authApi } from '../../services/api';
import {
  Terminal, ShieldCheck, CheckCircle2, AlertCircle, Copy, Check,
  ExternalLink, Cpu, Activity, RefreshCw
} from 'lucide-react';

export const ApiDiagnosticsView: React.FC = () => {
  const [health, setHealth] = useState<string>('Kontrol ediliyor...');
  const [latency, setLatency] = useState<number | null>(null);
  const [tokenCopied, setTokenCopied] = useState(false);
  const [curlCopied, setCurlCopied] = useState(false);

  const token = authApi.getToken();
  const user = authApi.getAktifKullanici();

  const checkHealth = async () => {
    const start = performance.now();
    try {
      const res = await systemApi.healthCheck();
      const end = performance.now();
      setHealth(res.status || 'UP');
      setLatency(Math.round(end - start));
    } catch {
      setHealth('DOWN / Yanıt Yok');
      setLatency(null);
    }
  };

  useEffect(() => {
    checkHealth();
  }, []);

  const copyToken = () => {
    if (token) {
      navigator.clipboard.writeText(token);
      setTokenCopied(true);
      setTimeout(() => setTokenCopied(false), 2000);
    }
  };

  const sampleCurl = `curl -X GET "http://localhost:8080/api/v1/firmalar" \\
  -H "Authorization: Bearer ${token || '<JWT_TOKEN>'}" \\
  -H "Content-Type: application/json"`;

  const copyCurl = () => {
    navigator.clipboard.writeText(sampleCurl);
    setCurlCopied(true);
    setTimeout(() => setCurlCopied(false), 2000);
  };

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Terminal className="text-primary" size={24} />
            Backend Durumu, Swagger & n8n Test Konsolu
          </h2>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            Geliştirici ve test amaçlı canlı token denetimi, cURL şablonları ve Swagger bağlantısı
          </p>
        </div>

        <button onClick={checkHealth} className="btn btn-secondary btn-sm">
          <RefreshCw size={14} /> Ping At
        </button>
      </div>

      <div className="grid-3" style={{ marginBottom: '1.25rem' }}>
        <div className="stat-card">
          <div className="stat-icon" style={{
            background: health === 'UP' ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
            color: health === 'UP' ? '#34d399' : '#f87171'
          }}>
            <Activity size={22} />
          </div>
          <div>
            <div className="stat-number" style={{ color: health === 'UP' ? '#34d399' : '#f87171' }}>
              {health}
            </div>
            <div className="stat-label">Spring Boot Health (8080) {latency !== null && `• ${latency}ms`}</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(99, 102, 241, 0.15)', color: '#818cf8' }}>
            <ShieldCheck size={22} />
          </div>
          <div>
            <div className="stat-number" style={{ fontSize: '1.1rem' }}>{user?.rol || 'Giriş Yapılmadı'}</div>
            <div className="stat-label">Aktif Oturum: {user?.adSoyad || user?.eposta || '-'}</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon" style={{ background: 'rgba(6, 182, 212, 0.15)', color: '#38bdf8' }}>
            <Cpu size={22} />
          </div>
          <div>
            <div className="stat-number" style={{ fontSize: '1.1rem' }}>PostgreSQL 15</div>
            <div className="stat-label">Docker Konteyner: crm-postgres (:5432)</div>
          </div>
        </div>
      </div>

      <div className="grid-2">
        {/* Token Inspect Card */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <ShieldCheck size={18} color="var(--primary)" />
              <span>Aktif JWT Bearer Belirteci (n8n & Postman İçin)</span>
            </div>
            <button onClick={copyToken} className="btn btn-secondary btn-sm">
              {tokenCopied ? <Check size={13} color="#34d399" /> : <Copy size={13} />}
              {tokenCopied ? 'Kopyalandı' : 'Kopyala'}
            </button>
          </div>

          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.75rem' }}>
            n8n HTTP Request düğümlerinde veya Postman'da <code>Authorization: Bearer &lt;token&gt;</code> olarak kullanabilirsiniz:
          </p>

          <pre style={{
            background: 'var(--bg-input)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-sm)',
            padding: '0.75rem',
            fontFamily: 'var(--font-mono)',
            fontSize: '0.72rem',
            color: '#a5b4fc',
            overflowX: 'auto',
            maxHeight: '120px'
          }}>
            {token || 'Aktif token bulunamadı. Lütfen giriş yapın.'}
          </pre>
        </div>

        {/* Quick cURL Card */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <Terminal size={18} color="#34d399" />
              <span>Terminalden Hızlı cURL Testi</span>
            </div>
            <button onClick={copyCurl} className="btn btn-secondary btn-sm">
              {curlCopied ? <Check size={13} color="#34d399" /> : <Copy size={13} />}
              {curlCopied ? 'Kopyalandı' : 'cURL Kopyala'}
            </button>
          </div>

          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.75rem' }}>
            Mac terminalinden doğrudan API'yi test etmek için yapıştırın:
          </p>

          <pre style={{
            background: 'var(--bg-input)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-sm)',
            padding: '0.75rem',
            fontFamily: 'var(--font-mono)',
            fontSize: '0.72rem',
            color: '#34d399',
            overflowX: 'auto',
            maxHeight: '120px'
          }}>
            {sampleCurl}
          </pre>
        </div>
      </div>

      {/* Swagger & Actuator External Links */}
      <div className="card" style={{ marginTop: '1.25rem' }}>
        <div className="card-header">
          <div className="card-title">
            <span>Doğrudan Backend Uç Noktaları</span>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <a
            href="http://localhost:8080/swagger-ui.html"
            target="_blank"
            rel="noopener noreferrer"
            className="btn btn-secondary"
            style={{ textDecoration: 'none' }}
          >
            <ExternalLink size={15} /> Swagger UI Dokümantasyonu (Port 8080)
          </a>

          <a
            href="http://localhost:8080/actuator/health"
            target="_blank"
            rel="noopener noreferrer"
            className="btn btn-secondary"
            style={{ textDecoration: 'none' }}
          >
            <ExternalLink size={15} /> Actuator Health JSON
          </a>
        </div>
      </div>
    </div>
  );
};
