import { useState, useRef } from 'react';
import axios from 'axios';
import '../../App';

function Subformulas() {
  const [formula, setFormula] = useState('');
  const [subformulas, setSubformulas] = useState([]);
  const inputRef = useRef([]);

  const insertSymbol = (symbol) => {
    const input = inputRef.current;
    if (input) {
      const start = input.selectionStart;
      const end = input.selectionEnd;
      const newFormula = formula.slice(0, start) + symbol + formula.slice(end);
      setFormula(newFormula);
      
      // Setează cursorul după simbol
      setTimeout(() => {
        input.focus();
        input.selectionStart = input.selectionEnd = start + symbol.length;
      }, 0);
    }
  };

  const handleSubmit = async () => {
    try {
      const response = await axios.post('http://localhost:8080/api/formula/subformulas', { formula });
      setSubformulas(response.data.subformulas);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div className="container">
  <h1 className="text-2xl font-bold mb-4">Extragere Subformule</h1>
  
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
    ref={inputRef}
    className="border p-2 rounded w-full mb-4"
    placeholder="Introduceți formula logică"
  />

  {/* Button pentru extragere subformule */}
  <button
    onClick={handleSubmit}
    className="bg-purple-500 text-white px-4 py-2 rounded hover:bg-purple-600 transition"
  >
    Extrage Subformule
  </button>

  {/* Lista de subformule */}
  {/* Lista de subformule */}
{subformulas.length > 0 && (
  <ul className="mt-4 list-disc list-inside text-blue-600">
    {subformulas.map((sub, index) => (
      <li key={index}>{sub}</li>
    ))}
  </ul>
)}
</div>
  );
}

export default Subformulas;
