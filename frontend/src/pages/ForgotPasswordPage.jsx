import { useState } from 'react';
import { requestPasswordReset } from '../api/authApi';

export default function ForgotPasswordPage({ onBackToLogin }) {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function submitRequest(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    setSubmitting(true);
    try {
      const result = await requestPasswordReset(email);
      setMessage(result.message);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="registration-page" aria-labelledby="forgot-password-heading">
      <div className="registration-card">
        <button type="button" className="registration-back" onClick={onBackToLogin}>
          ← Back to sign in
        </button>
        <h1 id="forgot-password-heading">Reset your password</h1>
        <p className="registration-intro">
          Enter your account email. If an account exists, we’ll send a reset link.
        </p>
        {message && <p className="registration-success" role="status">{message}</p>}
        {error && <p className="registration-error" role="alert">{error}</p>}

        <form className="registration-form" onSubmit={submitRequest}>
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
          <button className="registration-submit" type="submit" disabled={submitting}>
            {submitting ? 'Sending…' : 'Send reset link'}
          </button>
        </form>
      </div>
    </section>
  );
}
