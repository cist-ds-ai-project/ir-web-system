import * as React from 'react';
import { HistogramChart } from "./histogram-chart";
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
interface HistogramPoint {
    range:string;
    count:number;
}
type GradeCounts = Record<string, number>;
type ComparisonCounts = Record<string, GradeCounts>;
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
function BarChartRenderer({ data, unit ="人",label = "人数" }: { data: DataPoint[]; unit?: string; label?:string; }) {
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
                    formatter={(value): [string, string] => [`${value}${unit}`, label]}
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
 * 前年度比較用棒グラフ
 */
function ComparisonBarChartRenderer({data, gradeOrder}: {
        data: ComparisonCounts,
        gradeOrder:string[]
    }){
    const years = Object.keys(data);
    const chartData = gradeOrder.map(grade => {
            const row:any = {name: grade};
            years.forEach(year => {
                row[year] = data[year][grade] ?? 0;
            });
            return row;
        });
    return (<ResponsiveContainer width="100%" height="100%">
            <BarChart
                data={chartData}
                margin={{
                    top:10,
                    right:10,
                    left:-20,
                    bottom:0
                }}
            >
                <CartesianGrid
                    strokeDasharray="3 3"
                    vertical={false}
                />
                <XAxis
                    dataKey="name"
                    tick={{fontSize:13}}
                />
                <YAxis
                    allowDecimals={false}
                />
                <Tooltip
                    formatter={(value,name)=> [`${value}人`, String(name)]
                    }
                />
                <Legend
                    verticalAlign="top"
                    align="right"
                />
                {
                    years.map(
                        (year,index)=>(
                            <Bar
                                key={year}
                                dataKey={year}
                                fill={
                                index === 0
                                    ? "#A0A0A0"
                                    : "#5b9cf0"
                                }
                                radius={
                                    [4,4,0,0]
                                }
                            />
                        ))
                }
            </BarChart>
        </ResponsiveContainer>
    );
}

/**
 * 帯グラフ（％表示）
 * 100%積み上げ横棒
 */
function BandChartRenderer({ data, percentageUnit = "%", percentageLabel = "割合", order }: { data: DataPoint[], percentageUnit?:string, percentageLabel?:string, order?:string[] }) {
    const row: Record<string, number | string> = { label:percentageLabel };
    data.forEach((item) => {
        row[item.name] = item.value;
    });

    return (

        <div
            style={{
                width: "100%",
                height: "150px",
                position: "relative",
                zIndex: 10
            }}
        >
        <ResponsiveContainer width="100%" height="100%">
            <BarChart
                data={[row]}
                layout="vertical"
                margin={{ top: 20, right: 20, left: 20, bottom: 20 }}
            >
                <XAxis
                    type="number"
                    domain={[0, 100]}
                    tickFormatter={(value) => `${value}${percentageUnit}`}
                />
                <YAxis type="category" dataKey="label" hide />
                <Tooltip
                    itemSorter={(item) => {
                        const index = order?.indexOf(String(item.dataKey)) ?? -1;
                        return index === -1 ? Number.MAX_SAFE_INTEGER : index;
                    }}
                    formatter={(value, name) => [`${value}${percentageUnit}`, String(name)]}
                />

                {data.map((item, idx) => (
                    <Bar
                        key={item.name}
                        dataKey={item.name}
                        stackId="band"
                        fill={GRADE_COLORS[idx % GRADE_COLORS.length]}
                        barSize={50}
                        isAnimationActive={false}
                    >
                        <LabelList
                            dataKey={item.name}
                            position="inside"
                            formatter={(value) => typeof  value === "number" && value >= 7 ? `${item.name} ${value}${percentageUnit}` : ""}
                            style={{ fill: "#fff", fontSize: 12 }}
                        />
                    </Bar>
                ))}
            </BarChart>
        </ResponsiveContainer>
        </div>
    );
}

function PieChartRenderer({ data, percentageUnit = "%", percentageLabel = "割合" }: { data: DataPoint[], percentageUnit?:string, percentageLabel?:string }) {
    return (
        <ResponsiveContainer width="100%" height="100%">
            <PieChart>
            <Pie
                data = {data}
                cx = "50%"
                cy = "50%"
                outerRadius = "80%"
                dataKey = "value"
                label={({ name, value }) => `${name}: ${value}${percentageUnit}`}
            >
                {data.map((entry,index) =>(
                    <Cell key= {`cell-${index}`}fill = {GRADE_COLORS[index % GRADE_COLORS.length]} />
                ))}
            </Pie>
            <Tooltip
                formatter={(value): [string, string] => [`${value}${percentageUnit}`, percentageLabel]}/>
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
        const [comparisonDataJson] = hooks.useState<string>('comparisonDataJson', '{}');
        const [gradeOrderJson] = hooks.useState<string>('gradeOrderJson', '[]');
        const [unit] = hooks.useState<string>('unit','人');
        const [label] = hooks.useState<string>('label','人数');
        const [percentageUnit] = hooks.useState<string>('percentageUnit','%');
        const [percentageLabel] = hooks.useState<string>('percentageLabel','割合');
        const [xLabel] = hooks.useState<string>('xLabel', 'GPA');
        const [yLabel] = hooks.useState<string>('yLabel', '人数');
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

        const comparisonData: ComparisonCounts = React.useMemo(()=>{
                try{
                    return JSON.parse(comparisonDataJson || '{}');
                } catch{
                    return {};
                }
            },[comparisonDataJson]);

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


        const histogramData = React.useMemo(() => {
            try {
                return JSON.parse(chartDataJson || '[]') as HistogramPoint[];
            } catch {
                return [];
            }
        }, [chartDataJson]);

        if(Object.keys(comparisonData).length > 0){
            return (
                <div style={{
                    width:'100%',
                    height:'100%',
                    padding:'8px',
                    boxSizing:'border-box',
                }}>

                    <ComparisonBarChartRenderer
                        data={comparisonData}
                        gradeOrder={gradeOrder}
                    />
                </div>
            );
        }
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
                {chartType === "bar" && <BarChartRenderer data={data} unit={unit} label={label} />}
                {chartType === "band" && <BandChartRenderer data={data} percentageUnit={percentageUnit} percentageLabel={percentageLabel} order={gradeOrder} />}
                {chartType === "pie" && <PieChartRenderer data={data} percentageUnit={percentageUnit} percentageLabel={percentageLabel} />}
                {chartType === "histogram" && <HistogramChart data={histogramData} unit={unit} label={label} xLabel={xLabel} yLabel={yLabel} />}
            </div>
        );
    }
}
customElements.define('unified-chart', UnifiedChart);