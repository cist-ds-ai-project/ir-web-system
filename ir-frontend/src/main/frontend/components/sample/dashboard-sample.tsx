import React, { useMemo, useState } from "react";
import { ReactAdapterElement, type RenderHooks } from "Frontend/generated/flow/ReactAdapter";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from "recharts";

/**
 * Recharts dashboard sample (single page)
 * - KPI cards
 * - Line chart (time series)
 * - BarChart.tsx chart (category)
 * - Pie chart (share)
 * - Simple filters (range + search)
 */

// ── Styles ──────────────────────────────────────────────────────────────────
const styles = {
  page: {
    minHeight: "100%",
    background: "var(--lumo-base-color, #fff)",
    fontFamily: "var(--lumo-font-family, sans-serif)",
    color: "var(--lumo-body-text-color, #333)",
  } as React.CSSProperties,

  container: {
    maxWidth: 1152,
    margin: "0 auto",
    padding: "16px 24px",
    display: "flex",
    flexDirection: "column" as const,
    gap: 16,
  } as React.CSSProperties,

  headerRow: {
    display: "flex",
    flexWrap: "wrap" as const,
    alignItems: "flex-end",
    justifyContent: "space-between",
    gap: 12,
  } as React.CSSProperties,

  title: {
    fontSize: "1.5rem",
    fontWeight: 600,
    letterSpacing: "-0.01em",
  } as React.CSSProperties,

  subtitle: {
    fontSize: "0.8rem",
    color: "var(--lumo-secondary-text-color, #777)",
  } as React.CSSProperties,

  filterRow: {
    display: "flex",
    flexWrap: "wrap" as const,
    gap: 8,
    alignItems: "center",
  } as React.CSSProperties,

  btn: (active: boolean) =>
    ({
      padding: "6px 14px",
      fontSize: "0.82rem",
      border: active ? "none" : "1px solid var(--lumo-contrast-20pct, #ccc)",
      borderRadius: 8,
      cursor: "pointer",
      background: active
        ? "var(--lumo-primary-color, #1976d2)"
        : "var(--lumo-base-color, #fff)",
      color: active
        ? "#fff"
        : "var(--lumo-body-text-color, #333)",
      fontWeight: active ? 600 : 400,
      transition: "all 0.15s",
    }) as React.CSSProperties,

  input: {
    padding: "6px 12px",
    fontSize: "0.82rem",
    border: "1px solid var(--lumo-contrast-20pct, #ccc)",
    borderRadius: 8,
    outline: "none",
    width: 220,
    background: "var(--lumo-base-color, #fff)",
    color: "var(--lumo-body-text-color, #333)",
  } as React.CSSProperties,

  kpiGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
    gap: 12,
  } as React.CSSProperties,

  card: {
    background: "var(--lumo-base-color, #fff)",
    border: "1px solid var(--lumo-contrast-10pct, #e8e8e8)",
    borderRadius: 16,
    padding: 16,
    boxShadow: "0 1px 3px rgba(0,0,0,0.06)",
  } as React.CSSProperties,

  chartsRow: {
    display: "grid",
    gridTemplateColumns: "2fr 1fr",
    gap: 12,
  } as React.CSSProperties,

  bottomRow: {
    display: "grid",
    gridTemplateColumns: "1fr 2fr",
    gap: 12,
  } as React.CSSProperties,

  kpiLabel: {
    fontSize: "0.8rem",
    color: "var(--lumo-secondary-text-color, #777)",
  } as React.CSSProperties,

  kpiValue: {
    marginTop: 4,
    fontSize: "1.5rem",
    fontWeight: 600,
    letterSpacing: "-0.01em",
  } as React.CSSProperties,

  kpiSub: {
    marginTop: 8,
    fontSize: "0.75rem",
    color: "var(--lumo-secondary-text-color, #777)",
  } as React.CSSProperties,

  cardHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 12,
  } as React.CSSProperties,

  cardTitle: {
    fontWeight: 600,
    fontSize: "0.9rem",
  } as React.CSSProperties,

  cardSub: {
    fontSize: "0.75rem",
    color: "var(--lumo-secondary-text-color, #777)",
  } as React.CSSProperties,

  table: {
    width: "100%",
    fontSize: "0.82rem",
    borderCollapse: "collapse" as const,
  } as React.CSSProperties,

  th: {
    textAlign: "left" as const,
    padding: "8px",
    borderBottom: "1px solid var(--lumo-contrast-10pct, #e8e8e8)",
    fontWeight: 600,
    position: "sticky" as const,
    top: 0,
    background: "var(--lumo-base-color, #fff)",
  } as React.CSSProperties,

  thRight: {
    textAlign: "right" as const,
    padding: "8px",
    borderBottom: "1px solid var(--lumo-contrast-10pct, #e8e8e8)",
    fontWeight: 600,
    position: "sticky" as const,
    top: 0,
    background: "var(--lumo-base-color, #fff)",
  } as React.CSSProperties,

  td: {
    padding: "8px",
    borderBottom: "1px solid var(--lumo-contrast-10pct, #e8e8e8)",
  } as React.CSSProperties,

  tdRight: {
    padding: "8px",
    borderBottom: "1px solid var(--lumo-contrast-10pct, #e8e8e8)",
    textAlign: "right" as const,
  } as React.CSSProperties,

  note: {
    fontSize: "0.75rem",
    color: "var(--lumo-secondary-text-color, #777)",
  } as React.CSSProperties,

  tip: {
    marginTop: 8,
    fontSize: "0.72rem",
    color: "var(--lumo-secondary-text-color, #777)",
  } as React.CSSProperties,

  tableWrap: {
    marginTop: 12,
    overflow: "auto",
    borderRadius: 12,
    border: "1px solid var(--lumo-contrast-10pct, #e8e8e8)",
    maxHeight: 320,
  } as React.CSSProperties,
};

