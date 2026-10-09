/**
 * Akıllı Saha CRM - Merkezi API İstemcisi ve Servis Metotları
 * 3. Kişi (Frontend Geliştirici) için hazır Axios entegrasyonu.
 * 
 * Otomatik Yetkilendirme: 'Authorization: Bearer <token>' başlığını ekler.
 * Hata Yönetimi: 401 Unauthorized durumunda oturumu temizler ve yönlendirir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */

import axios, { AxiosInstance, AxiosResponse } from 'axios';
import {
  IslemSonucu,
  GirisIstegi,
  GirisYaniti,
  KayitIstegi,
  KullaniciOzetiYaniti,
  FirmaIstegi,
  FirmaYaniti,
  YetkiliIstegi,
  YetkiliYaniti,
  ZiyaretIstegi,
  ZiyaretYaniti,
  NumuneIstegi,
  NumuneYaniti,
  NumuneIstatistikYaniti,
  NumuneDurumu,
  TeklifIstegi,
  TeklifYaniti,
  TeklifDurumu,
  SayfalanmisYanit,
  ExcelIceAktarimSonucuYaniti,
} from '../types/crm.types';

// API Temel Adresi: Vite ortam değişkeni veya yerel proxy
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// İstek Araya Giricisi (Request Interceptor): Her isteğe JWT belirtecini ekler
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('erisimBelirteci');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Yanıt Araya Giricisi (Response Interceptor): 401 durumunda oturumu sonlandırır
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('erisimBelirteci');
      localStorage.removeItem('aktifKullanici');
      if (window.location.pathname !== '/giris') {
        window.location.href = '/giris';
      }
    }
    return Promise.reject(error);
  }
);

// =============================================================================
// API Servis Metotları
// =============================================================================

export const authApi = {
  girisYap: async (istek: GirisIstegi): Promise<GirisYaniti> => {
    const res: AxiosResponse<IslemSonucu<GirisYaniti>> = await apiClient.post(
      '/kimlik-dogrulama/giris',
      istek
    );
    const data = res.data.veri;
    localStorage.setItem('erisimBelirteci', data.erisimBelirteci);
    localStorage.setItem('aktifKullanici', JSON.stringify(data.kullanici));
    return data;
  },

  kayitOl: async (istek: KayitIstegi): Promise<GirisYaniti> => {
    const res: AxiosResponse<IslemSonucu<GirisYaniti>> = await apiClient.post(
      '/kimlik-dogrulama/kayit',
      istek
    );
    return res.data.veri;
  },

  profilim: async (): Promise<KullaniciOzetiYaniti> => {
    const res: AxiosResponse<IslemSonucu<KullaniciOzetiYaniti>> = await apiClient.get(
      '/kimlik-dogrulama/profilim'
    );
    return res.data.veri;
  },

  cikisYap: () => {
    localStorage.removeItem('erisimBelirteci');
    localStorage.removeItem('aktifKullanici');
    window.location.href = '/giris';
  },
};

export const firmaApi = {
  firmalariGetir: async (
    aramaMetni = '',
    sayfa = 0,
    boyut = 20
  ): Promise<SayfalanmisYanit<FirmaYaniti>> => {
    const res: AxiosResponse<IslemSonucu<SayfalanmisYanit<FirmaYaniti>>> = await apiClient.get(
      '/firmalar',
      { params: { arama: aramaMetni, sayfa, boyut } }
    );
    return res.data.veri;
  },

  firmaDetayiGetir: async (id: number): Promise<FirmaYaniti> => {
    const res: AxiosResponse<IslemSonucu<FirmaYaniti>> = await apiClient.get(`/firmalar/${id}`);
    return res.data.veri;
  },

  firmaOlustur: async (istek: FirmaIstegi): Promise<FirmaYaniti> => {
    const res: AxiosResponse<IslemSonucu<FirmaYaniti>> = await apiClient.post('/firmalar', istek);
    return res.data.veri;
  },

  firmaGuncelle: async (id: number, istek: FirmaIstegi): Promise<FirmaYaniti> => {
    const res: AxiosResponse<IslemSonucu<FirmaYaniti>> = await apiClient.put(`/firmalar/${id}`, istek);
    return res.data.veri;
  },

  firmaSil: async (id: number): Promise<void> => {
    await apiClient.delete(`/firmalar/${id}`);
  },

  yetkiliEkle: async (firmaId: number, istek: YetkiliIstegi): Promise<YetkiliYaniti> => {
    const res: AxiosResponse<IslemSonucu<YetkiliYaniti>> = await apiClient.post(
      `/firmalar/${firmaId}/yetkililer`,
      istek
    );
    return res.data.veri;
  },
};

