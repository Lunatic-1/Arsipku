<script>
  import "./app.css";
  import { onMount } from "svelte";
  import Toast from "./lib/Toast.svelte";
  import ModalKonfirmasi from "./lib/ModalKonfirmasi.svelte";
  import ModalDetail from "./lib/ModalDetail.svelte";

  /** @type {any[]} */
  let dataSuratMasuk = [];
  /** @type {any[]} */
  let dataSuratKeluar = [];

  let activeTab = "masuk";
  let showModal = false;
  let isLoading = false;
  let isEditMode = false;

  let searchQuery = "";
  let sortOrder = "DESC";

  // ===== TOAST =====
  /** @type {{ id: number; message: string; type: 'sukses' | 'error' }[]} */
  let toasts = [];

  function addToast(message, type = "sukses") {
    const id = Date.now();
    toasts = [...toasts, { id, message, type }];
    setTimeout(() => removeToast(id), 3500);
  }

  function removeToast(id) {
    toasts = toasts.filter((t) => t.id !== id);
  }

  // ===== MODAL KONFIRMASI HAPUS =====
  let showKonfirmasi = false;
  let konfirmasiNomor = "";
  let konfirmasiCallback = null;

  function bukaKonfirmasi(nomorSurat, callback) {
    konfirmasiNomor = nomorSurat;
    konfirmasiCallback = callback;
    showKonfirmasi = true;
  }

  function konfirmasiHapus() {
    if (konfirmasiCallback) konfirmasiCallback();
    showKonfirmasi = false;
    konfirmasiNomor = "";
    konfirmasiCallback = null;
  }

  function batalKonfirmasi() {
    showKonfirmasi = false;
    konfirmasiNomor = "";
    konfirmasiCallback = null;
  }

  // ===== MODAL DETAIL =====
  let showDetail = false;
  let detailSurat = null;
  let detailType = "masuk";

  function handleLihatDetail(surat) {
    detailSurat = surat;
    detailType = activeTab;
    showDetail = true;
  }

  // ===== FORM DATA =====
  let formDataMasuk = {
    nomorSurat: "",
    tanggalSurat: "",
    tanggalDiterima: "",
    pengirim: "",
    perihal: "",
    keterangan: "",
  };
  let formDataKeluar = {
    nomorSurat: "",
    tanggalSurat: "",
    tujuan: "",
    perihal: "",
    keterangan: "",
  };

  // ===== FETCH DATA =====
  async function fetchSuratMasuk() {
    isLoading = true;
    try {
      const response = await fetch(
        `http://localhost:8000/api/surat-masuk?sort=${sortOrder}`,
      );
      dataSuratMasuk = await response.json();
    } catch (error) {
      console.error("Gagal load surat masuk", error);
    } finally {
      isLoading = false;
    }
  }

  async function fetchSuratKeluar() {
    isLoading = true;
    try {
      const response = await fetch(
        `http://localhost:8000/api/surat-keluar?sort=${sortOrder}`,
      );
      dataSuratKeluar = await response.json();
    } catch (error) {
      console.error("Gagal load surat keluar", error);
    } finally {
      isLoading = false;
    }
  }

  onMount(() => {
    fetchSuratMasuk();
    fetchSuratKeluar();
  });

  // ===== FILTER =====
  $: filteredSuratMasuk = dataSuratMasuk.filter(
    (surat) =>
      surat.nomorSurat.toLowerCase().includes(searchQuery.toLowerCase()) ||
      surat.pengirim.toLowerCase().includes(searchQuery.toLowerCase()) ||
      surat.perihal.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  $: filteredSuratKeluar = dataSuratKeluar.filter(
    (surat) =>
      surat.nomorSurat.toLowerCase().includes(searchQuery.toLowerCase()) ||
      surat.tujuan.toLowerCase().includes(searchQuery.toLowerCase()) ||
      surat.perihal.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  // ===== SUBMIT =====
  async function handleSubmit() {
    const isMasuk = activeTab === "masuk";

    let url = "";
    if (isMasuk) {
      url = isEditMode
        ? "http://localhost:8000/api/surat-masuk/update"
        : "http://localhost:8000/api/surat-masuk/tambah";
    } else {
      url = isEditMode
        ? "http://localhost:8000/api/surat-keluar/update"
        : "http://localhost:8000/api/surat-keluar/tambah";
    }

    const dataYangDikirim = isMasuk ? formDataMasuk : formDataKeluar;

    try {
      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dataYangDikirim),
      });

      const result = await response.json();
      if (result.status === "sukses") {
        addToast(result.pesan, "sukses");
        showModal = false;
        isEditMode = false;
        if (isMasuk) {
          formDataMasuk = {
            nomorSurat: "",
            tanggalSurat: "",
            tanggalDiterima: "",
            pengirim: "",
            perihal: "",
            keterangan: "",
          };
          fetchSuratMasuk();
        } else {
          formDataKeluar = {
            nomorSurat: "",
            tanggalSurat: "",
            tujuan: "",
            perihal: "",
            keterangan: "",
          };
          fetchSuratKeluar();
        }
      } else {
        addToast("Error: " + result.pesan, "error");
      }
    } catch (error) {
      addToast("Gagal terhubung ke server Java!", "error");
    }
  }

  // ===== EDIT =====
  function handleEdit(surat) {
    isEditMode = true;
    showModal = true;
    if (activeTab === "masuk") {
      formDataMasuk = { ...surat };
    } else {
      formDataKeluar = { ...surat };
    }
  }

  // ===== HAPUS =====
  function handleHapus(nomorSurat) {
    bukaKonfirmasi(nomorSurat, async () => {
      const isMasuk = activeTab === "masuk";
      const url = isMasuk
        ? "http://localhost:8000/api/surat-masuk/hapus"
        : "http://localhost:8000/api/surat-keluar/hapus";

      try {
        const response = await fetch(url, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ nomorSurat }),
        });

        const result = await response.json();
        if (result.status === "sukses") {
          addToast("Surat berhasil dihapus.", "sukses");
          isMasuk ? fetchSuratMasuk() : fetchSuratKeluar();
        } else {
          addToast("Error: " + result.pesan, "error");
        }
      } catch (error) {
        addToast("Gagal menghapus data dari server!", "error");
      }
    });
  }

  function handleCetak() {
    window.print();
  }

  function toggleSort() {
    sortOrder = sortOrder === "DESC" ? "ASC" : "DESC";
    if (activeTab === "masuk") fetchSuratMasuk();
    else fetchSuratKeluar();
  }
