<script>
  /** @type {{ id: number; message: string; type: 'sukses' | 'error' }[]} */
  export let toasts = [];

  /** @type {(id: number) => void} */
  export let removeToast = () => {};
</script>

<div class="toast-container">
  {#each toasts as toast (toast.id)}
    <div class="toast toast-{toast.type}" role="alert">
      <span class="toast-icon">
        {toast.type === "sukses" ? "✅" : "❌"}
      </span>
      <span class="toast-msg">{toast.message}</span>
      <button class="toast-close" on:click={() => removeToast(toast.id)}>
        ✕
      </button>
    </div>
  {/each}
</div>

<style>
  .toast-container {
    position: fixed;
    bottom: 1.5rem;
    right: 1.5rem;
    z-index: 9999;
    display: flex;
    flex-direction: column;
    gap: 0.65rem;
    pointer-events: none;
  }

  .toast {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.9rem 1.1rem;
    border-radius: 0.85rem;
    min-width: 280px;
    max-width: 380px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.35);
    backdrop-filter: blur(12px);
    pointer-events: all;
    animation: slideIn 0.35s cubic-bezier(0.16, 1, 0.3, 1);
  }

  @keyframes slideIn {
    from {
      transform: translateX(110%);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }

  .toast-sukses {
    background: rgba(16, 185, 129, 0.15);
    border: 1px solid rgba(16, 185, 129, 0.35);
    color: #6ee7b7;
  }

  .toast-error {
    background: rgba(239, 68, 68, 0.15);
    border: 1px solid rgba(239, 68, 68, 0.35);
    color: #fca5a5;
  }

  .toast-icon {
    font-size: 1rem;
    flex-shrink: 0;
  }

  .toast-msg {
    flex: 1;
    font-size: 0.875rem;
    font-weight: 500;
    font-family: "Inter", sans-serif;
    line-height: 1.4;
  }

  .toast-close {
    background: transparent;
    border: none;
    color: inherit;
    cursor: pointer;
    opacity: 0.6;
    font-size: 0.8rem;
    padding: 0.1rem 0.25rem;
    border-radius: 0.25rem;
    transition: opacity 0.2s;
    flex-shrink: 0;
    font-family: "Inter", sans-serif;
  }

  .toast-close:hover {
    opacity: 1;
  }
</style>
