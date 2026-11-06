// 学生成績推移チャート【サンプル】

import * as React from 'react';
import { ReactAdapterElement, type RenderHooks } from 'Frontend/generated/flow/ReactAdapter';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface DataPoint {
    name: string;
    value: number;
}

class SampleStudentPerformanceChartElement extends ReactAdapterElement {
    protected override render(hooks: RenderHooks): React.ReactElement | null {
        const [data] = hooks.useState<DataPoint[]>('data', []);

        return (
            <div style={{width: '100%', height: '400px', backgroundColor: '#f8f9fa', padding: '20px', borderRadius: '8px'}}>
                <h3 style={{marginBottom: '20px', textAlign: 'center', color: '#333'}}>学生成績推移 (GPA)</h3>
                <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={data} margin={{top: 10, right: 30, left: 0, bottom: 0}}>
                        <CartesianGrid strokeDasharray="3 3"/>
                        <XAxis dataKey="name"/>
                        <YAxis domain={[0, 4]}/>
                        <Tooltip/>
                        <Area type="monotone" dataKey="value" stroke="#8884d8" fill="#8884d8" fillOpacity={0.3}/>
                    </AreaChart>
                </ResponsiveContainer>
            </div>
        );
    }
}

customElements.define('sample-student-performance-chart', SampleStudentPerformanceChartElement);
