import { useState } from 'react';
import { registerAccount, verifyEmail } from '../api/authApi';

const initialForm = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  promotions: false
};

export default function RegistrationPage({ onBackToHome }) {
  const [form, setForm] = useState(initialForm);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [verificationState, setVerificationState] = useState('');
  const [token] = useState(() => new URLSearchParams(window.location.search).get('verify'));

  function updateField(event) {
    const { name, value, checked, type } = event.target;
    setForm((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value
    }));
  }

  async function submitRegistration(event) {
    event.preventDefault();
    setError('');
    setMessage('');

    if (form.password !== form.confirmPassword) {
      setError('The passwords do not match.');
      return;
    }

    setSubmitting(true);
    try {
      const result = await registerAccount(form);
      setMessage(result.message);
      setForm(initialForm);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function confirmEmail() {
    setError('');
    setSubmitting(true);
    try {
      const result = await verifyEmail(token);
      setVerificationState(result.message);
      const currentUrl = new URL(window.location.href);
      currentUrl.searchParams.delete('verify');
      window.history.replaceState(null, '', `${currentUrl.pathname}${currentUrl.search}${currentUrl.hash}`);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="registration-page" aria-labelledby="registration-heading">
      <div className="registration-card">
        <button type="button" className="registration-back" onClick={onBackToHome}>
          ← Back to movies
        </button>

        {token ? (
          <div aria-live="polite">
            <h1 id="registration-heading">Email confirmation</h1>
            {verificationState && <p className="registration-success" role="status">{verificationState}</p>}
            {error && <p className="registration-error" role="alert">{error}</p>}
            {verificationState ? (
              <button type="button" className="registration-submit" onClick={onBackToHome}>
                Continue
              </button>
            ) : (
              <button type="button" className="registration-submit" onClick={confirmEmail} disabled={submitting}>
                {submitting ? 'Confirming email…' : 'Confirm email address'}
              </button>
            )}
          </div>
        ) : (
          <>
            <h1 id="registration-heading">Create your account</h1>
            <p className="registration-intro">Required fields are marked with an asterisk (*).</p>
            {message && <p className="registration-success" role="status">{message}</p>}
            {error && <p className="registration-error" role="alert">{error}</p>}

            <form className="registration-form" onSubmit={submitRegistration}>
              <label>
                First name *
                <input autoComplete="given-name" name="firstName" value={form.firstName} onChange={updateField} required maxLength="100" />
              </label>
              <label>
                Last name *
                <input autoComplete="family-name" name="lastName" value={form.lastName} onChange={updateField} required maxLength="100" />
              </label>
              <label>
                Email address *
                <input autoComplete="email" type="email" name="email" value={form.email} onChange={updateField} required maxLength="255" />
              </label>
              <label>
                Password *
                <input
                  autoComplete="new-password"
                  type="password"
                  name="password"
                  value={form.password}
                  onChange={updateField}
                  required
                  minLength="10"
                  maxLength="72"
                  pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{10,72}"
                  title="Use 10-72 characters, including an uppercase letter, a lowercase letter, a number, and a symbol."
                />
                <span className="registration-hint">At least 10 characters, including uppercase, lowercase, number, and symbol.</span>
              </label>
              <label>
                Confirm password *
                <input autoComplete="new-password" type="password" name="confirmPassword" value={form.confirmPassword} onChange={updateField} required />
              </label>
              <label className="registration-checkbox">
                <input type="checkbox" name="promotions" checked={form.promotions} onChange={updateField} />
                Email me about promotions (optional)
              </label>
              <button className="registration-submit" type="submit" disabled={submitting}>
                {submitting ? 'Creating account…' : 'Create account'}
              </button>
            </form>
          </>
        )}
      </div>
    </section>
  );
}
