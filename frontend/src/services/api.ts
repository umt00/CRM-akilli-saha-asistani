/**
 * Akıllı Saha CRM - Merkezi API İstemcisi ve Servis Metotları
 * Backend REST API uç noktalarıyla tam entegrasyon sağlar.
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
  NumuneDurumu,
  TeklifIstegi,
  TeklifYaniti,
  TeklifDurumu,
  SayfalanmisYanit,
  ExcelIceAktarimSonucuYaniti,
  TakvimEtkinlikYaniti,
} from '../types/crm.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Request Interceptor: Her isteğe JWT Bearer belirtecini ekler
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('erisimBelirteci') || localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('erisimBelirteci');
      localStorage.removeItem('token');
      localStorage.removeItem('aktifKullanici');
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
    }
    return Promise.reject(error);
  }
);

// =============================================================================
// API Servis Metotları
// =============================================================================

export const authApi = {
  girisYap: async (istek: GirisIstegi): Promise<any> => {
    const res: AxiosResponse<IslemSonucu<any>> = await apiClient.post(
      '/kimlik-dogrulama/giris',
      istek
    );
    const data = res.data.veri;
    const token = data.token || data.erisimBelirteci;
    const user: KullaniciOzetiYaniti = data.kullanici || {
      id: data.kullaniciId,
      eposta: data.eposta,
      adSoyad: data.adSoyad,
      rol: data.rol,
      telefon: data.telefon,
    };
    if (token) {
      localStorage.setItem('erisimBelirteci', token);
      localStorage.setItem('token', token);
      localStorage.setItem('aktifKullanici', JSON.stringify(user));
    }
    return { ...data, token, user };
  },

  kayitOl: async (istek: KayitIstegi): Promise<any> => {
    const res: AxiosResponse<IslemSonucu<any>> = await apiClient.post(
      '/kimlik-dogrulama/kayit',
      istek
    );
    const data = res.data.veri;
    const token = data.token || data.erisimBelirteci;
    const user: KullaniciOzetiYaniti = data.kullanici || {
      id: data.kullaniciId,
      eposta: data.eposta,
      adSoyad: data.adSoyad,
      rol: data.rol,
      telefon: data.telefon,
    };
    if (token) {
      localStorage.setItem('erisimBelirteci', token);
      localStorage.setItem('token', token);
      localStorage.setItem('aktifKullanici', JSON.stringify(user));
    }
    return { ...data, token, user };
  },

  profilim: async (): Promise<KullaniciOzetiYaniti> => {
    const res: AxiosResponse<IslemSonucu<KullaniciOzetiYaniti>> = await apiClient.get(
      '/kimlik-dogrulama/profilim'
    );
    return res.data.veri;
  },

  cikisYap: () => {
    localStorage.removeItem('erisimBelirteci');
    localStorage.removeItem('token');
    localStorage.removeItem('aktifKullanici');
    window.dispatchEvent(new CustomEvent('auth:logout'));
  },

  getAktifKullanici: (): KullaniciOzetiYaniti | null => {
    try {
      const stored = localStorage.getItem('aktifKullanici');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  },

  getToken: (): string | null => {
    return localStorage.getItem('erisimBelirteci') || localStorage.getItem('token');
  },
};

export const firmaApi = {
  firmalariGetir: async (
    aramaMetni = '',
    sayfa = 0,
    boyut = 50
  ): Promise<SayfalanmisYanit<FirmaYaniti>> => {
    const res: AxiosResponse<IslemSonucu<SayfalanmisYanit<FirmaYaniti>>> = await apiClient.get(
      '/firmalar',
      { params: { arama: aramaMetni || undefined, sayfa, boyut } }
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

  yetkiliSil: async (firmaId: number, yetkiliId: number): Promise<void> => {
    await apiClient.delete(`/firmalar/${firmaId}/yetkililer/${yetkiliId}`);
  },
};

export const ziyaretApi = {
  ziyaretleriGetir: async (
    sayfa = 0,
    boyut = 50,
    firmaId?: number
  ): Promise<SayfalanmisYanit<ZiyaretYaniti>> => {
    const res: AxiosResponse<IslemSonucu<SayfalanmisYanit<ZiyaretYaniti>>> = await apiClient.get(
      '/ziyaretler',
      { params: { sayfa, boyut, firmaId } }
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

  ziyaretSil: async (id: number): Promise<void> => {
    await apiClient.delete(`/ziyaretler/${id}`);
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
    boyut = 50,
    durum?: NumuneDurumu,
    firmaId?: number
  ): Promise<SayfalanmisYanit<NumuneYaniti>> => {
    const res: AxiosResponse<IslemSonucu<SayfalanmisYanit<NumuneYaniti>>> = await apiClient.get(
      '/numuneler',
      { params: { sayfa, boyut, durum, firmaId } }
    );
    return res.data.veri;
  },

  numuneOlustur: async (istek: NumuneIstegi): Promise<NumuneYaniti> => {
    const res: AxiosResponse<IslemSonucu<NumuneYaniti>> = await apiClient.post(
      '/numuneler',
      istek
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

  numuneSil: async (id: number): Promise<void> => {
    await apiClient.delete(`/numuneler/${id}`);
  },

  numuneIstatistikleriGetir: async (): Promise<Record<string, number>> => {
    const res: AxiosResponse<IslemSonucu<Record<string, number>>> = await apiClient.get(
      '/numuneler/istatistikler'
    );
    return res.data.veri;
  },
};

export const teklifApi = {
  teklifleriGetir: async (
    sayfa = 0,
    boyut = 50,
    durum?: TeklifDurumu,
    firmaId?: number
  ): Promise<SayfalanmisYanit<TeklifYaniti>> => {
    const res: AxiosResponse<IslemSonucu<SayfalanmisYanit<TeklifYaniti>>> = await apiClient.get(
      '/teklifler',
      { params: { sayfa, boyut, durum, firmaId } }
    );
    return res.data.veri;
  },

  teklifOlustur: async (istek: TeklifIstegi): Promise<TeklifYaniti> => {
    const res: AxiosResponse<IslemSonucu<TeklifYaniti>> = await apiClient.post(
      '/teklifler',
      istek
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

  teklifSil: async (id: number): Promise<void> => {
    await apiClient.delete(`/teklifler/${id}`);
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

export const takvimApi = {
  etkinlikleriGetir: async (
    baslangic?: string,
    bitis?: string,
    tur?: string
  ): Promise<TakvimEtkinlikYaniti[]> => {
    const res: AxiosResponse<IslemSonucu<TakvimEtkinlikYaniti[]>> = await apiClient.get(
      '/takvim/etkinlikler',
      {
        params: {
          baslangic: baslangic || undefined,
          bitis: bitis || undefined,
          tur: tur || undefined,
        },
      }
    );
    return res.data.veri || [];
  },
};

export const systemApi = {
  healthCheck: async (): Promise<{ status: string }> => {
    const res = await axios.get('/actuator/health');
    return res.data;
  },
};
