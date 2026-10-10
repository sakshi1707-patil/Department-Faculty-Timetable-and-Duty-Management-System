package com.timetable.model;

public class FixedActivity {
    private int fixedActivityId;
    private int divisionId;
    private String divisionCode;
    private String activityTitle;
    private String facultyText;
    private String roomText;
    private String activityDay;
    private int slotId;
    private String batchLabel;
    private String notes;

    public int getFixedActivityId() {
        return fixedActivityId;
    }

    public void setFixedActivityId(int fixedActivityId) {
        this.fixedActivityId = fixedActivityId;
    }

    public int getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(int divisionId) {
        this.divisionId = divisionId;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getFacultyText() {
        return facultyText;
    }

    public void setFacultyText(String facultyText) {
        this.facultyText = facultyText;
    }

    public String getRoomText() {
        return roomText;
    }

    public void setRoomText(String roomText) {
        this.roomText = roomText;
    }

    public String getActivityDay() {
        return activityDay;
    }

    public void setActivityDay(String activityDay) {
        this.activityDay = activityDay;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getBatchLabel() {
        return batchLabel;
    }

    public void setBatchLabel(String batchLabel) {
        this.batchLabel = batchLabel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

