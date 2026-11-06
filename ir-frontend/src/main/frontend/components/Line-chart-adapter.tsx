import * as React from "react";
import { ReactAdapterElement, type RenderHooks } from "Frontend/generated/flow/ReactAdapter";
// 共有されたコンポーネントをインポート（※パスは適宜環境に合わせて変更すること）
import { LineChartComponent } from "./Line-chart";

class LineChartAdapterElement extends ReactAdapterElement {
    protected override render(hooks: RenderHooks): React.ReactElement | null {
        const [chartDataJson] = hooks.useState<string>("chartDataJson", "[]");
        const [lineConfigJson] = hooks.useState<string>("lineConfigJson", "[]");

        // ↓↓↓ デバッグ用ログ（原因特定用。原因が分かったら削除してください） ↓↓↓
        console.log("chartDataJson =", chartDataJson);
        console.log("lineConfigJson =", lineConfigJson);
        // ↑↑↑ デバッグ用ログ ↑↑↑

        const data = React.useMemo(() => {
            try {
                return JSON.parse(chartDataJson ?? "[]");
            } catch (e) {
                console.error("Data parse error", e);
                return [];
            }
        }, [chartDataJson]);

        const lines = React.useMemo(() => {
            try {
                return JSON.parse(lineConfigJson ?? "[]");
            } catch (e) {
                console.error("Line config parse error", e);
                return [];
            }
        }, [lineConfigJson]);

        if (data.length === 0 || lines.length === 0) {
            return (
                <div style={{ width: "100%", height: "100%", display: "flex", alignItems: "center", justifyContent: "center" }}>
                    データがありません
                </div>
            );
        }

        return (
            <div style={{ width: "100%", height: "100%", padding: "8px", boxSizing: "border-box" }}>
                <LineChartComponent
                    data={data}
                    lines={lines}
                    xAxisLabel="入学年度"
                    yAxisLabel="人数"
                />
            </div>
        );
    }
}

if (!customElements.get("line-chart")) {
    customElements.define("line-chart", LineChartAdapterElement);
}