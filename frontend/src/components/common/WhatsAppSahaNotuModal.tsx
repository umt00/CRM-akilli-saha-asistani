import React, { useState, useRef, useEffect } from 'react';
import {
  X, Send, Bot, Check, CheckCheck, Sparkles, FileCode,
  Copy, CheckCircle2, AlertCircle, ArrowUpRight, Smartphone,
  Info, ExternalLink, RefreshCw
} from 'lucide-react';
import { webhookApi, SahaNotuYaniti } from '../../services/api';
import './WhatsAppSahaNotuModal.css';

interface WhatsAppSahaNotuModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
  onOpenCustomer360?: (firmaId: number) => void;
}

interface ChatMessage {
  id: string;
  sender: 'user' | 'bot';
  text: string;
  time: string;
  data?: SahaNotuYaniti;
  error?: boolean;
}

const ORNEK_METINLER = [
  {
    etiket: 'Balcı Fırın (Numune + Teklif)',
    metin: 'balci firin a.s den ahmet bey 20.000 adet orkide zeytin yagi istiyor fakat oncesinde deneme icin 50 teneke numune olarak bir haftalik deneme suresi istiyor !'
  },
  {
    etiket: 'Dilek Pastanesi (Numune)',
    metin: 'dilek pastanesi mehmet usta 5 koli nacho cheese numunesi istedi haftaya degerlendirecek !'
  },
  {
    etiket: 'Tatlı Dünyası (Ticari Teklif)',
    metin: 'tatli dunyasi fatma hanim 100 koli cikolata aromasi alim sozlesmesi icin fiyat teklifi bekliyor !'
  }
];

