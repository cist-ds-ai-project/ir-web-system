import { formatData } from "./chart-utils";
import { BarChartComponent } from "./bar-chart";
import { BandChartComponent } from "./band-chart";
import { HistogramChart} from "Frontend/components/histogram-chart";
import {NoDataView} from "Frontend/components/no-data";
import React from "react";
import type { DataPoint } from "./histogram-chart";

type ChartContainerProps = {
    chartType: string;
    chartData: Record<string, number>;
    gradeOrder: string[];
    countData: Record<string, number>;
    maxValue: number;
};
export function ChartContainer({
                                   chartType,
                                   chartData,
                                   gradeOrder,
                                   countData,
                                   maxValue
                               }:ChartContainerProps){
    const formattedData = React.useMemo(() => {
        return formatData(
            chartData as Record<string, number>,
            gradeOrder
        );
    }, [chartData, gradeOrder]);
    const histogramData = React.useMemo(() => {
        return chartData as unknown as DataPoint[];
    }, [chartData]);

        if (chartType === "histogram") {
            if (!histogramData.length) {
            return <NoDataView/>;
        }

            return <HistogramChart data={histogramData} yMax={maxValue > 0 ? maxValue : undefined} />;
        }


    if (!formattedData.length) return <NoDataView />;

    if (chartType === "bar") {
        return <BarChartComponent data={formattedData} maxValue={maxValue} />;
    }
    else if(chartType === "histogram"){
        return <HistogramChart data={histogramData}/>;
    }
    else{
        return <BandChartComponent data={formattedData} countData={countData}/>;
    }
}