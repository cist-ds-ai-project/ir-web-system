import * as React from 'react';
import { ReactAdapterElement, type RenderHooks } from "Frontend/generated/flow/ReactAdapter";
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    Tooltip,
    ResponsiveContainer,
    LabelList,
    CartesianGrid
} from "recharts";

type SurveyOption = {
    label: string;
    count: number;
    option_order?: number | string | null;
    optionOrder?: number | string | null;
    order?: number | string | null;
};

type YearlySurveyData = {
    year: number;
    options?: SurveyOption[] | null;
};

type AnswerItem = {
    label: string;
    order: number;
    color: string;
};

type ChartRow = {
    year: number;
    _total: number;
    [key: string]: string | number;
};

const BAND_COLORS = ['#135FA8', '#3E8CC3', '#70B1D8', '#A4C5D9', '#C6D7E4'];

const readOptionOrder = (option: SurveyOption) => {
    const value = Number(option.option_order ?? option.optionOrder ?? option.order);
    return Number.isFinite(value) ? value : Number.MAX_SAFE_INTEGER;
};

class MultiYearSurveyChart extends ReactAdapterElement {
    protected override render(hooks: RenderHooks): React.ReactElement | null {
        const [surveyDataJson] = hooks.useState<string>('surveyDataJson', "[]");

        const surveyData = React.useMemo<YearlySurveyData[]>(() => {
            try {
                const parsed = JSON.parse(surveyDataJson);
                return Array.isArray(parsed) ? parsed : [];
            } catch {
                return [];
            }
        }, [surveyDataJson]);

        const { chartData, answerItems } = React.useMemo(() => {
            if (surveyData.length === 0) {
                return { chartData: [] as ChartRow[], answerItems: [] as AnswerItem[] };
            }

            const optionsByLabel = new Map<string, SurveyOption>();

            surveyData.forEach(row => {
                (row.options ?? []).forEach(option => {
                    const current = optionsByLabel.get(option.label);

                    if (!current || readOptionOrder(option) < readOptionOrder(current)) {
                        optionsByLabel.set(option.label, option);
                    }
                });
            });

            const answerItems = Array.from(optionsByLabel.values())
                    .sort((a, b) => {
                        const orderDiff = readOptionOrder(b) - readOptionOrder(a);
                        return orderDiff !== 0 ? orderDiff : a.label.localeCompare(b.label);
                    })
                    .map((option, index) => ({
                        label: option.label,
                        order: readOptionOrder(option),
                        color: BAND_COLORS[index % BAND_COLORS.length]
                    }));

            const chartData = surveyData.map(row => {
                const newRow: ChartRow = {
                    year: row.year,
                    _total: 0
                };

                const yearTotal = (row.options ?? []).reduce(
                        (sum, option) => sum + Number(option.count ?? 0),
                        0
                );

                newRow._total = yearTotal;

                answerItems.forEach(item => {
                    newRow[`${item.label}_count`] = 0;
                    newRow[item.label] = 0;
                });

                (row.options ?? []).forEach(option => {
                    const count = Number(option.count ?? 0);
                    newRow[`${option.label}_count`] = count;
                    newRow[option.label] = yearTotal > 0
                            ? Number((count / yearTotal * 100).toFixed(1))
                            : 0;
                });

                return newRow;
            });

            return { chartData, answerItems };
        }, [surveyData]);

        if (chartData.length === 0) {
            return <div style={{ padding: '20px' }}>データがありません</div>;
        }

        return (
            <div style={{ width: '100%', height: '100%', padding: '16px', boxSizing: 'border-box' }}>
                <div style={{
                    display: 'flex',
                    justifyContent: 'center',
                    flexWrap: 'wrap',
                    gap: '12px',
                    paddingBottom: '20px',
                    fontSize: '13px'
                }}>
                    {answerItems.map(item => (
                        <span key={item.label} style={{ display: 'inline-flex', alignItems: 'center', gap: '5px' }}>
                            <span style={{
                                width: '14px',
                                height: '14px',
                                display: 'inline-block',
                                backgroundColor: item.color
                            }} />
                            {item.label}
                        </span>
                    ))}
                </div>

                <ResponsiveContainer width="100%" height={chartData.length * 55 + 110}>
                    <BarChart layout="vertical" data={chartData} margin={{ top: 10, right: 30, left: 40, bottom: 5 }}>
                        <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                        <XAxis type="number" domain={[0, 100]} unit="%" tick={{ fontSize: 12 }} />
                        <YAxis type="category" dataKey="year" tick={{ fontSize: 13, fontWeight: 'bold' }} width={80} />

                        <Tooltip
                            content={({ active, payload, label }) => {
                                if (!active || !payload || payload.length === 0) {
                                    return null;
                                }

                                const data = payload[0].payload as ChartRow;

                                return (
                                    <div style={{
                                        backgroundColor: '#fff',
                                        border: '1px solid #ccc',
                                        padding: '10px',
                                        fontSize: '12px'
                                    }}>
                                        <p style={{ margin: '0 0 5px', fontWeight: 'bold' }}>{label}</p>
                                        {answerItems.map(item => (
                                            <p key={item.label} style={{ margin: '0', color: item.color }}>
                                                {item.label}: {data[item.label]}% ({data[`${item.label}_count`]}人)
                                            </p>
                                        ))}
                                        <hr style={{ margin: '5px 0', border: 'none', borderTop: '1px solid #eee' }} />
                                        <p style={{ margin: '0', fontWeight: 'bold', color: '#666' }}>
                                            年度合計 {data._total}人
                                        </p>
                                    </div>
                                );
                            }}
                        />

                        {answerItems.map(item => (
                            <Bar
                                key={item.label}
                                dataKey={item.label}
                                name={item.label}
                                stackId="a"
                                fill={item.color}
                                barSize={40}
                            >
                                <LabelList
                                    dataKey={item.label}
                                    position="inside"
                                    formatter={(value: unknown) => {
                                        const num = Number(value);
                                        return Number.isFinite(num) && num >= 5 ? `${num}%` : '';
                                    }}
                                    style={{ fill: '#fff', fontSize: '11px', pointerEvents: 'none' }}
                                />
                            </Bar>
                        ))}
                    </BarChart>
                </ResponsiveContainer>
            </div>
        );
    }
}

customElements.define('multi-year-survey-chart', MultiYearSurveyChart);
