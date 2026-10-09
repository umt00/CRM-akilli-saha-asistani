#!/usr/bin/env bash
# ==============================================================================
# Akıllı Saha CRM - Canlı Ortam Smoke Test & Entegrasyon Doğrulama Betiği
# Faz 3: Canlıya Dağıtım ve Keep-Alive ($0 PoC)
# 
# Kullanım:
#   ./scripts/canli_smoke_test.sh [SUNUCU_URL]
# Örnekler:
#   ./scripts/canli_smoke_test.sh http://localhost:8080
#   ./scripts/canli_smoke_test.sh https://crm-akilli-saha-backend.onrender.com
# ==============================================================================

set -e

BASE_URL="${1:-http://localhost:8080}"
# Sondaki slash karakterini temizle
BASE_URL="${BASE_URL%/}"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}==============================================================================${NC}"
echo -e "${BLUE}  AKILLI SAHA CRM - CANLI SMOKE TEST OTOMASYONU (FAZ 3)${NC}"
echo -e "${BLUE}  Hedef Sunucu: ${YELLOW}${BASE_URL}${NC}"
echo -e "${BLUE}==============================================================================${NC}\n"

BASARILI_TEST_SAYISI=0
TOPLAM_TEST_SAYISI=0

test_adim() {
    TOPLAM_TEST_SAYISI=$((TOPLAM_TEST_SAYISI + 1))
    echo -n "Test $TOPLAM_TEST_SAYISI: $1... "
}

test_basarili() {
    BASARILI_TEST_SAYISI=$((BASARILI_TEST_SAYISI + 1))
    echo -e "${GREEN}[BAŞARILI]${NC}"
}

test_basarisiz() {
    echo -e "${RED}[BAŞARISIZ]${NC}"
    echo -e "${RED}Detay: $1${NC}"
}

# ------------------------------------------------------------------------------
# 1. Katman 9: Sağlık ve Keep-Alive (/actuator/health)
# ------------------------------------------------------------------------------
test_adim "Katman 9 Sağlık Kontrolü (/actuator/health)"
HEALTH_RESPONSE=$(curl -s -w "\n%{http_code}" "${BASE_URL}/actuator/health" || true)
HEALTH_CODE=$(echo "$HEALTH_RESPONSE" | tail -n1)
HEALTH_BODY=$(echo "$HEALTH_RESPONSE" | sed '$d')

if [ "$HEALTH_CODE" = "200" ]; then
    test_basarili
else
    test_basarisiz "HTTP Kodu: $HEALTH_CODE. Yanıt: $HEALTH_BODY"
fi

# ------------------------------------------------------------------------------
# 2. Swagger / OpenAPI Erişilebilirliği
# ------------------------------------------------------------------------------
test_adim "Swagger UI Dokümantasyon Erişimi (/swagger-ui.html)"
SWAGGER_CODE=$(curl -s -o /dev/null -w "%{http_code}" -L "${BASE_URL}/swagger-ui.html" || true)
if [ "$SWAGGER_CODE" = "200" ] || [ "$SWAGGER_CODE" = "302" ]; then
    test_basarili
else
    test_basarisiz "HTTP Kodu: $SWAGGER_CODE"
fi

# ------------------------------------------------------------------------------
# 3. Kimlik Doğrulama: Yönetici Girişi (ADMIN)
# ------------------------------------------------------------------------------
test_adim "Yönetici Girişi (admin@akillisaha.com)"
ADMIN_LOGIN_RES=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/v1/kimlik-dogrulama/giris" \
    -H "Content-Type: application/json" \
    -d '{"eposta":"admin@akillisaha.com","sifre":"admin123"}' || true)
ADMIN_CODE=$(echo "$ADMIN_LOGIN_RES" | tail -n1)
ADMIN_BODY=$(echo "$ADMIN_LOGIN_RES" | sed '$d')

ADMIN_TOKEN=""
if [ "$ADMIN_CODE" = "200" ]; then
    ADMIN_TOKEN=$(echo "$ADMIN_BODY" | grep -o '"erisimBelirteci":"[^"]*' | cut -d'"' -f4)
    if [ -n "$ADMIN_TOKEN" ]; then
        test_basarili
    else
        test_basarisiz "Erişim belirteci (token) yanıtta bulunamadı."
    fi
else
    test_basarisiz "Giriş başarısız. HTTP: $ADMIN_CODE, Gövde: $ADMIN_BODY"
fi

# ------------------------------------------------------------------------------
# 4. Kimlik Doğrulama: Satış Temsilcisi Girişi (PLASIYER)
# ------------------------------------------------------------------------------
test_adim "Plasiyer Girişi (temsilci@akillisaha.com)"
REP_LOGIN_RES=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/v1/kimlik-dogrulama/giris" \
    -H "Content-Type: application/json" \
    -d '{"eposta":"temsilci@akillisaha.com","sifre":"saha123"}' || true)
REP_CODE=$(echo "$REP_LOGIN_RES" | tail -n1)
REP_BODY=$(echo "$REP_LOGIN_RES" | sed '$d')

REP_TOKEN=""
if [ "$REP_CODE" = "200" ]; then
    REP_TOKEN=$(echo "$REP_BODY" | grep -o '"erisimBelirteci":"[^"]*' | cut -d'"' -f4)
    if [ -n "$REP_TOKEN" ]; then
        test_basarili
    else
        test_basarisiz "Plasiyer belirteci bulunamadı."
    fi
else
    test_basarisiz "Plasiyer girişi başarısız. HTTP: $REP_CODE"
fi

