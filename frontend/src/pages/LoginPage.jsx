import { useState } from 'react';
import { loginAccount } from '../api/authApi';

export default function LoginPage({ onLogin, onForgotPassword, onRegister }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function submitLogin(event) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const user = await loginAccount({ email, password });
      await onLogin(user);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="registration-page" aria-labelledby="login-heading">
      <div className="registration-card">
        <button type="button" className="registration-back" onClick={onRegister}>
          Create an account
        </button>
        <h1 id="login-heading">Sign in</h1>
        <p className="registration-intro">Sign in to continue to Cinema E-Booking.</p>
        {error && <p className="registration-error" role="alert">{error}</p>}

        <form className="registration-form" onSubmit={submitLogin}>
          <label>
            Email address
            <input
              autoComplete="email"
              type="email"
              name="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              maxLength="255"
            />
          </label>
          <label>
            Password
            <input
              autoComplete="current-password"
              type="password"
              name="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>
          <button className="registration-submit" type="submit" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <button type="button" className="auth-text-button" onClick={onForgotPassword}>
          Forgot my password
        </button>
      </div>
    </section>
  );
}
