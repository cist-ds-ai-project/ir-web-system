package jp.ac.ir.service.scholarship;

import jp.ac.ir.model.dto.scholarship.ScholarshipResponse;
import jp.ac.ir.model.dto.scholarship.ScholarshipTrendResponse;
import jp.ac.ir.repository.scholarship.ScholarshipApiClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScholarshipService {

    private final ScholarshipApiClient scholarshipApiClient;

    public ScholarshipService(ScholarshipApiClient scholarshipApiClient) {
        this.scholarshipApiClient = scholarshipApiClient;
    }

    public ScholarshipResponse getScholarshipDistribution(List<String> type, List<String> academicYear, List<String> faculty, List<String> department) {
        return scholarshipApiClient.getScholarshipDistribution(type, academicYear, faculty, department);
    }

    public ScholarshipTrendResponse getScholarshipTrend(List<String> type, List<String> faculty, List<String> department) {
        return scholarshipApiClient.getScholarshipTrend(type, faculty, department);
    }

    public List<String> getTypes() {
        return scholarshipApiClient.getTypes();
    }

    public List<String> getAcademicYears() {
        return scholarshipApiClient.getAcademicYears();
    }

    public List<String> getFaculties() {
        return scholarshipApiClient.getFaculties();
    }

    public List<String> getDepartments(String faculty) {
        return scholarshipApiClient.getDepartments(faculty);
    }

    public List<String> getEnrollmentDates() {
        return scholarshipApiClient.getEnrollmentDates();
    }
}