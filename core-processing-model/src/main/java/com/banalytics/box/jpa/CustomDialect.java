package com.banalytics.box.jpa;

import org.hibernate.dialect.DerbyTenSixDialect;

import java.sql.Types;

public class CustomDialect extends DerbyTenSixDialect {
    public CustomDialect() {
        this.registerColumnType(Types.JAVA_OBJECT, "clob");
    }
}
