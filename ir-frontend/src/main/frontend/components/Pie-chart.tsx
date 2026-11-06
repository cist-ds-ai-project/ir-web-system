import * as React from "react";
import { ReactAdapterElement, type RenderHooks } from "Frontend/generated/flow/ReactAdapter";

import {
    PieChart as RechartsPieChart,
    Pie,
    Tooltip,
    ResponsiveContainer,
    Cell
} from "recharts";

interface DataPoint {
    name: string;
    value: number;
}

const PIE_COLORS = [
    "#135FA8",
    "#3E8CC3",
    "#70B1D8",
    "#A4C5D9",
    "#C6D7E4",
    "#D1E0EC",
    "#E2EAF1"
];

function NoDataView() {
    return (
        <div
            style={{
                width: "100%",
                height: "100%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center"
            }}
        >
            データがありません
        </div>
    );
}

class PieChartElement extends ReactAdapterElement {

    protected override render(hooks: RenderHooks): React.ReactElement | null {

        const [chartDataJson] = hooks.useState<string>("chartDataJson", "{}");

        console.log("chartDataJson =", chartDataJson);

        const data: DataPoint[] = React.useMemo(() => {
            try {
                const parsed = JSON.parse(chartDataJson ?? "{}");

                return Object.entries(parsed).map(([name, value]) => ({
                    name,
                    value: Number(value)
                }));
            } catch (e) {
                console.error(e);
                return [];
            }
        }, [chartDataJson]);

        console.log(data);

        if (data.length === 0) {
            return <NoDataView />;
        }

        return (
            <div
                style={{
                    width: "100%",
                    height: "100%",
                    padding: "8px",
                    boxSizing: "border-box"
                }}
            >
                <ResponsiveContainer width="100%" height={400}>
                    <RechartsPieChart>
                        <Pie
                            data={data}
                            dataKey="value"
                            nameKey="name"
                            cx="50%"
                            cy="50%"
                            outerRadius={140}
                            label
                        >
                            {data.map((_, index) => (
                                <Cell
                                    key={index}
                                    fill={PIE_COLORS[index % PIE_COLORS.length]}
                                />
                            ))}
                        </Pie>

                        <Tooltip
                            formatter={(value) => `${value}人`}
                            labelFormatter={(label) => label}
                        />
                    </RechartsPieChart>
                </ResponsiveContainer>
            </div>
        );
    }
}

if (!customElements.get("pie-chart")) {
    customElements.define("pie-chart", PieChartElement);
}