</script>

<!-- PRINT HEADER -->
<div class="hidden print:block print-header">
  <h1>LAPORAN ARSIP SURAT {activeTab === "masuk" ? "MASUK" : "KELUAR"}</h1>
  <p>
    Dicetak oleh Sistem Arsipku &mdash;
    {new Date().toLocaleDateString("id-ID", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    })}
  </p>
</div>

<!-- APP SHELL -->
<div class="app-shell print:hidden">
  <aside class="sidebar">
    <div class="sidebar-brand">
      <div class="brand-icon">📂</div>
      <div>
        <h1 class="brand-title">Arsipku</h1>
        <p class="brand-sub">Manajemen Surat</p>
      </div>
    </div>

    <nav class="sidebar-nav">
      <button
        class="nav-item {activeTab === 'masuk' ? 'nav-active-masuk' : ''}"
        on:click={() => {
          activeTab = "masuk";
          searchQuery = "";
          sortOrder = "DESC";
          fetchSuratMasuk();
        }}
      >
        <span class="nav-icon">📥</span>
        <span>Surat Masuk</span>
        <span class="nav-badge badge-blue">{dataSuratMasuk.length}</span>
      </button>

      <button
        class="nav-item {activeTab === 'keluar' ? 'nav-active-keluar' : ''}"
        on:click={() => {
          activeTab = "keluar";
          searchQuery = "";
          sortOrder = "DESC";
          fetchSuratKeluar();
        }}
      >
        <span class="nav-icon">📤</span>
        <span>Surat Keluar</span>
        <span class="nav-badge badge-emerald">{dataSuratKeluar.length}</span>
      </button>
    </nav>

    <div class="sidebar-footer">
      <p>© 2025 Arsipku</p>
    </div>
  </aside>

  <main class="main-content">
    <header class="topbar">
      <div class="topbar-left">
        <h2 class="page-title">
          {activeTab === "masuk" ? "📥 Surat Masuk" : "📤 Surat Keluar"}
        </h2>
        <p class="page-sub">
          {activeTab === "masuk"
            ? filteredSuratMasuk.length
            : filteredSuratKeluar.length} data ditemukan
        </p>
      </div>
      <button
        class="btn-tambah"
        on:click={() => {
          isEditMode = false;
          formDataMasuk = {
            nomorSurat: "",
            tanggalSurat: "",
            tanggalDiterima: "",
            pengirim: "",
            perihal: "",
            keterangan: "",
          };
          formDataKeluar = {
            nomorSurat: "",
            tanggalSurat: "",
            tujuan: "",
            perihal: "",
            keterangan: "",
          };
          showModal = true;
        }}
      >
        <span>＋</span> Tambah Surat
      </button>
    </header>

    <div class="stat-grid">
      <div class="stat-card stat-masuk">
        <div class="stat-icon">📥</div>
        <div>
          <p class="stat-label">Total Surat Masuk</p>
          <p class="stat-value">{dataSuratMasuk.length}</p>
        </div>
      </div>
      <div class="stat-card stat-keluar">
        <div class="stat-icon">📤</div>
        <div>
          <p class="stat-label">Total Surat Keluar</p>
          <p class="stat-value">{dataSuratKeluar.length}</p>
        </div>
      </div>
      <div class="stat-card stat-total">
        <div class="stat-icon">📋</div>
        <div>
          <p class="stat-label">Total Semua Surat</p>
          <p class="stat-value">
            {dataSuratMasuk.length + dataSuratKeluar.length}
          </p>
        </div>
      </div>
    </div>

    <div class="toolbar">
      <div class="search-wrapper">
        <span class="search-icon">🔍</span>
        <input
          type="text"
          bind:value={searchQuery}
          placeholder="Cari nomor, pengirim, perihal..."
          class="search-input"
        />
        {#if searchQuery}
          <button class="search-clear" on:click={() => (searchQuery = "")}
            >✕</button
          >
        {/if}
      </div>
      <div class="toolbar-actions">
        <button
          class="btn-sort {sortOrder === 'ASC' ? 'btn-sort-asc' : ''}"
          on:click={toggleSort}
        >
          {sortOrder === "DESC" ? "↓ Terbaru" : "↑ Terlama"}
        </button>
        <button class="btn-print" on:click={handleCetak}>🖨️ Cetak PDF</button>
      </div>
    </div>

    <div class="table-card">
      {#if isLoading}
        <div class="empty-state">
          <div class="loader"></div>
          <p>Memuat data...</p>
        </div>
      {:else if activeTab === "masuk"}
        {#if filteredSuratMasuk.length === 0}
          <div class="empty-state">
            <div class="empty-icon">📭</div>
            <p class="empty-title">Tidak ada data</p>
            <p class="empty-sub">Belum ada surat masuk yang tersimpan.</p>
          </div>
        {:else}
          <div class="table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th class="th-no">#</th>
                  <th>No. Surat</th>
                  <th>Tgl. Surat</th>
                  <th>Pengirim</th>
                  <th>Perihal</th>
                  <th class="th-aksi">Aksi</th>
                </tr>
              </thead>
              <tbody>
                {#each filteredSuratMasuk as surat, i}
                  <tr class="table-row">
                    <td class="td-no">{i + 1}</td>
                    <td
                      ><span class="badge-nomor badge-blue-soft"
                        >{surat.nomorSurat}</span
                      ></td
                    >
                    <td class="td-date">{surat.tanggalSurat}</td>
                    <td class="td-name">{surat.pengirim}</td>
                    <td class="td-perihal">{surat.perihal}</td>
                    <td class="td-aksi">
                      <div class="aksi-flex">
                        <button
                          class="btn-detail"
                          on:click={() => handleLihatDetail(surat)}
                          >👁 Detail</button
                        >
                        <button
                          class="btn-edit"
                          on:click={() => handleEdit(surat)}>✏️ Edit</button
                        >
                        <button
                          class="btn-hapus"
                          on:click={() => handleHapus(surat.nomorSurat)}
                          >🗑 Hapus</button
                        >
                      </div>
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        {/if}
      {:else if filteredSuratKeluar.length === 0}
        <div class="empty-state">
          <div class="empty-icon">📭</div>
          <p class="empty-title">Tidak ada data</p>
          <p class="empty-sub">Belum ada surat keluar yang tersimpan.</p>
        </div>
      {:else}
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th class="th-no">#</th>
                <th>No. Surat</th>
                <th>Tgl. Surat</th>
                <th>Tujuan</th>
                <th>Perihal</th>
                <th class="th-aksi">Aksi</th>
              </tr>
            </thead>
            <tbody>
              {#each filteredSuratKeluar as surat, i}
                <tr class="table-row">
                  <td class="td-no">{i + 1}</td>
                  <td
                    ><span class="badge-nomor badge-emerald-soft"
                      >{surat.nomorSurat}</span
                    ></td
                  >
                  <td class="td-date">{surat.tanggalSurat}</td>
                  <td class="td-name">{surat.tujuan}</td>
                  <td class="td-perihal">{surat.perihal}</td>
                  <td class="td-aksi">
                    <div class="aksi-flex">
                      <button
                        class="btn-detail"
                        on:click={() => handleLihatDetail(surat)}
                        >👁 Detail</button
                      >
                      <button
                        class="btn-edit"
                        on:click={() => handleEdit(surat)}>✏️ Edit</button
                      >
                      <button
                        class="btn-hapus"
                        on:click={() => handleHapus(surat.nomorSurat)}
                        >🗑 Hapus</button
                      >
                    </div>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </div>
  </main>
</div>

<!-- PRINT TABLE -->
<div class="hidden print:block">
  <table class="print-table">
    <thead>
      <tr>
        <th>#</th>
        <th>No. Surat</th>
        <th>Tanggal</th>
        <th>{activeTab === "masuk" ? "Pengirim" : "Tujuan"}</th>
        <th>Perihal</th>
      </tr>
    </thead>
    <tbody>
      {#each activeTab === "masuk" ? filteredSuratMasuk : filteredSuratKeluar as surat, i}
        <tr>
          <td>{i + 1}</td>
          <td>{surat.nomorSurat}</td>
          <td>{surat.tanggalSurat}</td>
          <td>{activeTab === "masuk" ? surat.pengirim : surat.tujuan}</td>
          <td>{surat.perihal}</td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>

<!-- MODAL TAMBAH / EDIT -->
{#if showModal}
  <div class="modal-backdrop" on:click|self={() => (showModal = false)}>
    <div class="modal-box">
      <div
        class="modal-header {activeTab === 'masuk'
          ? 'modal-header-masuk'
          : 'modal-header-keluar'}"
      >
        <div>
          <h2 class="modal-title">
            {activeTab === "masuk"
              ? isEditMode
                ? "📥 Ubah Surat Masuk"
                : "📥 Tambah Surat Masuk"
              : isEditMode
                ? "📤 Ubah Surat Keluar"
                : "📤 Tambah Surat Keluar"}
          </h2>
          <p class="modal-sub">Isi form di bawah dengan data yang benar</p>
        </div>
        <button class="modal-close" on:click={() => (showModal = false)}
          >✕</button
        >
      </div>

      <form on:submit|preventDefault={handleSubmit} class="modal-form">
        {#if activeTab === "masuk"}
          <div class="form-group">
            <label class="form-label"
              >Nomor Surat <span class="required">*</span></label
            >
            <input
              type="text"
              bind:value={formDataMasuk.nomorSurat}
              required
              disabled={isEditMode}
              class="form-input {isEditMode ? 'disabled-input' : ''}"
              placeholder="Contoh: 001/SM/VI/2025"
            />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label"
                >Tanggal Surat <span class="required">*</span></label
              >
              <input
                type="date"
                bind:value={formDataMasuk.tanggalSurat}
                required
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label class="form-label"
                >Tgl. Diterima <span class="required">*</span></label
              >
              <input
                type="date"
                bind:value={formDataMasuk.tanggalDiterima}
                required
                class="form-input"
              />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label"
              >Pengirim <span class="required">*</span></label
            >
            <input
              type="text"
              bind:value={formDataMasuk.pengirim}
              required
              class="form-input"
              placeholder="Nama instansi atau perorangan"
            />
          </div>
          <div class="form-group">
            <label class="form-label"
              >Perihal <span class="required">*</span></label
            >
            <input
              type="text"
              bind:value={formDataMasuk.perihal}
              required
              class="form-input"
              placeholder="Ringkasan isi surat"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Keterangan</label>
            <textarea
              bind:value={formDataMasuk.keterangan}
              class="form-input form-textarea"
              rows="2"
              placeholder="Catatan tambahan (opsional)"
            ></textarea>
          </div>
        {:else}
          <div class="form-group">
            <label class="form-label"
              >Nomor Surat <span class="required">*</span></label
            >
            <input
              type="text"
              bind:value={formDataKeluar.nomorSurat}
              required
              disabled={isEditMode}
              class="form-input {isEditMode ? 'disabled-input' : ''}"
              placeholder="Contoh: 001/SK/VI/2025"
            />
          </div>
          <div class="form-group">
            <label class="form-label"
              >Tanggal Surat <span class="required">*</span></label
            >
            <input
              type="date"
              bind:value={formDataKeluar.tanggalSurat}
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label class="form-label"
              >Tujuan <span class="required">*</span></label
            >
            <input
              type="text"
              bind:value={formDataKeluar.tujuan}
              required
              class="form-input"
              placeholder="Nama instansi atau perorangan tujuan"
            />
          </div>
          <div class="form-group">
            <label class="form-label"
              >Perihal <span class="required">*</span></label
            >
            <input
              type="text"
              bind:value={formDataKeluar.perihal}
              required
              class="form-input"
              placeholder="Ringkasan isi surat"
            />
          </div>
          <div class="form-group">
            <label class="form-label">Keterangan</label>
            <textarea
              bind:value={formDataKeluar.keterangan}
              class="form-input form-textarea"
              rows="2"
              placeholder="Catatan tambahan (opsional)"
            ></textarea>
          </div>
        {/if}

        <div class="modal-footer">
          <button
            type="button"
            class="btn-batal"
            on:click={() => (showModal = false)}>Batal</button
          >
          <button
            type="submit"
            class="btn-simpan {activeTab === 'masuk'
              ? 'btn-simpan-masuk'
              : 'btn-simpan-keluar'}"
          >
            {isEditMode ? "Simpan Perubahan" : "Simpan Data"}
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}

<!-- KOMPONEN BARU -->
<Toast {toasts} {removeToast} />

<ModalKonfirmasi
  show={showKonfirmasi}
  nomorSurat={konfirmasiNomor}
  onConfirm={konfirmasiHapus}
  onCancel={batalKonfirmasi}
/>

<ModalDetail
  show={showDetail}
  surat={detailSurat}
  type={detailType}
  onClose={() => (showDetail = false)}
/>

<style>
  /* ===== AKSI ===== */
  .aksi-flex {
    display: flex;
    gap: 0.4rem;
    justify-content: flex-end;
  }

  .btn-detail {
    background: rgba(100, 116, 139, 0.15);
    color: #94a3b8;
    border: 1px solid rgba(100, 116, 139, 0.25);
    padding: 0.4rem 0.65rem;
    border-radius: 0.5rem;
    cursor: pointer;
    font-size: 0.78rem;
    font-weight: 600;
    transition: all 0.2s;
    white-space: nowrap;
  }
  .btn-detail:hover {
    background: rgba(100, 116, 139, 0.25);
    color: #cbd5e1;
  }

  .btn-edit {
    background: rgba(59, 130, 246, 0.15);
    color: #93c5fd;
    border: 1px solid rgba(59, 130, 246, 0.3);
    padding: 0.4rem 0.65rem;
    border-radius: 0.5rem;
    cursor: pointer;
    font-size: 0.78rem;
    font-weight: 600;
    transition: all 0.2s;
    white-space: nowrap;
  }
  .btn-edit:hover {
    background: rgba(59, 130, 246, 0.25);
    color: #bfdbfe;
  }

  .btn-hapus {
    background: rgba(239, 68, 68, 0.15);
    color: #fca5a5;
    border: 1px solid rgba(239, 68, 68, 0.3);
    padding: 0.4rem 0.65rem;
    border-radius: 0.5rem;
    cursor: pointer;
    font-size: 0.78rem;
    font-weight: 600;
    transition: all 0.2s;
    white-space: nowrap;
  }
  .btn-hapus:hover {
    background: rgba(239, 68, 68, 0.25);
    color: #fecaca;
  }

  .disabled-input {
    background: rgba(0, 0, 0, 0.2) !important;
    opacity: 0.6;
    cursor: not-allowed;
  }

  /* ===== BASE ===== */
  :global(body) {
    margin: 0;
    font-family: "Inter", sans-serif;
    background-color: #0f172a;
    color: #f1f5f9;
  }

  .app-shell {
    display: flex;
    height: 100vh;
    overflow: hidden;
  }

  /* ===== SIDEBAR ===== */
  .sidebar {
    width: 260px;
    background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
    border-right: 1px solid rgba(255, 255, 255, 0.05);
    display: flex;
    flex-direction: column;
    padding: 1.5rem;
    flex-shrink: 0;
  }

  .sidebar-brand {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 2.5rem;
  }

  .brand-icon {
    font-size: 2rem;
  }

  .brand-title {
    font-size: 1.25rem;
    font-weight: 800;
    background: linear-gradient(to right, #818cf8, #c084fc);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin: 0;
  }

  .brand-sub {
    margin: 0;
    font-size: 0.7rem;
    color: #64748b;
    letter-spacing: 0.05em;
    text-transform: uppercase;
  }

  .sidebar-nav {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    flex: 1;
  }

  .nav-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 1rem;
    border-radius: 0.75rem;
    border: none;
    cursor: pointer;
    font-size: 0.9rem;
    font-weight: 500;
    color: #94a3b8;
    background: transparent;
    transition: all 0.2s ease;
    text-align: left;
    width: 100%;
  }
  .nav-item:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #e2e8f0;
  }

  .nav-active-masuk {
    background: rgba(99, 102, 241, 0.2) !important;
    color: #a5b4fc !important;
    border: 1px solid rgba(99, 102, 241, 0.3);
    box-shadow: 0 0 20px rgba(99, 102, 241, 0.1);
  }

  .nav-active-keluar {
    background: rgba(16, 185, 129, 0.15) !important;
    color: #6ee7b7 !important;
    border: 1px solid rgba(16, 185, 129, 0.3);
    box-shadow: 0 0 20px rgba(16, 185, 129, 0.1);
  }

  .nav-icon {
    font-size: 1.1rem;
  }

  .nav-badge {
    margin-left: auto;
    font-size: 0.7rem;
    font-weight: 700;
    padding: 0.15rem 0.5rem;
    border-radius: 9999px;
  }

  .badge-blue {
    background: rgba(99, 102, 241, 0.2);
    color: #a5b4fc;
  }
  .badge-emerald {
    background: rgba(16, 185, 129, 0.2);
    color: #6ee7b7;
  }

  .sidebar-footer {
    padding-top: 1.5rem;
    border-top: 1px solid rgba(255, 255, 255, 0.05);
    font-size: 0.75rem;
    color: #475569;
    text-align: center;
  }

  /* ===== MAIN CONTENT ===== */
  .main-content {
    flex: 1;
    padding: 2rem 3rem;
    display: flex;
    flex-direction: column;
    gap: 2rem;
    height: 100vh;
    overflow: hidden;
    min-width: 0;
  }

  /* ===== TOPBAR ===== */
  .topbar {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
  }

  .page-title {
    font-size: 1.75rem;
    font-weight: 700;
    margin: 0 0 0.25rem 0;
    color: #f8fafc;
  }

  .page-sub {
    margin: 0;
    color: #94a3b8;
    font-size: 0.9rem;
  }

  .btn-tambah {
    background: linear-gradient(135deg, #4f46e5, #7c3aed);
    color: white;
    border: none;
    padding: 0.75rem 1.25rem;
    border-radius: 0.75rem;
    font-weight: 600;
    font-size: 0.9rem;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 4px 15px rgba(79, 70, 229, 0.3);
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  .btn-tambah:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(79, 70, 229, 0.4);
  }

  /* ===== STAT CARDS ===== */
  .stat-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
  }

  .stat-card {
    background: rgba(30, 41, 59, 0.7);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.05);
    padding: 1.5rem;
    border-radius: 1rem;
    display: flex;
    align-items: center;
    gap: 1.25rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  }

  .stat-masuk {
    background: linear-gradient(
      135deg,
      rgba(99, 102, 241, 0.15),
      rgba(79, 70, 229, 0.1)
    );
    border-color: rgba(99, 102, 241, 0.25);
  }
  .stat-keluar {
    background: linear-gradient(
      135deg,
      rgba(16, 185, 129, 0.15),
      rgba(5, 150, 105, 0.1)
    );
    border-color: rgba(16, 185, 129, 0.25);
  }
  .stat-total {
    background: linear-gradient(
      135deg,
      rgba(245, 158, 11, 0.15),
      rgba(217, 119, 6, 0.1)
    );
    border-color: rgba(245, 158, 11, 0.25);
  }

  .stat-icon {
    font-size: 2rem;
  }
  .stat-label {
    font-size: 0.75rem;
    color: #64748b;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    font-weight: 600;
    margin: 0 0 0.2rem 0;
  }
  .stat-value {
    font-size: 2rem;
    font-weight: 800;
    color: #f1f5f9;
    line-height: 1.1;
    margin: 0;
  }

  /* ===== TOOLBAR ===== */
  .toolbar {
    display: flex;
    gap: 1rem;
    align-items: center;
    flex-wrap: wrap;
  }

  .search-wrapper {
    position: relative;
    flex: 1;
    min-width: 200px;
  }

  .search-icon {
    position: absolute;
    left: 0.9rem;
    top: 50%;
    transform: translateY(-50%);
    font-size: 0.9rem;
    pointer-events: none;
  }

  .search-input {
    width: 100%;
    padding: 0.65rem 2.5rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 0.75rem;
    color: #e2e8f0;
    font-size: 0.875rem;
    font-family: "Inter", sans-serif;
    outline: none;
    transition: border-color 0.2s;
    box-sizing: border-box;
  }
  .search-input:focus {
    border-color: #6366f1;
    background: rgba(255, 255, 255, 0.08);
  }

  .search-clear {
    position: absolute;
    right: 0.8rem;
    top: 50%;
    transform: translateY(-50%);
    background: transparent;
    border: none;
    color: #94a3b8;
    cursor: pointer;
    font-size: 0.9rem;
  }

  .toolbar-actions {
    display: flex;
    gap: 0.75rem;
  }

  .btn-sort {
    background: rgba(255, 255, 255, 0.05);
    color: #cbd5e1;
    border: 1px solid rgba(255, 255, 255, 0.1);
    padding: 0.65rem 1rem;
    border-radius: 0.75rem;
    cursor: pointer;
    font-size: 0.85rem;
    font-weight: 500;
    transition: all 0.2s;
  }
  .btn-sort:hover {
    background: rgba(255, 255, 255, 0.1);
  }
  .btn-sort-asc {
    background: rgba(99, 102, 241, 0.1);
    color: #a5b4fc;
    border-color: rgba(99, 102, 241, 0.3);
  }

  .btn-print {
    background: rgba(30, 41, 59, 0.8);
    color: #e2e8f0;
    border: 1px solid rgba(255, 255, 255, 0.1);
    padding: 0.65rem 1rem;
    border-radius: 0.75rem;
    cursor: pointer;
    font-size: 0.85rem;
    font-weight: 500;
    transition: all 0.2s;
  }
  .btn-print:hover {
    background: #334155;
    border-color: rgba(255, 255, 255, 0.2);
  }

  /* ===== TABLE ===== */
  .table-card {
    background: rgba(30, 41, 59, 0.5);
    border: 1px solid rgba(255, 255, 255, 0.05);
    border-radius: 1rem;
    backdrop-filter: blur(10px);
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .empty-state {
    padding: 4rem 2rem;
    text-align: center;
  }
  .empty-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
    opacity: 0.5;
  }
  .empty-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #cbd5e1;
    margin: 0 0 0.5rem 0;
  }
  .empty-sub {
    font-size: 0.9rem;
    color: #64748b;
    margin: 0;
  }

  .loader {
    width: 40px;
    height: 40px;
    border: 3px solid rgba(255, 255, 255, 0.1);
    border-radius: 50%;
    border-top-color: #6366f1;
    animation: spin 1s ease-in-out infinite;
    margin: 0 auto 1rem auto;
  }

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  .table-wrapper {
    overflow-x: auto;
    overflow-y: auto;
    flex: 1;
    min-height: 0;
  }

  .table-wrapper::-webkit-scrollbar {
    width: 6px;
  }
  .table-wrapper::-webkit-scrollbar-track {
    background: transparent;
  }
  .table-wrapper::-webkit-scrollbar-thumb {
    background: transparent;
    border-radius: 999px;
    transition: background 0.3s ease;
  }
  .table-wrapper:hover::-webkit-scrollbar-thumb {
    background: #475569;
  }
  .table-wrapper:hover::-webkit-scrollbar-thumb:hover {
    background: #64748b;
  }

  .data-table {
    width: 100%;
    border-collapse: collapse;
    text-align: left;
  }

  .data-table th {
    background: rgba(15, 23, 42, 0.6);
    padding: 1rem 1.25rem;
    font-weight: 600;
    font-size: 0.75rem;
    letter-spacing: 0.06em;
    text-transform: uppercase;
    color: #64748b;
    text-align: left;
    position: sticky;
    top: 0;
    z-index: 1;
  }

  .th-no {
    width: 50px;
    text-align: center;
  }
  .th-aksi {
    width: 200px;
    text-align: center;
  }

  .table-row {
    border-bottom: 1px solid rgba(255, 255, 255, 0.04);
    transition: background 0.15s;
  }
  .table-row:hover {
    background: rgba(255, 255, 255, 0.04);
  }
  .table-row:last-child {
    border-bottom: none;
  }

  .data-table td {
    padding: 1rem 1.25rem;
    color: #cbd5e1;
    vertical-align: middle;
  }

  .td-no {
    color: #475569;
    font-size: 0.8rem;
    text-align: center;
  }
  .td-date {
    color: #94a3b8;
    font-size: 0.82rem;
    white-space: nowrap;
  }
  .td-name {
    font-weight: 500;
    color: #e2e8f0;
  }
  .td-perihal {
    color: #94a3b8;
    max-width: 220px;
  }
  .td-aksi {
    text-align: right;
  }

  .badge-nomor {
    display: inline-block;
    padding: 0.2rem 0.6rem;
    border-radius: 0.4rem;
    font-size: 0.78rem;
    font-weight: 600;
    white-space: nowrap;
  }

  .badge-blue-soft {
    background: rgba(99, 102, 241, 0.15);
    color: #a5b4fc;
    border: 1px solid rgba(99, 102, 241, 0.25);
  }

  .badge-emerald-soft {
    background: rgba(16, 185, 129, 0.12);
    color: #6ee7b7;
    border: 1px solid rgba(16, 185, 129, 0.25);
  }

  /* ===== MODAL TAMBAH/EDIT ===== */
  .modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(15, 23, 42, 0.8);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 50;
    padding: 1rem;
  }

  .modal-box {
    background: #1e293b;
    width: 100%;
    max-width: 500px;
    border-radius: 1.25rem;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.1);
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

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
  }

  .modal-header-masuk {
    background: linear-gradient(
      135deg,
      rgba(99, 102, 241, 0.3),
      rgba(139, 92, 246, 0.2)
    );
    border-bottom: 1px solid rgba(99, 102, 241, 0.25);
  }

  .modal-header-keluar {
    background: linear-gradient(
      135deg,
      rgba(16, 185, 129, 0.25),
      rgba(5, 150, 105, 0.15)
    );
    border-bottom: 1px solid rgba(16, 185, 129, 0.25);
  }

  .modal-title {
    font-size: 1.15rem;
    font-weight: 700;
    color: #f1f5f9;
    margin: 0;
  }
  .modal-sub {
    font-size: 0.78rem;
    color: #94a3b8;
    margin: 0.2rem 0 0 0;
  }

  .modal-close {
    background: transparent;
    border: none;
    color: #94a3b8;
    font-size: 1.25rem;
    cursor: pointer;
    transition: color 0.2s;
  }
  .modal-close:hover {
    color: #f8fafc;
  }

  .modal-form {
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
  }

  .form-label {
    font-size: 0.85rem;
    font-weight: 500;
    color: #cbd5e1;
  }
  .required {
    color: #ef4444;
  }

  .form-input {
    background: rgba(15, 23, 42, 0.6);
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: #f1f5f9;
    padding: 0.75rem 1rem;
    border-radius: 0.75rem;
    font-size: 0.9rem;
    font-family: "Inter", sans-serif;
    transition: all 0.2s;
    outline: none;
  }
  .form-input:focus {
    border-color: #6366f1;
    background: rgba(15, 23, 42, 0.8);
    box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
  }

  .form-textarea {
    resize: vertical;
    min-height: 80px;
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 0.5rem;
    padding-top: 1.5rem;
    border-top: 1px solid rgba(255, 255, 255, 0.05);
  }

  .btn-batal {
    background: transparent;
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: #cbd5e1;
    padding: 0.6rem 1.25rem;
    border-radius: 0.75rem;
    font-size: 0.9rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
  }
  .btn-batal:hover {
    background: rgba(255, 255, 255, 0.05);
    color: #f8fafc;
  }

  .btn-simpan {
    border: none;
    color: white;
    padding: 0.6rem 1.5rem;
    border-radius: 0.75rem;
    font-size: 0.9rem;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.2s;
    font-family: "Inter", sans-serif;
  }

  .btn-simpan-masuk {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  }

  .btn-simpan-keluar {
    background: linear-gradient(135deg, #10b981, #059669);
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  }

  .btn-simpan:hover {
    transform: translateY(-1px);
    filter: brightness(1.1);
  }

  /* ===== PRINT ===== */
  @media print {
    :global(body) {
      background: white !important;
      color: black !important;
      margin: 0 !important;
      padding: 0 !important;
    }
    .app-shell,
    .modal-backdrop {
      display: none !important;
    }
    .print\:block {
      display: block !important;
    }

    @page {
      size: A4 portrait;
      margin: 1.5cm;
    }

    .print-header {
      text-align: center;
      padding-bottom: 1rem;
      border-bottom: 2px solid black;
      margin-bottom: 1.5rem;
    }
    .print-header h1 {
      font-size: 1.4rem;
      margin: 0 0 0.5rem 0;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
    .print-header p {
      font-size: 0.9rem;
      color: #333;
      margin: 0;
    }

    .print-table {
      width: 100%;
      border-collapse: collapse;
    }
    .print-table th,
    .print-table td {
      border: 1px solid #000 !important;
      padding: 0.6rem 0.8rem;
      text-align: left;
      font-size: 0.85rem;
    }
    .print-table th {
      background-color: #f1f5f9 !important;
      font-weight: bold;
      -webkit-print-color-adjust: exact;
      print-color-adjust: exact;
    }
  }
</style>
