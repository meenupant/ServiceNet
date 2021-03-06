package org.benetech.servicenet.adapter.smcconnect;

import org.benetech.servicenet.adapter.MultipleDataAdapter;
import org.benetech.servicenet.adapter.shared.model.MultipleImportData;
import org.benetech.servicenet.adapter.smcconnect.persistence.SmcDataManager;
import org.benetech.servicenet.domain.DataImportReport;
import org.benetech.servicenet.manager.ImportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("SMCConnectDataAdapter")
public class SMCConnectDataAdapter extends MultipleDataAdapter {

    private static final int NUMBER_OF_FILES = 10;

    @Autowired
    private ImportManager importManager;

    @Override
    public DataImportReport importData(MultipleImportData data) {
        verifyData(data);
        return new SmcDataManager(importManager, data)
            .importData(data);
    }

    @Override
    protected int getNumberOfFilesToProcess() {
        return NUMBER_OF_FILES;
    }
}
