<script>
  export let show = false;
  export let nomorSurat = "";

  /** @type {() => void} */
  export let onConfirm = () => {};

  /** @type {() => void} */
  export let onCancel = () => {};
</script>

{#if show}
  <div class="backdrop" on:click|self={onCancel}>
    <div class="modal-box" role="dialog" aria-modal="true">
      <div class="icon-wrapper">
        <span class="icon">🗑️</span>
      </div>

      <h2 class="title">Hapus Surat?</h2>
      <p class="desc">Kamu akan menghapus surat dengan nomor</p>
      <p class="nomor">{nomorSurat}</p>
      <p class="warning">Tindakan ini tidak dapat dibatalkan.</p>

      <div class="actions">
        <button class="btn-batal" on:click={onCancel}>Batal</button>
        <button class="btn-hapus" on:click={onConfirm}>Ya, Hapus</button>
      </div>
    </div>
  </div>
{/if}

<style>
  .backdrop {
    position: fixed;
    inset: 0;
    background: rgba(15, 23, 42, 0.85);
    backdrop-filter: blur(6px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
    padding: 1rem;
  }

  .modal-box {
    background: #1e293b;
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 1.25rem;
    padding: 2rem 2rem 1.75rem;
    width: 100%;
    max-width: 380px;
    text-align: center;
    box-shadow: 0 25px 60px rgba(0, 0, 0, 0.5);
    animation: popIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  }

  @keyframes popIn {
    from {
      transform: scale(0.9);
      opacity: 0;
    }
    to {
      transform: scale(1);
      opacity: 1;
    }
  }

  .icon-wrapper {
    width: 64px;
    height: 64px;
    background: rgba(239, 68, 68, 0.12);
    border: 1px solid rgba(239, 68, 68, 0.25);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 1.25rem auto;
  }

  .icon {
    font-size: 1.75rem;
  }

  .title {
    font-size: 1.2rem;
    font-weight: 700;
    color: #f1f5f9;
    margin: 0 0 0.6rem 0;
  }

  .desc {
    font-size: 0.875rem;
    color: #94a3b8;
    margin: 0 0 0.35rem 0;
  }

  .nomor {
    font-size: 0.95rem;
    font-weight: 700;
    color: #fca5a5;
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid rgba(239, 68, 68, 0.2);
    border-radius: 0.5rem;
    padding: 0.3rem 0.9rem;
    display: inline-block;
    margin: 0 0 0.6rem 0;
  }

  .warning {
    font-size: 0.78rem;
    color: #64748b;
    margin: 0 0 1.75rem 0;
  }

  .actions {
    display: flex;
    gap: 0.75rem;
    justify-content: center;
  }

  .btn-batal {
    flex: 1;
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: #cbd5e1;
    padding: 0.65rem 1rem;
    border-radius: 0.75rem;
    font-size: 0.9rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    font-family: "Inter", sans-serif;
  }

  .btn-batal:hover {
    background: rgba(255, 255, 255, 0.05);
    color: #f8fafc;
  }

  .btn-hapus {
    flex: 1;
    background: linear-gradient(135deg, #ef4444, #dc2626);
    border: none;
    color: white;
    padding: 0.65rem 1rem;
    border-radius: 0.75rem;
    font-size: 0.9rem;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
    font-family: "Inter", sans-serif;
  }

  .btn-hapus:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4);
    filter: brightness(1.1);
  }
</style>
