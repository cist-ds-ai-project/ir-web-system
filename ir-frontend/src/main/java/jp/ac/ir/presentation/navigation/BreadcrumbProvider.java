package jp.ac.ir.presentation.navigation;

import java.util.List;

public interface BreadcrumbProvider {
    List<BreadcrumbItem> getBreadcrumbItems();
}