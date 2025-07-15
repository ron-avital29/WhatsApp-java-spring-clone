package com.app.model;

/**
 * ReportStatus represents the status of a report in the application.
 * It can be one of the following:
 * - PENDING: The report is awaiting action.
 * - DISMISSED: The report has been dismissed without action.
 * - ACTION_TAKEN: Action has been taken on the report.
 */
public enum ReportStatus {
    PENDING,
    DISMISSED,
    ACTION_TAKEN
}
