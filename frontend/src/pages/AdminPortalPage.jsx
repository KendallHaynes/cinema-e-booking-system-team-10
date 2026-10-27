import { useState } from 'react';

const adminSections = ['Manage Movies', 'Promotions', 'Users', 'Bookings'];

export default function AdminPortalPage({ user, onLogout }) {
  const [selectedSection, setSelectedSection] = useState('');

  return (
    <section className="admin-portal" aria-labelledby="admin-portal-heading">
      <header className="admin-portal-header">
        <div>
          <p className="registration-intro">Cinema E-Booking System</p>
          <h1 id="admin-portal-heading">Admin Portal</h1>
          <p>Signed in as {user?.name || user?.email}</p>
        </div>
        <button type="button" className="login-action-btn" onClick={onLogout}>Log out</button>
      </header>

      <h2>Admin menu</h2>
      <nav className="admin-menu" aria-label="Admin menu">
        {adminSections.map((section) => (
          <button
            key={section}
            type="button"
            className={`admin-menu-item ${selectedSection === section ? 'active' : ''}`}
            onClick={() => setSelectedSection(section)}
          >
            {section}
          </button>
        ))}
      </nav>
      <div className="admin-portal-panel" aria-live="polite">
        {selectedSection
          ? <p>{selectedSection} is a prototype section and is not connected yet.</p>
          : <p>Select an option from the admin menu.</p>}
      </div>
    </section>
  );
}
