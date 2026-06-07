package com.superclinic.doctorassistant.ai.tools;

import com.superclinic.doctorassistant.ai.tools.dto.DoctorSearchResult;
import com.superclinic.doctorassistant.domain.doctor.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorSearchTool {

    private final DoctorService doctorService;

    @Tool(
            name = "findDoctorByName",
            description = """
                    Search active doctors by full or partial name. \
                    Returns a list of matching doctors with specialty, rating, and contact details. \
                    Use when the patient asks for a specific doctor or wants to find doctors by name."""
    )
    public List<DoctorSearchResult> findDoctorByName(
            @ToolParam(description = "Doctor full name or partial name, e.g. 'Sharma' or 'Dr. Rajesh Sharma'")
            String name) {
        log.info("Tool invoked: findDoctorByName(name={})", name);
        List<DoctorSearchResult> results = doctorService.findDoctorByName(name).stream()
                .map(DoctorSearchResult::from)
                .toList();
        log.info("Tool findDoctorByName returned {} result(s)", results.size());
        return results;
    }

    @Tool(
            name = "findDoctorsBySpeciality",
            description = """
                    Search active doctors by medical specialty code or name. \
                    Examples: 'CARDIOLOGY', 'Cardiology', 'ORTHOPEDICS', 'Orthopedics', 'Orthopedic'. \
                    Partial names work (e.g. 'Orthopedic' matches 'Orthopedics'). \
                    Returns doctors ordered by rating. \
                    Use when the patient needs a doctor for a specific medical specialty."""
    )
    public List<DoctorSearchResult> findDoctorsBySpeciality(
            @ToolParam(description = "Medical specialty code or name, e.g. 'CARDIOLOGY' or 'Pediatrics'")
            String speciality) {
        log.info("Tool invoked: findDoctorsBySpeciality(speciality={})", speciality);
        List<DoctorSearchResult> results = doctorService.findDoctorsBySpeciality(speciality).stream()
                .map(DoctorSearchResult::from)
                .toList();
        log.info("Tool findDoctorsBySpeciality returned {} result(s)", results.size());
        return results;
    }
}
