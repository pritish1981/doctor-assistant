package com.superclinic.doctorassistant.common.util;

import java.time.Year;
import java.util.UUID;

public final class AppointmentReferenceGenerator {

    private AppointmentReferenceGenerator() {
    }

    public static String generate() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "APT-%d-%s".formatted(Year.now().getValue(), suffix);
    }
}