export const ziyaretApi = {
  ziyaretleriGetir: async (
    sayfa = 0,
    boyut = 20
  ): Promise<SayfalanmisYanit<ZiyaretYaniti>> => {
    const res: AxiosResponse<IslemSonucu<SayfalanmisYanit<ZiyaretYaniti>>> = await apiClient.get(
      '/ziyaretler',
      { params: { sayfa, boyut } }
    );
    return res.data.veri;
  },

  ziyaretOlustur: async (istek: ZiyaretIstegi): Promise<ZiyaretYaniti> => {
    const res: AxiosResponse<IslemSonucu<ZiyaretYaniti>> = await apiClient.post(
      '/ziyaretler',
      istek
    );
    return res.data.veri;
  },

  ajandaZiyaretleriniGetir: async (): Promise<ZiyaretYaniti[]> => {
    const res: AxiosResponse<IslemSonucu<ZiyaretYaniti[]>> = await apiClient.get(
      '/ziyaretler/ajanda'
    );
    return res.data.veri;
  },
};

export const numuneApi = {
  numuneleriGetir: async (
    sayfa = 0,
    boyut = 20
  ): Promise<SayfalanmisYanit<NumuneYaniti>> => {
    const res: AxiosResponse<IslemSonucu<SayfalanmisYanit<NumuneYaniti>>> = await apiClient.get(
      '/numuneler',
      { params: { sayfa, boyut } }
    );
    return res.data.veri;
  },

  numuneDurumuGuncelle: async (
    id: number,
    durum: NumuneDurumu,
    sonucNotlari?: string
  ): Promise<NumuneYaniti> => {
    const res: AxiosResponse<IslemSonucu<NumuneYaniti>> = await apiClient.patch(
      `/numuneler/${id}/durum`,
      null,
      { params: { durum, sonucNotlari } }
    );
    return res.data.veri;
  },

  numuneIstatistikleriGetir: async (): Promise<NumuneIstatistikYaniti> => {
    const res: AxiosResponse<IslemSonucu<NumuneIstatistikYaniti>> = await apiClient.get(
      '/numuneler/istatistikler'
    );
    return res.data.veri;
  },
};

export const teklifApi = {
  teklifleriGetir: async (
    sayfa = 0,
    boyut = 20
  ): Promise<SayfalanmisYanit<TeklifYaniti>> => {
    const res: AxiosResponse<IslemSonucu<SayfalanmisYanit<TeklifYaniti>>> = await apiClient.get(
      '/teklifler',
      { params: { sayfa, boyut } }
    );
    return res.data.veri;
  },

  teklifDurumuGuncelle: async (id: number, durum: TeklifDurumu): Promise<TeklifYaniti> => {
    const res: AxiosResponse<IslemSonucu<TeklifYaniti>> = await apiClient.patch(
      `/teklifler/${id}/durum`,
      null,
      { params: { durum } }
    );
    return res.data.veri;
  },
};

export const excelApi = {
  disaAktar: async (baslangicTarihi?: string, bitisTarihi?: string): Promise<Blob> => {
    const res = await apiClient.get('/excel/disa-aktar', {
      params: { baslangicTarihi, bitisTarihi },
      responseType: 'blob',
    });
    return res.data;
  },

  iceAktar: async (dosya: File): Promise<ExcelIceAktarimSonucuYaniti> => {
    const formData = new FormData();
    formData.append('dosya', dosya);
    const res: AxiosResponse<IslemSonucu<ExcelIceAktarimSonucuYaniti>> = await apiClient.post(
      '/excel/ice-aktar',
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
    return res.data.veri;
  },
};
