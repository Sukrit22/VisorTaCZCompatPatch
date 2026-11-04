package dev.visorcompat.tacz.core;

/** Read-only boundary: loader/Minecraft types must not cross into mechanism logic. */
public record GunSnapshot(String gunId, String displayId, int magazineRounds, boolean chambered, String fireMode) {}
