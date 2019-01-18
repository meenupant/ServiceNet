package org.benetech.servicenet.adapter.eden.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class EdenTakeAllRequest implements Serializable {

    private static final long serialVersionUID = -2600278568932255382L;

    private String term = "*";

    @NonNull
    private String modifiedSince;

    private boolean takeALL = true;
}