# ------------------------------------------------------------------------------
# 5. Profil Sorgusu (/api/v1/kimlik-dogrulama/profilim)
# ------------------------------------------------------------------------------
test_adim "Plasiyer Profil Sorgusu (JWT Doğrulama)"
if [ -n "$REP_TOKEN" ]; then
    ME_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/kimlik-dogrulama/profilim" \
        -H "Authorization: Bearer $REP_TOKEN")
    if [ "$ME_CODE" = "200" ]; then
        test_basarili
    else
        test_basarisiz "Profil sorgusu başarısız: HTTP $ME_CODE"
    fi
else
    test_basarisiz "Token olmadığı için atlandı."
fi

# ------------------------------------------------------------------------------
# 6. Veri İzolasyonlu Firma Listesi (/api/v1/firmalar)
# ------------------------------------------------------------------------------
test_adim "İzole Firma Portföyü Sorgulama (/api/v1/firmalar)"
if [ -n "$REP_TOKEN" ]; then
    COMPANIES_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/firmalar" \
        -H "Authorization: Bearer $REP_TOKEN")
    if [ "$COMPANIES_CODE" = "200" ]; then
        test_basarili
    else
        test_basarisiz "Firmalar sorgulanamadı: HTTP $COMPANIES_CODE"
    fi
else
    test_basarisiz "Token olmadığı için atlandı."
fi

# ------------------------------------------------------------------------------
# 7. Ajanda & Randevu Takibi (/api/v1/ziyaretler/ajanda)
# ------------------------------------------------------------------------------
test_adim "Yaklaşan Randevular Ajandası (/api/v1/ziyaretler/ajanda)"
if [ -n "$REP_TOKEN" ]; then
    AGENDA_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/ziyaretler/ajanda" \
        -H "Authorization: Bearer $REP_TOKEN")
    if [ "$AGENDA_CODE" = "200" ]; then
        test_basarili
    else
        test_basarisiz "Ajanda sorgusu başarısız: HTTP $AGENDA_CODE"
    fi
else
    test_basarisiz "Token olmadığı için atlandı."
fi

# ------------------------------------------------------------------------------
# 8. Numune Takip Masası & İstatistikler (/api/v1/numuneler/istatistikler)
# ------------------------------------------------------------------------------
test_adim "Numune Durum İstatistikleri (/api/v1/numuneler/istatistikler)"
if [ -n "$REP_TOKEN" ]; then
    STATS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/numuneler/istatistikler" \
        -H "Authorization: Bearer $REP_TOKEN")
    if [ "$STATS_CODE" = "200" ]; then
        test_basarili
    else
        test_basarisiz "Numune istatistikleri sorgulanamadı: HTTP $STATS_CODE"
    fi
else
    test_basarisiz "Token olmadığı için atlandı."
fi

# ------------------------------------------------------------------------------
# 9. Teklifler Masası (/api/v1/teklifler)
# ------------------------------------------------------------------------------
test_adim "Teklifler Masası Sorgulama (/api/v1/teklifler)"
if [ -n "$REP_TOKEN" ]; then
    OFFERS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/teklifler" \
        -H "Authorization: Bearer $REP_TOKEN")
    if [ "$OFFERS_CODE" = "200" ]; then
        test_basarili
    else
        test_basarisiz "Teklifler sorgulanamadı: HTTP $OFFERS_CODE"
    fi
else
    test_basarisiz "Token olmadığı için atlandı."
fi

# ------------------------------------------------------------------------------
# 10. Apache POI 17 Kolonluk Excel Dışa Aktarımı (/api/v1/excel/disa-aktar)
# ------------------------------------------------------------------------------
test_adim "17 Kolonluk Excel Dışa Aktarım (/api/v1/excel/disa-aktar)"
if [ -n "$REP_TOKEN" ]; then
    EXCEL_HTTP_STATUS=$(curl -s -w "%{http_code}" -o /tmp/test_saha_raporu.xlsx "${BASE_URL}/api/v1/excel/disa-aktar" \
        -H "Authorization: Bearer $REP_TOKEN")
    
    if [ "$EXCEL_HTTP_STATUS" = "200" ] && [ -s /tmp/test_saha_raporu.xlsx ]; then
        FILE_SIZE=$(wc -c < /tmp/test_saha_raporu.xlsx)
        rm -f /tmp/test_saha_raporu.xlsx
        test_basarili
    else
        test_basarisiz "Excel indirilemedi. HTTP: $EXCEL_HTTP_STATUS"
    fi
else
    test_basarisiz "Token olmadığı için atlandı."
fi

# ------------------------------------------------------------------------------
# Özet ve Değerlendirme
# ------------------------------------------------------------------------------
echo -e "\n${BLUE}==============================================================================${NC}"
echo -e "  TEST SONUCU: ${GREEN}${BASARILI_TEST_SAYISI}/${TOPLAM_TEST_SAYISI}${NC} test başarıyla tamamlandı."
if [ "$BASARILI_TEST_SAYISI" -eq "$TOPLAM_TEST_SAYISI" ]; then
    echo -e "  DURUM: ${GREEN}SİSTEM SAĞLIKLI VE CANLIYA HAZIR! ($0 PoC Uyumlu)${NC}"
    echo -e "${BLUE}==============================================================================${NC}\n"
    exit 0
else
    echo -e "  DURUM: ${RED}BAZI TESTLER BAŞARISIZ OLDU. LÜTFEN LOGLARI KONTROL EDİN.${NC}"
    echo -e "${BLUE}==============================================================================${NC}\n"
    exit 1
fi
