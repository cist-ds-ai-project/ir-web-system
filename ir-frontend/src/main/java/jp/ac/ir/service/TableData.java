package jp.ac.ir.service;

import java.util.List;

public record TableData<T>(
        List<T> data

) {
}
