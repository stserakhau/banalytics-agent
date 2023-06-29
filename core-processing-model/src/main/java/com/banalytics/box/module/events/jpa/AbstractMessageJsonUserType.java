package com.banalytics.box.module.events.jpa;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.jpa.types.AbstractJsonUserType;

import java.sql.Types;

public class AbstractMessageJsonUserType extends AbstractJsonUserType {
    @Override
    protected Object createObject(String content) throws Exception {
        return AbstractMessage.from(content);
    }

    @Override
    protected int getDataType() {
        return Types.CLOB;
    }

    @Override
    public Class returnedClass() {
        return AbstractMessage.class;
    }
}
