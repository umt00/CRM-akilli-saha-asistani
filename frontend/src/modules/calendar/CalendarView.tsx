import React, { useState, useEffect, useMemo } from 'react';
import { takvimApi } from '../../services/api';
import { TakvimEtkinlikYaniti } from '../../types/crm.types';
import { CompanyHoverCard } from '../../components/common/CustomerCard';
import {
  Calendar as CalendarIcon, Clock, ChevronLeft, ChevronRight,
  Filter, Search, MapPin, User, Phone, Mail, CheckCircle2,
  AlertTriangle, Plus, Eye, ArrowRight, Sparkles, Building2,
  CalendarDays, CalendarRange, ListFilter, X, RefreshCw
} from 'lucide-react';

type CalendarViewMode = 'month' | 'week' | 'agenda';

export const CalendarView: React.FC = () => {
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [events, setEvents] = useState<TakvimEtkinlikYaniti[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedTypeFilter, setSelectedTypeFilter] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState<CalendarViewMode>('month');
  const [activeEvent, setActiveEvent] = useState<TakvimEtkinlikYaniti | null>(null);

  // Load events for the current month view (window covering 1 month before to 2 months after)
  const loadEvents = async () => {
    setLoading(true);
    try {
      const year = currentDate.getFullYear();
      const month = currentDate.getMonth();
      const start = new Date(year, month - 1, 1).toISOString();
      const end = new Date(year, month + 2, 0, 23, 59, 59).toISOString();

      const data = await takvimApi.etkinlikleriGetir(start, end);
      setEvents(data);
    } catch (err) {
      console.error('Takvim etkinlikleri alınamadı:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEvents();
  }, [currentDate.getFullYear(), currentDate.getMonth()]);

  // Navigation handlers
  const handlePrev = () => {
    if (viewMode === 'month') {
      setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
    } else if (viewMode === 'week') {
      const newD = new Date(currentDate);
      newD.setDate(newD.getDate() - 7);
      setCurrentDate(newD);
    }
  };

  const handleNext = () => {
    if (viewMode === 'month') {
      setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
    } else if (viewMode === 'week') {
      const newD = new Date(currentDate);
      newD.setDate(newD.getDate() + 7);
      setCurrentDate(newD);
    }
  };

  const handleToday = () => {
    setCurrentDate(new Date());
  };

  // Filtered Events
  const filteredEvents = useMemo(() => {
    return events.filter((ev) => {
      const matchType = selectedTypeFilter === 'ALL' || ev.tur === selectedTypeFilter;
      const query = searchQuery.toLowerCase().trim();
      const matchQuery =
        !query ||
        ev.baslik?.toLowerCase().includes(query) ||
        ev.firmaUnvani?.toLowerCase().includes(query) ||
        ev.aciklama?.toLowerCase().includes(query) ||
        ev.sehirBolge?.toLowerCase().includes(query) ||
        ev.yetkiliAdSoyad?.toLowerCase().includes(query);
      return matchType && matchQuery;
    });
  }, [events, selectedTypeFilter, searchQuery]);

  // Month grid calculations
  const monthData = useMemo(() => {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);

    // Monday as start of week (0=Mon, 6=Sun)
    let startDayOfWeek = firstDayOfMonth.getDay() - 1;
    if (startDayOfWeek === -1) startDayOfWeek = 6;

    const daysInMonth = lastDayOfMonth.getDate();

    // Previous month filler days
    const prevMonthLastDay = new Date(year, month, 0).getDate();
    const prevDays: { day: number; date: Date; isCurrentMonth: boolean }[] = [];
    for (let i = startDayOfWeek - 1; i >= 0; i--) {
      const d = prevMonthLastDay - i;
      prevDays.push({
        day: d,
        date: new Date(year, month - 1, d),
        isCurrentMonth: false,
      });
    }

    // Current month days
    const currentDays: { day: number; date: Date; isCurrentMonth: boolean }[] = [];
    for (let d = 1; d <= daysInMonth; d++) {
      currentDays.push({
        day: d,
        date: new Date(year, month, d),
        isCurrentMonth: true,
      });
    }

    // Next month filler days to complete 35 or 42 cells
    const totalFilled = prevDays.length + currentDays.length;
    const totalCells = totalFilled <= 35 ? 35 : 42;
    const nextDays: { day: number; date: Date; isCurrentMonth: boolean }[] = [];
    for (let d = 1; d <= totalCells - totalFilled; d++) {
      nextDays.push({
        day: d,
        date: new Date(year, month + 1, d),
        isCurrentMonth: false,
      });
    }

    return [...prevDays, ...currentDays, ...nextDays];
  }, [currentDate]);

  // Week days calculation
  const weekDays = useMemo(() => {
    const curr = new Date(currentDate);
    const day = curr.getDay();
    const diff = curr.getDate() - day + (day === 0 ? -6 : 1); // adjust when day is sunday
    const monday = new Date(curr.setDate(diff));

    const days: Date[] = [];
    for (let i = 0; i < 7; i++) {
      const nextDay = new Date(monday);
      nextDay.setDate(monday.getDate() + i);
      days.push(nextDay);
    }
    return days;
  }, [currentDate]);

  // Helper to test if two dates are same calendar day
  const isSameDay = (d1: Date, d2: Date) => {
    return (
      d1.getFullYear() === d2.getFullYear() &&
      d1.getMonth() === d2.getMonth() &&
      d1.getDate() === d2.getDate()
    );
  };

  const isToday = (d: Date) => isSameDay(d, new Date());

  const getEventsForDay = (date: Date) => {
    return filteredEvents.filter((ev) => {
      const evDate = new Date(ev.tarih);
      return isSameDay(evDate, date);
    });
  };

  const monthNames = [
    'Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran',
    'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık',
  ];

  const weekDayNames = ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'];

  return (
    <div className="calendar-module-container fade-in">
      {/* Top Header & Bitrix24 Style Bar */}
      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: '1rem',
          marginBottom: '1.25rem',
        }}
      >
        <div>
          <h2
            style={{
              fontSize: '1.35rem',
              fontWeight: 700,
              display: 'flex',
              alignItems: 'center',
              gap: '0.6rem',
              color: '#f8fafc',
            }}
          >
            <CalendarIcon className="text-primary" size={26} />
            <span>Akıllı Saha Takvimi & Randevular</span>
          </h2>
          <p style={{ fontSize: '0.82rem', color: '#94a3b8', marginTop: '0.2rem' }}>
            Bitrix24 tarzı birleşik ziyaret, randevu ve numune zaman çizelgesi
          </p>
        </div>

        {/* View Mode Switcher */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div
            style={{
              background: 'rgba(30, 41, 59, 0.8)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              borderRadius: '8px',
              padding: '3px',
              display: 'flex',
              gap: '2px',
            }}
          >
            <button
              onClick={() => setViewMode('month')}
              className={`btn btn-sm ${viewMode === 'month' ? 'btn-primary' : ''}`}
              style={{
                border: 'none',
                background: viewMode === 'month' ? 'var(--primary, #6366f1)' : 'transparent',
                color: '#fff',
                display: 'flex',
                alignItems: 'center',
                gap: '0.35rem',
                padding: '0.35rem 0.75rem',
                borderRadius: '6px',
                fontSize: '0.8rem',
              }}
            >
              <CalendarDays size={14} />
              <span>Ay</span>
            </button>
            <button
              onClick={() => setViewMode('week')}
              className={`btn btn-sm ${viewMode === 'week' ? 'btn-primary' : ''}`}
              style={{
                border: 'none',
                background: viewMode === 'week' ? 'var(--primary, #6366f1)' : 'transparent',
                color: '#fff',
                display: 'flex',
                alignItems: 'center',
                gap: '0.35rem',
                padding: '0.35rem 0.75rem',
                borderRadius: '6px',
                fontSize: '0.8rem',
              }}
            >
              <CalendarRange size={14} />
              <span>Hafta</span>
            </button>
            <button
              onClick={() => setViewMode('agenda')}
              className={`btn btn-sm ${viewMode === 'agenda' ? 'btn-primary' : ''}`}
              style={{
                border: 'none',
                background: viewMode === 'agenda' ? 'var(--primary, #6366f1)' : 'transparent',
                color: '#fff',
                display: 'flex',
                alignItems: 'center',
                gap: '0.35rem',
                padding: '0.35rem 0.75rem',
                borderRadius: '6px',
                fontSize: '0.8rem',
              }}
            >
              <ListFilter size={14} />
              <span>Gündem</span>
            </button>
          </div>

          <button
            onClick={loadEvents}
            className="btn btn-secondary btn-sm"
            style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}
            title="Yenile"
          >
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Navigation & Filter Ribbon */}
      <div
        className="card"
        style={{
          padding: '0.85rem 1.25rem',
          marginBottom: '1rem',
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '1rem',
        }}
      >
        {/* Date Navigator */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            <button
              onClick={handlePrev}
              className="btn btn-secondary btn-sm"
              style={{ padding: '0.4rem 0.6rem' }}
            >
              <ChevronLeft size={16} />
            </button>
            <button
              onClick={handleToday}
              className="btn btn-secondary btn-sm"
              style={{ fontWeight: 600, padding: '0.4rem 0.75rem' }}
            >
              Bugün
            </button>
            <button
              onClick={handleNext}
              className="btn btn-secondary btn-sm"
              style={{ padding: '0.4rem 0.6rem' }}
            >
              <ChevronRight size={16} />
            </button>
          </div>

          <div style={{ fontWeight: 700, fontSize: '1.15rem', color: '#f1f5f9' }}>
            {monthNames[currentDate.getMonth()]} {currentDate.getFullYear()}
          </div>
        </div>

        {/* Filter Pills */}
        <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '0.4rem' }}>
          <button
            onClick={() => setSelectedTypeFilter('ALL')}
            style={{
              background: selectedTypeFilter === 'ALL' ? 'rgba(255, 255, 255, 0.15)' : 'rgba(255, 255, 255, 0.05)',
              border: `1px solid ${selectedTypeFilter === 'ALL' ? '#818cf8' : 'rgba(255, 255, 255, 0.1)'}`,
              color: '#fff',
              borderRadius: '20px',
              padding: '0.3rem 0.75rem',
              fontSize: '0.76rem',
              cursor: 'pointer',
              fontWeight: selectedTypeFilter === 'ALL' ? 600 : 400,
            }}
          >
            Tümü ({events.length})
          </button>

          <button
            onClick={() => setSelectedTypeFilter('RANDEVU')}
            style={{
              background: selectedTypeFilter === 'RANDEVU' ? 'rgba(139, 92, 246, 0.25)' : 'rgba(139, 92, 246, 0.08)',
              border: `1px solid ${selectedTypeFilter === 'RANDEVU' ? '#a78bfa' : 'rgba(139, 92, 246, 0.2)'}`,
              color: '#c4b5fd',
              borderRadius: '20px',
              padding: '0.3rem 0.75rem',
              fontSize: '0.76rem',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.3rem',
            }}
          >
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#8b5cf6' }} />
            <span>Randevular</span>
          </button>

          <button
            onClick={() => setSelectedTypeFilter('ZIYARET')}
            style={{
              background: selectedTypeFilter === 'ZIYARET' ? 'rgba(59, 130, 246, 0.25)' : 'rgba(59, 130, 246, 0.08)',
              border: `1px solid ${selectedTypeFilter === 'ZIYARET' ? '#60a5fa' : 'rgba(59, 130, 246, 0.2)'}`,
              color: '#93c5fd',
              borderRadius: '20px',
              padding: '0.3rem 0.75rem',
              fontSize: '0.76rem',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.3rem',
            }}
          >
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#3b82f6' }} />
            <span>Ziyaretler</span>
          </button>

          <button
            onClick={() => setSelectedTypeFilter('NUMUNE')}
            style={{
              background: selectedTypeFilter === 'NUMUNE' ? 'rgba(16, 185, 129, 0.25)' : 'rgba(16, 185, 129, 0.08)',
              border: `1px solid ${selectedTypeFilter === 'NUMUNE' ? '#34d399' : 'rgba(16, 185, 129, 0.2)'}`,
              color: '#6ee7b7',
              borderRadius: '20px',
              padding: '0.3rem 0.75rem',
              fontSize: '0.76rem',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.3rem',
            }}
          >
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#10b981' }} />
            <span>Numuneler</span>
          </button>

          <button
            onClick={() => setSelectedTypeFilter('TEKLIF')}
            style={{
              background: selectedTypeFilter === 'TEKLIF' ? 'rgba(245, 158, 11, 0.25)' : 'rgba(245, 158, 11, 0.08)',
              border: `1px solid ${selectedTypeFilter === 'TEKLIF' ? '#fbbf24' : 'rgba(245, 158, 11, 0.2)'}`,
              color: '#fcd34d',
              borderRadius: '20px',
              padding: '0.3rem 0.75rem',
              fontSize: '0.76rem',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.3rem',
            }}
          >
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#f59e0b' }} />
            <span>Teklif Vadeleri</span>
          </button>
        </div>

        {/* Search Bar */}
        <div style={{ position: 'relative', width: '220px' }}>
          <Search
            size={14}
            style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)', color: '#94a3b8' }}
          />
          <input
            type="text"
            className="form-control"
            placeholder="Takvimde ara..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{
              paddingLeft: '30px',
              paddingTop: '0.35rem',
              paddingBottom: '0.35rem',
              fontSize: '0.8rem',
              background: 'rgba(15, 23, 42, 0.6)',
            }}
          />
        </div>
      </div>

      {/* MAIN VIEW: 1. MONTH VIEW */}
      {viewMode === 'month' && (
        <div
          className="calendar-month-grid card"
          style={{ padding: '0', overflow: 'hidden', border: '1px solid rgba(255, 255, 255, 0.08)' }}
        >
          {/* Day of Week Header */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(7, 1fr)',
              background: 'rgba(15, 23, 42, 0.8)',
              borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
              textAlign: 'center',
              fontWeight: 600,
              fontSize: '0.82rem',
              color: '#94a3b8',
              padding: '0.6rem 0',
            }}
          >
            {weekDayNames.map((name, idx) => (
              <div key={idx} style={{ color: idx >= 5 ? '#f43f5e' : '#cbd5e1' }}>
                {name}
              </div>
            ))}
          </div>

          {/* Month Cells Grid */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(7, 1fr)',
              gridAutoRows: 'minmax(110px, 1fr)',
              gap: '1px',
              background: 'rgba(255, 255, 255, 0.06)',
            }}
          >
            {monthData.map((cell, idx) => {
              const dayEvents = getEventsForDay(cell.date);
              const today = isToday(cell.date);

              return (
                <div
                  key={idx}
                  className="calendar-day-cell"
                  style={{
                    background: cell.isCurrentMonth
                      ? today
                        ? 'rgba(99, 102, 241, 0.08)'
                        : 'var(--bg-card, #1e293b)'
                      : 'rgba(15, 23, 42, 0.5)',
                    padding: '0.45rem',
                    position: 'relative',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.3rem',
                    transition: 'background 0.15s ease',
                    minHeight: '110px',
                  }}
                >
                  {/* Day Number Header */}
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      marginBottom: '0.2rem',
                    }}
                  >
                    <span
                      style={{
                        fontSize: '0.78rem',
                        fontWeight: today ? 700 : 500,
                        color: cell.isCurrentMonth ? (today ? '#ffffff' : '#e2e8f0') : '#64748b',
                        background: today ? '#6366f1' : 'transparent',
                        padding: today ? '2px 6px' : '0',
                        borderRadius: '10px',
                      }}
                    >
                      {cell.day}
                    </span>

                    {today && (
                      <span
                        style={{
                          fontSize: '0.62rem',
                          background: 'rgba(99, 102, 241, 0.2)',
                          color: '#a5b4fc',
                          padding: '1px 5px',
                          borderRadius: '4px',
                          fontWeight: 600,
                        }}
                      >
                        BUGÜN
                      </span>
                    )}
                  </div>

                  {/* Day Events Chips (Bitrix24 Style) */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', flex: 1, overflow: 'hidden' }}>
                    {dayEvents.slice(0, 3).map((ev) => {
                      const timeStr = new Date(ev.tarih).toLocaleTimeString('tr-TR', {
                        hour: '2-digit',
                        minute: '2-digit',
                      });

                      return (
                        <div
                          key={ev.id}
                          onClick={() => setActiveEvent(ev)}
                          title={`${ev.turEtiketi}: ${ev.baslik}`}
                          style={{
                            background: `${ev.renkKodu}22`,
                            borderLeft: `3px solid ${ev.renkKodu}`,
                            borderRadius: '4px',
                            padding: '2px 5px',
                            fontSize: '0.7rem',
                            color: '#f8fafc',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '0.3rem',
                            whiteSpace: 'nowrap',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            transition: 'all 0.12s ease',
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'translateX(2px)';
                            e.currentTarget.style.background = `${ev.renkKodu}44`;
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'none';
                            e.currentTarget.style.background = `${ev.renkKodu}22`;
                          }}
                        >
                          <span style={{ fontSize: '0.65rem', color: '#94a3b8', fontWeight: 600, flexShrink: 0 }}>
                            {timeStr}
                          </span>
                          <span style={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {ev.firmaUnvani || ev.baslik}
                          </span>
                        </div>
                      );
                    })}

                    {dayEvents.length > 3 && (
                      <div
                        onClick={() => {
                          setCurrentDate(cell.date);
                          setViewMode('agenda');
                        }}
                        style={{
                          fontSize: '0.66rem',
                          color: '#818cf8',
                          cursor: 'pointer',
                          fontWeight: 600,
                          textAlign: 'center',
                          padding: '1px 0',
                        }}
                      >
                        +{dayEvents.length - 3} etkinlik daha
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* MAIN VIEW: 2. WEEK VIEW */}
      {viewMode === 'week' && (
        <div className="card" style={{ padding: '0', overflow: 'hidden' }}>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(7, 1fr)',
              background: 'rgba(15, 23, 42, 0.8)',
              borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
              textAlign: 'center',
              padding: '0.75rem 0',
            }}
          >
            {weekDays.map((d, idx) => {
              const today = isToday(d);
              return (
                <div key={idx} style={{ borderRight: idx < 6 ? '1px solid rgba(255, 255, 255, 0.05)' : 'none' }}>
                  <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>{weekDayNames[idx]}</div>
                  <div
                    style={{
                      fontSize: '1.05rem',
                      fontWeight: 700,
                      marginTop: '0.2rem',
                      color: today ? '#818cf8' : '#f1f5f9',
                    }}
                  >
                    {d.getDate()} {monthNames[d.getMonth()].substring(0, 3)}
                  </div>
                </div>
              );
            })}
          </div>

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(7, 1fr)',
              minHeight: '400px',
              background: 'rgba(255, 255, 255, 0.02)',
            }}
          >
            {weekDays.map((d, idx) => {
              const dayEvents = getEventsForDay(d);
              const today = isToday(d);

              return (
                <div
                  key={idx}
                  style={{
                    borderRight: idx < 6 ? '1px solid rgba(255, 255, 255, 0.06)' : 'none',
                    padding: '0.65rem',
                    background: today ? 'rgba(99, 102, 241, 0.04)' : 'transparent',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.5rem',
                  }}
                >
                  {dayEvents.length === 0 ? (
                    <div style={{ textAlign: 'center', color: '#64748b', fontSize: '0.75rem', marginTop: '2rem' }}>
                      Kayıt yok
                    </div>
                  ) : (
                    dayEvents.map((ev) => (
                      <div
                        key={ev.id}
                        onClick={() => setActiveEvent(ev)}
                        style={{
                          background: 'rgba(30, 41, 59, 0.85)',
                          border: `1px solid ${ev.renkKodu}55`,
                          borderLeft: `4px solid ${ev.renkKodu}`,
                          borderRadius: '8px',
                          padding: '0.6rem',
                          cursor: 'pointer',
                          transition: 'transform 0.15s ease',
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem' }}>
                          <span
                            style={{
                              fontSize: '0.65rem',
                              background: `${ev.renkKodu}25`,
                              color: ev.renkKodu,
                              padding: '1px 5px',
                              borderRadius: '4px',
                              fontWeight: 700,
                            }}
                          >
                            {ev.turEtiketi}
                          </span>
                          <span style={{ fontSize: '0.7rem', color: '#94a3b8' }}>
                            {new Date(ev.tarih).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        </div>

                        <div style={{ fontWeight: 600, fontSize: '0.82rem', color: '#f8fafc', marginBottom: '0.2rem' }}>
                          {ev.firmaUnvani}
                        </div>

                        {ev.aciklama && (
                          <div
                            style={{
                              fontSize: '0.72rem',
                              color: '#94a3b8',
                              display: '-webkit-box',
                              WebkitLineClamp: 2,
                              WebkitBoxOrient: 'vertical',
                              overflow: 'hidden',
                            }}
                          >
                            {ev.aciklama}
                          </div>
                        )}
                      </div>
                    ))
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* MAIN VIEW: 3. AGENDA / GÜNDEM STREAM (Bitrix24 Card Stream - NOT boring raw table!) */}
      {viewMode === 'agenda' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
          {filteredEvents.length === 0 ? (
            <div className="card empty-state" style={{ padding: '3rem', textAlign: 'center' }}>
              <CheckCircle2 size={42} style={{ color: '#10b981', margin: '0 auto 0.75rem', opacity: 0.8 }} />
              <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>Planlanmış Randevu / Etkinlik Bulunmuyor</div>
              <p style={{ color: '#94a3b8', fontSize: '0.82rem', marginTop: '0.35rem' }}>
                Filtre kriterlerinize uyan hiçbir etkinlik bulunamadı.
              </p>
            </div>
          ) : (
            filteredEvents.map((ev) => {
              const evDate = new Date(ev.tarih);
              const isPast = evDate.getTime() < Date.now();
              const isEventToday = isToday(evDate);

              return (
                <div
                  key={ev.id}
                  className="card"
                  style={{
                    padding: '1rem 1.25rem',
                    display: 'flex',
                    flexWrap: 'wrap',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    gap: '1rem',
                    borderLeft: `4px solid ${ev.renkKodu}`,
                    background: isEventToday ? 'rgba(99, 102, 241, 0.06)' : 'var(--bg-card)',
                    transition: 'transform 0.15s ease',
                  }}
                >
                  {/* Left: Date & Time Badge */}
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <div
                      style={{
                        width: '54px',
                        height: '54px',
                        borderRadius: '12px',
                        background: isEventToday
                          ? 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)'
                          : 'rgba(255, 255, 255, 0.05)',
                        border: '1px solid rgba(255, 255, 255, 0.1)',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexShrink: 0,
                      }}
                    >
                      <span style={{ fontSize: '1.1rem', fontWeight: 700, color: '#fff', lineHeight: 1 }}>
                        {evDate.getDate()}
                      </span>
                      <span style={{ fontSize: '0.65rem', color: isEventToday ? '#e0e7ff' : '#94a3b8', textTransform: 'uppercase', marginTop: '2px' }}>
                        {monthNames[evDate.getMonth()].substring(0, 3)}
                      </span>
                    </div>

                    {/* Middle Info */}
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                        <span
                          style={{
                            fontSize: '0.7rem',
                            fontWeight: 700,
                            padding: '2px 8px',
                            borderRadius: '12px',
                            background: `${ev.renkKodu}22`,
                            color: ev.renkKodu,
                            border: `1px solid ${ev.renkKodu}44`,
                          }}
                        >
                          {ev.turEtiketi}
                        </span>

                        <span style={{ fontSize: '0.78rem', color: '#94a3b8', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                          <Clock size={12} />
                          {evDate.toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })}
                        </span>

                        {isPast && ev.tur === 'RANDEVU' && (
                          <span
                            style={{
                              fontSize: '0.68rem',
                              fontWeight: 600,
                              background: 'rgba(239, 68, 68, 0.2)',
                              color: '#f87171',
                              padding: '2px 6px',
                              borderRadius: '4px',
                            }}
                          >
                            ⚠️ Vadesi Geçti
                          </span>
                        )}
                      </div>

                      {/* Customer Hover Card on Company Name */}
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <CompanyHoverCard
                          data={{
                            firmaId: ev.firmaId,
                            firmaUnvani: ev.firmaUnvani || ev.baslik,
                            sehirBolge: ev.sehirBolge,
                            yetkiliAdSoyad: ev.yetkiliAdSoyad,
                            yetkiliUnvan: ev.yetkiliUnvan,
                            yetkiliTelefon: ev.yetkiliTelefon,
                            yetkiliEposta: ev.yetkiliEposta,
                            plasiyerAdSoyad: ev.plasiyerAdSoyad,
                          }}
                          onOpenDetails={() => setActiveEvent(ev)}
                        >
                          <span
                            style={{
                              fontWeight: 700,
                              fontSize: '1rem',
                              color: '#f8fafc',
                              cursor: 'pointer',
                              borderBottom: '1px dashed #64748b',
                            }}
                          >
                            {ev.firmaUnvani || ev.baslik}
                          </span>
                        </CompanyHoverCard>

                        {ev.sehirBolge && (
                          <span style={{ fontSize: '0.75rem', color: '#94a3b8', display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
                            <MapPin size={12} />
                            {ev.sehirBolge}
                          </span>
                        )}
                      </div>

                      {ev.aciklama && (
                        <div style={{ fontSize: '0.8rem', color: '#cbd5e1', marginTop: '0.3rem' }}>
                          {ev.aciklama}
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Right: Quick Action Buttons */}
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    {ev.yetkiliTelefon && (
                      <a
                        href={`tel:${ev.yetkiliTelefon}`}
                        className="btn btn-secondary btn-sm"
                        style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: '#34d399' }}
                      >
                        <Phone size={14} />
                        <span>Ara</span>
                      </a>
                    )}

                    <button
                      onClick={() => setActiveEvent(ev)}
                      className="btn btn-secondary btn-sm"
                      style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}
                    >
                      <Eye size={14} />
                      <span>Detay</span>
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </div>
      )}

      {/* EVENT DETAILS MODAL (Slide-over / Modal) */}
      {activeEvent && (
        <div
          className="modal-overlay"
          onClick={() => setActiveEvent(null)}
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.75)',
            backdropFilter: 'blur(8px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 9999,
            padding: '1rem',
          }}
        >
          <div
            className="modal-content card fade-in"
            onClick={(e) => e.stopPropagation()}
            style={{
              maxWidth: '540px',
              width: '100%',
              padding: '1.5rem',
              borderRadius: '16px',
              boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.7)',
              border: `1px solid ${activeEvent.renkKodu}44`,
            }}
          >
            {/* Modal Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
              <div>
                <span
                  style={{
                    fontSize: '0.72rem',
                    fontWeight: 700,
                    padding: '3px 10px',
                    borderRadius: '12px',
                    background: `${activeEvent.renkKodu}22`,
                    color: activeEvent.renkKodu,
                    border: `1px solid ${activeEvent.renkKodu}55`,
                  }}
                >
                  {activeEvent.turEtiketi}
                </span>

                <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginTop: '0.5rem', color: '#f8fafc' }}>
                  {activeEvent.firmaUnvani || activeEvent.baslik}
                </h3>
              </div>

              <button
                onClick={() => setActiveEvent(null)}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: '#94a3b8',
                  cursor: 'pointer',
                  padding: '4px',
                }}
              >
                <X size={20} />
              </button>
            </div>

            {/* Modal Body / Information Grid */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem', marginBottom: '1.5rem' }}>
              {/* Date & Time */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.6rem',
                  background: 'rgba(255, 255, 255, 0.03)',
                  padding: '0.65rem 0.85rem',
                  borderRadius: '8px',
                }}
              >
                <Clock size={16} className="text-primary" />
                <div style={{ fontSize: '0.85rem' }}>
                  <span style={{ color: '#94a3b8' }}>Hedef / Gerçekleşme Tarihi: </span>
                  <strong style={{ color: '#f1f5f9' }}>
                    {new Date(activeEvent.tarih).toLocaleString('tr-TR', { dateStyle: 'full', timeStyle: 'short' })}
                  </strong>
                </div>
              </div>

              {/* Location */}
              {activeEvent.sehirBolge && (
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.6rem',
                    background: 'rgba(255, 255, 255, 0.03)',
                    padding: '0.65rem 0.85rem',
                    borderRadius: '8px',
                  }}
                >
                  <MapPin size={16} style={{ color: '#38bdf8' }} />
                  <div style={{ fontSize: '0.85rem' }}>
                    <span style={{ color: '#94a3b8' }}>Konum / Bölge: </span>
                    <strong style={{ color: '#f1f5f9' }}>{activeEvent.sehirBolge}</strong>
                  </div>
                </div>
              )}

              {/* Contact Person Box */}
              <div
                style={{
                  background: 'rgba(99, 102, 241, 0.06)',
                  border: '1px solid rgba(99, 102, 241, 0.2)',
                  borderRadius: '10px',
                  padding: '0.85rem',
                }}
              >
                <div style={{ fontSize: '0.72rem', color: '#a5b4fc', textTransform: 'uppercase', fontWeight: 600, marginBottom: '0.4rem' }}>
                  Görüşülen Yetkili Muhatap
                </div>
                <div style={{ fontWeight: 700, fontSize: '0.95rem', color: '#f8fafc' }}>
                  {activeEvent.yetkiliAdSoyad || 'Firma Sahibi / Yetkili'}
                </div>
                {activeEvent.yetkiliUnvan && (
                  <div style={{ fontSize: '0.78rem', color: '#94a3b8', marginTop: '0.1rem' }}>
                    {activeEvent.yetkiliUnvan}
                  </div>
                )}

                {activeEvent.yetkiliTelefon && (
                  <div style={{ marginTop: '0.6rem', display: 'flex', gap: '0.5rem' }}>
                    <a
                      href={`tel:${activeEvent.yetkiliTelefon}`}
                      className="btn btn-sm"
                      style={{
                        background: '#10b981',
                        color: '#fff',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.35rem',
                        fontSize: '0.78rem',
                        fontWeight: 600,
                        textDecoration: 'none',
                      }}
                    >
                      <Phone size={13} />
                      <span>{activeEvent.yetkiliTelefon} (Ara)</span>
                    </a>
                  </div>
                )}
              </div>

              {/* Details & Notes */}
              {activeEvent.aciklama && (
                <div
                  style={{
                    background: 'rgba(255, 255, 255, 0.03)',
                    padding: '0.85rem',
                    borderRadius: '8px',
                    fontSize: '0.84rem',
                    color: '#e2e8f0',
                    lineHeight: 1.5,
                  }}
                >
                  <div style={{ fontSize: '0.72rem', color: '#94a3b8', textTransform: 'uppercase', marginBottom: '0.3rem' }}>
                    Görüşme Notu / Planlanan Aksiyon
                  </div>
                  <div>{activeEvent.aciklama}</div>
                </div>
              )}

              {/* Representative */}
              <div style={{ fontSize: '0.76rem', color: '#94a3b8' }}>
                Sorumlu Satış Temsilcisi: <strong style={{ color: '#cbd5e1' }}>{activeEvent.plasiyerAdSoyad}</strong>
              </div>
            </div>

            {/* Modal Actions */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
              <button
                onClick={() => setActiveEvent(null)}
                className="btn btn-secondary btn-sm"
              >
                Kapat
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
