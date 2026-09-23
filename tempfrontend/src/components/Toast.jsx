export default function Toast({ toast }) {
  if (!toast) return null;

  const isSuccess = toast.type === 'success';
  const isError = toast.type === 'error';

  return (
    <div className={`ces-toast ${isSuccess ? 'toast-success' : isError ? 'toast-error' : 'toast-info'}`}>
      <span className="toast-icon">
        {isSuccess ? '✓' : isError ? '⚠' : 'ℹ'}
      </span>
      <span className="toast-text">{toast.message}</span>
    </div>
  );
}
