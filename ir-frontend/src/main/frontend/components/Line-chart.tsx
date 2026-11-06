import {
    LineChart, Line, XAxis, YAxis,
    CartesianGrid, Tooltip, ResponsiveContainer, Legend
} from "recharts";

// 1データポイントに複数の値（value, value2など）を持てるように拡張
interface DataPoint {
    name: string;
    [key: string]: string | number;
}

// 描画する線の設定を定義するインターフェース
interface LineConfig {
    key: string;    // DataPointのプロパティ名
    color: string;  // 線の色
    label: string;  // 凡例やツールチップでの表示名
}

interface Props {
    data: DataPoint[];
    lines: LineConfig[]; // 線の設定を配列で受け取る
    maxValue?: number;
    xAxisLabel?: string;
    yAxisLabel?: string;
}

export function LineChartComponent({ data, lines, maxValue, xAxisLabel, yAxisLabel }: Props) {
    return (
        <ResponsiveContainer width="100%" height={400}>
            <LineChart data={data} margin={{ top: 20, right: 30, left: 30, bottom: 20 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis
                    dataKey="name"
                    axisLine={false}
                    tickLine={false}
                    label={{ value: xAxisLabel, position: 'insideBottom', offset: -10 }}
                />
                <YAxis
                    allowDecimals={false}
                    domain={[0, maxValue || 'auto']}
                    axisLine={false}
                    tickLine={false}
                    label={{ value: yAxisLabel, angle: -90, position: 'insideLeft', offset: 10 }}
                />

                <Tooltip
                    cursor={{ strokeDasharray: '3 3' }}
                />

                {/* 2本以上の線の時は凡例を表示すると分かりやすい */}
                {lines.length > 1 && <Legend verticalAlign="top" height={36}/>}

                {/* lines配列をループしてLineを描画 */}
                {lines.map((line) => (
                    <Line
                        key={line.key}
                        type="linear"
                        dataKey={line.key}
                        name={line.label} // ツールチップや凡例に反映される
                        stroke={line.color}
                        strokeWidth={2}
                        dot={{ r: 4 }}
                        activeDot={{ r: 6 }}
                    />
                ))}
            </LineChart>
        </ResponsiveContainer>
    );
}