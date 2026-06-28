<script>
  /** @type {any} */
  export let surat = null;

  export let show = false;
  export let type = "masuk"; // 'masuk' | 'keluar'

  /** @type {() => void} */
  export let onClose = () => {};

  function formatTanggal(tgl) {
    if (!tgl) return "-";
    try {
      return new Date(tgl).toLocaleDateString("id-ID", {
        weekday: "long",
        day: "numeric",
        month: "long",
        year: "numeric",
      });
    } catch {
      return tgl;
    }
  }
</script>

{#if show && surat}
  <div class="backdrop" on:click|self={onClose}>
    <div class="modal-box" role="dialog" aria-modal="true">
      <!-- Header -->
      <div
        class="modal-header {type === 'masuk'
          ? 'header-masuk'
          : 'header-keluar'}"
      >
        <div class="header-left">
          <span class="header-icon">{type === "masuk" ? "📥" : "📤"}</span>
          <div>
            <h2 class="modal-title">
              Detail Surat {type === "masuk" ? "Masuk" : "Keluar"}
            </h2>
            <p class="modal-sub">Informasi lengkap arsip surat</p>
          </div>
        </div>
        <button class="btn-close" on:click={onClose}>✕</button>
      </div>

      <!-- Nomor surat badge besar -->
      <div class="nomor-wrapper">
        <span
          class="nomor-badge {type === 'masuk'
            ? 'badge-masuk'
            : 'badge-keluar'}"
        >
          {surat.nomorSurat}
        </span>
      </div>

      <!-- Fields -->
      <div class="fields">
        {#if type === "masuk"}
          <div class="field-row">
            <div class="field">
              <p class="field-label">Tanggal Surat</p>
              <p class="field-value">{formatTanggal(surat.tanggalSurat)}</p>
            </div>
            <div class="field">
              <p class="field-label">Tanggal Diterima</p>
              <p class="field-value">{formatTanggal(surat.tanggalDiterima)}</p>
            </div>
          </div>

          <div class="field field-full">
            <p class="field-label">Pengirim</p>
            <p class="field-value">{surat.pengirim || "-"}</p>
          </div>
        {:else}
          <div class="field field-full">
            <p class="field-label">Tanggal Surat</p>
            <p class="field-value">{formatTanggal(surat.tanggalSurat)}</p>
          </div>

          <div class="field field-full">
            <p class="field-label">Tujuan</p>
            <p class="field-value">{surat.tujuan || "-"}</p>
          </div>
        {/if}

        <div class="field field-full">
          <p class="field-label">Perihal</p>
          <p class="field-value">{surat.perihal || "-"}</p>
        </div>

        <div class="field field-full">
          <p class="field-label">Keterangan</p>
          <p class="field-value field-keterangan">
            {surat.keterangan && surat.keterangan.trim() !== ""
              ? surat.keterangan
              : "Tidak ada keterangan."}
          </p>
        </div>
      </div>

      <!-- Footer -->
      <div class="modal-footer">
        <button class="btn-tutup" on:click={onClose}>Tutup</button>
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
    width: 100%;
    max-width: 480px;
    box-shadow: 0 25px 60px rgba(0, 0, 0, 0.5);
    overflow: hidden;
    animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  }

  @keyframes slideUp {
    from {
      transform: translateY(20px);
      opacity: 0;
    }
    to {
      transform: translateY(0);
      opacity: 1;
    }
  }

  /* Header */
  .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1.25rem 1.5rem;
  }

  .header-masuk {
    background: linear-gradient(
      135deg,
      rgba(99, 102, 241, 0.25),
      rgba(139, 92, 246, 0.15)
    );
    border-bottom: 1px solid rgba(99, 102, 241, 0.2);
  }

  .header-keluar {
    background: linear-gradient(
      135deg,
      rgba(16, 185, 129, 0.2),
      rgba(5, 150, 105, 0.12)
    );
    border-bottom: 1px solid rgba(16, 185, 129, 0.2);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 0.85rem;
  }

  .header-icon {
    font-size: 1.5rem;
  }

  .modal-title {
    font-size: 1rem;
    font-weight: 700;
    color: #f1f5f9;
    margin: 0;
  }

  .modal-sub {
    font-size: 0.73rem;
    color: #94a3b8;
    margin: 0.15rem 0 0 0;
  }

  .btn-close {
    background: transparent;
    border: none;
    color: #64748b;
    font-size: 1.1rem;
    cursor: pointer;
    transition: color 0.2s;
    padding: 0.25rem;
  }

  .btn-close:hover {
    color: #f8fafc;
  }

  /* Nomor badge */
  .nomor-wrapper {
    padding: 1.25rem 1.5rem 0.75rem;
    text-align: center;
  }

  .nomor-badge {
    display: inline-block;
    padding: 0.4rem 1.25rem;
    border-radius: 0.6rem;
    font-size: 1rem;
    font-weight: 700;
    letter-spacing: 0.03em;
  }

  .badge-masuk {
    background: rgba(99, 102, 241, 0.15);
    color: #a5b4fc;
    border: 1px solid rgba(99, 102, 241, 0.3);
  }

  .badge-keluar {
    background: rgba(16, 185, 129, 0.12);
    color: #6ee7b7;
    border: 1px solid rgba(16, 185, 129, 0.3);
  }

  /* Fields */
  .fields {
    padding: 0.75rem 1.5rem 0.5rem;
    display: flex;
    flex-direction: column;
    gap: 0.85rem;
  }

  .field-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.85rem;
  }

  .field {
    background: rgba(15, 23, 42, 0.4);
    border: 1px solid rgba(255, 255, 255, 0.05);
    border-radius: 0.75rem;
    padding: 0.75rem 1rem;
  }

  .field-full {
    grid-column: 1 / -1;
  }

  .field-label {
    font-size: 0.7rem;
    font-weight: 600;
    color: #475569;
    text-transform: uppercase;
    letter-spacing: 0.07em;
    margin: 0 0 0.35rem 0;
  }

  .field-value {
    font-size: 0.9rem;
    color: #e2e8f0;
    margin: 0;
    font-weight: 500;
    line-height: 1.4;
  }

  .field-keterangan {
    color: #94a3b8;
    font-weight: 400;
    font-style: italic;
  }

  /* Footer */
  .modal-footer {
    padding: 1rem 1.5rem 1.25rem;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid rgba(255, 255, 255, 0.05);
    margin-top: 0.5rem;
  }

  .btn-tutup {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: #cbd5e1;
    padding: 0.6rem 1.75rem;
    border-radius: 0.75rem;
    font-size: 0.9rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    font-family: "Inter", sans-serif;
  }

  .btn-tutup:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #f8fafc;
  }
</style>
