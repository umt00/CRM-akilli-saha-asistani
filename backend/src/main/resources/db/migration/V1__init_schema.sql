-- =====================================================================
-- AKILLI SAHA CRM - V1__init_schema.sql
-- PostgreSQL Şeması ve İndeksleri (17 Kolonluk Excel & Saha İş Mantığı)
-- =====================================================================

-- 1. KULLANICILAR TABLOSU (Users & Authentication)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'SALES_REP', -- 'ADMIN', 'SALES_REP'
    phone VARCHAR(30),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- 2. FİRMALAR / MÜŞTERİLER TABLOSU (Companies & Excel Müşteri Bilgileri)
CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city_region VARCHAR(100),                         -- Kolon D: Bölge / Şehir
    address TEXT,
    phone VARCHAR(50),
    email VARCHAR(120),
    current_supplier_competitor VARCHAR(255),         -- Kolon I: Mevcut Tedarikçi / Rakip
    supplied_products TEXT,                           -- Kolon F: Tedarik Ettiği Ürünler
    monthly_consumption VARCHAR(100),                 -- Kolon G: Aylık Kullanım Miktarı
    purchased_products TEXT,                          -- Kolon H: Bizden Aldığı Ürünler
    assigned_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL, -- Sorumlu Plasiyer (Veri İzolasyonu)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_companies_assigned_user ON companies(assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_companies_city_region ON companies(city_region);
CREATE INDEX IF NOT EXISTS idx_companies_name ON companies(name);

-- 3. FİRMA YETKİLİLERİ TABLOSU (Contacts)
CREATE TABLE IF NOT EXISTS company_contacts (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    full_name VARCHAR(120) NOT NULL,                  -- Kolon B: Ziyaret Edilen Kişi
    department_role VARCHAR(120),                     -- Kolon C: Görevi / Departmanı (Satın Alma, Şef vb.)
    phone VARCHAR(50),
    email VARCHAR(120),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_company_contacts_company_id ON company_contacts(company_id);

-- 4. ÜRÜN KATALOĞU TABLOSU (Products: Kerry, Cargill, Orkide vb.)
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(60) UNIQUE,
    name VARCHAR(255) NOT NULL,                       -- Ürün Adı (Örn: Nacho Cheese, Çikolata Aroması)
    brand VARCHAR(100) NOT NULL,                      -- Marka (Kerry, Cargill, Orkide vb.)
    category VARCHAR(100),                            -- Kategori (Aroma, Kakao, Yağ, Sos vb.)
    unit VARCHAR(30) DEFAULT 'KG',                    -- Birim (KG, GR, ADET, KOLI)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- 5. SAHA ZİYARETLERİ TABLOSU (Visits - 17 Kolonun Çekirdeği)
CREATE TABLE IF NOT EXISTS visits (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    contact_id BIGINT REFERENCES company_contacts(id) ON DELETE SET NULL,
    representative_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    visit_date TIMESTAMP WITH TIME ZONE NOT NULL,     -- Kolon E: Ziyaret Tarihi
    topic VARCHAR(255) NOT NULL,                      -- Kolon J: Ziyaret Konusu
    supplied_products TEXT,                           -- Kolon F: Görüşülen Tedarik Ürünleri
    monthly_consumption VARCHAR(100),                 -- Kolon G: Aylık Tüketim
    purchased_products TEXT,                          -- Kolon H: Bizden Aldığı Ürünler
    current_supplier_competitor VARCHAR(255),         -- Kolon I: Rakip Bilgisi
    has_sample BOOLEAN NOT NULL DEFAULT FALSE,        -- Kolon K: Numune Verildi mi?
    has_offer BOOLEAN NOT NULL DEFAULT FALSE,         -- Kolon M: Teklif Verildi mi?
    next_action TEXT,                                 -- Kolon O: Sonraki Aksiyon
    next_visit_date TIMESTAMP WITH TIME ZONE,         -- Kolon P: Sonraki Ziyaret Tarihi (Ajanda)
    notes TEXT,                                       -- Kolon Q: Notlar
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_visits_company_id ON visits(company_id);
CREATE INDEX IF NOT EXISTS idx_visits_representative_id ON visits(representative_id);
CREATE INDEX IF NOT EXISTS idx_visits_visit_date ON visits(visit_date);
CREATE INDEX IF NOT EXISTS idx_visits_next_visit_date ON visits(next_visit_date);

-- 6. NUMUNE TAKİP TABLOSU (Samples)
CREATE TABLE IF NOT EXISTS samples (
    id BIGSERIAL PRIMARY KEY,
    visit_id BIGINT REFERENCES visits(id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    representative_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    product_name VARCHAR(255) NOT NULL,               -- Ürün Adı
    brand VARCHAR(100),                               -- Marka (Kerry, Cargill vb.)
    quantity VARCHAR(60) NOT NULL,                    -- Miktar (Örn: 250 GR, 500 GR, 25 KG)
    status VARCHAR(40) NOT NULL DEFAULT 'BEKLEMEDE',  -- BEKLEMEDE, TEST_ASAMASINDA, BEGENDI, REDDETTI, SIPARISE_DONUSTU
    result_notes TEXT,                                -- Kolon L: Numune Sonucu
    sent_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    evaluated_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_samples_company_id ON samples(company_id);
CREATE INDEX IF NOT EXISTS idx_samples_representative_id ON samples(representative_id);
CREATE INDEX IF NOT EXISTS idx_samples_status ON samples(status);

-- 7. TEKLİF & TİCARİ FIRSAT TABLOSU (Offers / Opportunities)
CREATE TABLE IF NOT EXISTS offers (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    visit_id BIGINT REFERENCES visits(id) ON DELETE SET NULL,
    representative_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    amount NUMERIC(15, 2),                            -- Kolon N: Teklif Tutarı
    currency VARCHAR(10) DEFAULT 'TRY',               -- TRY, USD, EUR
    status VARCHAR(40) NOT NULL DEFAULT 'ACIK',       -- ACIK, KAZANILDI, KAYBEDILDI, IPTAL
    notes TEXT,
    valid_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_offers_company_id ON offers(company_id);
CREATE INDEX IF NOT EXISTS idx_offers_representative_id ON offers(representative_id);
CREATE INDEX IF NOT EXISTS idx_offers_status ON offers(status);

-- =====================================================================
-- BAŞLANGIÇ TOHUM VERİLERİ (Seed Data)
-- Admin: admin@akillisaha.com (Şifre: admin123)
-- Temsilci: temsilci@akillisaha.com (Şifre: saha123)
-- (Şifreler BCrypt ile hashlenmiştir)
-- =====================================================================
INSERT INTO users (email, password_hash, full_name, role, is_active)
VALUES 
('admin@akillisaha.com', '$2a$10$7Zk8lC6oEreM65ZfPvZPTejQvWkn8qQv0p8f9tKvZ8r7s9X1s1W1q', 'Sistem Yöneticisi', 'ADMIN', true),
('temsilci@akillisaha.com', '$2a$10$7Zk8lC6oEreM65ZfPvZPTejQvWkn8qQv0p8f9tKvZ8r7s9X1s1W1q', 'Ahmet Saha Temsilcisi', 'SALES_REP', true)
ON CONFLICT (email) DO NOTHING;

-- Örnek Ürünler
INSERT INTO products (code, name, brand, category, unit)
VALUES
('KRY-001', 'Nacho Cheese Flavour', 'Kerry', 'Aroma', 'KG'),
('KRY-002', 'Fındık Aroması', 'Kerry', 'Aroma', 'KG'),
('KRY-003', 'Çikolata Aroması', 'Kerry', 'Aroma', 'KG'),
('KRY-004', 'Karamel Aroması', 'Kerry', 'Aroma', 'KG'),
('CRG-001', 'DB-82 H Kakao', 'Cargill', 'Kakao', 'KG'),
('CRG-002', 'DB-400 Kakao', 'Cargill', 'Kakao', 'KG'),
('CRG-003', 'SSP Bitkisel Yağ', 'Cargill', 'Yağ', 'KG'),
('ORK-001', 'Endüstriyel Kızartma Yağı', 'Orkide', 'Yağ', 'KG')
ON CONFLICT (code) DO NOTHING;
