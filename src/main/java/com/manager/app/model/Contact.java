package com.manager.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Document(collection = "contacts") 
public class Contact {

    @Id
    private String id;

    private String nombre;
    private String email;
    private Telefono telefono; // Objeto anidado
    private String empresa;
    private String descripcion;
    private Date fecha;
    private String estado;
    
    @Data
    public static class Telefono {
        @JsonProperty("codigoPais")
        private String codigoPais;

        @JsonProperty("formateado")
        private String formateado;

        @JsonProperty("numero")
        private String numero;

    }    
}