export interface DataPoint {
    name: string;
    value: number;
}

export type GradeCounts = Record<string, number>;

export function formatData(
    chartData: GradeCounts,
    gradeOrder: string[]
): DataPoint[] {

    if (!chartData || Object.keys(chartData).length === 0) return [];

    const keys = Object.keys(chartData);

    const orderedKnown = gradeOrder.filter(g => keys.includes(g));
    const unknown = keys.filter(g => !gradeOrder.includes(g)).sort();

    const finalOrder = [...orderedKnown, ...unknown];

    return finalOrder.map(g => ({
        name: g,
        value: Number(chartData[g] ?? 0)
    }));
}