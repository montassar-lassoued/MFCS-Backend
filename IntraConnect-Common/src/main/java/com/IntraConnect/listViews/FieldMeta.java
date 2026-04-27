package com.IntraConnect.listViews;

import com.IntraConnect.listViews.fieldType.Field;

public class FieldMeta {

    private final String field;
    boolean editable = false;
    String query ="";
    String defaultValue ="";
    Field type = Field.TEXT_FIELD;
    boolean nullable = true;
    private boolean visible = true;


    private FieldMeta (String field){
        this.field = field;
    }
    public static FieldMeta of(String field){
        return new FieldMeta(field);
    }

    public FieldMeta type(Field type) {
        this.type = type;

        return this;
    }

    public FieldMeta editable(boolean editable){
        this.editable = editable;

        return this;
    }
    public FieldMeta visible(boolean visible) {
        this.visible =visible;
        return this;
    }
    public FieldMeta nullable(boolean nullable){
        this.nullable = nullable;

        return this;
    }
    public FieldMeta query(String query){
        this.query = query;

        return this;
    }
    public FieldMeta defaultValue(String value){
        this.defaultValue = value;

        return this;
    }

    public String getField() {
        return field;
    }

    public String Of(){
        return field;
    }
    public boolean isEditable() {
        return editable;
    }

    public String getQuery() {
        return query;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
    public Field getType() {
        return type;
    }
	
	public boolean isNullable() {
        return nullable;
    }

    public boolean isVisible() {
        return visible;
    }
}
