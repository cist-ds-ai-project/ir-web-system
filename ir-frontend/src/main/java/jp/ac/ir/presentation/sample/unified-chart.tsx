import * as React from 'react';
import { ReactAdapterElement, type RenderHooks } from "Frontend/generated/flow/ReactAdapter";
import{
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    Cell,
    LabelList,
    PieChart,
    Pie,
    Legend

} from "recharts";

interface DataPoint {
    name: string;
    value: number;
}
type GradeCounts = Record<string, number>;
const BAR_COLORS = ['#5b9cf0', '#5b9cf0', '#5b9cf0', '#5b9cf0', '#5b9cf0'];
const GRADE_COLORS = ['#135FA8','#3E8CC3','#70B1D8', '#A4C5D9', '#C6D7E4'];
function NoDataView() {
    return (
        <div style={{
            width: '100%',
            height: '100%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--lumo-secondary-text-color, #777)',
            fontSize: '0.9rem',
        }}>
            データがありません
        </div>
    );
}

function formatData(chartData: GradeCounts, gradeOrder: string[]): DataPoint[]{
    if (!chartData || Object.keys(chartData).length === 0) return [];

    const keys = Object.keys(chartData);

    // Java側の順序を優先
    const orderedKnown = gradeOrder.filter((grade) => keys.includes(grade));

    // 未知ラベルも後ろに追加
    const unknown = keys.filter((grade) => !gradeOrder.includes(grade)).sort();

    const finalOrder = [...orderedKnown, ...unknown];

    return finalOrder.map((grade) => ({
        name: grade,
        value: chartData[grade] ?? 0,
    }));
}

/**
 * 棒グラフ（人数表示）
 */
function BarChartRenderer({ data }: { data: DataPoint[] }) {
    return (
        <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis
                    dataKey="name"
                    tick={{ fontSize: 13 }}
                    axisLine={false}
                    tickLine={false}
                />
                <YAxis
                    allowDecimals={false}
                    tick={{ fontSize: 12 }}
                    axisLine={false}
                    tickLine={false}
                />
                <Tooltip
                    formatter={(value): [string, string] => [`${value}人`, '人数']}
                    labelFormatter={(label) => `成績: ${label}`}
                />
                <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                    {data.map((entry, idx) => (
                        <Cell
                            key={idx}
                            fill={BAR_COLORS[idx % BAR_COLORS.length]}
                        />
                    ))}
                </Bar>
            </BarChart>
        </ResponsiveContainer>
    );
}

/**
 * 帯グラフ（％表示）
 * 100%積み上げ横棒
 */
function BandChartRenderer({ data }: { data: DataPoint[] }) {
    const row: Record<string, number | string> = { label: "割合" };

    data.forEach((item) => {
        row[item.name] = item.value;
    });

    return (
        <ResponsiveContainer width="100%" height="100%">
            <BarChart
                data={[row]}
                layout="vertical"
                margin={{ top: 20, right: 20, left: 20, bottom: 20 }}
            >
                <XAxis
                    type="number"
                    domain={[0, 100]}
                    tickFormatter={(value) => `${value}%`}
                />
                <YAxis type="category" dataKey="label" hide />
                <Tooltip
                    formatter={(value,name)=> [`${value}%`, String(name)]}
                />

                {data.map((item, idx) => (
                    <Bar
                        key={item.name}
                        dataKey={item.name}
                        stackId="a"
                        fill={GRADE_COLORS[idx % GRADE_COLORS.length]}
                    >
                        <LabelList
                            dataKey={item.name}
                            position="inside"
                            formatter={(value) => typeof  value === "number" && value >= 7 ? `${item.name} ${value}%` : ""}
                            style={{ fill: "#fff", fontSize: 12 }}
                        />
                    </Bar>
                ))}
            </BarChart>
        </ResponsiveContainer>
    );
}

function PieChartRenderer({ data }: { data: DataPoint[] }) {
    return (
        <ResponsiveContainer width="100%" height="100%">
            <PieChart>
            <Pie
                data = {data}
                cx = "50%"
                cy = "50%"
                outerRadius = "80%"
                dataKey = "value"
                label={({ name, value }) => `${name}: ${value}人`}
            >
                {data.map((entry,index) =>(
                    <Cell key= {`cell-${index}`}fill = {GRADE_COLORS[index % GRADE_COLORS.length]} />
                ))}
            </Pie>
            <Tooltip
                formatter={(value): [string, string] => [`${value}人`, '人数']}/>
            <Legend />
        </PieChart>
        </ResponsiveContainer>
    );
}



class UnifiedChart extends ReactAdapterElement {
    protected override render(hooks: RenderHooks): React.ReactElement | null {
        // JSON文字列 として受け取る
        // Java側から受け取る
        const [chartType] = hooks.useState<string>('chartType', 'bar');
        const [chartDataJson] = hooks.useState<string>('chartDataJson', '{}');
        const [gradeOrderJson] = hooks.useState<string>('gradeOrderJson', '[]');

        /**
         * JSON文字列 → オブジェクト変換
         */
        const chartData: GradeCounts = React.useMemo(() => {
            try {
                return JSON.parse(chartDataJson || '{}') as GradeCounts;
            } catch {
                return {};
            }
        }, [chartDataJson]);

        /**
         * JSON文字列 → 配列変換
         */
        const gradeOrder: string[] = React.useMemo(() => {
            try {
                return JSON.parse(gradeOrderJson || '[]');
            } catch {
                return [];
            }
        }, [gradeOrderJson]);

        /**
         * Recharts 用データに変換
         */
        const data: DataPoint[] = React.useMemo(() => {
            return formatData(chartData, gradeOrder);
        },[chartData, gradeOrder]);

        if (!data || data.length === 0) {
            return <NoDataView />;
        }

        return (
            <div style={{
                width: '100%',
                height: '100%',
                padding: '8px',
                boxSizing: 'border-box',
            }}>
                {chartType === "bar" && <BarChartRenderer data={data} />}
                {chartType === "band" && <BandChartRenderer data={data} />}
                {chartType === "pie" && <PieChartRenderer data={data} />}
            </div>
        );
    }
}
customElements.define('unified-chart', UnifiedChart);