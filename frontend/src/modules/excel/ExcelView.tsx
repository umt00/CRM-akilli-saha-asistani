import React, { useState } from 'react';
import { excelApi } from '../../services/api';
import { ExcelIceAktarimSonucuYaniti } from '../../types/crm.types';
import {
  FileSpreadsheet, Download, Upload, CheckCircle2, AlertTriangle,
  Info, Sparkles, Layers, ArrowDownCircle
} from 'lucide-react';

export const ExcelView: React.FC = () => {
  const [downloading, setDownloading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [importResult, setImportResult] = useState<ExcelIceAktarimSonucuYaniti | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleExport = async () => {
    setDownloading(true);
    try {
      const blob = await excelApi.disaAktar();
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Akilli_Saha_CRM_17_Kolon_${new Date().toISOString().slice(0, 10)}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err: any) {
      alert('Excel dışa aktarılırken hata: ' + (err.message || 'Bilinmeyen hata'));
    } finally {
      setDownloading(false);
    }
  };

  const handleImport = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) return;

    setUploading(true);
    setImportResult(null);
    setErrorMsg(null);

    try {
      const result = await excelApi.iceAktar(selectedFile);
      setImportResult(result);
    } catch (err: any) {
      setErrorMsg(
        err.response?.data?.mesaj ||
          err.message ||
          'Excel yüklenirken bir hata oluştu. Lütfen dosya formatını kontrol edin.'
      );
    } finally {
      setUploading(false);
    }
  };

  const columnsLegend = [
    { col: 'Kolon A', name: 'Müşteri / Firma Adı', desc: 'Firma unvanı' },
    { col: 'Kolon B', name: 'Ziyaret Edilen Kişi', desc: 'İlgili yetkili veya şef' },
    { col: 'Kolon C', name: 'Görevi / Departmanı', desc: 'Satın alma, mutfak şefi vb.' },
    { col: 'Kolon D', name: 'Bölge / Şehir', desc: 'İl ve ilçe lokasyonu' },
    { col: 'Kolon E', name: 'Ziyaret Tarihi', desc: 'Görüşmenin yapıldığı gün' },
    { col: 'Kolon F', name: 'Tedarik Ettiği Ürünler', desc: 'Kullandığı hammadde ve çeşitler' },
    { col: 'Kolon G', name: 'Aylık Tüketim Miktarı', desc: 'Kullanım hacmi (KG/Koli)' },
    { col: 'Kolon H', name: 'Bizden Satın Aldığı Ürünler', desc: 'Aktif alımı olan ürünler' },
    { col: 'Kolon I', name: 'Mevcut Rakip Tedarikçi', desc: 'Halen çalıştığı rakip üretici' },
    { col: 'Kolon J', name: 'Ziyaret Konusu', desc: 'Toplantı gündemi ve amacı' },
    { col: 'Kolon K', name: 'Numune Verildi mi?', desc: 'EVET / HAYIR' },
    { col: 'Kolon L', name: 'Numune Sonucu', desc: 'Tadım notu ve geri bildirim' },
    { col: 'Kolon M', name: 'Teklif Verildi mi?', desc: 'EVET / HAYIR' },
    { col: 'Kolon N', name: 'Teklif Tutarı', desc: 'Verilen fiyat / fırsat tutarı' },
    { col: 'Kolon O', name: 'Sonraki Aksiyon', desc: 'Takip edilecek sonraki adım' },
    { col: 'Kolon P', name: 'Sonraki Ziyaret Tarihi', desc: 'Randevu / Ajanda günü' },
    { col: 'Kolon Q', name: 'Notlar', desc: 'Kritik saha notları' },
  ];

  return (
    <div className="fade-in">
      <div style={{ marginBottom: '1.25rem' }}>
        <h2 style={{ fontSize: '1.3rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <FileSpreadsheet className="text-primary" size={24} />
          17 Kolonluk Excel Merkezi & İçe/Dışa Aktarım
        </h2>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          Mevcut kurumsal Excel dosyalarını tek tıkla yükleyin veya veritabanındaki tüm saha ziyaretlerini orijinal şablonda indirin.
        </p>
      </div>

      <div className="grid-2" style={{ marginBottom: '1.5rem' }}>
        {/* Export Card */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <Download size={18} color="var(--primary)" />
              <span>17 Kolonluk Excel İndir (Export)</span>
            </div>
            <span className="badge badge-info">.XLSX</span>
          </div>

          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginBottom: '1.25rem' }}>
            Sistemdeki tüm kayıtlı firmaları, saha ziyaretlerini, numuneleri ve teklifleri şirketinizin standart 17 kolonluk Excel şablonunda derleyip indirir.
          </p>

          <button
            onClick={handleExport}
            disabled={downloading}
            className="btn btn-primary"
            style={{ width: '100%', padding: '0.85rem' }}
          >
            <Download size={16} />
            {downloading ? 'Excel Dosyası Üretiliyor...' : 'Tüm Verileri Excel (.xlsx) Olarak İndir'}
          </button>
        </div>

        {/* Import Card */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">
              <Upload size={18} color="#10b981" />
              <span>Excel Dosyasını Sisteme Aktar (Import)</span>
            </div>
            <span className="badge badge-success">Toplu Yükleme</span>
          </div>

          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
            Sahada doldurulan 17 kolonluk Excel dosyasını seçin; sistem firmaları, yetkilileri ve ziyaretleri otomatik eşleştirerek kaydeder.
          </p>

          <form onSubmit={handleImport}>
            <div className="form-group">
              <input
                type="file"
                accept=".xlsx, .xls"
                className="form-control"
                onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
                style={{ padding: '0.5rem' }}
              />
            </div>

            <button
              type="submit"
              disabled={!selectedFile || uploading}
              className="btn btn-success"
              style={{ width: '100%', padding: '0.85rem' }}
            >
              <Upload size={16} />
              {uploading ? 'İşleniyor...' : 'Seçilen Excel Dosyasını Yükle ve İşle'}
            </button>
          </form>

          {errorMsg && (
            <div style={{
              background: 'rgba(239, 68, 68, 0.12)',
              border: '1px solid rgba(239, 68, 68, 0.3)',
              borderRadius: 'var(--radius-sm)',
              padding: '0.75rem',
              color: '#f87171',
              fontSize: '0.78rem',
              marginTop: '1rem'
            }}>
              <AlertTriangle size={14} style={{ display: 'inline', marginRight: '0.35rem' }} />
              {errorMsg}
            </div>
          )}

          {importResult && (
            <div style={{
              background: 'rgba(16, 185, 129, 0.12)',
              border: '1px solid rgba(16, 185, 129, 0.3)',
              borderRadius: 'var(--radius-sm)',
              padding: '0.85rem',
              color: '#34d399',
              fontSize: '0.8rem',
              marginTop: '1rem'
            }}>
              <div style={{ fontWeight: 700, marginBottom: '0.35rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <CheckCircle2 size={16} /> Excel Başarıyla İçe Aktarıldı
              </div>
              <div>Toplam Okunan Satır: <strong>{importResult.toplamSatirSayisi}</strong></div>
              <div>Başarıyla Kaydedilen: <strong>{importResult.basariliKayitSayisi}</strong></div>
              {importResult.hataliSatirSayisi > 0 && (
                <div style={{ color: '#f87171', marginTop: '0.35rem' }}>
                  Hatalı Satır Sayısı: {importResult.hataliSatirSayisi}
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* 17 Columns Reference Table */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">
            <Info size={18} color="var(--primary)" />
            <span>17 Kolonluk Saha Excel Şablonu Mimarisi</span>
          </div>
        </div>

        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Kolon</th>
                <th>Alan Adı</th>
                <th>Açıklama / Saha Karşılığı</th>
              </tr>
            </thead>
            <tbody>
              {columnsLegend.map((c, i) => (
                <tr key={i}>
                  <td><span className="badge badge-purple">{c.col}</span></td>
                  <td style={{ fontWeight: 600 }}>{c.name}</td>
                  <td style={{ color: 'var(--text-secondary)' }}>{c.desc}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
