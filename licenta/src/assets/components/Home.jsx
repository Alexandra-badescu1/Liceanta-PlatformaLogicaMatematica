import '../../App';

function Home() {
  return (
    <div className="p-8 text-justify">
      <h1 className="text-4xl font-bold mb-4 text-center">Bine ați venit!</h1>
      <p className="mb-4">
        Această aplicație vă oferă un set de instrumente pentru analiza și manipularea formulelor logice.
        Prin utilizarea funcționalităților de validare, generare a tabelelor de adevăr, extragere de subformule
        și transformare în forme normale, veți putea înțelege mai bine structura logică a enunțurilor.
      </p>
      <p className="mb-4">
        Navigați folosind meniul de sus pentru a accesa fiecare funcționalitate. 
        Interfața prietenoasă și suportul pentru modul întunecat fac experiența de învățare mai confortabilă.
      </p>
      <p className="mb-4 italic text-center">
        „Logica este arhitectura gândirii clare.” — Alfred North Whitehead
      </p>
    </div>
  );
}

export default Home;
