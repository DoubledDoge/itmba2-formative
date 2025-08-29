package com.example.itmba2_formative.objects;

public class Trip {
    private final String destination;
    private final String startDate;
    private final String endDate;
    private final int travelersCount;
    private final String activitiesSelected;
    private final String customExpenses;
    private final String notes;

    public Trip(
                String destination,
                String startDate,
                String endDate,
                int travelersCount,

                String activitiesSelected,
                String customExpenses,
                String notes) {
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.travelersCount = travelersCount;
        this.activitiesSelected = activitiesSelected;
        this.customExpenses = customExpenses;
        this.notes = notes;
    }

    public String getDestination() { return destination; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getTravelersCount() { return travelersCount; }

    public String getActivitiesSelected() { return activitiesSelected; }
    public String getCustomExpenses() { return customExpenses; }
    public String getNotes() { return notes; }
}
