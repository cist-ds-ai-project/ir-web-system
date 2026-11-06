import React from "react";
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    Tooltip,
    ResponsiveContainer,
    CartesianGrid,
    Cell
} from "recharts";

export type DataPoint = {
    range: string;
    count: number;
};

interface Props{
    data: DataPoint[];
    yMax?: number;
    unit?: string;
    label?:string;
    xLabel?:string;
    yLabel?:string;
}

const COLORS = ["#5b9cf0"];

export function HistogramChart({data, yMax, unit = "人", label = "人数", xLabel = "GPA", yLabel = "人数"}: Props) {

    const maxCount = Math.max(...data.map(d => d.count), 0);

    const axisMax = yMax ?? Math.ceil(maxCount / 10) * 10;

    return (
        <ResponsiveContainer width="100%" height="100%">
            <BarChart
                data={data}
                margin={{ top: 10, right: 20, left: 0, bottom: 10 }}
                barCategoryGap={0}
                barGap={0}
            >
                <CartesianGrid strokeDasharray="3 3" />

                <XAxis
                    dataKey="range"
                    tick={{ fontSize: 12 }}
                    label={{
                        value: xLabel,
                        position: "insideBottom",
                        offset: -5
                    }}
                />

                <YAxis
                    allowDecimals={false}
                    domain={[0, axisMax]}
                    tick={{ fontSize: 12 }}
                    label={{
                        value: yLabel,
                        angle: -90,
                        position: "insideLeft"
                    }}
                />

                <Tooltip
                    formatter={(value) => [`${value}${unit}`, label]}
                    labelFormatter={(label) => `${xLabel}: ${label}`}
                />

                <Bar dataKey="count">
                    {data.map((_, i) => (
                        <Cell
                            key={i}
                            fill={COLORS[i % COLORS.length]}
                        />
                    ))}
                </Bar>

            </BarChart>
        </ResponsiveContainer>
    );
}