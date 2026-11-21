package com.hostel.complaint.dto;

import java.util.List;
import java.util.Map;

public class WeeklyAnalyticsResponse {
    private long totalComplaints;
    private long openComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private double averageResolutionHours;
    private long slaBreaches;
    private List<Map<String, Object>> categoryDistribution;
    private List<Map<String, Object>> blockDistribution;

    public WeeklyAnalyticsResponse() {}

    public long getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; }

    public long getOpenComplaints() { return openComplaints; }
    public void setOpenComplaints(long openComplaints) { this.openComplaints = openComplaints; }

    public long getInProgressComplaints() { return inProgressComplaints; }
    public void setInProgressComplaints(long inProgressComplaints) { this.inProgressComplaints = inProgressComplaints; }

    public long getResolvedComplaints() { return resolvedComplaints; }
    public void setResolvedComplaints(long resolvedComplaints) { this.resolvedComplaints = resolvedComplaints; }

    public double getAverageResolutionHours() { return averageResolutionHours; }
    public void setAverageResolutionHours(double averageResolutionHours) { this.averageResolutionHours = averageResolutionHours; }

    public long getSlaBreaches() { return slaBreaches; }
    public void setSlaBreaches(long slaBreaches) { this.slaBreaches = slaBreaches; }

    public List<Map<String, Object>> getCategoryDistribution() { return categoryDistribution; }
    public void setCategoryDistribution(List<Map<String, Object>> categoryDistribution) { this.categoryDistribution = categoryDistribution; }

    public List<Map<String, Object>> getBlockDistribution() { return blockDistribution; }
    public void setBlockDistribution(List<Map<String, Object>> blockDistribution) { this.blockDistribution = blockDistribution; }
}