export const WhatsAppSahaNotuModal: React.FC<WhatsAppSahaNotuModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  onOpenCustomer360
}) => {
  const [activeTab, setActiveTab] = useState<'chat' | 'n8n'>('chat');
  const [inputMessage, setInputMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [copiedCode, setCopiedCode] = useState(false);
  const chatBottomRef = useRef<HTMLDivElement>(null);

  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome-1',
      sender: 'bot',
      text: '👋 Merhaba! Ben *Akıllı Saha WhatsApp Asistanı*.\n\nSaha ziyaretinde plasiyerin aldığı notları sonuna `!` veya `#crm` ekleyerek yaz. Otomatik olarak:\n- 🏢 Müşteri Firma\n- 👤 Yetkili Kişi\n- 🧪 Numune Talebi\n- 💼 Ticari Teklif Taslağı\nolarak CRM sistemine sıfır maliyetle kaydedeyim.',
      time: '09:00'
    }
  ]);

  useEffect(() => {
    if (isOpen) {
      chatBottomRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isOpen]);

  if (!isOpen) return null;

  const handleSend = async (textToSend?: string) => {
    const rawText = (textToSend || inputMessage).trim();
    if (!rawText || isLoading) return;

    const currentTime = new Date().toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' });

    // Kullanıcı mesajı ekle
    const userMsg: ChatMessage = {
      id: 'user-' + Date.now(),
      sender: 'user',
      text: rawText,
      time: currentTime
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputMessage('');
    setIsLoading(true);

    try {
      const res = await webhookApi.sahaNotuGonder({
        hamMetin: rawText,
        gonderenTel: '+905321002030'
      });

      const botMsg: ChatMessage = {
        id: 'bot-' + Date.now(),
        sender: 'bot',
        text: res.teyitMesaji,
        time: new Date().toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' }),
        data: res
      };

      setMessages((prev) => [...prev, botMsg]);
      onSuccess?.();
    } catch (err: any) {
      const botErrMsg: ChatMessage = {
        id: 'bot-err-' + Date.now(),
        sender: 'bot',
        text: '❌ Mesaj işlenirken bir sorun oluştu: ' + (err.response?.data?.mesaj || err.message || 'Bilinmeyen hata'),
        time: new Date().toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' }),
        error: true
      };
      setMessages((prev) => [...prev, botErrMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const copyCurl = () => {
    const curlCommand = `curl -X POST http://localhost:8080/api/v1/webhook/saha-notu \\
  -H "Content-Type: application/json" \\
  -H "X-Webhook-Secret: akillisaha-crm-secret-2026" \\
  -d '{"hamMetin": "balci firin a.s den ahmet bey 20.000 adet orkide zeytin yagi istiyor fakat oncesinde deneme icin 50 teneke numune olarak bir haftalik deneme suresi istiyor !"}'`;
    navigator.clipboard.writeText(curlCommand);
    setCopiedCode(true);
    setTimeout(() => setCopiedCode(false), 2000);
  };

  return (
    <div className="wa-modal-overlay" onClick={onClose}>
      <div className="wa-modal-container" onClick={(e) => e.stopPropagation()}>
        {/* Üst Sekmeler */}
        <div className="bg-[#111b21] px-4 pt-2.5 flex items-center justify-between border-b border-white/5">
          <div className="flex gap-2">
            <button
              onClick={() => setActiveTab('chat')}
              className={`px-3 py-1.5 rounded-t-lg text-xs font-semibold flex items-center gap-1.5 transition-all ${
                activeTab === 'chat'
                  ? 'bg-[#202c33] text-[#25d366] border-b-2 border-[#25d366]'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Smartphone className="w-3.5 h-3.5" />
              WhatsApp Simülatörü
            </button>
            <button
              onClick={() => setActiveTab('n8n')}
              className={`px-3 py-1.5 rounded-t-lg text-xs font-semibold flex items-center gap-1.5 transition-all ${
                activeTab === 'n8n'
                  ? 'bg-[#202c33] text-[#25d366] border-b-2 border-[#25d366]'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <FileCode className="w-3.5 h-3.5" />
              n8n Kılavuzu & JSON
            </button>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {activeTab === 'chat' ? (
          <>
            {/* WhatsApp Başlığı */}
            <div className="wa-modal-header">
              <div className="wa-header-left">
                <div className="wa-avatar">
                  <Bot className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-sm font-semibold text-white flex items-center gap-1.5">
                    Akıllı Saha Asistanı
                    <span className="text-[10px] bg-[#00a884]/20 text-[#25d366] px-1.5 py-0.2 rounded font-mono">
                      n8n Webhook
                    </span>
                  </h3>
                  <p className="text-[11px] text-[#8696a0] flex items-center">
                    <span className="wa-status-dot"></span>
                    Çevrimiçi (Sıfır Maliyetli Yerel NLP)
                  </p>
                </div>
              </div>
            </div>

            {/* Sohbet Alanı */}
            <div className="wa-chat-area">
              {messages.map((m) => (
                <div
                  key={m.id}
                  className={`wa-bubble ${
                    m.sender === 'user' ? 'wa-bubble-user' : 'wa-bubble-bot'
                  } ${m.error ? 'border border-red-500/40 text-red-200' : ''}`}
                >
                  <div className="whitespace-pre-wrap">{m.text}</div>

                  {/* Eğer bot başarılı CRM kaydı dönerse hızlı aksiyon butonu */}
                  {m.data?.firmaId && (
                    <div className="mt-2.5 pt-2 border-t border-white/10 flex items-center justify-between gap-2">
                      <span className="text-[11px] text-[#25d366] font-medium flex items-center gap-1">
                        <CheckCircle2 className="w-3.5 h-3.5" />
                        CRM'e İşlendi
                      </span>
                      {onOpenCustomer360 && (
                        <button
                          onClick={() => {
                            onClose();
                            onOpenCustomer360(m.data!.firmaId!);
                          }}
                          className="inline-flex items-center gap-1 bg-[#00a884] hover:bg-[#25d366] text-black text-[11px] font-semibold px-2 py-1 rounded transition-colors"
                        >
                          Müşteri 360° Aç
                          <ArrowUpRight className="w-3 h-3" />
                        </button>
                      )}
                    </div>
                  )}

                  <div className="wa-msg-time">
                    <span>{m.time}</span>
                    {m.sender === 'user' && (
                      <CheckCheck className="w-3.5 h-3.5 text-[#53bdeb]" />
                    )}
                  </div>
                </div>
              ))}

              {isLoading && (
                <div className="wa-bubble wa-bubble-bot flex items-center gap-2 text-slate-300">
                  <RefreshCw className="w-3.5 h-3.5 animate-spin text-[#25d366]" />
                  <span className="text-xs">Plasiyer notu çözümleniyor & CRM güncelleniyor...</span>
                </div>
              )}

              <div ref={chatBottomRef} />
            </div>

            {/* Hızlı Örnek Notlar */}
            <div className="wa-chips-section">
              <span className="text-[10px] text-slate-400 font-medium">Hızlı Örnek Not Dene:</span>
              <div className="wa-chips-scroll">
                {ORNEK_METINLER.map((o, idx) => (
                  <button
                    key={idx}
                    onClick={() => {
                      setInputMessage(o.metin);
                      handleSend(o.metin);
                    }}
                    className="wa-chip"
                    title={o.metin}
                  >
                    ⚡ {o.etiket}
                  </button>
                ))}
              </div>
            </div>

            {/* Mesaj Yazma Çubuğu */}
            <div className="wa-input-bar">
              <input
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleSend();
                }}
                placeholder="Örn: Balcı Fırın Ahmet Bey 50 teneke numune istedi !"
                className="wa-input-field"
                disabled={isLoading}
              />
              <button
                onClick={() => handleSend()}
                disabled={!inputMessage.trim() || isLoading}
                className="wa-send-btn"
                title="Saha Notunu Gönder"
              >
                <Send className="w-4 h-4 ml-0.5" />
              </button>
            </div>
          </>
        ) : (
          /* n8n Entegrasyon Kılavuzu & JSON */
          <div className="p-5 flex-1 overflow-y-auto space-y-4 text-slate-300 text-xs">
            <div className="bg-[#1f2c34] p-3.5 rounded-xl border border-white/5 space-y-2">
              <h4 className="font-semibold text-white flex items-center gap-2 text-sm">
                <Sparkles className="w-4 h-4 text-[#25d366]" />
                n8n Entegrasyonu Nasıl Çalışır?
              </h4>
              <p className="text-slate-300 leading-relaxed">
                Plasiyer WhatsApp'tan yazdığı mesajın sonuna <code className="text-[#25d366] bg-black/40 px-1 py-0.5 rounded font-mono">!</code> veya <code className="text-[#25d366] bg-black/40 px-1 py-0.5 rounded font-mono">#crm</code> eklediğinde n8n otomasyonu devreye girer. Sıradan konuşmalar elenir, iş notları anında Spring Boot backend'imize iletilir.
              </p>
            </div>

            <div className="space-y-2">
              <div className="font-semibold text-white flex items-center justify-between">
                <span>1. Hazır n8n Şablonu (1 Tıkla İçe Aktar):</span>
                <a
                  href="/n8n-workflow-whatsapp-crm.json"
                  download="n8n-workflow-whatsapp-crm.json"
                  className="bg-[#00a884] hover:bg-[#25d366] text-black font-semibold px-2.5 py-1 rounded text-[11px] inline-flex items-center gap-1"
                >
                  <FileCode className="w-3.5 h-3.5" />
                  JSON Şablonunu İndir
                </a>
              </div>
              <p className="text-slate-400 text-[11px]">
                n8n arayüzünde <b>Workflows -&gt; Import from File</b> diyerek projenin ana dizinindeki <code className="text-white">n8n-workflow-whatsapp-crm.json</code> dosyasını seçebilirsiniz.
              </p>
            </div>

            <div className="space-y-2">
              <div className="font-semibold text-white flex items-center justify-between">
                <span>2. Doğrudan API Testi (cURL):</span>
                <button
                  onClick={copyCurl}
                  className="text-slate-400 hover:text-white inline-flex items-center gap-1 text-[11px]"
                >
                  {copiedCode ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-[#25d366]" />
                      Kopyalandı!
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5" />
                      Kodu Kopyala
                    </>
                  )}
                </button>
              </div>
              <pre className="bg-black/60 p-3 rounded-xl border border-white/5 font-mono text-[10px] text-emerald-400 overflow-x-auto select-all">
{`curl -X POST http://localhost:8080/api/v1/webhook/saha-notu \\
  -H "Content-Type: application/json" \\
  -H "X-Webhook-Secret: akillisaha-crm-secret-2026" \\
  -d '{"hamMetin": "balci firin a.s den ahmet bey 20.000 adet orkide zeytin yagi istiyor fakat oncesinde deneme icin 50 teneke numune olarak bir haftalik deneme suresi istiyor !"}'`}
              </pre>
            </div>

            <div className="bg-[#1f2c34]/60 p-3 rounded-xl border border-white/5 space-y-1.5 text-[11px]">
              <div className="font-semibold text-white flex items-center gap-1.5">
                <Info className="w-3.5 h-3.5 text-blue-400" />
                Sıfır Maliyet Garantisi
              </div>
              <p className="text-slate-400 leading-relaxed">
                Sistem OpenAI/Whisper gibi ücretli veya yavaş harici AI API'lerine ihtiyaç duymaz. Kural tabanlı Türkçe NLP ayrıştırma motoru milisaniyeler içinde çalışır ve sunucunuzda sıfır maliyetle çalışmaya devam eder.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
