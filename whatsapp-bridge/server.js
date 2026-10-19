const { default: makeWASocket, useMultiFileAuthState, DisconnectReason } = require('@whiskeysockets/baileys');
const pino = require('pino');
const express = require('express');
const QRCode = require('qrcode');
const qrcodeTerminal = require('qrcode-terminal');
const axios = require('axios');
const path = require('path');

const app = express();
const PORT = 3333;
const BACKEND_URL = 'http://localhost:8080/api/v1/webhook/saha-notu';
const N8N_URL = 'http://localhost:5678/webhook/whatsapp-saha-notu';
const WEBHOOK_SECRET = 'akillisaha-crm-secret-2026';

let currentQR = '';
let connectionStatus = 'Başlatılıyor...';
let connectedUser = null;
let recentLogs = [];
let sock = null;

app.use(express.json());

// Log ekleme yardımcısı
function addLog(type, message) {
  const time = new Date().toLocaleTimeString('tr-TR');
  const logItem = { time, type, message };
  recentLogs.unshift(logItem);
  if (recentLogs.length > 30) recentLogs.pop();
  console.log(`[${time}] [${type}] ${message}`);
}

// WhatsApp Bağlantısını Başlat
async function connectToWhatsApp() {
  const { state, saveCreds } = await useMultiFileAuthState(path.join(__dirname, 'auth_info_baileys'));

  sock = makeWASocket({
    auth: state,
    logger: pino({ level: 'silent' }),
    printQRInTerminal: false,
    browser: ['Yeşiller Gıda CRM', 'Chrome', '1.0.0']
  });

  sock.ev.on('creds.update', saveCreds);

  sock.ev.on('connection.update', async (update) => {
    const { connection, lastDisconnect, qr } = update;

    if (qr) {
      currentQR = qr;
      connectionStatus = 'QR Kod Taranması Bekleniyor';
      addLog('INFO', 'Yeni WhatsApp QR kodu üretildi. Lütfen telefonunuzdan taratın.');
      qrcodeTerminal.generate(qr, { small: true });
    }

    if (connection === 'close') {
      const shouldReconnect = (lastDisconnect?.error)?.output?.statusCode !== DisconnectReason.loggedOut;
      connectionStatus = 'Bağlantı Kesildi, Yeniden Bağlanılıyor...';
      currentQR = '';
      addLog('WARN', `Bağlantı koptu. Yeniden bağlanma: ${shouldReconnect}`);
      if (shouldReconnect) {
        setTimeout(connectToWhatsApp, 3000);
      }
    } else if (connection === 'open') {
      connectionStatus = 'Bağlandı ✅ (Aktif)';
      currentQR = '';
      connectedUser = sock.user?.id || 'Bağlı Hesap';
      addLog('SUCCESS', `WhatsApp bağlantısı başarıyla kuruldu! Hesap: ${connectedUser}`);
    }
  });

  // Gelen Mesajları Dinle
  sock.ev.on('messages.upsert', async (m) => {
    try {
      const msg = m.messages[0];
      if (!msg || !msg.message) return;

      // Mesaj metnini çıkart
      const text = msg.message?.conversation ||
                   msg.message?.extendedTextMessage?.text ||
                   msg.message?.imageMessage?.caption || '';

      if (!text || text.trim() === '') return;

      const remoteJid = msg.key.remoteJid || '';
      const isGroup = remoteJid.endsWith('@g.us');
      const senderPhone = msg.key.participant || remoteJid;

      // Tetikleyici kontrolü: Mesajda ~crm, #crm veya ! var mı?
      const hasTrigger = text.includes('~crm') || text.includes('#crm') || text.trim().endsWith('!');
      if (!hasTrigger) {
        // Tetikleyici yoksa CRM'e aktarma
        return;
      }

      addLog('MSG', `Grup/Sohbet: ${remoteJid} | Gönderen: ${senderPhone}`);
      addLog('PROCESS', `Tetikleyici yakalandı, CRM'e iletiliyor: "${text}"`);

      // n8n veya doğrudan Spring Boot Backend'e gönder
      let teyitMesaji = '';
      try {
        // Önce n8n üzerinden deneyelim
        const n8nRes = await axios.post(N8N_URL, {
          body: {
            message: text,
            sender: senderPhone,
            chatId: remoteJid
          }
        }, { timeout: 3500 });

        teyitMesaji = n8nRes.data?.whatsappMesaji || n8nRes.data?.veri?.teyitMesaji;
      } catch (n8nErr) {
        // n8n aktif değilse doğrudan Spring Boot backend'imize gönder
        const crmRes = await axios.post(BACKEND_URL, {
          hamMetin: text,
          gonderenTel: senderPhone
        }, {
          headers: { 'X-Webhook-Secret': WEBHOOK_SECRET },
          timeout: 5000
        });

        teyitMesaji = crmRes.data?.veri?.teyitMesaji || crmRes.data?.mesaj;
      }

      if (teyitMesaji) {
        addLog('CRM', `CRM Onayı Alındı: ${teyitMesaji.substring(0, 50)}...`);
        // WhatsApp grubuna veya kişiye onay yanıtını geri gönder
        await sock.sendMessage(remoteJid, { text: teyitMesaji }, { quoted: msg });
        addLog('REPLY', `Teyit mesajı WhatsApp grubuna gönderildi! ✅`);
      }
    } catch (err) {
      addLog('ERROR', `Mesaj işlenirken hata oluştu: ${err.message}`);
    }
  });
}

