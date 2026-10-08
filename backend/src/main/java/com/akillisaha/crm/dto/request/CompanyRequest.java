package com.akillisaha.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {

    @NotBlank(message = "Müşteri / Firma adı zorunludur")
    @Size(max = 255, message = "Firma adı en fazla 255 karakter olabilir")
    private String name;

    @Size(max = 100, message = "Bölge/Şehir en fazla 100 karakter olabilir")
    private String cityRegion;

    private String address;

    @Size(max = 50, message = "Telefon en fazla 50 karakter olabilir")
    private String phone;

    private String email;

    private String currentSupplierCompetitor; // Kolon I: Mevcut Tedarikçi / Rakip
    private String suppliedProducts;          // Kolon F: Tedarik Ettiği Ürünler
    private String monthlyConsumption;        // Kolon G: Aylık Kullanım Miktarı
    private String purchasedProducts;         // Kolon H: Bizden Aldığı Ürünler

    private Long assignedUserId;              // Boş bırakılırsa oluşturan temsilciye atanır
}
