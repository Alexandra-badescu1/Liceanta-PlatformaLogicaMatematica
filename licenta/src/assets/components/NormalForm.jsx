import { useState, useRef } from 'react';
import axios from 'axios';
import '../../App.css';

function NormalForm() {
  const [formula, setFormula] = useState('');
  const [steps, setSteps] = useState([]);
  const [normalForm, setNormalForm] = useState(null);
  const inputRef = useRef(null); // adăugat pentru manipulare cursor

  const handleTransform = async () => {
    try {
      const response = await axios.post('http://localhost:8080/api/normal-form/transform', formula, {
        headers: { 'Content-Type': 'text/plain' }
      });
      setSteps(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const handleCheck = async () => {
    try {
      const response = await axios.post('http://localhost:8080/api/normal-form/check', { formula });
      setNormalForm(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const insertSymbol = (symbol) => {
    const input = inputRef.current;
    if (input) {
      const start = input.selectionStart;
      const end = input.selectionEnd;
      const newFormula = formula.slice(0, start) + symbol + formula.slice(end);
      setFormula(newFormula);

      // Mută cursorul după simbol
      setTimeout(() => {
        input.focus();
        input.selectionStart = input.selectionEnd = start + symbol.length;
      }, 0);
    }
  };

  return (
    <div className="container">
      <h1 className="text-2xl font-bold mb-4">Forme Normale (FNC / FND)</h1>

      {/* Butoane pentru simboluri */}
      <div className="flex flex-nowrap overflow-x-auto gap-2 p-1 mb-4">
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

      <input
        value={formula}
        onChange={(e) => setFormula(e.target.value)}
        ref={inputRef} // adăugat ref
        className="border p-2 rounded w-full mb-4"
        placeholder="Introduceți formula logică"
      />
      <div className="flex flex-wrap justify-center gap-4 mb-4">
        <button onClick={handleTransform} className="bg-indigo-500 text-white px-4 py-2 rounded">
          Transformă în Formă Normală
        </button>
        <button onClick={handleCheck} className="bg-teal-500 text-white px-4 py-2 rounded">
          Verifică Tipul Formei
        </button>
      </div>
      {steps.length > 0 && (
        <div className="mb-4">
          <h2 className="text-xl font-semibold">Pași de Transformare:</h2>
          <ul className="list-decimal list-inside">
            {steps.map((step, index) => (
              <li key={index}>
                {step.description}: <span className="font-mono">{step.formula}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
      {normalForm && (
        <div>
          <h2 className="text-xl font-semibold">Clasificare:</h2>
          <p>Formula este: <span className="font-bold">{normalForm.typeName}</span></p>
        </div>
      )}
    </div>
  );
}

export default NormalForm;
