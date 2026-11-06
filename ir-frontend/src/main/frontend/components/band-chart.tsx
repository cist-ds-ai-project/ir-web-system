
import {
    BarChart, Bar, XAxis, YAxis,
    Tooltip, ResponsiveContainer, LabelList
} from "recharts";

interface DataPoint {
    name: string;
    value: number;
}
const GRADE_COLORS = ['#135FA8','#3E8CC3','#70B1D8', '#A4C5D9', '#C6D7E4'];
export function BandChartComponent({ data, countData, unit = "%", tooltipNote = "※()内は人数"  }:{
    data: DataPoint[],
    countData: Record<string, number>
    unit?: string;
    tooltipNote?: string;
}) {
    const clamp = (v: number) => v > 99.95 ? 100 : v;

    const row: any = { label: "割合" };
    data.forEach(d => row[d.name] = d.value);

    return (
        <ResponsiveContainer width="100%" height="100%">
            <BarChart data={[row]} layout="vertical" margin={{top: 0, right: 0, left: 0, bottom: 0}}>

                <XAxis
                    type="number"
                    domain={[0, 100]}
                    ticks={[0,25,50,75,100]}
                    tickFormatter={(v) =>
                        `${clamp(v).toFixed(1).replace(/\.0$/, "")}${unit}`
                    }
                />

                <YAxis type="category" dataKey="label" hide />

                <Tooltip
                    content={({ payload }) => {
                        if (!payload) return null;

                        const sorted = data.map(d =>
                            payload.find(p => p.name === d.name)
                        ).filter(Boolean);

                        return (
                            <div style={{
                                background: "#fff",
                                padding: 8,
                                fontSize: 12,
                                color: "#000"
                            }}>
                                <div style={{ fontSize: 11 }}>
                                    {tooltipNote}
                                </div>

                                {sorted.map((p: any, i) => {
                                    const percent = clamp(Number(p.value))
                                        .toFixed(1)
                                        .replace(/\.0$/, "");

                                    const count = countData[p.name] ?? 0;

                                    return (
                                        <div key={i}>
                                            {p.name}: {percent}{unit}({count})
                                        </div>
                                    );
                                })}
                            </div>
                        );
                    }}
                />

                {data.map((d, i) => (
                    <Bar key={d.name} dataKey={d.name} stackId="a" fill={GRADE_COLORS[i]}>
                        <LabelList
                            dataKey={d.name}
                            fill="#fff"
                            position="inside"
                            formatter={(v) => {
                                const num = Number(v);
                                if (isNaN(num) || num < 5) return "";
                                return `${clamp(num).toFixed(1).replace(/\.0$/, "")}${unit}`;
                            }}
                        />
                    </Bar>
                ))}
            </BarChart>
        </ResponsiveContainer>
    );
}
