package com.manager.app.dto;

import java.util.Map;

public class PdfRequestDTO {
    private String action;
    private String exportId;
    private Map<String, Object> filter;

    // Constructor vacío (Necesario para la serialización de Jackson)
    public PdfRequestDTO() {
    }

    // Constructor con parámetros
    public PdfRequestDTO(String action, String exportId, Map<String, Object> filter) {
        this.action = action;
        this.exportId = exportId;
        this.filter = filter;
    }

    // Getters y Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getExportId() {
        return exportId;
    }

    public void setExportId(String exportId) {
        this.exportId = exportId;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }
}