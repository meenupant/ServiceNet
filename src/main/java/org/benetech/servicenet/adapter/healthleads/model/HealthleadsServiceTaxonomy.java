package org.benetech.servicenet.adapter.healthleads.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class HealthleadsServiceTaxonomy extends ServiceRelatedData {

    @SerializedName("taxonomy_id")
    private String taxonomyId;

    @SerializedName("taxonomy_detail")
    private String taxonomyDetail;
}