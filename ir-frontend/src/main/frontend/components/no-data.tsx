export function NoDataView() {
    return (
        <div
            style={{
                width: '100%',
                height: '100%',
                minHeight: '250px',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--lumo-secondary-text-color, #777)',
                textAlign: 'center',
                gap: '8px',
                boxSizing: 'border-box',
            }}
        >
            <div
                style={{
                    fontSize: '2rem',
                    opacity: 0.5,
                }}
            >

            </div>

            <div
                style={{
                    fontSize: '1.1rem',
                    fontWeight: 600,
                }}
            >
                表示できるデータがありません
            </div>

            <div
                style={{
                    fontSize: '0.9rem',
                }}
            >
                選択した条件に該当する成績はありません
            </div>
        </div>
    );
}