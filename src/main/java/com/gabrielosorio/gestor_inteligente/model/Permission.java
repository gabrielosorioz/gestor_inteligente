package com.gabrielosorio.gestor_inteligente.model;

import com.gabrielosorio.gestor_inteligente.model.enums.PermissionType;
import java.time.LocalDateTime;
import java.util.Objects;

public class Permission {
    private long id;
    private PermissionType permissionType;
    private String name;
    private String description;
    private String category;
    private boolean active;
    private LocalDateTime createdAt;

    public Permission() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Permission(PermissionType permissionType, String name, String description, String category) {
        this();
        this.permissionType = permissionType;
        this.name = name;
        this.description = description;
        this.category = category;
    }


    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public PermissionType getPermissionType() { return permissionType; }
    public void setPermissionType(PermissionType permissionType) { this.permissionType = permissionType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}