package ru.rootmen.backup.backend.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/backup-postgres/api/v1/")
public class ApplicationConfig extends Application {}
