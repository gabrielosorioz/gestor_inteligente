package com.gabrielosorio.gestor_inteligente.model.enums;

public enum PermissionType {
    // User Management
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    VIEW_USER,
    RESET_PASSWORD,

    // Role & Permission Management
    CREATE_ROLE,
    UPDATE_ROLE,
    DELETE_ROLE,
    VIEW_ROLE,
    ASSIGN_PERMISSIONS,

    // Product Management
    CREATE_PRODUCT,
    UPDATE_PRODUCT,
    DELETE_PRODUCT,
    VIEW_PRODUCT,
    VIEW_PRODUCT_INVENTORY,
    UPDATE_INVENTORY,

    // Sales Operations
    CREATE_SALE,
    CANCEL_SALE,
    VIEW_SALE,
    APPLY_DISCOUNT,
    SALE_REFUND,

    // Cash Management
    OPEN_CASH_REGISTER,
    CLOSE_CASH_REGISTER,
    VIEW_CASH_BALANCE,
    ADJUST_CASH_BALANCE,
    VIEW_CASH_HISTORY,

    // Reports
    VIEW_SALES_REPORT,
    VIEW_INVENTORY_REPORT,
    VIEW_USER_ACTIVITY_REPORT,
    VIEW_FINANCIAL_REPORT,

    // System Administration
    SYSTEM_CONFIGURATION,
    BACKUP_DATA,
    VIEW_SYSTEM_LOGS,
    MAINTAIN_DATABASE
}
