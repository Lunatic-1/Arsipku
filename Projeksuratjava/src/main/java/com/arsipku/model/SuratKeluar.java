package com.arsipku.model;

import java.sql.Date;
import java.sql.Timestamp;

public class SuratKeluar {
    private Long id;
    private String nomorSurat;
    private Date tanggalSurat;
    private String tujuan;
    private String perihal;
    private String keterangan;
    private String filePath;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNomorSurat() {
        return nomorSurat;
    }
    public void setNomorSurat(String nomorSurat) {
        this.nomorSurat = nomorSurat;
    }
    public Date getTanggalSurat() {
        return tanggalSurat;
    }
    public void setTanggalSurat(Date tanggalSurat) {
        this.tanggalSurat = tanggalSurat;
    }
    public String getTujuan() {
        return tujuan;
    }
    public void setTujuan(String tujuan) {
        this.tujuan = tujuan;
    }
    public String getPerihal() {
        return perihal;
    }
    public void setPerihal(String perihal) {
        this.perihal = perihal;
    }
    public String getKeterangan() {
        return keterangan;
    }
    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}