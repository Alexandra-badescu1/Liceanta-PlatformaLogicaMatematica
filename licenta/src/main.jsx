import React, { StrictMode } from 'react'; // Asigură-te că imporți 'React'
import { createRoot } from 'react-dom/client'; // Folosești createRoot pentru React 18+
import './index.css'; // Importă stilurile CSS
import App from './App.jsx'; // Importă componenta principală

// Crează root-ul React și montează aplicația în elementul cu id-ul 'root'
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App /> {/* Aici renderizezi aplicația */}
  </StrictMode>
);
