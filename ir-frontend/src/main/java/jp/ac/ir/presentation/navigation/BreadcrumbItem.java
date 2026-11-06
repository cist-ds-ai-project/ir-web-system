package jp.ac.ir.presentation.navigation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;

public record BreadcrumbItem(
        String label,
        Class<? extends Component> navigationTarget,
        RouteParameters routeParameters,
        QueryParameters queryParameters
) {
    public static BreadcrumbItem of(String label, Class<? extends Component> navigationTarget) {
        return new BreadcrumbItem(label, navigationTarget, null, null);
    }

    public static BreadcrumbItem of(
            String label,
            Class<? extends Component> navigationTarget,
            RouteParameters routeParameters
    ) {
        return new BreadcrumbItem(label, navigationTarget, routeParameters, null);
    }

    public static BreadcrumbItem of(
            String label,
            Class<? extends Component> navigationTarget,
            RouteParameters routeParameters,
            QueryParameters queryParameters
    ) {
        return new BreadcrumbItem(label, navigationTarget, routeParameters, queryParameters);
    }
}
