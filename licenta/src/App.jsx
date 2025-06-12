import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './assets/components/Home';
import ValidateFormula from './assets/components/ValideFormula';
import TruthTable from './assets/components/TruthTable';
import Subformulas from './assets/components/SubFormulas';
import NormalForm from './assets/components/NormalForm';
import Navbar from './assets/components/NavBar';

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-gray-100">
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/validate" element={<ValidateFormula />} />
          <Route path="/truth-table" element={<TruthTable />} />
          <Route path="/subformulas" element={<Subformulas />} />
          <Route path="/normal-form" element={<NormalForm />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;

