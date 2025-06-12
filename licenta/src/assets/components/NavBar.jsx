import { Link } from 'react-router-dom';
import { useState } from 'react';
import './Navbar.css'; // Now this will work

function Navbar() {
  const [darkMode, setDarkMode] = useState(false);

  const toggleDarkMode = () => {
  document.body.classList.toggle('dark');
  setDarkMode(!darkMode);
};

  return (
    <nav className="navbar">
      <div className="nav-links">
        <Link to="/">Acasă</Link>
        <Link to="/validate">Validare Formulă</Link>
        <Link to="/truth-table">Tabel de Adevăr</Link>
        <Link to="/subformulas">Subformule</Link>
        <Link to="/normal-form">Forme Normale</Link>
      </div>
      <button onClick={toggleDarkMode} className="toggle-btn">
        {darkMode ? 'Luminos' : 'Întunecat'}
      </button>
    </nav>
  );
}

export default Navbar;
