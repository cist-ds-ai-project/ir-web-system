package jp.ac.ir.service.grade;

import jp.ac.ir.model.dto.grade.*;
import jp.ac.ir.repository.grade.GradeApiClient;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private final GradeApiClient gradeApiClient;

    public GradeService(GradeApiClient gradeApiClient) {
        this.gradeApiClient = gradeApiClient;
    }

    public GradeDetailResponse getLectureDetail(String lectureName) {
        if (lectureName == null || lectureName.isBlank()) {
            return null;
        }
        return gradeApiClient.getGradeDetailResponse(lectureName.trim());
    }


    public Map<String, String> getPercentages(YearlyDistribution dist) {
        Map<String, String> result = new LinkedHashMap<>();

        if (dist == null || dist.percentages() == null) {
            return result;
        }

        for (Map.Entry<String, Double> entry : dist.percentages().entrySet()) {
            Double value = entry.getValue() != null ? entry.getValue() : 0.0;
            result.put(entry.getKey(), String.format("%.1f%%", value));
        }

        return result;
    }
}
