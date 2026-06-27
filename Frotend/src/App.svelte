<script>
  import "./app.css";
  import { onMount } from "svelte";

  /** @type {any[]} */
  let dataSuratMasuk = [];
  /** @type {any[]} */
  let dataSuratKeluar = [];

  let activeTab = "masuk";
  let showModal = false;
  let isLoading = false;

  // Variabel untuk fitur pencarian
  let searchQuery = "";

  // Variabel untuk sorting
  let sortOrder = "DESC";

  // Struktur form input data
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

  // Ambil Data dari API
  async function fetchSuratMasuk() {
    isLoading = true;
    try {
      const response = await fetch(`http://localhost:8000/api/surat-masuk?sort=${sortOrder}`);
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
      const response = await fetch(`http://localhost:8000/api/surat-keluar?sort=${sortOrder}`);
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

  // Logika Filter Pencarian Otomatis
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

  // Simpan Data Baru
  async function handleSubmit() {
    const isMasuk = activeTab === "masuk";
    const url = isMasuk
      ? "http://localhost:8000/api/surat-masuk/tambah"
      : "http://localhost:8000/api/surat-keluar/tambah";
    const dataYangDikirim = isMasuk ? formDataMasuk : formDataKeluar;

    try {
      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dataYangDikirim),
      });

      const result = await response.json();
      if (result.status === "sukses") {
        alert(result.pesan);
        showModal = false;
        if (isMasuk) {
          formDataMasuk = { nomorSurat: "", tanggalSurat: "", tanggalDiterima: "", pengirim: "", perihal: "", keterangan: "" };
          fetchSuratMasuk();
        } else {
          formDataKeluar = { nomorSurat: "", tanggalSurat: "", tujuan: "", perihal: "", keterangan: "" };
          fetchSuratKeluar();
        }
      } else {
        alert("Error: " + result.pesan);
      }
    } catch (error) {
      alert("Gagal terhubung ke server Java!");
    }
  }

  // Fungsi Hapus Data
  async function handleHapus(nomorSurat) {
    if (!confirm(`Hapus surat nomor: ${nomorSurat}?`)) return;

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
        isMasuk ? fetchSuratMasuk() : fetchSuratKeluar();
      } else {
        alert("Error: " + result.pesan);
      }
    } catch (error) {
      alert("Gagal menghapus data dari server!");
    }
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

<!-- ===== PRINT HEADER (hidden on screen) ===== -->
<div class="hidden print:block print-header">
  <h1>LAPORAN ARSIP SURAT {activeTab === "masuk" ? "MASUK" : "KELUAR"}</h1>
  <p>Dicetak oleh Sistem Arsipku &mdash; {new Date().toLocaleDateString("id-ID", { weekday:"long", year:"numeric", month:"long", day:"numeric" })}</p>
</div>

