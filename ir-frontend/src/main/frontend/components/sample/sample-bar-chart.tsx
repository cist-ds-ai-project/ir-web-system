// 棒グラフコンポーネント【サンプル】
//
// データ形式: { name: "カテゴリ名", value: 数値 } の配列

import * as React from 'react';

// Vaadin と React を橋渡しするためのアダプター
import {ReactAdapterElement, type RenderHooks} from 'Frontend/generated/flow/ReactAdapter';

// Recharts（グラフ描画ライブラリ）から必要なパーツをインポート
import {BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell} from 'recharts';

// データの型定義
interface DataPoint {
    name: string;
    value: number;
}

// 棒ごとの色の設定
const BAR_COLORS = ['#6366f1', '#8b5cf6', '#603ba7', '#c4b5fd', '#7c3aed', '#4f46e5'];

// Vaadinとの連携用カスタム要素クラス
class SampleBarChartElement extends ReactAdapterElement {
    protected override render(hooks: RenderHooks): React.ReactElement | null {
        // 引数からデータを取得
        const [data] = hooks.useState<DataPoint[]>('data', []);
        const [title] = hooks.useState<string>('title', '');

        // データが空の場合の例外処理
        if (data.length === 0) {
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

        // グラフの見た目を定義する
        return (
            <div style={{width: '100%', height: '100%', padding: '16px', boxSizing: 'border-box'}}>

                {/* タイトルが設定されている場合のみ表示 */}
                {title && (
                    <h3 style={{
                        margin: '0 0 16px 0',
                        textAlign: 'center',
                        fontSize: '1rem',
                        fontWeight: 600,
                        color: 'var(--lumo-body-text-color, #333)',
                    }}>
                        {title}
                    </h3>
                )}

                {/* ResponsiveContainerにより親要素のサイズに合わせてグラフを自動リサイズされる */}
                <ResponsiveContainer width="100%" height={title ? '85%' : '100%'}>

                    {/* BarChart: 棒グラフ本体 */}
                    <BarChart data={data} margin={{top: 10, right: 20, left: 0, bottom: 5}}>
                        <CartesianGrid strokeDasharray="3 3"/>

                        <XAxis dataKey="name"/>

                        <YAxis/>

                        <Tooltip/>

                        <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                            {/* 棒ごとに個別の色を設定 */}
                            {data.map((_, idx) => (
                                <Cell key={idx} fill={BAR_COLORS[idx % BAR_COLORS.length]}/>
                            ))}
                        </Bar>

                    </BarChart>
                </ResponsiveContainer>
            </div>
        );
    }
}
// カスタム要素として登録
customElements.define('sample-bar-chart', SampleBarChartElement);
