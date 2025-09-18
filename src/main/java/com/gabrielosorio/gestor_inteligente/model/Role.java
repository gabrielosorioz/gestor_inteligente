package com.gabrielosorio.gestor_inteligente.model;
import com.gabrielosorio.gestor_inteligente.model.enums.PermissionType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Role {
    private long id;
    private String name;
    private String description;
    private List<Permission> permissions;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Role() {
        this.permissions = new ArrayList<>();
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Role(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    public boolean hasPermission(PermissionType permissionType) {
        return permissions.stream()
                .anyMatch(p -> p.getPermissionType() == permissionType);
    }

    public void addPermission(Permission permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removePermission(Permission permission) {
        if (permissions.remove(permission)) {
            this.updatedAt = LocalDateTime.now();
        }
    }


    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() { return description; }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public List<Permission> getPermissions() { return new ArrayList<>(permissions); }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = new ArrayList<>(permissions);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id == role.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}