// Web Arayüzü (QR Kod ve Canlı Durum Paneli)
app.get('/', async (req, res) => {
  let qrImageHtml = '';
  if (currentQR) {
    const qrDataUrl = await QRCode.toDataURL(currentQR, { width: 280, margin: 2 });
    qrImageHtml = `
      <div style="background: white; padding: 16px; border-radius: 16px; display: inline-block; box-shadow: 0 10px 25px rgba(0,0,0,0.3);">
        <img src="${qrDataUrl}" alt="WhatsApp QR Code" style="display: block; width: 280px; height: 280px;" />
      </div>
      <p style="margin-top: 14px; font-size: 14px; color: #a0aec0;">
        📱 <b>WhatsApp</b> uygulamanızı açın &gt; <b>Ayarlar / Bağlı Cihazlar</b> &gt; <b>Cihaz Bağla</b> diyerek bu karekodu taratın.
      </p>
    `;
  } else if (connectionStatus.includes('Bağlandı')) {
    qrImageHtml = `
      <div style="background: rgba(37, 211, 102, 0.1); border: 2px solid #25d366; padding: 24px; border-radius: 16px; display: inline-block;">
        <h2 style="color: #25d366; margin: 0 0 8px 0;">🎉 WhatsApp Bağlantısı Başarılı!</h2>
        <p style="color: #e2e8f0; margin: 0; font-size: 15px;">Telefonunuz sisteme bağlandı. Artık WhatsApp'taki <b>CRM</b> grubuna yazdığınız <code style="color: #25d366; background: #000; padding: 2px 6px; border-radius: 4px;">~crm</code> notları anında CRM'e işlenecektir.</p>
      </div>
    `;
  } else {
    qrImageHtml = `<p style="color: #cbd5e1; font-size: 16px;">⏳ WhatsApp bağlantısı başlatılıyor, lütfen bekleyin...</p>`;
  }

  const logsHtml = recentLogs.map(l => `
    <div style="font-family: monospace; font-size: 12px; margin-bottom: 6px; color: ${l.type === 'ERROR' ? '#f87171' : l.type === 'SUCCESS' ? '#4ade80' : l.type === 'CRM' ? '#38bdf8' : '#cbd5e1'};">
      <span style="color: #64748b;">[${l.time}]</span> <b>[${l.type}]</b> ${l.message}
    </div>
  `).join('');

  res.send(`
    <!DOCTYPE html>
    <html lang="tr">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>Yeşiller Gıda - WhatsApp Saha Köprüsü</title>
      <meta http-equiv="refresh" content="4">
      <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #0b141a; color: #fff; margin: 0; padding: 24px; display: flex; flex-direction: column; align-items: center; }
        .card { max-width: 640px; width: 100%; background: #111b21; border-radius: 20px; border: 1px solid rgba(255,255,255,0.1); padding: 28px; text-align: center; box-shadow: 0 20px 40px rgba(0,0,0,0.5); }
        .badge { display: inline-block; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: bold; background: #202c33; color: #25d366; margin-bottom: 20px; border: 1px solid rgba(37,211,102,0.3); }
        .log-box { background: #060d11; border: 1px solid rgba(255,255,255,0.08); border-radius: 12px; padding: 14px; max-height: 220px; overflow-y: auto; text-align: left; margin-top: 24px; }
      </style>
    </head>
    <body>
      <div class="card">
        <h1 style="margin: 0 0 6px 0; font-size: 22px;">Yeşiller Gıda - WhatsApp Saha Köprüsü</h1>
        <p style="margin: 0 0 16px 0; color: #8696a0; font-size: 13px;">CRM Grubu & Saha Asistanı Entegrasyonu</p>
        <div class="badge">Durum: ${connectionStatus}</div>
        <div>${qrImageHtml}</div>
        <div class="log-box">
          <div style="font-size: 11px; font-weight: bold; color: #94a3b8; margin-bottom: 8px; border-bottom: 1px solid rgba(255,255,255,0.05); padding-bottom: 4px;">CANLI BAĞLANTI & SAHA LOGLARI</div>
          ${logsHtml || '<div style="color: #64748b; font-size: 12px;">Henüz aktivite yok.</div>'}
        </div>
      </div>
    </body>
    </html>
  `);
});

// Başlat
app.listen(PORT, () => {
  addLog('INFO', `WhatsApp Köprüsü Web Arayüzü http://localhost:${PORT} adresinde hazır.`);
  connectToWhatsApp();
});
