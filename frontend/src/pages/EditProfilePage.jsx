import { useEffect, useState } from 'react';
import {
  addPaymentCard,
  changePassword,
  getProfile,
  removePaymentCard,
  saveProfile
} from '../api/authApi';

const EMPTY_ADDRESS = {
  street: '',
  city: '',
  region: '',
  postalCode: '',
  country: ''
};

const EMPTY_PASSWORD = {
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
};

export default function EditProfilePage({ onBack, onSessionExpired }) {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    address: { ...EMPTY_ADDRESS }
  });
  const [cardNumber, setCardNumber] = useState('');
  const [expirationMonth, setExpirationMonth] = useState('');
  const [expirationYear, setExpirationYear] = useState('');
  const [passwordForm, setPasswordForm] = useState({ ...EMPTY_PASSWORD });
  const [loading, setLoading] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingCard, setSavingCard] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    let ignore = false;
    getProfile()
      .then((data) => {
        if (ignore) return;
        setProfile(data);
        setForm({
          firstName: data.firstName,
          lastName: data.lastName,
          email: data.email,
          address: data.address || { ...EMPTY_ADDRESS }
        });
      })
      .catch((requestError) => {
        if (ignore) return;
        setError(requestError.message);
        if (requestError.message.toLowerCase().includes('sign in')) {
          onSessionExpired();
        }
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, [onSessionExpired]);

  function updateAddress(event) {
    const { name, value } = event.target;
    setForm((previous) => ({
      ...previous,
      address: { ...previous.address, [name]: value }
    }));
  }

  async function submitProfile(event) {
    event.preventDefault();
    setMessage('');
    setError('');
    const addressValues = Object.values(form.address);
    const hasAddress = addressValues.some((value) => value.trim());
    if (hasAddress && addressValues.some((value) => !value.trim())) {
      setError('Complete every address field or leave the optional address blank.');
      return;
    }

    setSavingProfile(true);
    try {
      const updatedProfile = await saveProfile({
        firstName: form.firstName,
        lastName: form.lastName,
        address: hasAddress ? form.address : null
      });
      setProfile(updatedProfile);
      setForm((previous) => ({
        ...previous,
        firstName: updatedProfile.firstName,
        lastName: updatedProfile.lastName,
        address: updatedProfile.address || { ...EMPTY_ADDRESS }
      }));
      setMessage('Your profile was saved. A security email is sent when personal information changes.');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSavingProfile(false);
    }
  }

  async function submitCard(event) {
    event.preventDefault();
    setMessage('');
    setError('');
    setSavingCard(true);
    try {
      const card = await addPaymentCard({
        cardNumber,
        expirationMonth: Number(expirationMonth),
        expirationYear: Number(expirationYear)
      });
      setProfile((previous) => ({
        ...previous,
        paymentCards: [...previous.paymentCards, card]
      }));
      setCardNumber('');
      setExpirationMonth('');
      setExpirationYear('');
      setMessage('Payment card saved securely. Only its brand and last four digits are shown.');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSavingCard(false);
    }
  }

  async function deleteCard(cardId) {
    if (!window.confirm('Remove this saved payment card?')) return;
    setMessage('');
    setError('');
    try {
      await removePaymentCard(cardId);
      setProfile((previous) => ({
        ...previous,
        paymentCards: previous.paymentCards.filter((card) => card.cardId !== cardId)
      }));
      setMessage('Payment card removed.');
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function submitPassword(event) {
    event.preventDefault();
    setMessage('');
    setError('');
    setSavingPassword(true);
    try {
      const result = await changePassword(passwordForm);
      setPasswordForm({ ...EMPTY_PASSWORD });
      setMessage(result.message);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setSavingPassword(false);
    }
  }

  if (loading) {
    return <p className="profile-loading" role="status">Loading your profile…</p>;
  }

  if (!profile) {
    return (
      <section className="profile-page" aria-labelledby="profile-heading">
        <button type="button" className="registration-back" onClick={onBack}>← Back</button>
        <h1 id="profile-heading">My Profile</h1>
        {error && <p className="registration-error" role="alert">{error}</p>}
      </section>
    );
  }

  const cardLimitReached = profile.paymentCards.length >= 3;
  const years = Array.from({ length: 21 }, (_, index) => new Date().getFullYear() + index);

  return (
    <section className="profile-page" aria-labelledby="profile-heading">
      <button type="button" className="registration-back" onClick={onBack}>← Back to movies</button>
      <div className="profile-heading-row">
        <div>
          <h1 id="profile-heading">My Profile</h1>
          <p>Update your account details and saved payment methods.</p>
        </div>
        <span className="profile-user-label">Signed in as {profile.firstName} {profile.lastName}</span>
      </div>

      {message && <p className="registration-success" role="status">{message}</p>}
      {error && <p className="registration-error" role="alert">{error}</p>}

      <div className="profile-sections">
        <section className="profile-section" aria-labelledby="personal-info-heading">
          <h2 id="personal-info-heading">Personal information</h2>
          <form className="registration-form" onSubmit={submitProfile}>
            <label>
              First name <span className="required-indicator">(required)</span>
              <input
                type="text"
                name="firstName"
                value={form.firstName}
                onChange={(event) => setForm((previous) => ({ ...previous, firstName: event.target.value }))}
                autoComplete="given-name"
                maxLength="100"
                required
              />
            </label>
            <label>
              Last name <span className="required-indicator">(required)</span>
              <input
                type="text"
                name="lastName"
                value={form.lastName}
                onChange={(event) => setForm((previous) => ({ ...previous, lastName: event.target.value }))}
                autoComplete="family-name"
                maxLength="100"
                required
              />
            </label>
            <label>
              Email address <span className="profile-help">(cannot be changed)</span>
              <input type="email" value={form.email} readOnly aria-readonly="true" />
            </label>

            <fieldset className="profile-address-fields">
              <legend>Mailing address <span className="profile-help">(one address per account; leave blank to keep the current address)</span></legend>
              <label>
                Street
                <input name="street" value={form.address.street} onChange={updateAddress} autoComplete="street-address" maxLength="150" />
              </label>
              <label>
                City
                <input name="city" value={form.address.city} onChange={updateAddress} autoComplete="address-level2" maxLength="100" />
              </label>
              <label>
                State / Province / Region
                <input name="region" value={form.address.region} onChange={updateAddress} autoComplete="address-level1" maxLength="100" />
              </label>
              <label>
                Postal code
                <input name="postalCode" value={form.address.postalCode} onChange={updateAddress} autoComplete="postal-code" maxLength="20" />
              </label>
              <label>
                Country
                <input name="country" value={form.address.country} onChange={updateAddress} autoComplete="country-name" maxLength="100" />
              </label>
            </fieldset>

            <button className="registration-submit" type="submit" disabled={savingProfile}>
              {savingProfile ? 'Saving…' : 'Save profile'}
            </button>
          </form>
        </section>

        <section className="profile-section" aria-labelledby="payment-cards-heading">
          <h2 id="payment-cards-heading">Payment cards</h2>
          <p className="profile-help">
            Card numbers are encrypted before storage. CVV is never requested or saved. You can save up to three cards.
          </p>
          {profile.paymentCards.length > 0 ? (
            <ul className="profile-card-list">
              {profile.paymentCards.map((card) => (
                <li className="profile-saved-card" key={card.cardId}>
                  <span>
                    <strong>{card.brand}</strong>
                    <span> ending in {card.lastFour} · expires {card.expirationDate.slice(5, 7)}/{card.expirationDate.slice(0, 4)}</span>
                  </span>
                  <button type="button" className="profile-remove-button" onClick={() => deleteCard(card.cardId)}>
                    Remove
                  </button>
                </li>
              ))}
            </ul>
          ) : (
            <p className="profile-help">No payment cards are saved.</p>
          )}

          {!cardLimitReached && (
            <form className="registration-form" onSubmit={submitCard}>
              <label>
                Card number <span className="required-indicator">(required)</span>
                <input
                  type="text"
                  name="cardNumber"
                  inputMode="numeric"
                  autoComplete="cc-number"
                  value={cardNumber}
                  onChange={(event) => setCardNumber(event.target.value)}
                  minLength="13"
                  maxLength="23"
                  required
                />
              </label>
              <div className="profile-expiry-row">
                <label>
                  Expiration month <span className="required-indicator">(required)</span>
                  <select value={expirationMonth} onChange={(event) => setExpirationMonth(event.target.value)} required>
                    <option value="">Month</option>
                    {Array.from({ length: 12 }, (_, index) => (
                      <option key={index + 1} value={index + 1}>{String(index + 1).padStart(2, '0')}</option>
                    ))}
                  </select>
                </label>
                <label>
                  Expiration year <span className="required-indicator">(required)</span>
                  <select value={expirationYear} onChange={(event) => setExpirationYear(event.target.value)} required>
                    <option value="">Year</option>
                    {years.map((year) => <option key={year} value={year}>{year}</option>)}
                  </select>
                </label>
              </div>
              <button className="registration-submit" type="submit" disabled={savingCard || !profile.address}>
                {savingCard ? 'Saving…' : profile.address ? 'Save payment card' : 'Add an address before saving a card'}
              </button>
            </form>
          )}
          {cardLimitReached && <p className="profile-help">You have reached the three-card limit. Remove a card to add another.</p>}
        </section>

        <section className="profile-section" aria-labelledby="change-password-heading">
          <h2 id="change-password-heading">Change password</h2>
          <p className="profile-help">For your security, enter your current password before choosing a new one.</p>
          <form className="registration-form" onSubmit={submitPassword}>
            <label>
              Current password <span className="required-indicator">(required)</span>
              <input
                type="password"
                autoComplete="current-password"
                value={passwordForm.currentPassword}
                onChange={(event) => setPasswordForm((previous) => ({ ...previous, currentPassword: event.target.value }))}
                required
              />
            </label>
            <label>
              New password <span className="required-indicator">(required)</span>
              <input
                type="password"
                autoComplete="new-password"
                minLength="10"
                maxLength="72"
                value={passwordForm.newPassword}
                onChange={(event) => setPasswordForm((previous) => ({ ...previous, newPassword: event.target.value }))}
                required
              />
            </label>
            <label>
              Confirm new password <span className="required-indicator">(required)</span>
              <input
                type="password"
                autoComplete="new-password"
                value={passwordForm.confirmPassword}
                onChange={(event) => setPasswordForm((previous) => ({ ...previous, confirmPassword: event.target.value }))}
                required
              />
            </label>
            <p className="profile-help">Use 10–72 characters with uppercase and lowercase letters, a number, and a symbol.</p>
            <button className="registration-submit" type="submit" disabled={savingPassword}>
              {savingPassword ? 'Updating…' : 'Update password'}
            </button>
          </form>
        </section>
      </div>
    </section>
  );
}
