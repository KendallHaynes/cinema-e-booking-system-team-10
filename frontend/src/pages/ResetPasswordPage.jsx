import { useState } from 'react';
import { resetPassword } from '../api/authApi';

export default function ResetPasswordPage({ token, onBackToLogin }) {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function submitReset(event) {
    event.preventDefault();
    setError('');
    setMessage('');
    if (password !== confirmPassword) {
      setError('The passwords do not match.');
      return;
    }

    setSubmitting(true);
    try {
      const result = await resetPassword({ token, password, confirmPassword });
      setMessage(result.message);
      setPassword('');
      setConfirmPassword('');
      const currentUrl = new URL(window.location.href);
      currentUrl.searchParams.delete('reset');
      window.history.replaceState(null, '', `${currentUrl.pathname}${currentUrl.search}${currentUrl.hash}`);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="registration-page" aria-labelledby="reset-password-heading">
      <div className="registration-card">
        <button type="button" className="registration-back" onClick={onBackToLogin}>
          ← Back to sign in
        </button>
        <h1 id="reset-password-heading">Choose a new password</h1>
        {!token && <p className="registration-error" role="alert">This reset link is missing or invalid.</p>}
        {message && <p className="registration-success" role="status">{message}</p>}
        {error && <p className="registration-error" role="alert">{error}</p>}

        {token && !message && (
          <form className="registration-form" onSubmit={submitReset}>
            <label>
              New password
              <input
                autoComplete="new-password"
                type="password"
                name="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
                minLength="10"
                maxLength="72"
                pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{10,72}"
                title="Use 10-72 characters, including an uppercase letter, a lowercase letter, a number, and a symbol."
              />
            </label>
            <label>
              Confirm new password
              <input
                autoComplete="new-password"
                type="password"
                name="confirmPassword"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                required
              />
            </label>
            <p className="registration-hint">Use at least 10 characters, including uppercase, lowercase, a number, and a symbol.</p>
            <button className="registration-submit" type="submit" disabled={submitting}>
              {submitting ? 'Updating password…' : 'Update password'}
            </button>
          </form>
        )}
      </div>
    </section>
  );
}
