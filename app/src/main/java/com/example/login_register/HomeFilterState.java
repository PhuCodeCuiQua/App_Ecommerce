package com.example.login_register;

import java.util.LinkedHashSet;
import java.util.Set;

public final class HomeFilterState {
    private String query;
    private String gender;
    private final Set<String> types = new LinkedHashSet<>();
    private boolean saleOnly;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query == null || query.trim().isEmpty() ? null : query.trim();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Set<String> getTypes() {
        return new LinkedHashSet<>(types);
    }

    public void setTypes(Iterable<String> values) {
        types.clear();
        if (values != null) {
            for (String value : values) {
                if (value != null) {
                    types.add(value);
                }
            }
        }
    }

    public void setSaleOnly(boolean saleOnly) {
        this.saleOnly = saleOnly;
    }

    public boolean isSaleOnly() {
        return saleOnly;
    }

    public boolean hasFilters() {
        return query != null || gender != null || !types.isEmpty() || saleOnly;
    }

    public void clear() {
        query = null;
        gender = null;
        types.clear();
        saleOnly = false;
    }
}
