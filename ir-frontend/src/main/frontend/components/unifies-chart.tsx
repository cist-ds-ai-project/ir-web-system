import * as React from 'react';
import { ReactAdapterElement, type RenderHooks } from "Frontend/generated/flow/ReactAdapter";
import { LineChartComponent } from './Line-chart';

interface DataPoint {
    name: string; // 年度
    [key: string]: string | number;
}

interface LineConfig {
    key: string;
    color: string;
    label: string;
}

const COLORS = [
    '#5b9cf0',
    '#eb5757',
    '#6fcf97',
    '#bb6bd9',
    '#f2c94c',
    '#56ccf2',
    '#f08a5b',
    '#27ae60',
];

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

function formatEnrollmentData(chartData: Record<string, number>): DataPoint[] {
    if (!chartData || Object.keys(chartData).length === 0) return [];

    const sortedYears = Object.keys(chartData)
        .sort((a, b) => Number(a) - Number(b));

    return sortedYears.map(year => ({
        name: year,
        value: chartData[year]
    }));
}

function getSubjectNames(multiSeriesChartData: Record<string, Record<string, number>>): string[] {
    const subjectSet = new Set<string>();

    Object.keys(multiSeriesChartData)
        .sort((a, b) => Number(a) - Number(b))
        .forEach(year => {
            Object.keys(multiSeriesChartData[year]).forEach(subjectName => {
                subjectSet.add(subjectName);
            });
        });

    return Array.from(subjectSet);
}

function formatMultiSeriesData(
    multiSeriesChartData: Record<string, Record<string, number>>,
    subjectNames: string[]
): DataPoint[] {
    if (!multiSeriesChartData || Object.keys(multiSeriesChartData).length === 0) return [];

    const sortedYears = Object.keys(multiSeriesChartData)
        .sort((a, b) => Number(a) - Number(b));

    return sortedYears.map(year => {
        const row: DataPoint = {
            name: year
        };

        subjectNames.forEach((subjectName, index) => {
            const value = multiSeriesChartData[year]?.[subjectName];

            if (value !== undefined && value !== null) {
                row[`series${index}`] = value;
            }
        });

        return row;
    });
}

function createMultiSeriesLines(subjectNames: string[]): LineConfig[] {
    return subjectNames.map((subjectName, index) => ({
        key: `series${index}`,
        color: COLORS[index % COLORS.length],
        label: subjectName
    }));
}

class UnifiesChart extends ReactAdapterElement {
    protected override render(hooks: RenderHooks): React.ReactElement | null {
        const [chartDataJson] = hooks.useState<string>('chartDataJson', '{}');
        const [multiSeriesChartDataJson] = hooks.useState<string>('multiSeriesChartDataJson', '{}');
        const [maxValue] = hooks.useState<number | undefined>('maxValue', undefined);
        const [seriesLabel] = hooks.useState<string>('seriesLabel', '履修者数');
        const [yAxisLabel] = hooks.useState<string>('yAxisLabel', '履修者数(人)');

        const labels = {
            x: "開講年度(年)",
            y: yAxisLabel
        };

        const chartData: Record<string, number> = React.useMemo(() => {
            try {
                return JSON.parse(chartDataJson || '{}');
            } catch {
                return {};
            }
        }, [chartDataJson]);

        const multiSeriesChartData: Record<string, Record<string, number>> = React.useMemo(() => {
            try {
                return JSON.parse(multiSeriesChartDataJson || '{}');
            } catch {
                return {};
            }
        }, [multiSeriesChartDataJson]);

        const isMultiSeries = Object.keys(multiSeriesChartData).length > 0;

        const subjectNames = React.useMemo(() => {
            if (!isMultiSeries) return [];
            return getSubjectNames(multiSeriesChartData);
        }, [isMultiSeries, multiSeriesChartData]);

        const data: DataPoint[] = React.useMemo(() => {
            if (isMultiSeries) {
                return formatMultiSeriesData(multiSeriesChartData, subjectNames);
            }

            return formatEnrollmentData(chartData);
        }, [isMultiSeries, multiSeriesChartData, subjectNames, chartData]);

        const lines: LineConfig[] = React.useMemo(() => {
            if (isMultiSeries) {
                return createMultiSeriesLines(subjectNames);
            }

            return [
                { key: 'value', color: '#5b9cf0', label: seriesLabel }
            ];
        }, [isMultiSeries, subjectNames, seriesLabel]);

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
                <LineChartComponent
                    data={data}
                    lines={lines}
                    xAxisLabel={labels.x}
                    yAxisLabel={labels.y}
                    maxValue={maxValue}
                />
            </div>
        );
    }
}

if (!customElements.get('unifies-chart')) {
    customElements.define('unifies-chart', UnifiesChart);
}
