package org.benetech.servicenet.adapter.healthleads.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@Data
@EqualsAndHashCode(callSuper = true)
public class HealthleadsLocation extends HealthleadsBaseData {

    private String name;

    @SerializedName("alternate_name")
    private String alternateName;

    private String description;

    private String transportation;

    private String latitude;

    private String longitude;

    @SerializedName("x_schedule")
    private String schedule;

    public double getLatitude() {
        if (StringUtils.isBlank(latitude)) {
            return 0;
        }
        return Double.valueOf(latitude);
    }

    public double getLongitude() {
        if (StringUtils.isBlank(longitude)) {
            return 0;
        }
        return Double.valueOf(longitude);
    }
}
