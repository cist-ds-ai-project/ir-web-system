package jp.ac.ir.service.gpa;
import jp.ac.ir.model.dto.gpa.*;
import jp.ac.ir.repository.gpa.GpaApiClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GpaService {

    private final GpaApiClient gpaApiClient;
    private GpaSelectionOptionsResponse optionsCache = null;

    public GpaService(GpaApiClient gpaClient) {
        this.gpaApiClient = gpaClient;
    }

    public GpaSelectionOptionsResponse getOptions() {
        if (optionsCache == null) {
            optionsCache = gpaApiClient.getOptions();
        }
        return optionsCache;
    }

    public List<Double> getRawGpa(GpaFilter filter) {
        GpaRawResponse res = gpaApiClient.getRawGpa(normalizeFilter(filter));

        return res != null && res.gpas() != null
                ? res.gpas()
                : List.of();
    }

    public List<DepartmentGpa> getGpaByDepartment(GpaFilter filter) {
        DepartmentGpaResponse res = gpaApiClient.getByDepartment(normalizeFilter(filter));

        return res != null && res.departments() != null
                ? res.departments()
                : List.of();
    }

    public GpaStatsResponse getStats(GpaFilter filter) {
        return gpaApiClient.getStats(normalizeFilter(filter));
    }


    public List<HistogramBin> buildHistogram(List<Double> gpas,double min, double max) {
        if (gpas == null || gpas.isEmpty()) {
            return List.of();
        }
        //階級数をスタージェスの公式で決定、範囲を最大最小から取ってくる
        int n = gpas.size();
        int classCount = (int) Math.ceil(1 + Math.log(n) / Math.log(2));
        double width = (max - min) / classCount;
        if (width == 0) {
            return List.of(new HistogramBin(String.format("%.2f - %.2f", min, max), n));
        }
        int[] counts = new int[classCount];

        for (double gpa : gpas) {
            int index = (int)((gpa - min) / width);
            if (index >= classCount) {
                index = classCount - 1;
            }
            if (index < 0) {
                index = 0;
            }
            counts[index]++;
        }


        List<HistogramBin> result = new ArrayList<>();

        for (int i = 0; i < classCount; i++) {
            double from = min + width * i;
            double to = from + width;

            result.add(new HistogramBin(String.format("%.1f - %.1f", from, to), counts[i]));
        }

        return result;
    }

    private GpaFilter normalizeFilter(GpaFilter f) {

        if (f == null) {
            return new GpaFilter(List.of(), null, List.of(), List.of());
        }

        return new GpaFilter(
                // year
                f.years() == null
                        ? List.of()
                        : f.years()
                        .stream()
                        .filter(y -> y != null && y != 0)
                        .toList(),

                // faculty
                normalizeText(f.faculty()),

                //departments
                f.departments() == null
                        ? List.of()
                        : f.departments().stream()
                        .filter(s -> s != null
                                && !s.isBlank()
                                && !"全体".equals(s))
                        .toList(),

                // grade
                f.grades() == null
                        ? List.of()
                        : f.grades().stream()
                        .filter(s -> s != null && !s.isBlank()
                                && !"全体".equals(s))
                        .toList());
    }




    private String normalizeText(String value) {
        if (value == null || value.isBlank() || value.equals("全体")) {
            return null;
        }
        return value.trim();
    }

    private String normalizeGrade(String grade) {
        if (grade == null || grade.isBlank() || grade.equals("全体")) {
            return null;
        }
        return grade
                .replace("年生", "")
                .replace("年", "")
                .trim();
    }

    public List<Map<String, Object>> convertToHistogram(List<HistogramBin> histogramBins) {

        List<Map<String, Object>> result = new ArrayList<>();

        if (histogramBins == null || histogramBins.isEmpty()) {
            return result;
        }

        for (HistogramBin bin : histogramBins) {

            Map<String, Object> row = new LinkedHashMap<>();

            row.put("range", bin.range());
            row.put("count", bin.count());

            result.add(row);
        }

        return result;
    }
}
