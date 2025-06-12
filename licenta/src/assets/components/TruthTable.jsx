import { useState, useRef } from 'react';
import axios from 'axios';
import { toPng } from 'html-to-image'; // înlocuit html2canvas cu html-to-image
import jsPDF from 'jspdf';
import '../../App.css';

function TruthTable() {
  const [formula, setFormula] = useState('');
  const [result, setResult] = useState(null);
  const inputRef = useRef(null);  // pentru input formula
  const tableRef = useRef(null);  // pentru tabel

  const handleSubmit = async () => {
    try {
      const response = await axios.post('http://localhost:8080/api/formula/truth-table', { formula });
      setResult(response.data);
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
      
      setTimeout(() => {
        input.focus();
        input.selectionStart = input.selectionEnd = start + symbol.length;
      }, 0);
    }
  };

  const exportPDF = async () => {
    if (tableRef.current) {
      const dataUrl = await toPng(tableRef.current);
      const pdf = new jsPDF();
      const imgProps = pdf.getImageProperties(dataUrl);
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const pdfHeight = (imgProps.height * pdfWidth) / imgProps.width;

      pdf.addImage(dataUrl, 'PNG', 10, 10, pdfWidth - 20, pdfHeight);
      pdf.save('tabel-adevar.pdf');
    }
  };

  return (
    <div className="container">
      <h1 className="text-2xl font-bold mb-4">Generare Tabel de Adevăr</h1>

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

      <input
        value={formula}
        onChange={(e) => setFormula(e.target.value)}
        ref={inputRef} // adăugat ref
        className="border p-2 rounded w-full mb-4"
        placeholder="Introduceți formula logică"
      />

      <button onClick={handleSubmit} className="bg-green-500 text-white px-4 py-2 rounded">
        Generează Tabel
      </button>

      {result && (
        <div>
          <div ref={tableRef} id="truth-table" className="overflow-auto mt-4">
            <table className="table-auto w-full border-collapse border border-gray-400">
              <thead>
                <tr>
                  {result.headers.map((header, index) => (
                    <th key={index} className="border border-blue-400 px-4 py-2">{header}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {result.table.map((row, index) => (
                  <tr key={index}>
                    {result.headers.map((header, i) => (
                      <td key={i} className="border border-blue-400 px-4 py-2">{row[header]}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <button onClick={exportPDF} className="mt-4 bg-red-500 text-white px-4 py-2 rounded">
            Exportă PDF
          </button>
        </div>
      )}
    </div>
  );
}

export default TruthTable;
