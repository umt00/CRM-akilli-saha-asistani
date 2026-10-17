#!/usr/bin/env bash
# ==============================================================================
# Akıllı Saha CRM - Test Frontend Temizleme Betiği
# Bu betik, backend'i test etmek için geçici olarak kurulan test arayüzünü (UI)
# tek komutla temizler ve repository'yi orijinal haline döndürür.
# api.ts ve crm.types.ts (Frontend geliştirici sözleşmesi) KORUNUR.
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$ROOT_DIR/frontend"

echo "🧹 Test Frontend dosyaları temizleniyor..."

# 1. npm bağımlılıkları ve build çıktıları
rm -rf "$FRONTEND_DIR/node_modules"
rm -rf "$FRONTEND_DIR/dist"
rm -f "$FRONTEND_DIR/package.json"
rm -f "$FRONTEND_DIR/package-lock.json"
rm -f "$FRONTEND_DIR/vite.config.ts"
rm -f "$FRONTEND_DIR/tsconfig.json"
rm -f "$FRONTEND_DIR/index.html"

# 2. Geçici UI bileşenleri ve stiller
rm -f "$FRONTEND_DIR/src/App.tsx"
rm -f "$FRONTEND_DIR/src/index.css"
rm -f "$FRONTEND_DIR/src/main.tsx"
rm -f "$FRONTEND_DIR/src/vite-env.d.ts"
rm -rf "$FRONTEND_DIR/src/modules/auth/AuthView.tsx"
rm -rf "$FRONTEND_DIR/src/modules/companies/CompaniesView.tsx"
rm -rf "$FRONTEND_DIR/src/modules/excel/ExcelView.tsx"
rm -rf "$FRONTEND_DIR/src/modules/offers/OffersView.tsx"
rm -rf "$FRONTEND_DIR/src/modules/samples/SamplesView.tsx"
rm -rf "$FRONTEND_DIR/src/modules/system"
rm -rf "$FRONTEND_DIR/src/modules/visits/AgendaView.tsx"
rm -rf "$FRONTEND_DIR/src/modules/visits/VisitsView.tsx"

# 3. .gitkeep dosyalarının korunmasını sağla
mkdir -p "$FRONTEND_DIR/src/modules/auth" && touch "$FRONTEND_DIR/src/modules/auth/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/modules/companies" && touch "$FRONTEND_DIR/src/modules/companies/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/modules/dashboard" && touch "$FRONTEND_DIR/src/modules/dashboard/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/modules/excel" && touch "$FRONTEND_DIR/src/modules/excel/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/modules/offers" && touch "$FRONTEND_DIR/src/modules/offers/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/modules/samples" && touch "$FRONTEND_DIR/src/modules/samples/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/modules/visits" && touch "$FRONTEND_DIR/src/modules/visits/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/components/common" && touch "$FRONTEND_DIR/src/components/common/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/components/layout" && touch "$FRONTEND_DIR/src/components/layout/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/context" && touch "$FRONTEND_DIR/src/context/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/hooks" && touch "$FRONTEND_DIR/src/hooks/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/utils" && touch "$FRONTEND_DIR/src/utils/.gitkeep"
mkdir -p "$FRONTEND_DIR/src/assets" && touch "$FRONTEND_DIR/src/assets/.gitkeep"

echo "✅ Test Frontend başarıyla temizlendi!"
echo "📁 Kalan dosyalar: frontend/src/services/api.ts ve frontend/src/types/crm.types.ts"