const PIE_COLORS = ["#6366f1", "#f59e0b", "#10b981", "#ef4444", "#8b5cf6"];

// ── Mock data ───────────────────────────────────────────────────────────────
const RAW = [
  { date: "2026-01-05", sales: 120, users: 80, orders: 34, region: "Tokyo" },
  { date: "2026-01-12", sales: 200, users: 110, orders: 49, region: "Tokyo" },
  { date: "2026-01-19", sales: 160, users: 95, orders: 41, region: "Osaka" },
  { date: "2026-01-26", sales: 240, users: 140, orders: 58, region: "Osaka" },
  { date: "2026-02-02", sales: 220, users: 133, orders: 55, region: "Nagoya" },
  { date: "2026-02-09", sales: 260, users: 152, orders: 63, region: "Tokyo" },
  { date: "2026-02-16", sales: 210, users: 128, orders: 52, region: "Nagoya" },
  { date: "2026-02-23", sales: 300, users: 175, orders: 71, region: "Tokyo" },
];

const CATEGORY = [
  { name: "A", value: 210 },
  { name: "B", value: 195 },
  { name: "C", value: 140 },
];

function formatJPY(n: number) {
  return new Intl.NumberFormat("ja-JP", {
    style: "currency",
    currency: "JPY",
    maximumFractionDigits: 0,
  }).format(n);
}

function formatDateLabel(iso: string) {
  const parts = iso.split("-").map((x) => Number(x));
  return `${parts[1]}/${parts[2]}`;
}

function clampRange(range: string) {
  return range === "4w" || range === "8w" || range === "all" ? range : "8w";
}

function computeKpis(rows: typeof RAW) {
  const sumSales = rows.reduce((a, r) => a + r.sales, 0);
  const sumUsers = rows.reduce((a, r) => a + r.users, 0);
  const sumOrders = rows.reduce((a, r) => a + r.orders, 0);
  const aov = sumOrders ? sumSales / sumOrders : 0;
  const cvr = sumUsers ? (sumOrders / sumUsers) * 100 : 0;
  return { sumSales, sumUsers, sumOrders, aov, cvr };
}

