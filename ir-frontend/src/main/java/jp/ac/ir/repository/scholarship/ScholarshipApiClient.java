package jp.ac.ir.repository.scholarship;

import jp.ac.ir.model.dto.scholarship.ScholarshipResponse;
import jp.ac.ir.model.dto.scholarship.ScholarshipTrendResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/scholarship")
public interface ScholarshipApiClient {

    @GetExchange("/all")
    ScholarshipResponse getScholarshipDistribution(
            @RequestParam(name = "type", required = false) List<String> type,
            @RequestParam(name = "academic_year", required = false) List<String> academicYear,
            @RequestParam(name = "faculty", required = false) List<String> faculty,
            @RequestParam(name = "department", required = false) List<String> department
    );

    @GetExchange("/trend")
    ScholarshipTrendResponse getScholarshipTrend(
            @RequestParam(name = "type", required = false) List<String> type,
            @RequestParam(name = "faculty", required = false) List<String> faculty,
            @RequestParam(name = "department", required = false) List<String> department
    );

    @GetExchange("/types")
    List<String> getTypes();

    @GetExchange("/academic-years")
    List<String> getAcademicYears();

    @GetExchange("/faculties")
    List<String> getFaculties();

    @GetExchange("/departments")
    List<String> getDepartments(@RequestParam(name = "faculty", required = false) String faculty);

    @GetExchange("/enrollment-date")
    List<String> getEnrollmentDates();
}