/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#FFFDF5',
          100: '#FFF8E1',
          200: '#FFECB3',
          300: '#FFE082',
          400: '#FFD54F',
          500: '#FFC300',
          600: '#FFB800',
          700: '#FF8F00',
          800: '#FF6B00',
          900: '#E65100',
        },
        success: {
          50: '#F0FCFC',
          500: '#26C6B8',
          600: '#009688',
        },
        warning: {
          50: '#FFF3E0',
          500: '#FF9800',
          600: '#E65100',
        },
        danger: {
          50: '#FFF5F5',
          500: '#FF5252',
          600: '#D32F2F',
        },
      },
    },
  },
  plugins: [],
  // avoid conflicts with Element Plus
  corePlugins: {
    preflight: false,
  },
}
