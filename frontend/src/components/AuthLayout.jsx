import "./AuthLayout.css";

export default function AuthLayout({ children }) {
  return (
    <div className="auth-container">
      <div className="background-waves">
        <div className="wave wave-blue"></div>
        <div className="wave wave-pink"></div>
      </div>

      <div className="auth-wrapper">
        <button className="expensphie-btn" type="button">
          Expensphie
        </button>

        <h1 className="app-title">Expense Manage App</h1>

        {children}
      </div>
    </div>
  );
}

