package com.timetable.model;

public class TimeSlot {
    private int slotId;
    private String slotLabel;
    private int sortOrder;
    private boolean breakSlot;
    private String breakName;

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getSlotLabel() {
        return slotLabel;
    }

    public void setSlotLabel(String slotLabel) {
        this.slotLabel = slotLabel;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isBreakSlot() {
        return breakSlot;
    }

    public void setBreakSlot(boolean breakSlot) {
        this.breakSlot = breakSlot;
    }

    public String getBreakName() {
        return breakName;
    }

    public void setBreakName(String breakName) {
        this.breakName = breakName;
    }
}
