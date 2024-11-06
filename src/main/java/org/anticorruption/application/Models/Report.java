package org.anticorruption.application.Models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Report {
    private Long id;
    private String dateSubmitted;
    private String reporterId;
    private String incidentDate;
    private String incidentTime;
    private String incidentLocation;
    private String involvedPersons;
    private String description;
    private String evidenceDescription;
    private String witnesses;
    private String status;
    private String assignedTo;
    private String lastUpdated;
    private String solution;

    // Геттеры и сеттеры
}