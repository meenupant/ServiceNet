package org.benetech.servicenet.adapter.healthleads.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class RequiredDocument extends BaseData {

    @SerializedName("service_id")
    private String serviceId;

    private String document;
}