<!-- ===== MAIN APP ===== -->
<div class="app-shell print:hidden">

  <!-- SIDEBAR -->
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
        on:click={() => { activeTab = "masuk"; searchQuery = ""; sortOrder = "DESC"; fetchSuratMasuk(); }}
      >
        <span class="nav-icon">📥</span>
        <span>Surat Masuk</span>
        <span class="nav-badge badge-blue">{dataSuratMasuk.length}</span>
      </button>

      <button
        class="nav-item {activeTab === 'keluar' ? 'nav-active-keluar' : ''}"
        on:click={() => { activeTab = "keluar"; searchQuery = ""; sortOrder = "DESC"; fetchSuratKeluar(); }}
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

  <!-- MAIN CONTENT -->
  <main class="main-content">

    <!-- TOP BAR -->
    <header class="topbar">
      <div class="topbar-left">
        <h2 class="page-title">
          {activeTab === "masuk" ? "📥 Surat Masuk" : "📤 Surat Keluar"}
        </h2>
        <p class="page-sub">
          {activeTab === "masuk" ? filteredSuratMasuk.length : filteredSuratKeluar.length} data ditemukan
        </p>
      </div>
      <button
        class="btn-tambah"
        on:click={() => (showModal = true)}
      >
        <span>＋</span> Tambah Surat
      </button>
    </header>

    <!-- STAT CARDS -->
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
          <p class="stat-value">{dataSuratMasuk.length + dataSuratKeluar.length}</p>
        </div>
      </div>
    </div>

    <!-- TOOLBAR -->
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
          <button class="search-clear" on:click={() => searchQuery = ""}>✕</button>
        {/if}
      </div>

      <div class="toolbar-actions">
        <button class="btn-sort {sortOrder === 'ASC' ? 'btn-sort-asc' : ''}" on:click={toggleSort} title="Urutkan berdasarkan tanggal">
          {sortOrder === "DESC" ? "↓ Terbaru" : "↑ Terlama"}
        </button>
        <button class="btn-print" on:click={handleCetak}>
          🖨️ Cetak PDF
        </button>
      </div>
    </div>

    <!-- TABLE AREA -->
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
                    <td><span class="badge-nomor badge-blue-soft">{surat.nomorSurat}</span></td>
                    <td class="td-date">{surat.tanggalSurat}</td>
                    <td class="td-name">{surat.pengirim}</td>
                    <td class="td-perihal">{surat.perihal}</td>
                    <td class="td-aksi">
                      <button class="btn-hapus" on:click={() => handleHapus(surat.nomorSurat)}>
                        🗑 Hapus
                      </button>
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        {/if}

      {:else}
        {#if filteredSuratKeluar.length === 0}
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
                    <td><span class="badge-nomor badge-emerald-soft">{surat.nomorSurat}</span></td>
                    <td class="td-date">{surat.tanggalSurat}</td>
                    <td class="td-name">{surat.tujuan}</td>
                    <td class="td-perihal">{surat.perihal}</td>
                    <td class="td-aksi">
                      <button class="btn-hapus" on:click={() => handleHapus(surat.nomorSurat)}>
                        🗑 Hapus
                      </button>
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        {/if}
      {/if}
    </div>

  </main>
</div>

<!-- ===== PRINT TABLE ===== -->
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
      {#each (activeTab === "masuk" ? filteredSuratMasuk : filteredSuratKeluar) as surat, i}
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

<!-- ===== MODAL ===== -->
{#if showModal}
  <!-- svelte-ignore a11y-click-events-have-key-events -->
  <!-- svelte-ignore a11y-no-static-element-interactions -->
  <div class="modal-backdrop" on:click|self={() => (showModal = false)}>
    <div class="modal-box">
      <!-- Modal Header -->
      <div class="modal-header {activeTab === 'masuk' ? 'modal-header-masuk' : 'modal-header-keluar'}">
        <div>
          <h2 class="modal-title">
            {activeTab === "masuk" ? "📥 Tambah Surat Masuk" : "📤 Tambah Surat Keluar"}
          </h2>
          <p class="modal-sub">Isi form di bawah dengan data yang benar</p>
        </div>
        <button class="modal-close" on:click={() => (showModal = false)}>✕</button>
      </div>

      <!-- Modal Body -->
      <form on:submit|preventDefault={handleSubmit} class="modal-form">
        {#if activeTab === "masuk"}
          <div class="form-group">
            <label class="form-label">Nomor Surat <span class="required">*</span></label>
            <input type="text" bind:value={formDataMasuk.nomorSurat} required class="form-input" placeholder="Contoh: 001/SM/VI/2025" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Tanggal Surat <span class="required">*</span></label>
              <input type="date" bind:value={formDataMasuk.tanggalSurat} required class="form-input" />
            </div>
            <div class="form-group">
              <label class="form-label">Tgl. Diterima <span class="required">*</span></label>
              <input type="date" bind:value={formDataMasuk.tanggalDiterima} required class="form-input" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Pengirim <span class="required">*</span></label>
            <input type="text" bind:value={formDataMasuk.pengirim} required class="form-input" placeholder="Nama instansi atau perorangan" />
          </div>
          <div class="form-group">
            <label class="form-label">Perihal <span class="required">*</span></label>
            <input type="text" bind:value={formDataMasuk.perihal} required class="form-input" placeholder="Ringkasan isi surat" />
          </div>
          <div class="form-group">
            <label class="form-label">Keterangan</label>
            <textarea bind:value={formDataMasuk.keterangan} class="form-input form-textarea" rows="2" placeholder="Catatan tambahan (opsional)"></textarea>
          </div>
        {:else}
          <div class="form-group">
            <label class="form-label">Nomor Surat <span class="required">*</span></label>
            <input type="text" bind:value={formDataKeluar.nomorSurat} required class="form-input" placeholder="Contoh: 001/SK/VI/2025" />
          </div>
          <div class="form-group">
            <label class="form-label">Tanggal Surat <span class="required">*</span></label>
            <input type="date" bind:value={formDataKeluar.tanggalSurat} required class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">Tujuan <span class="required">*</span></label>
            <input type="text" bind:value={formDataKeluar.tujuan} required class="form-input" placeholder="Nama instansi atau perorangan tujuan" />
          </div>
          <div class="form-group">
            <label class="form-label">Perihal <span class="required">*</span></label>
            <input type="text" bind:value={formDataKeluar.perihal} required class="form-input" placeholder="Ringkasan isi surat" />
          </div>
          <div class="form-group">
            <label class="form-label">Keterangan</label>
            <textarea bind:value={formDataKeluar.keterangan} class="form-input form-textarea" rows="2" placeholder="Catatan tambahan (opsional)"></textarea>
          </div>
        {/if}

        <div class="modal-footer">
          <button type="button" class="btn-batal" on:click={() => (showModal = false)}>Batal</button>
          <button type="submit" class="btn-simpan {activeTab === 'masuk' ? 'btn-simpan-masuk' : 'btn-simpan-keluar'}">
            ✓ Simpan Data
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}

<style>
  /* ===== GOOGLE FONT ===== */
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap');

  :global(*, *::before, *::after) { box-sizing: border-box; margin: 0; padding: 0; }
  :global(body) { font-family: 'Inter', sans-serif; background: #0f1117; color: #e2e8f0; }

  /* ===== LAYOUT ===== */
  .app-shell {
    display: flex;
    min-height: 100vh;
  }

  /* ===== SIDEBAR ===== */
  .sidebar {
    width: 260px;
    min-height: 100vh;
    background: linear-gradient(180deg, #1e1b4b 0%, #1a1a2e 60%, #0f0f1a 100%);
    display: flex;
    flex-direction: column;
    padding: 1.5rem 1rem;
    border-right: 1px solid rgba(99, 102, 241, 0.2);
    flex-shrink: 0;
  }

  .sidebar-brand {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 0.5rem 1.5rem;
    border-bottom: 1px solid rgba(255,255,255,0.08);
    margin-bottom: 1.5rem;
  }

  .brand-icon {
    font-size: 2rem;
    filter: drop-shadow(0 0 8px rgba(99,102,241,0.6));
  }

  .brand-title {
    font-size: 1.4rem;
    font-weight: 800;
    background: linear-gradient(135deg, #a5b4fc, #818cf8);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    line-height: 1.2;
  }

  .brand-sub {
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
    background: rgba(255,255,255,0.06);
    color: #e2e8f0;
  }

  .nav-active-masuk {
    background: rgba(99, 102, 241, 0.2) !important;
    color: #a5b4fc !important;
    border: 1px solid rgba(99, 102, 241, 0.3);
    box-shadow: 0 0 20px rgba(99,102,241,0.1);
  }

  .nav-active-keluar {
    background: rgba(16, 185, 129, 0.15) !important;
    color: #6ee7b7 !important;
    border: 1px solid rgba(16, 185, 129, 0.3);
    box-shadow: 0 0 20px rgba(16,185,129,0.1);
  }

  .nav-icon { font-size: 1.1rem; }

  .nav-badge {
    margin-left: auto;
    font-size: 0.7rem;
    font-weight: 700;
    padding: 0.15rem 0.5rem;
    border-radius: 9999px;
  }

  .badge-blue { background: rgba(99,102,241,0.25); color: #a5b4fc; }
  .badge-emerald { background: rgba(16,185,129,0.2); color: #6ee7b7; }

  .sidebar-footer {
    padding-top: 1rem;
    border-top: 1px solid rgba(255,255,255,0.06);
    font-size: 0.7rem;
    color: #334155;
    text-align: center;
  }

  /* ===== MAIN CONTENT ===== */
  .main-content {
    flex: 1;
    background: #0f1117;
    padding: 2rem;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  /* ===== TOPBAR ===== */
  .topbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .page-title {
    font-size: 1.6rem;
    font-weight: 700;
    color: #f1f5f9;
  }

  .page-sub {
    font-size: 0.8rem;
    color: #475569;
    margin-top: 0.2rem;
  }

  .btn-tambah {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.65rem 1.4rem;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    color: white;
    border: none;
    border-radius: 0.75rem;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 4px 15px rgba(99, 102, 241, 0.35);
  }

  .btn-tambah:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
  }

  .btn-tambah:active { transform: translateY(0); }

  /* ===== STAT CARDS ===== */
  .stat-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1rem;
  }

  .stat-card {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1.25rem 1.5rem;
    border-radius: 1rem;
    border: 1px solid rgba(255,255,255,0.06);
    backdrop-filter: blur(10px);
    transition: transform 0.2s;
  }

  .stat-card:hover { transform: translateY(-2px); }

  .stat-masuk {
    background: linear-gradient(135deg, rgba(99,102,241,0.15), rgba(139,92,246,0.1));
    border-color: rgba(99,102,241,0.25);
  }

  .stat-keluar {
    background: linear-gradient(135deg, rgba(16,185,129,0.15), rgba(5,150,105,0.1));
    border-color: rgba(16,185,129,0.25);
  }

  .stat-total {
    background: linear-gradient(135deg, rgba(245,158,11,0.15), rgba(217,119,6,0.1));
    border-color: rgba(245,158,11,0.25);
  }

  .stat-icon { font-size: 2rem; }

  .stat-label {
    font-size: 0.75rem;
    color: #64748b;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    font-weight: 600;
  }

  .stat-value {
    font-size: 2rem;
    font-weight: 800;
    color: #f1f5f9;
    line-height: 1.1;
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
    background: rgba(255,255,255,0.05);
    border: 1px solid rgba(255,255,255,0.1);
    border-radius: 0.75rem;
    color: #e2e8f0;
    font-size: 0.875rem;
    font-family: 'Inter', sans-serif;
    outline: none;
    transition: border-color 0.2s, box-shadow 0.2s;
  }

  .search-input::placeholder { color: #475569; }

  .search-input:focus {
    border-color: rgba(99,102,241,0.5);
    box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
  }

  .search-clear {
    position: absolute;
    right: 0.75rem;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    color: #64748b;
    cursor: pointer;
    font-size: 0.8rem;
  }

  .toolbar-actions {
    display: flex;
    gap: 0.75rem;
  }

  .btn-sort {
    padding: 0.65rem 1.2rem;
    border-radius: 0.75rem;
    border: 1px solid rgba(99,102,241,0.35);
    background: rgba(99,102,241,0.1);
    color: #a5b4fc;
    font-size: 0.85rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    font-family: 'Inter', sans-serif;
  }

  .btn-sort:hover {
    background: rgba(99,102,241,0.2);
    border-color: rgba(99,102,241,0.6);
  }

  .btn-sort-asc {
    background: rgba(245,158,11,0.1);
    border-color: rgba(245,158,11,0.35);
    color: #fcd34d;
  }

  .btn-print {
    padding: 0.65rem 1.2rem;
    border-radius: 0.75rem;
    border: 1px solid rgba(255,255,255,0.1);
    background: rgba(255,255,255,0.05);
    color: #94a3b8;
    font-size: 0.85rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    font-family: 'Inter', sans-serif;
  }

  .btn-print:hover {
    background: rgba(255,255,255,0.1);
    color: #e2e8f0;
  }

  /* ===== TABLE CARD ===== */
  .table-card {
    background: rgba(255,255,255,0.03);
    border: 1px solid rgba(255,255,255,0.07);
    border-radius: 1rem;
    overflow: hidden;
    flex: 1;
  }

  .table-wrapper { overflow-x: auto; }

  .data-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.875rem;
  }

  .data-table thead tr {
    background: rgba(255,255,255,0.05);
    border-bottom: 1px solid rgba(255,255,255,0.08);
  }

  .data-table th {
    padding: 1rem 1.25rem;
    font-weight: 600;
    font-size: 0.75rem;
    letter-spacing: 0.06em;
    text-transform: uppercase;
    color: #64748b;
    text-align: left;
  }

  .th-no { width: 50px; text-align: center; }
  .th-aksi { width: 100px; text-align: center; }

  .table-row {
    border-bottom: 1px solid rgba(255,255,255,0.04);
    transition: background 0.15s;
  }

  .table-row:hover { background: rgba(255,255,255,0.04); }
  .table-row:last-child { border-bottom: none; }

  .data-table td {
    padding: 1rem 1.25rem;
    color: #cbd5e1;
    vertical-align: middle;
  }

  .td-no { color: #475569; font-size: 0.8rem; text-align: center; }
  .td-date { color: #94a3b8; font-size: 0.82rem; white-space: nowrap; }
  .td-name { font-weight: 500; color: #e2e8f0; }
  .td-perihal { color: #94a3b8; max-width: 260px; }
  .td-aksi { text-align: center; }

  .badge-nomor {
    display: inline-block;
    padding: 0.2rem 0.6rem;
    border-radius: 0.4rem;
    font-size: 0.78rem;
    font-weight: 600;
    white-space: nowrap;
  }

  .badge-blue-soft { background: rgba(99,102,241,0.15); color: #a5b4fc; border: 1px solid rgba(99,102,241,0.25); }
  .badge-emerald-soft { background: rgba(16,185,129,0.12); color: #6ee7b7; border: 1px solid rgba(16,185,129,0.2); }

  .btn-hapus {
    padding: 0.3rem 0.7rem;
    background: rgba(239,68,68,0.1);
    border: 1px solid rgba(239,68,68,0.25);
    color: #fca5a5;
    border-radius: 0.5rem;
    font-size: 0.78rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    font-family: 'Inter', sans-serif;
  }

  .btn-hapus:hover {
    background: rgba(239,68,68,0.25);
    color: #ef4444;
    border-color: rgba(239,68,68,0.5);
  }

  /* ===== EMPTY STATE ===== */
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem 2rem;
    gap: 0.75rem;
  }

  .empty-icon { font-size: 3.5rem; opacity: 0.4; }
  .empty-title { font-size: 1rem; font-weight: 600; color: #475569; }
  .empty-sub { font-size: 0.83rem; color: #334155; }

  /* ===== LOADER ===== */
  .loader {
    width: 36px;
    height: 36px;
    border: 3px solid rgba(99,102,241,0.2);
    border-top-color: #6366f1;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  @keyframes spin { to { transform: rotate(360deg); } }

  /* ===== MODAL ===== */
  .modal-backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.75);
    backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
    padding: 1rem;
    animation: fadeIn 0.15s ease;
  }

  @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

  .modal-box {
    background: #1a1a2e;
    border: 1px solid rgba(255,255,255,0.1);
    border-radius: 1.25rem;
    width: 100%;
    max-width: 500px;
    overflow: hidden;
    animation: slideUp 0.2s ease;
    box-shadow: 0 25px 60px rgba(0,0,0,0.6);
  }

  @keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
  }

  .modal-header-masuk { background: linear-gradient(135deg, rgba(99,102,241,0.3), rgba(139,92,246,0.2)); border-bottom: 1px solid rgba(99,102,241,0.25); }
  .modal-header-keluar { background: linear-gradient(135deg, rgba(16,185,129,0.25), rgba(5,150,105,0.15)); border-bottom: 1px solid rgba(16,185,129,0.25); }

  .modal-title { font-size: 1.15rem; font-weight: 700; color: #f1f5f9; }
  .modal-sub { font-size: 0.78rem; color: #64748b; margin-top: 0.25rem; }

  .modal-close {
    background: rgba(255,255,255,0.07);
    border: none;
    color: #64748b;
    width: 32px;
    height: 32px;
    border-radius: 0.5rem;
    cursor: pointer;
    font-size: 0.85rem;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .modal-close:hover { background: rgba(239,68,68,0.2); color: #fca5a5; }

  .modal-form {
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
    max-height: 70vh;
    overflow-y: auto;
  }

  .form-group { display: flex; flex-direction: column; gap: 0.4rem; }
  .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }

  .form-label {
    font-size: 0.8rem;
    font-weight: 600;
    color: #94a3b8;
    text-transform: uppercase;
    letter-spacing: 0.04em;
  }

  .required { color: #f87171; }

  .form-input {
    padding: 0.6rem 0.9rem;
    background: rgba(255,255,255,0.05);
    border: 1px solid rgba(255,255,255,0.1);
    border-radius: 0.6rem;
    color: #e2e8f0;
    font-size: 0.875rem;
    font-family: 'Inter', sans-serif;
    outline: none;
    transition: border-color 0.2s, box-shadow 0.2s;
    width: 100%;
  }

  .form-input::placeholder { color: #334155; }

  .form-input:focus {
    border-color: rgba(99,102,241,0.5);
    box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
  }

  .form-textarea { resize: vertical; min-height: 70px; }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    padding-top: 0.5rem;
    border-top: 1px solid rgba(255,255,255,0.07);
    margin-top: 0.5rem;
  }

  .btn-batal {
    padding: 0.6rem 1.25rem;
    border: 1px solid rgba(255,255,255,0.1);
    background: transparent;
    color: #64748b;
    border-radius: 0.65rem;
    font-size: 0.875rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    font-family: 'Inter', sans-serif;
  }

  .btn-batal:hover { background: rgba(255,255,255,0.05); color: #94a3b8; }

  .btn-simpan {
    padding: 0.6rem 1.5rem;
    border: none;
    color: white;
    border-radius: 0.65rem;
    font-size: 0.875rem;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.2s;
    font-family: 'Inter', sans-serif;
  }

  .btn-simpan-masuk {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    box-shadow: 0 4px 12px rgba(99,102,241,0.3);
  }

  .btn-simpan-keluar {
    background: linear-gradient(135deg, #10b981, #059669);
    box-shadow: 0 4px 12px rgba(16,185,129,0.3);
  }

  .btn-simpan:hover { transform: translateY(-1px); filter: brightness(1.1); }

  /* ===== PRINT STYLES ===== */
  @media print {
    :global(body) { background: white !important; color: black !important; }

    .print-header {
      text-align: center;
      padding: 1rem;
      border-bottom: 2px solid black;
      margin-bottom: 1rem;
    }

    .print-header h1 {
      font-size: 1.4rem;
      font-weight: 800;
      text-transform: uppercase;
    }

    .print-header p { font-size: 0.85rem; color: #555; margin-top: 0.3rem; }

    .print-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.85rem;
    }

    .print-table th, .print-table td {
      border: 1px solid #ccc;
      padding: 0.5rem 0.75rem;
      text-align: left;
    }

    .print-table th {
      background: #f0f0f0;
      font-weight: 700;
      text-transform: uppercase;
      font-size: 0.75rem;
    }

    .print-table tr:nth-child(even) { background: #f9f9f9; }
  }
</style>
