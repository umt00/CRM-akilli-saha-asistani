import React from 'react';
import type { LucideIcon } from 'lucide-react';
import type { Rol } from './crm.types';

export interface NavItem {
  id: string;
  label: string;
  path: string;
  icon: LucideIcon;
  badge?: string | number;
  badgeColor?: 'primary' | 'gold' | 'danger';
  roller?: Rol[];
  description?: string;
}

export interface BottomNavItem {
  id: string;
  label: string;
  path: string;
  icon: LucideIcon;
  badge?: number;
}
