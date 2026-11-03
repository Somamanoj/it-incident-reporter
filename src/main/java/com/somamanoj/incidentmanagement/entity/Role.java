package com.somamanoj.incidentmanagement.entity;

/**
 * Enums are a Java way to store a fixed list of values.
 * Spring Security later prefixes these names with ROLE_ when checking access.
 */
public enum Role {
    EMPLOYEE,
    SUPPORT_ENGINEER,
    ADMIN
}
