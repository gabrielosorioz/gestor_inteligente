package com.gabrielosorio.gestor_inteligente.model;

public class RolePermission {

    private long id;
    private Role role;
    private Permission permission;

    public RolePermission(Role role, Permission permission) {
        this.role = role;
        this.permission = permission;
    }

    public RolePermission(long id, Role role, Permission permission) {
        this.id = id;
        this.role = role;
        this.permission = permission;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }

    @Override
    public String toString() {
        return "RolePermission{" +
                "id=" + id +
                ", role=" + (role != null ? role.getName() : "null") +
                ", permission=" + (permission != null ? permission.getName() : "null") +
                '}';
    }
}
