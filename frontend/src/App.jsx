import Login from './components/Login';
import SignUp from './components/SignUp';
import { useState } from 'react';

function App() {
  const [isSignUp, setIsSignUp] = useState(false);
  
  function switchToSignUp() { 
    setIsSignUp(true); 
  }

  function switchToLogin() { 
    setIsSignUp(false); 
  }
  
  return (
    <>
      {isSignUp ? <SignUp switchToLogin={switchToLogin} /> : 
      <Login switchToSignUp={switchToSignUp} />}
    </>
  )
}

export default App

