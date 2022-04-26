import "./App.css";
import { Routes, Route } from "react-router-dom";
import { Reset } from "styled-reset";

import HobbyChoicePage from "./pages/HobbyChoicePage.js";
import LevelCheckPage from "./pages/LevelCheckPage.js";
import LevelYearPage from "./pages/LevelYearPage.js";
import LevelResultPage from "./pages/LevelResultPage.js";
import SplashPage from "./pages/SplashPage.js";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import NicknamePage from "./pages/NicknamePage";

function App() {
  return (
    <div>
      <Reset />
      <Routes>
        <Route path='/' element={<SplashPage />} />
        <Route path='/login' element={<LoginPage />} />
        <Route path='/hobbychoice' element={<HobbyChoicePage />} />
        <Route path='/levelyear' element={<LevelYearPage />} />
        <Route path='/levelcheck' element={<LevelCheckPage />} />
        <Route path='/levelresult' element={<LevelResultPage />} />
        <Route path='/register' element={<RegisterPage />} />
        <Route path='/nickname' element={<NicknamePage />} />
      </Routes>
    </div>
  );
}

export default App;
