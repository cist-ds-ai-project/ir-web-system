import {
    BarChart, Bar, XAxis, YAxis,
    CartesianGrid, Tooltip, ResponsiveContainer, Cell
} from "recharts";
interface DataPoint {
    name: string;
    value: number;
}

interface Props {
    data: DataPoint[];
    maxValue?: number;
    unit?: string;
    label?: string;
}

const BAR_COLORS = ['#5b9cf0'];
export function BarChartComponent({ data, maxValue, unit = "人",label = "人数" }: Props){
    return (
        <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name"
                        axisLine={false}
                        tickLine={false}
                />
                <YAxis allowDecimals={false}
                       domain={[0, maxValue || 'auto']}
                       axisLine={false}
                       tickLine={false}
                />

                <Tooltip
                    formatter={(v) => [`${v}${unit}`, label]}
                />

                <Bar dataKey="value">
                    {data.map((_, i) => (
                        <Cell key={i} fill={BAR_COLORS[i % BAR_COLORS.length]}/>
                    ))}
                </Bar>
            </BarChart>
        </ResponsiveContainer>
    );
}