package com.akillisaha.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private CompanyContact contact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_id", nullable = false)
    private User representative;

    @Column(name = "visit_date", nullable = false)
    private OffsetDateTime visitDate;

    @Column(nullable = false, length = 255)
    private String topic;

    @Column(name = "supplied_products", columnDefinition = "TEXT")
    private String suppliedProducts;

    @Column(name = "monthly_consumption", length = 100)
    private String monthlyConsumption;

    @Column(name = "purchased_products", columnDefinition = "TEXT")
    private String purchasedProducts;

    @Column(name = "current_supplier_competitor", length = 255)
    private String currentSupplierCompetitor;

    @Column(name = "has_sample", nullable = false)
    @Builder.Default
    private Boolean hasSample = false;

    @Column(name = "has_offer", nullable = false)
    @Builder.Default
    private Boolean hasOffer = false;

    @Column(name = "next_action", columnDefinition = "TEXT")
    private String nextAction;

    @Column(name = "next_visit_date")
    private OffsetDateTime nextVisitDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Sample> samples = new ArrayList<>();

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Offer> offers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
