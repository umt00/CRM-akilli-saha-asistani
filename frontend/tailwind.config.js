/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        // Yeşiller Gıda (https://www.yesillergida.com.tr/) Kurumsal Renk Paleti
        primary: {
          50: '#f2f9f4',
          100: '#e1f2e6',
          200: '#c4e5ce',
          300: '#98d2aa',
          400: '#64b780',
          500: '#3a8155',  // Sitedeki açık vurgu yeşili
          600: '#1d6e3c',  // Sitedeki ana kurumsal yeşil (#1d6e3c)
          700: '#185831',
          800: '#154728',
          900: '#123b23',
          950: '#072112',
        },
        // Gıda sektörüne özel sıcak altın/bal tonları (Teklif & Önemli Vurgular)
        gold: {
          500: '#e69500',
          600: '#cf8200',
        },
        forest: {
          850: '#0f1c14',
          900: '#0a140e',
          950: '#050c08',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      boxShadow: {
        'soft-sm': '0 1px 3px 0 rgba(0, 0, 0, 0.05), 0 1px 2px 0 rgba(0, 0, 0, 0.03)',
        'soft': '0 4px 6px -1px rgba(0, 0, 0, 0.07), 0 2px 4px -1px rgba(0, 0, 0, 0.04)',
        'soft-lg': '0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -2px rgba(0, 0, 0, 0.03)',
        'glow-primary': '0 0 20px -2px rgba(29, 110, 60, 0.45)',
      },
    },
  },
  plugins: [],
};