// ── KPI Card ────────────────────────────────────────────────────────────────
function KpiCard({ title, value, sub }: { title: string; value: string; sub: string }) {
  return (
    <div style={styles.card}>
      <div style={styles.kpiLabel}>{title}</div>
      <div style={styles.kpiValue}>{value}</div>
      <div style={styles.kpiSub}>{sub}</div>
    </div>
  );
}

// ── Main Dashboard ──────────────────────────────────────────────────────────
function DashboardSampleContent() {
  const [range, setRange] = useState("8w");
  const [search, setSearch] = useState("");

  const filtered = useMemo(() => {
    const r = clampRange(range);
    const q = search.trim().toLowerCase();
    const base = (() => {
      if (r === "all") return RAW;
      const n = r === "4w" ? 4 : 8;
      return RAW.slice(Math.max(0, RAW.length - n));
    })();
    if (!q) return base;
    return base.filter((row) => row.region.toLowerCase().includes(q));
  }, [range, search]);

  const kpi = useMemo(() => computeKpis(filtered), [filtered]);

  const byRegion = useMemo(() => {
    const m = new Map<string, { region: string; sales: number; users: number; orders: number }>();
    for (const r of filtered) {
      const prev = m.get(r.region) ?? { region: r.region, sales: 0, users: 0, orders: 0 };
      prev.sales += r.sales;
      prev.users += r.users;
      prev.orders += r.orders;
      m.set(r.region, prev);
    }
    return Array.from(m.values()).sort((a, b) => b.sales - a.sales);
  }, [filtered]);

  const pieData = useMemo(() => {
    if (search.trim()) {
      const total = byRegion.reduce((a, r) => a + r.sales, 0);
      return byRegion.map((r) => ({ name: r.region, value: r.sales, pct: total ? (r.sales / total) * 100 : 0 }));
    }
    return CATEGORY;
  }, [byRegion, search]);

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        {/* Header */}
        <div style={styles.headerRow}>
          <div>
            <div style={styles.title}>Dashboard (Recharts Sample)</div>
            <div style={styles.subtitle}>Mock data • KPI / Time Series / Breakdown</div>
          </div>
          <div style={styles.filterRow}>
            {(["4w", "8w", "all"] as const).map((v) => (
              <button key={v} style={styles.btn(range === v)} onClick={() => setRange(v)}>
                {v === "all" ? "All" : v === "4w" ? "4 weeks" : "8 weeks"}
              </button>
            ))}
            <input
              style={styles.input}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search region (e.g., Tokyo)"
            />
          </div>
        </div>

        {/* KPI Cards */}
        <div style={styles.kpiGrid}>
          <KpiCard title="Sales" value={formatJPY(kpi.sumSales)} sub={`${filtered.length} points`} />
          <KpiCard title="Users" value={new Intl.NumberFormat("ja-JP").format(kpi.sumUsers)} sub="Total" />
          <KpiCard title="Orders" value={new Intl.NumberFormat("ja-JP").format(kpi.sumOrders)} sub="Total" />
          <KpiCard title="AOV / CVR" value={`${formatJPY(kpi.aov)} / ${kpi.cvr.toFixed(1)}%`} sub="Avg order value / Conversion" />
        </div>

        {/* Charts row */}
        <div style={styles.chartsRow}>
          {/* Line chart */}
          <div style={styles.card}>
            <div style={styles.cardHeader}>
              <div style={styles.cardTitle}>Time Series</div>
              <div style={styles.cardSub}>sales / users</div>
            </div>
            <div style={{ height: 288 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart
                  data={filtered.map((r) => ({ ...r, label: formatDateLabel(r.date) }))}
                  margin={{ top: 10, right: 20, left: 0, bottom: 0 }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="label" />
                  <YAxis />
                  <Tooltip
                      formatter={(value: any, name: any) => {
                        const num = Number(value ?? 0);

                        if (name === "sales") {
                          return [formatJPY(num), "sales"];
                        }

                        return [new Intl.NumberFormat("ja-JP").format(num), String(name)];
                      }}
                  />
                  <Legend />
                  <Line type="monotone" dataKey="sales" dot={false} strokeWidth={2} stroke="#6366f1" />
                  <Line type="monotone" dataKey="users" dot={false} strokeWidth={2} stroke="#10b981" />
                </LineChart>
              </ResponsiveContainer>
            </div>
            <div style={styles.tip}>
              Tip: Recharts は ResponsiveContainer でカード内リサイズに強いです。
            </div>
          </div>

          {/* BarChart.tsx chart */}
          <div style={styles.card}>
            <div style={styles.cardHeader}>
              <div style={styles.cardTitle}>Breakdown</div>
              <div style={styles.cardSub}>by region</div>
            </div>
            <div style={{ height: 288 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={byRegion} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="region" />
                  <YAxis />
                  <Tooltip
                      formatter={(v: any, n: any) =>
                          n === "sales"
                              ? [formatJPY(Number(v ?? 0)), "sales"]
                              : [Number(v ?? 0), String(n)]
                      }
                  />
                  <Bar dataKey="sales" fill="#6366f1" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Bottom row */}
        <div style={styles.bottomRow}>
          {/* Pie chart */}
          <div style={styles.card}>
            <div style={styles.cardHeader}>
              <div style={styles.cardTitle}>Share</div>
              <div style={styles.cardSub}>category / region</div>
            </div>
            <div style={{ height: 256 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} dataKey="value" nameKey="name" outerRadius={85} label>
                    {pieData.map((_, idx) => (
                      <Cell key={idx} fill={PIE_COLORS[idx % PIE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                      formatter={(value: any) => {
                        const n = Number(value ?? 0);

                        return [
                          search.trim()
                              ? formatJPY(n)
                              : new Intl.NumberFormat("ja-JP").format(n),
                          "value",
                        ];
                      }}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div style={styles.tip}>
              検索欄に地域を入れると、円グラフが地域シェアに切り替わります。
            </div>
          </div>

          {/* Data table */}
          <div style={styles.card}>
            <div style={styles.cardHeader}>
              <div style={styles.cardTitle}>Data Preview</div>
              <div style={styles.cardSub}>latest first</div>
            </div>
            <div style={styles.tableWrap}>
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th style={styles.th}>Date</th>
                    <th style={styles.th}>Region</th>
                    <th style={styles.thRight}>Sales</th>
                    <th style={styles.thRight}>Users</th>
                    <th style={styles.thRight}>Orders</th>
                  </tr>
                </thead>
                <tbody>
                  {[...filtered].reverse().map((r) => (
                    <tr key={r.date + r.region}>
                      <td style={{ ...styles.td, whiteSpace: "nowrap" }}>{r.date}</td>
                      <td style={styles.td}>{r.region}</td>
                      <td style={styles.tdRight}>{formatJPY(r.sales)}</td>
                      <td style={styles.tdRight}>{new Intl.NumberFormat("ja-JP").format(r.users)}</td>
                      <td style={styles.tdRight}>{new Intl.NumberFormat("ja-JP").format(r.orders)}</td>
                    </tr>
                  ))}
                  {filtered.length === 0 && (
                    <tr>
                      <td colSpan={5} style={{ ...styles.td, padding: 24, textAlign: "center", color: "#999" }}>
                        No data (try clearing the search)
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div style={styles.note}>
          Note: このページは「単体コンポーネントで見た目の良いダッシュボード」を目的にしています。API 連携する場合は
          <code style={{ margin: "0 4px" }}>RAW</code>
          を fetch で置き換え、
          <code style={{ margin: "0 4px" }}>useMemo</code>
          で集計する形がそのまま使えます。
        </div>
      </div>
    </div>
  );
}

// ── ReactAdapterElement for Vaadin integration ──────────────────────────────
class DashboardSampleElement extends ReactAdapterElement {
  protected override render(hooks: RenderHooks): React.ReactElement | null {
    return <DashboardSampleContent />;
  }
}

customElements.define("dashboard-sample", DashboardSampleElement);
