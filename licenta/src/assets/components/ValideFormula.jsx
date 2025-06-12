import { useState, useRef } from 'react';
import axios from 'axios';
import { MathJax, MathJaxContext } from 'better-react-mathjax';
import '../../App.css';

function ValidateFormula() {
  const [formula, setFormula] = useState('');
  const [result, setResult] = useState(null);
  const inputRef = useRef(null); // <--- adăugat ref pentru input

  const handleSubmit = async () => {
    try {
      const response = await axios.post('http://localhost:8080/api/formula/validate', { formula });
      setResult(response.data);
    } catch (error) {
      console.error(error);
      setResult({ error: 'Eroare de validare.' });
    }
  };

  const insertSymbol = (symbol) => {
    const input = inputRef.current;
    if (input) {
      const start = input.selectionStart;
      const end = input.selectionEnd;
      const newFormula = formula.slice(0, start) + symbol + formula.slice(end);
      setFormula(newFormula);

      // Mutăm cursorul după simbol
      setTimeout(() => {
        input.focus();
        input.selectionStart = input.selectionEnd = start + symbol.length;
      }, 0);
    }
  };

  const mathJaxConfig = {
    loader: { load: ['input/tex', 'output/chtml'] },
  };

  return (
    <div className="container">
      <h1 className="text-2xl font-bold mb-4">Validare Formulă</h1>

      {/* Butoane pentru simboluri */}
      <div className="flex flex-nowrap gap-2 mb-4 overflow-x-auto p-1">
        {[
          { label: '∧', value: '∧' },
          { label: '∨', value: '∨' },
          { label: '→', value: '→' },
          { label: '↔', value: '↔' },
          { label: '¬', value: '¬' },
        ].map((symbol) => (
          <button
            key={symbol.value}
            onClick={() => insertSymbol(symbol.value)}
            className="symbol-button bg-gray-200 text-gray-800 font-mono px-4 py-2 rounded-md shadow-md hover:bg-gray-300 hover:scale-95 transition transform flex-shrink-0"
            type="button"
          >
            {symbol.label}
          </button>
        ))}
      </div>

      {/* Input formular */}
      <input
        value={formula}
        onChange={(e) => setFormula(e.target.value)}
        ref={inputRef} // <--- adăugat ref aici
        className="border p-2 rounded w-full mb-4"
        placeholder="Introduceți formula logică"
      />

      <button onClick={handleSubmit} className="bg-blue-500 text-white px-4 py-2 rounded">
        Validează
      </button>

      {result && (
        <div className="mt-4">
          {result.error ? (
            <p className="text-red-500">{result.error}</p>
          ) : (
            <>
              <p>Formulă validă: {result.valid ? '✅' : '❌'}</p>
              
            </>
          )}
        </div>
      )}
    </div>
  );
}

export default ValidateFormula;
